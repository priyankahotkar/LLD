// Core Elevator class with a simple queue for managing requests
class Elevator {
    private int id; // Unique identifier for the elevator
    private int currentFloor; // The floor where the elevator is currently located
    private Direction direction; // The current direction of the elevator (UP, DOWN, or IDLE)
    private ElevatorState state; // The current operational state of the elevator
                                 // (e.g., MOVING, IDLE, etc.)
    private List<ElevatorObserver> observers; // A list of observers (listeners) that monitor the elevator's
                                              // status
    private Queue<ElevatorRequest> requests; // A simple queue to manage floor requests in the order they are
                                             // received

    // Get the elevator's ID
    public int getId() {
        return id;
    }

    // Get the elevator's current floor
    public int getCurrentFloor() {
        return currentFloor;
    }

    // Get the elevator's current direction
    public Direction getDirection() {
        return direction;
    }

    // Get the elevator's current state
    public ElevatorState getState() {
        return state;
    }

    // Get a copy of the current requests queue to prevent external modification
    public Queue<ElevatorRequest> getRequestsQueue() {
        return new LinkedList<>(requests);
    }

    // Get a list of all destination floors for display purposes
    public List<ElevatorRequest> getDestinationFloors() {
        return new ArrayList<>(requests);
    }
}

class Building {
    private String name; // Name of the building
    private int numberOfFloors; // Total number of floors in the building
    private ElevatorController elevatorController; // Controller to manage all elevators in the building
}

// Represents a floor in a building
public class Floor {
    private int floorNumber;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public int getFloorNumber() {
        return floorNumber;
    }
}

// Manages the operations and coordination of all elevators in a building
class ElevatorController {
    private List<Elevator> elevators; // List of all elevators controlled by this system
    private List<Floor> floors; // List of floors in the building
    private SchedulingStrategy schedulingStrategy; // Strategy to decide which elevator should handle a request
    private int currentElevatorId; // Keeps track of the current elevator's ID for handling internal requests
}

// Enum to represent the direction of the elevator
enum Direction {
    UP, // The elevator is moving upward
    DOWN, // The elevator is moving downward
    IDLE // The elevator is stationary, not moving
}

// Enum to represent the state of the elevator
enum ElevatorState {
    IDLE, // The elevator is not moving, waiting for requests
    MOVING, // The elevator is in motion (either up or down)
    STOPPED, // The elevator has temporarily stopped (e.g., at a floor)
    MAINTENANCE // The elevator is out of service and undergoing maintenance
}

// Observer interface for handling elevator events
interface ElevatorObserver {
    // Called when an elevator's state changes
    void onElevatorStateChange(Elevator elevator, ElevatorState state);

    // Called when an elevator changes its current floor
    void onElevatorFloorChange(Elevator elevator, int floor);
}

// Concrete implementation of the Observer interface
class ElevatorDisplay implements ElevatorObserver {
    @Override
    public void onElevatorStateChange(Elevator elevator, ElevatorState state) {
        // Display the new state of the elevator
        System.out.println("Elevator " + elevator.getId() + " state changed to " + state);
    }

    @Override
    public void onElevatorFloorChange(Elevator elevator, int floor) {
        // Display the elevator's movement to a new floor
        System.out.println("Elevator " + elevator.getId() + " moved to floor " + floor);
    }
}

// Command Pattern for Request Processing
interface ElevatorCommand {
    // Method to execute the command
    void execute();
}

public class ElevatorRequest implements ElevatorCommand {
    private int elevatorId; // ID of the elevator involved in the request
    private int floor; // Floor where the request is made
    private Direction requestDirection; // The direction of the elevator request
    private ElevatorController controller; // Reference to the ElevatorController to handle the request
    private boolean isInternalRequest; // Distinguishes internal vs external requests
    // Constructor to initialize the elevator request

    public ElevatorRequest(int elevatorId, int floor, boolean isInternalRequest,
            Direction direction) {
        this.elevatorId = elevatorId;
        this.floor = floor;
        this.isInternalRequest = isInternalRequest;
        this.requestDirection = direction;
        this.controller = new ElevatorController();
    }

    // Execute method to process the request via the controller
    @Override
    public void execute() {
        if (isInternalRequest)
            controller.requestFloor(elevatorId, floor);
        else
            controller.requestElevator(elevatorId, floor, requestDirection);
    }

    // Getters and Setters for the ElevatorRequest
    public Direction getDirection() {
        return requestDirection;
    }

    public int getFloor() {
        return floor;
    }

    public boolean checkIsInternalRequest() {
        return isInternalRequest;
    }
}

// Strategy Pattern for Scheduling
interface SchedulingStrategy {
    // Determines the next stop for the given elevator
    int getNextStop(Elevator elevator);
}

// First-Come-First-Served Algorithm
class FCFSSchedulingStrategy implements SchedulingStrategy {
    @Override
    public int getNextStop(Elevator elevator) {
        // Get the elevator's current direction and floor
        Direction elevatorDirection = elevator.getDirection();
        int currentFloor = elevator.getCurrentFloor();

        // Retrieve the FIFO queue of floor requests
        Queue<ElevatorRequest> requestQueue = elevator.getRequestsQueue();

        // If the request queue is empty, stay on the current floor
        if (requestQueue.isEmpty())
            return currentFloor;

        // Fetch the next requested floor
        int nextRequestedFloor = requestQueue.poll().getFloor();

        // If the next floor is the current floor, return it
        if (nextRequestedFloor == currentFloor)
            return currentFloor;
        // Set elevator's direction based on its current state and next floor
        if (elevatorDirection == Direction.IDLE) {
            elevator.setDirection(
                    nextRequestedFloor > currentFloor ? Direction.UP : Direction.DOWN);
        } else if (elevatorDirection == Direction.UP
                && nextRequestedFloor < currentFloor) {
            elevator.setDirection(Direction.DOWN);
        } else if (nextRequestedFloor > currentFloor) {
            elevator.setDirection(Direction.UP);
        }

        // Return the next requested floor
        return nextRequestedFloor;
    }
}

/*
 * SIMULATION SCENARIO : Down Request While Elevator is Moving Down
 * -
 * - Setup:
 * - - Elevator is at floor 10
 * - - Elevator is moving DOWN
 * - - Elevator has destinations at floors 7, 5, and 2
 * - - External DOWN request arrives from floor 8
 * -
 * - System Reaction:
 * - The elevator controller will assign Elevator to serve this request since
 * it's already
 * - moving DOWN and will pass floor 8. The external request will be inserted
 * into
 * - Elevator's request queue in proper order (between floors 10 and 7).
 * - The elevator will stop at floor 8 to pick up the passenger without changing
 * - its overall downward journey pattern, continuing to floors 7, 5, and 2
 * afterward.
 */

// Scan Scheduling Strategy for handling elevator requests
class ScanSchedulingStrategy implements SchedulingStrategy {
    @Override
    public int getNextStop(Elevator elevator) {
        // Retrieve elevator's current direction and floor
        Direction elevatorDirection = elevator.getDirection();
        int currentFloor = elevator.getCurrentFloor();
        Queue<ElevatorRequest> requests = elevator.getRequestsQueue();

        // If there are no requests, stay on the current floor
        if (requests.isEmpty())
            return currentFloor;

        // Priority queues to handle requests in up and down directions
        PriorityQueue<ElevatorRequest> upQueue = new PriorityQueue<>(); // Min-heap for upward requests
        PriorityQueue<ElevatorRequest> downQueue = new PriorityQueue<>((a, b) -> b.getFloor() - a.getFloor()); // Max-heap
                                                                                                               // for
                                                                                                               // downward
                                                                                                               // requests

        // Categorize requests based on their relative position to the current floor
        while (!requests.isEmpty()) {
            ElevatorRequest elevatorRequest = requests.poll();
            int floor = elevatorRequest.getFloor();
            if (floor > currentFloor)
                upQueue.add(elevatorRequest);
            else
                downQueue.add(elevatorRequest);
        }

        // Handle the case when the elevator is IDLE
        if (elevatorDirection == Direction.IDLE) {
            // Determine the nearest request and set direction accordingly
            int nearestUpwardRequest = upQueue.isEmpty() ? -1 : upQueue.peek().getFloor();
            int nearestDownwardRequest = downQueue.isEmpty() ? -1 : downQueue.peek().getFloor();

            if (nearestUpwardRequest == -1) {
                elevator.setDirection(Direction.DOWN);
                return downQueue.poll().getFloor();
            } else if (nearestDownwardRequest == -1) {
                elevator.setDirection(Direction.UP);
                return upQueue.poll().getFloor();
            } else {
                // Choose the closest request
                if (Math.abs(nearestUpwardRequest - currentFloor) < Math.abs(nearestDownwardRequest - currentFloor)) {
                    elevator.setDirection(Direction.UP);
                    return upQueue.poll().getFloor();
                } else {
                    elevator.setDirection(Direction.DOWN);
                    return downQueue.poll().getFloor();
                }
            }
        }

        // Handle movement in the UP direction
        if (elevatorDirection == Direction.UP) {
            return !upQueue.isEmpty() ? UpQueue.poll().getFloor()
                    : switchDirection(elevator, downQueue);
        }
        // Handle movement in the DOWN direction
        else {
            return !downQueue.isEmpty() ? DownQueue.poll().getFloor()
                    : switchDirection(elevator, upQueue);
        }
    }

    // Helper method to switch the elevator's direction when no further requests
    // exist in the current direction
    private int switchDirection(
            Elevator elevator, PriorityQueue<Integer> requestsQueue) {
        elevator.setDirection(elevator.getDirection() == Direction.UP
                ? Direction.DOWN
                : Direction.UP);
        return requestsQueue.isEmpty() ? elevator.getCurrentFloor()
                : requestsQueue.poll().getFloor();
    }
}

/*
 * SIMULATION SCENARIO : Up Request While Elevator is Moving Down
 * - - Setup:
 * - - Elevator is at floor 10
 * - - Elevator is moving DOWN
 * - - Elevator has destinations at floors 6 and 2
 * - - External UP request arrives from floor 4
 * - - System Reaction:
 * - The Scan Scheduling Strategy would not assign Elevator to this request
 * - because its current direction doesn't match the request direction. Instead,
 * - this request would be queued. the Elevator will complete its current DOWN
 * - journey to floor 2, then reverse direction and fulfill the UP request from
 * - floor 4.
 */

public class LookSchedulingStrategy implements SchedulingStrategy {
    @Override
    public int getNextStop(Elevator elevator) {
        int currentFloor = elevator.getCurrentFloor();
        Queue<ElevatorRequest> requests = elevator.getRequestsQueue();
        // If there are no pending requests, remain on the current floor.
        if (requests == null || requests.isEmpty()) {
            return currentFloor;
        }
        // Determine the primary target from the first request in the queue.
        ElevatorRequest primaryRequest = requests.peek();
        int primaryFloor = primaryRequest.getFloor();
        // Determine the travel direction based on the primary target.
        Direction travelDirection;
        if (primaryFloor > currentFloor) {
            travelDirection = Direction.UP;
        } else if (primaryFloor < currentFloor) {
            travelDirection = Direction.DOWN;
        } else {
            return currentFloor; // Already at the requested floor.
        }
        // Look for any request along the journey from currentFloor to primaryFloor.
        // For upward movement, we need the smallest floor greater than currentFloor and
        // <=
        // primaryFloor. For downward movement, we need the largest floor less than
        // currentFloor and >=
        // primaryFloor.
        Integer candidate = null;

        for (ElevatorRequest req : requests) {
            int reqFloor = req.getFloor();
            // Check if the request is within the range between currentFloor and
            // primaryFloor.
            if (travelDirection == Direction.UP && reqFloor > currentFloor && reqFloor <= primaryFloor) {
                // For internal requests we always consider; for external requests, only if they
                // are going
                // UP.
                if (req.checkIsInternalRequest()
                        || (!req.checkIsInternalRequest() && req.getDirection() == Direction.UP)) {
                    // Choose the candidate that is closest to the current floor (i.e. the smallest
                    // floor
                    // greater than currentFloor).
                    if (candidate == null || reqFloor < candidate) {
                        candidate = reqFloor;
                    }
                }
            } else if (travelDirection == Direction.DOWN && reqFloor < currentFloor
                    && reqFloor >= primaryFloor) {
                // For downward movement, consider the request if internal or if external with
                // direction
                // DOWN.
                if (req.checkIsInternalRequest()
                        || (!req.checkIsInternalRequest() && req.getDirection() == Direction.DOWN)) {
                    // For a downward journey, we choose the candidate that is closest to the
                    // current floor
                    // (i.e. the largest floor less than currentFloor).
                    if (candidate == null || reqFloor > candidate) {
                        candidate = reqFloor;
                    }
                }
            }
        }
        // If a candidate was found in the path, return that as the next stop;
        // otherwise, fall back to the primary target.
        return (candidate != null) ? candidate : primaryFloor;
    }
}

// Core Elevator class with simple queue for managing requests
class Elevator {
    // Unique ID for the elevator
    private int id;
    // Current floor where the elevator is located
    private int currentFloor;
    // Current direction of the elevator (UP, DOWN, or IDLE)
    private Direction direction;
    // Current operational state of the elevator (IDLE, MOVING, etc.)
    private ElevatorState state;
    // List of observers to monitor elevator events
    private List<ElevatorObserver> observers;
    // Queue to manage all requests (both internal and external)
    private Queue<ElevatorRequest> requests;

    // Constructor to initialize the elevator
    public Elevator(int id) {
        this.id = id;
        this.currentFloor = 1; // Default initial floor
        this.direction = Direction.IDLE;
        this.state = ElevatorState.IDLE;
        this.observers = new ArrayList<>();
        this.requests = new LinkedList<>();
    }

    // Add an observer to monitor elevator events
    public void addObserver(ElevatorObserver observer) {
        observers.add(observer);
    }

    // Remove an observer
    public void removeObserver(ElevatorObserver observer) {
        observers.remove(observer);
    }

    // Notify all observers about a state change
    private void notifyStateChange(ElevatorState state) {
        for (ElevatorObserver observer : observers) {
            observer.onElevatorStateChange(this, state);
        }
    }

    // Notify all observers about a floor change
    private void notifyFloorChange(int floor) {
        for (ElevatorObserver observer : observers) {
            observer.onElevatorFloorChange(this, floor);
        }
    }

    // Set a new state for the elevator and notify observers
    public void setState(ElevatorState newState) {
        this.state = newState;
        notifyStateChange(newState);
    }

    // Set the direction of the elevator
    public void setDirection(Direction newDirection) {
        this.direction = newDirection;
    }

    // Add a new floor request to the queue
    public void addRequest(ElevatorRequest elevatorRequest) {
        // Avoid duplicate requests
        if (!requests.contains(elevatorRequest)) {
            requests.add(elevatorRequest);
        }

        int requestedFloor = elevatorRequest.getFloor();
        // If elevator is idle, determine direction and start moving
        if (state == ElevatorState.IDLE && !requests.isEmpty()) {
            if (requestedFloor > currentFloor) {
                direction = Direction.UP;
            } else if (requestedFloor < currentFloor) {
                direction = Direction.DOWN;
            }
            setState(ElevatorState.MOVING);
        }
    }

    /*
     * SIMULATION SCENARIO : Down Request from Above Current Position
     * - - Setup:
     * - - Elevator is at floor 5
     * - - Elevator is IDLE
     * - - External DOWN request arrives from floor 11
     * - - System Reaction:
     * - The elevator controller would send Elevator UP to floor 11 to service the
     * - DOWN request. After reaching floor 11, Elevator would change its direction
     * - to DOWN and wait for the passenger to select their destination floor. This
     * - demonstrates how the system correctly handles serving a request that
     * - initially requires moving in the opposite direction of the requested
     * - travel.
     */

    // Move the elevator to the next stop as decided by the scheduling strategy
    public void moveToNextStop(int nextStop) {
        // Only move if the elevator is currently in the MOVING state
        if (state != ElevatorState.MOVING)
            return;
        while (currentFloor != nextStop) {
            // Update floor based on direction
            if (direction == Direction.UP) {
                currentFloor++;
            } else {
                currentFloor--;
            }
            // Notify observers about the floor change
            notifyFloorChange(currentFloor);
            // Complete arrival once the target floor is reached
            if (currentFloor == nextStop) {
                completeArrival();
                return;
            }
        }
    }

    // Handle the elevator's arrival at a destination floor
  private void completeArrival() {
    // Stop the elevator and notify observers
    setState(ElevatorState.STOPPED);
    // Remove the current floor from the requests queue
    requests.removeIf(request = > request.getFloor() == currentFloor);
    // If no more requests, set state to IDLE
    if (requests.isEmpty()) {
      direction = Direction.IDLE;
      setState(ElevatorState.IDLE);
    } else {
      // Otherwise, continue moving after a brief stop
      setState(ElevatorState.MOVING);
    }
  }

    // Get the elevator's ID
    public int getId() {
        return id;
    }

    // Get the elevator's current floor
    public int getCurrentFloor() {
        return currentFloor;
    }

    // Get the elevator's current direction
    public Direction getDirection() {
        return direction;
    }

    // Get the elevator's current state
    public ElevatorState getState() {
        return state;
    }

    // Get a copy of the current requests queue to prevent external modification
    public Queue<ElevatorRequest> getRequestsQueue() {
        return new LinkedList<>(requests);
    }

    // Get a list of all destination floors for display purposes
    public List<ElevatorRequest> getDestinationFloors() {
        return new ArrayList<>(requests);
    }
}

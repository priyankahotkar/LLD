import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

abstract class Vehicle {
    protected String licensePlate;
    protected String vehicleType;
    private ParkingFeeStrategy feeStrategy;

    public Vehicle(String licensePlate, String vehicleType, ParkingFeeStrategy feeStrategy) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.feeStrategy = feeStrategy;
    }

    public String getVehicleType() {
        return this.vehicleType;
    }
}

class CarVehicle extends Vehicle {
    public CarVehicle(String licensePlate, ParkingFeeStrategy feeStrategy) {
        super(licensePlate, "car", feeStrategy);
    }
}

class VehicleFactory {
    public static Vehicle createVehicle(String vehicleType, String licensePlate, ParkingFeeStrategy feeStrategy) {
        if (vehicleType.equalsIgnoreCase("Car")) {
            return new CarVehicle(licensePlate, feeStrategy);
        } else if (vehicleType.equalsIgnoreCase("Bike")) {

        } else {
            // bla bla
        }
    }
}

class ParkingLot {
    private List<ParkingSpot> slots;

    public ParkingLot(List<ParkingSpot> parkingSpots) {
        this.slots = parkingSpots;
    }

    public ParkingSpot findAvailableSpot(String vehicleType) {
        for (ParkingSpot spot : slots) {
            if (!spot.isOccupied() && spot.getSpotType().equals(vehicleType)) {
                return spot;
            }
        }

        return null;
    }

    public ParkingSpot parkVehicle(Vehicle vehicle) {
        ParkingSpot spot = findAvailableSpot(vehicle.getVehicleType());
        if (spot != null) {
            spot.parkVehicle(vehicle);
            System.out.println("Vehicle parked successsfully in spot" + spot.getSpotNumber());
            return spot;
        }

        System.out.println("No parkinng slots available");
        return null;
    }

    public void vacateSpot(ParkingSpot spot, Vehicle vehicle) {
        if (spot != null && spot.isOccupied()
                && spot.getVehicle().equals(vehicle)) {
            spot.vacate(); // Free the spot
            System.out.println(vehicle.getVehicleType()
                    + " vacated the spot: " + spot.getSpotNumber());
        } else {
            System.out.println("Invalid operation! Either the spot is already vacant "
                    + "or the vehicle does not match.");
        }
    }

    // Method to find a spot by its number
    public ParkingSpot getSpotByNumber(int spotNumber) {
        for (ParkingSpot spot : slots) {
            if (spot.getSpotNumber() == spotNumber) {
                return spot;
            }
        }
        return null; // Spot not found
    }

    // Getter for parking spots
    public List<ParkingSpot> getParkingSpots() {
        return slots;
    }
}

abstract class ParkingSpot {
    private int spotNumber;
    private boolean isOccupied;
    private Vehicle vehicle;
    private String spotType;

    // Constructor to initialize parking spot with spot number and type
    public ParkingSpot(int spotNumber, String spotType) {
        this.spotNumber = spotNumber;
        this.isOccupied = false;
        this.spotType = spotType;
    }

    // Method to check if the spot is occupied
    public boolean isOccupied() {
        return isOccupied;
    }

    // Abstract method to check if a vehicle can park in this spot
    public abstract boolean canParkVehicle(Vehicle vehicle);

    // Method to park a vehicle in the spot
    public void parkVehicle(Vehicle vehicle) {
        // Check if the spot is already occupied
        if (isOccupied) {
            throw new IllegalStateException("Spot is already occupied.");
        }
        // Check if the vehicle can be parked in this spot
        if (!canParkVehicle(vehicle)) {
            throw new IllegalArgumentException(
                    "This spot is not suitable for" + vehicle.getVehicleType());
        }
        this.vehicle = vehicle;
        this.isOccupied = true;
    }

    // Method to vacate the parking spot
    public void vacate() {
        // Check if the spot is already vacant
        if (!isOccupied) {
            throw new IllegalStateException("Spot is already vacant.");
        }
        this.vehicle = null;
        this.isOccupied = false;
    }

    // Getter for spot number
    public int getSpotNumber() {
        return spotNumber;
    }

    // Getter for the vehicle parked in the spot
    public Vehicle getVehicle() {
        return vehicle;
    }

    // Getter for spot type
    public String getSpotType() {
        return spotType;
    }
}

class CarParkingSpot extends ParkingSpot {
    public CarParkingSpot(int spotNumber, String spotType) {
        super(spotNumber, spotType);
    }

    @Override
    public boolean canParkVehicle(Vehicle vehicle) {
        return "Car".equalsIgnoreCase(vehicle.getVehicleType());
    }
}

interface PaymentStrategy {
    void processPayment(double amount);
}

enum DurationType {
    HOURS
    DAYS
}

interface ParkingFeeStrategy {
    // @param vehicleType Type of vehicle being parked
    // @param duration Duration of parking (in hours or days)
    // @param durationType Type of duration (HOURS or DAYS)
    // @return Calculated parking fee

    double calculateFee(String vehicleType, int duration, DurationType durationType);
}

class BasicHourlyRateStrategy implements ParkingFeeStrategy {
    @Override
    public double calculateFee(String vehicleType, int duration, DurationType durationType) {
        switch (vehicleType.toLowerCase()) {
            case "car":
                return durationType == DurationType.HOURS ? duration = 10 : (duration = 10 * 24);
            case "bike":
                return durationType == DurationType.HOURS ? duration = 5 : (duration = 5 * 24);
            case "auto":
                return durationType == DurationType.HOURS ? duration = 8 : (duration = 8 * 24);
            default:
                break;
        }
    }
}

// Similarly PremiumRateStrategy

class CashPayment implements PaymentStrategy {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing cash payment of $" + amount);
    }
}

class Payment {
    private double amount;
    private PaymentStrategy paymentStrategy; // Payment strategy interface
    // Constructor to initialize the payment amount and payment strategy

    public Payment(double amount, PaymentStrategy paymentStrategy) {
        this.amount = amount;
        this.paymentStrategy = paymentStrategy;
    }

    // Process the payment using the assigned strategy
    public void processPayment() {
        if (amount > 0) {
            paymentStrategy.processPayment(amount); // Delegating to strategy
        } else {
            System.out.println("Invalid payment amount.");
        }
    }
}

public class ParkingLotSystem {
    public static void main(String[] args) {
        // Initialize parking spots
        List<ParkingSpot> parkingSpots = new ArrayList<>();
        parkingSpots.add(new CarParkingSpot(1));
        parkingSpots.add(new CarParkingSpot(2));
        parkingSpots.add(new BikeParkingSpot(3));
        parkingSpots.add(new BikeParkingSpot(4));
        // Initialize parking lot
        ParkingLot parkingLot = new ParkingLot(parkingSpots);
        // Create fee strategies
        ParkingFeeStrategy basicHourlyRateStrategy = new BasicHourlyRateStrategy();
        ParkingFeeStrategy premiumRateStrategy = new PremiumRateStrategy();
        // Create vehicles using Factory Pattern with fee strategies
        Vehicle car1 = VehicleFactory.createVehicle("Car", "CAR123", basicHourlyRateStrategy);
        Vehicle car2 = VehicleFactory.createVehicle("Car", "CAR345", basicHourlyRateStrategy);

        Vehicle bike1 = VehicleFactory.createVehicle("Bike", "BIKE456", premiumRateStrategy);
        Vehicle bike2 = VehicleFactory.createVehicle("Bike", "BIKE123", premiumRateStrategy);

        // Park vehicles
        ParkingSpot carSpot = parkingLot.parkVehicle(car1);
        ParkingSpot bikeSpot = parkingLot.parkVehicle(bike1);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select payment method for your vehicle:");
        System.out.println("1. Credit Card");
        System.out.println("2. Cash");
        int paymentMethod = scanner.nextInt();
        // Process payments using Strategy Patterns
        if (carSpot != null) {
            // Calculate fee using the specific strategy for the vehicle
            double carFee = car1.calculateFee(2, DurationType.HOURS);
            PaymentStrategy carPaymentStrategy = getPaymentStrategy(paymentMethod, carFee);
            carPaymentStrategy.processPayment(carFee);
            parkingLot.vacateSpot(carSpot, car1);
        }
        if (bikeSpot != null) {
            // Calculate fee using the specific strategy for the vehicle
            double bikeFee = bike1.calculateFee(3, DurationType.HOURS);
            PaymentStrategy bikePaymentStrategy = getPaymentStrategy(paymentMethod, bikeFee);
            bikePaymentStrategy.processPayment(bikeFee);
            parkingLot.vacateSpot(bikeSpot, bike1);
        }
        scanner.close();
    }

    private static PaymentStrategy getPaymentStrategy(
            int paymentMethod, double fee) {
        switch (paymentMethod) {
            case 1:
                return new CreditCardPayment(fee);
            case 2:
                return new CashPayment(fee);
            default:
                System.out.println("Invalid choice! Default to Credit card payment.");
                return new CreditCardPayment(fee);
        }
    }
}
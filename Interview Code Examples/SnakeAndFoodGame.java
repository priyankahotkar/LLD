import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings("unchecked")
class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!key.equals(pair.key))
            return false;
        return value.equals(pair.value);
    }

    @Override
    public String toString() {
        return "(" + key + ", " + value + ")";
    }
}

class Snake {
    private Deque<Pair<Integer, Integer>> body;
    private Map<Pair<Integer, Integer>, Boolean> positionMap;

    public Snake() {
        this.body = new LinkedList<>();
        this.positionMap = new HashMap<>();

        Pair<Integer, Integer> initialPos = new Pair<>(0, 0);
        this.body.offerFirst(initialPos);
        this.positionMap.put(initialPos, true);
    }
}

class Food {
    private int[][] foodPositions;
    private int currentFoodIndex;

    public Food(int[][] foodPositions) {
        this.foodPositions = foodPositions;
        this.currentFoodIndex = 0;
    }
}

interface MoveMentStrategy {
    Pair<Integer, Integer> getNextPosition(Pair<Integer, Integer> currentHead, String direction);
}

class HumanMovementStrategy implements MoveMentStrategy {
    @Override
    public Pair<Integer, Integer> getNextPosition(Pair<Integer, Integer> currentHead, String direction) {
        int row = currentHead.getKey();
        int col = currentHead.getValue();

        switch (direction) {
            case "U":
                return new Pair<>(row - 1, col);
            case "D":
                return new Pair<>(row + 1, col);
            case "L":
                return new Pair<>(row, col - 1);
            case "R":
                return new Pair<>(row, col + 1);
            default:
                return currentHead;
        }
    }
}

abstract class FoodItem {
    protected int row, column;
    protected int points;

    public FoodItem(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getPoints() {
        return points;
    }
}

class NormalFood extends FoodItem {
    public NormalFood(int row, int col) {
        super(row, col);
        this.points = 1;
    }
}

class BonusFood extends FoodItem {
    public BonusFood(int row, int col) {
        super(row, col);
        this.points = 3;
    }
}

class FoodFactory {
    public static FoodItem createFood(int[] position, String type) {
        if ("bonus".equals(type)) {
            return new BonusFood(position[0], position[1]);
        }

        return new NormalFood(position[0], position[1]);
    }
}

class GameBoard {
    private static GameBoard instance;
    private int width;
    private int height;

    private GameBoard(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static GameBoard getInstance(int width, int height) {
        if (instance == null) {
            instance = new GameBoard(width, height);
        }
        return instance;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

class SnakeGame {
    private GameBoard board;
    Deque<Pair<Integer, Integer>> snake;
    private Map<Pair<Integer, Integer>, Boolean> snakeMap;
    private int[][] food;
    private int foodIndex;
    private MoveMentStrategy movementStrategy;

    // Initialize the game with specified dimensions and food positions.
    public SnakeGame(int width, int height, int[][] food) {
        this.board = GameBoard.getInstance(width, height);
        this.food = food;
        this.foodIndex = 0;

        // Initialize snake
        this.snake = new LinkedList<>();
        this.snakeMap = new HashMap<>();
        Pair<Integer, Integer> initialPos = new Pair<>(0, 0);
        this.snake.offerFirst(initialPos);
        this.snakeMap.put(initialPos, true);

        // Set default movement strategy
        this.movementStrategy = new HumanMovementStrategy();
    }

    // Set the movement strategy (Human or AI)
    public void setMovementStrategy(MoveMentStrategy strategy) {
        this.movementStrategy = strategy;
    }

    // Returns the new score or -1 if game over.
    public int move(String direction) {
        // Get current head
        Pair currentHead = this.snake.peekFirst();

        // Get next position using strategy pattern
        Pair<Integer, Integer> newHead = this.movementStrategy.getNextPosition(currentHead, direction);
        int newHeadRow = newHead.getKey();
        int newHeadColumn = newHead.getValue();
        // Check boundary conditions
        boolean crossesBoundary = newHeadRow < 0 || newHeadRow >= this.board.getHeight() ||
                newHeadColumn < 0 || newHeadColumn >= this.board.getWidth();

        // Get current tail for collision check
        Pair<Integer, Integer> currentTail = this.snake.peekLast();

        // Check if snake bites itself (excluding tail which will move away)
        boolean bitesItself = this.snakeMap.containsKey(newHead) &&
                !(newHead.getKey() == currentTail.getKey() &&
                        newHead.getValue() == currentTail.getValue());

        // Game over conditions
        if (crossesBoundary || bitesItself) {
            return -1;
        }
        // Check if snake eats food
        boolean ateFood = (this.foodIndex < this.food.length) &&
                (this.food[this.foodIndex][0] == newHeadRow) &&
                (this.food[this.foodIndex][1] == newHeadColumn);
        if (ateFood) {
            // Increment food index to move to next food
            this.foodIndex++;
        } else {
            // If no food eaten, remove tail
            this.snake.pollLast();
            this.snakeMap.remove(currentTail);
        }
        // Add new head
        this.snake.addFirst(newHead);
        this.snakeMap.put(newHead, true);
        // Calculate ans return score
        int score = this.snake.size() - 1;
        return score;
    }
}

public class SnakeAndFoodGame {
    public static void main(String[] args) {
        // Define game configuration
        // can be taken as user input as well
        int width = 20;
        int height = 15;
        // Define some food positions (more can be generated during gameplay)
        int[][] foodPositions = {
                { 5, 5 }, // Initial food
                { 10, 8 }, // Second food
                { 3, 12 }, // Third food
                { 8, 17 }, // Fourth food
                { 12, 3 } // Fifth food
        };
        // Initialize the game
        SnakeGame game = new SnakeGame(width, height, foodPositions);
        // Display game instructions
        System.out.println("===== SNAKE GAME =====");
        System.out.println(
                "Controls: W (Up), S (Down), A (Left), D (Right), Q (Quit)");
        System.out.println("Eat food to grow your snake and increase your score.");
        System.out.println("Don't hit the walls or bite yourself!");
        System.out.println("=======================");
        // Create scanner for user input
        Scanner scanner = new Scanner(System.in);
        boolean gameRunning = true;
        int score = 0;
        // Main game loop
        while (gameRunning) {
            // Display current game state (in a real implementation, you would
            // have a graphical representation of the board)
            displayGameState(game);
            // Get user input
            System.out.print("Enter move (W/A/S/D) or Q to quit: ");
            String input = scanner.nextLine().toUpperCase();
            // Handle quit command
            if (input.equals("Q")) {
                System.out.println("Game ended by player. Final score: " + score);
                gameRunning = false;
                continue;
            }
            // Convert WASD input to UDLR for game processing
            String direction = convertInput(input);
            // Skip invalid inputs
            if (direction.isEmpty()) {
                System.out.println("Invalid input! Use W/A/S/D to move or Q to quit.");
                continue;
            }
            // Make the move and get the new score
            score = game.move(direction);
            // Check for game over
            if (score == -1) {
                System.out.println("GAME OVER! You hit a wall or bit yourself.");
                System.out.println("Final score: " + (game.snake.size() - 1));
                gameRunning = false;
            } else {
                System.out.println("Score: " + score);
            }
        }
        scanner.close();
        System.out.println("Thanks for playing!");
    }

    // Convert user-friendly WASD input to UDLR for the game engine
    private static String convertInput(String input) {
        switch (input) {
            case "W":
                return "U"; // Up
            case "S":
                return "D"; // Down
            case "A":
                return "L"; // Left
            case "D":
                return "R"; // Right
            default:
                return ""; // Invalid input
        }
    }

    // A simple method to display the game state in the console
    // In a real implementation, this would be replaced with graphics
    private static void displayGameState(SnakeGame game) {
        // This is a placeholder - in a real implementation, you would
        // access the game's state and render it appropriately
        System.out.println("nCurrent snake length: " + game.snake.size());
        // In a complete implementation, you would render the board with the
        // snake, food, and boundaries visually
    }
}
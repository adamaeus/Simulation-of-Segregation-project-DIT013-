import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.*;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then method start() far below.
 * - To test methods uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    class Actor {
        final Color color;        // Color an existing JavaFX class
        boolean isSatisfied;      // false by default

        Actor(Color color) {      // Constructor to initialize
            this.color = color;
        }  // Constructor, used to initialize
    }

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used directly in methods init() and updateWorld()
   Actor[][] world;              // The world is a square matrix of Actors

    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        double threshold = 0.7;
        updateUnsatisfied(world);
        world = updateWorldMethod(world, threshold);

        // TODO
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime
    // That's why we must have "@Override" and "public" (just accept for now)
    @Override
    public void init() {

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.50};
        int nLocations = 900;   // Should also try 90 000


        // TODO
        world = plotWorld(nLocations, dist);
        shuffleWorld(world);


        // Should be last
        fixScreenSize(nLocations);
    }


    // ------------  WORLD -----------------------------


    // Plot the World.
    // dist[0] * nLocations ger oss en spridning, samt ett antal varav 25% av nLocations (tex 900) ska vara "blue".
    // for (loc) loopen håller oss innanför bounds.

    private Actor[][] plotWorld(int nLocations, double[] dist) {
        Actor blue = new Actor(Color.BLUE);
        Actor red = new Actor(Color.RED);
        int axisLength = (int) Math.sqrt(nLocations);
        Actor[][] newWorld = new Actor[axisLength][axisLength];
        int i = 0;
        for (int loc = 0; loc < nLocations; loc++) {
            int row = loc / axisLength;
            int col = loc % axisLength;
            if (i < (dist[0] * nLocations)) {
                newWorld[row][col] = blue;
            } else if (i < (dist[1] * nLocations) + (dist[0] * nLocations)) {
                newWorld[row][col] = red;
            }
            i++;
        }
        return newWorld;
    }


    // Shuffle the world.

    private void shuffleWorld(Actor[][] newWorld) {
        int axisLength = newWorld.length;
        Random rand = new Random();

        for (int i = axisLength - 1; i >= 0; i--) {
            for (int j = axisLength - 1; j >= 0; j--) {
                int randomRow = rand.nextInt(i + 1);
                int randomCol = rand.nextInt(j + 1);

                Actor temp = newWorld[i][j];
                newWorld[i][j] = newWorld[randomRow][randomCol];
                newWorld[randomRow][randomCol] = temp;
            }
        }
    }


   /* 3. Now your task is to implement the method updateWorld() which will find all
    dissatisfied actors (RED or BLUE) and move them to new (random, previously NOT occupied) positions,
    until all are satisfied. All unsatisfied are moved at the same time, else a move may affect state
    of other Actor. The nulls are never considered in any calculation.
    The updateWorld() method is automatically called by a timer (you don't need to call it).
    The updateWorld() method is *HUGE* if written as one single method! Must break it down.
    If new to programming apply strategy as in 1. Make sketch etc.....

    NOTE: Not guaranteed to get all satisfied if selecting other values for dist and
    threshold variables (see code).


    */




    // ------------  NULL -----------------------------

    //Shuffle the positions "occupied" by a null value.
    int[][] shuffleNullPositions(int[][] positions){
        int dim = positions.length;
        Random rand = new Random();


        for (int i = 0; i < dim; i++) {
            int rand1 = rand.nextInt(dim-1);
            int[] temp = positions[rand1];
            positions[rand1] = positions[i];
            positions[i]= temp;

        }
        return positions;
    }


    // Count amount of nulls.
    int nulls(Actor[][] world){
        int nulls = 0;
        int dimension = world.length;

        for (int i = 0; i < dimension ; i++) {
            for (int j = 0; j < dimension; j++) {
                if (world[i][j] == null)
                    nulls++;
            }

        }
        return nulls;
    }


    // Method for getting the positions of nulls.
    private int[][] getNullPositions(Actor[][] world) {
        int[][] nullPositions = new int[nulls(world)][2];
        int index = 0;
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world.length; j++) {
                if (world[i][j] == null) {
                    nullPositions[index][0] = i;
                    nullPositions[index][1] = j;
                    index++;
                }
            }
        }
        return nullPositions;
    }



    // ------------  MOVEMENT & RELOCATION -----------------------------

    // Condensed method to be called from "main" (updateWorld).
    // Delegates the call down to bigger methods.
    public void updateUnsatisfied(Actor [][] world){
        moveUnsatisfiedActors(world);
    }


    Actor[][] moveUnsatisfiedActors(Actor[][] world) {
        int rows = world.length;
        int cols = world[0].length;
        Actor[][] newWorld = new Actor[rows][cols];
        Random rand = new Random();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int newRow, newCol;
                do {
                    newRow = rand.nextInt(rows);
                    newCol = rand.nextInt(cols);
                } while (newWorld[newRow][newCol] != null);
            }
        }
        return newWorld;
    }

    Actor[][] updateWorldMethod(Actor[][] world, double threshold) {
        int[][] unsatIndex = getUnsatisfiedPositions(world);
        int dim = unsatIndex.length;
        int[][] shuffledNulls = shuffleNullPositions(getNullPositions(world));

        for (int i = 0; i < dim; i++) {
            int rowS = unsatIndex[i][0];
            int colS = unsatIndex[i][1];

            if (!isSatisfied(world, world[rowS][colS], threshold, rowS, colS)) {
                int rowN = shuffledNulls[i][0];
                int colN = shuffledNulls[i][1];

                world[rowN][colN] = world[rowS][colS];
                world[rowS][colS] = null;
            }
        }
        return world;
    }



    // ------------  NEIGHBOURS & SATISFACTION -----------------------------


    // Helper method (delegating) to check if an Actor is satisfied.
    // Delegates call to "hasGoodNeighbours".
    boolean isSatisfied(Actor[][] world, Actor target, double threshold, int row, int col) {
        return hasGoodNeighbours(world, target, threshold, row, col);
    }


    //Method to check if neighbours are satisfied for a given index in world.
    private boolean hasGoodNeighbours(Actor[][] world, Actor target, double threshold, int row, int col) {
        int count = 0;
        int total = 0;

        for (int i = row - 1; i <= row + 1; i++){
            for (int j = col - 1; j <= col + 1; j ++){
                if (isValidLocation(world.length, i, j)){
                    if((world[i][j] != null)){
                        if (compareColor(world[i][j], target) && (i != row || j != col)) {
                            count++;
                        }
                        total++;
                    }
                }
            }
        }

        double percentage = (double) count / total;
        double roundedPercentage = Math.round(percentage);
        return roundedPercentage >= threshold;
    }

    public boolean compareIfNotNull(Actor target){
        return target == null;
    }


    // Generates a number of unsatisfied actors in the world.
    int countUnsatisfied(Actor[][] world) {
        int dim = world.length;
        int dissatisfied = 0;


        for (int i = 0; i < dim; i++){
            for (int j = 0; j < dim; j++) {
                if (world[i][j] != null) {
                    if (!world[i][j].isSatisfied) {
                        dissatisfied++;
                    }
                }
            }
        }
        return dissatisfied;
    }

    // Gets the positions of the unsatisfied actors.
    int[][] getUnsatisfiedPositions(Actor[][] world) {
        int dim = world.length;
        int unsatisfiedPlaces = countUnsatisfied(world);
        int[][] unsatisfiedIndex = new int[unsatisfiedPlaces][2];
        int unsatisfiedOccurence = 0;

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if(world[i][j] != null){
                    if (!world[i][j].isSatisfied) {
                        unsatisfiedIndex[unsatisfiedOccurence] = new int[]{i, j};
                        unsatisfiedOccurence++;
                    }
                }
            }
        }
        return unsatisfiedIndex;
    }


    // ------------  HELPER METHODS -----------------------------
    public boolean compareColor(Actor actor, Actor target){
        return actor.color == target.color;
    }
    boolean isValidLocation(int size, int row, int col) {
        return 0 <= row && row < size && 0 <= col && col < size;
    }



    // ------------ MISC -----------------------------
    Actor doIt(Actor[][] world){
        return world[0][0];
    }


    void test() {

        exit(0);
    }

    // ******************** NOTHING to do below this row, it's JavaFX stuff  **************

    double width = 500;   // Size for window
    double height = 500;
    final double margin = 50;
    double dotSize;

    void fixScreenSize(int nLocations) {
        // Adjust screen window
        dotSize = (double) 9000 / nLocations;
        if (dotSize < 1) {
            dotSize = 2;
        }
        width = sqrt(nLocations) * dotSize + 2 * margin;
        height = width;
    }

    long lastUpdateTime;
    final long INTERVAL = 450_000_000;


    @Override
    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long now) {
                long elapsedNanos = now - lastUpdateTime;
                if (elapsedNanos > INTERVAL) {
                    updateWorld();
                    renderWorld(gc);
                    lastUpdateTime = now;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int x = (int) (dotSize * col + margin);
                int y = (int) (dotSize * row + margin);
                if (world[row][col] != null) {
                    g.setFill(world[row][col].color);
                    g.fillOval(x, y, dotSize, dotSize);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Question1 {
    public static void main(String[] args) {
        int size;
        int iterations;
        Scanner sc = new Scanner(System.in);

        // Prompt user to enter size of image and number of iterations.
        System.out.print("Enter size of image to be processed: ");
        size = sc.nextInt();
        System.out.print("Enter number of iterations of convolution to perform: ");
        iterations = sc.nextInt();

        // Create an image to be processed
        long[][] image = generateImageArray(size);

        // Print the image to be processed
        System.out.println("Original image:\n");
        for (int row = 0; row < image.length; row++) {
            for (int col = 0; col < image[0].length; col++) {

                System.out.printf("%4d", image[row][col]);

                if (col != image[0].length - 1) {
                    System.out.print(",");
                }
            }
            System.out.println("");
        }
        System.out.println("\n\n");
        long[][] outputArray = image;

        // Perform convolution for a specified number of iterations
        for (int i = 0; i < iterations; i++) {
            outputArray = new Convolve(outputArray).convolve();
        }

        // Print the result of convolution
        System.out.println("Output after convolution: \n");
        for (int row = 0; row < outputArray.length; row++) {
            for (int col = 0; col < outputArray[0].length; col++) {

                System.out.printf("%4d", outputArray[row][col]);

                if (col != outputArray[0].length - 1) {
                    System.out.print(",");
                }
            }
            System.out.println("");
        }
    }

    // Function to generate an image with a specified size
    private static long[][] generateImageArray(int size) {
        // Create image variable with size provided by user.
        long[][] image = new long[size][size];
        Random rand = new Random();

        // Fill the image array with random values between 0-255.
        for (int i = 0; i < size; i++) {
            for (int x = 0; x < size; x++) {
                image[i][x] = rand.nextInt(256);
            }
        }
        return image;
    }
}

class Convolve {
    private int rows;
    private int cols;
    private long[][] image;
    private long[][] output;

    //Constructor
    Convolve(long[][] image) {
        this.image = image;
        this.rows = image.length;
        this.cols = image[0].length;
        this.output = new long[rows - 2][cols - 2];
    }

    public long[][] convolve() {
        // Get number of processing cores available.
        int cores = Runtime.getRuntime().availableProcessors();
        int numOfThreads;
        int timesToSplit;
        int rowsForEachSplit;

        //Get the number of threads to create, number of times to split the image and the rows for each split array if
        // number of rows exceed the number of cores available.
        if (rows > cores) {
            timesToSplit = cores;
            numOfThreads = cores;
            rowsForEachSplit = Math.round((rows - 2) / timesToSplit);
        } else {
            timesToSplit = rows;
            numOfThreads = rows;
            rowsForEachSplit = 1;
        }

        // Create arrayList to store the split image.
        ArrayList<Integer> splitStartIndex = new ArrayList<Integer>();
        ArrayList<Integer> splitEndIndex = new ArrayList<Integer>();

        int currentRow = 1;

        for (int i = 0; i < timesToSplit; i++) {
            int start = currentRow;
            int end = currentRow + rowsForEachSplit;

            if (i == timesToSplit - 1) {
                end = rows - 1;//Take remaining row
            }

            currentRow = end;
            splitStartIndex.add(start);
            splitEndIndex.add(end);
        }

        // Create a thread pool based on the number of threads obtained.
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);

        // Submit the convolution tasks to the executor.
        for (int i = 0; i < timesToSplit; i++) {
            executor.submit(new SplitTask(this, splitStartIndex.get(i), splitEndIndex.get(i), cols));
        }

        executor.shutdown();

        // Wait for all threads to finish executing
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }

        return output;
    }

    // Get the image to be processed
    public long[][] getArray() {
        return image;
    }

    // Save output of convolution into output array
    public void saveOutput(int row, int column, long result) {
        output[row][column] = result;
    }
}

class SplitTask implements Runnable {
    private Convolve convolve;
    private int start;
    private int end;
    private int cols;

    // Create the kernel for Convolution.
    final static int[][] kernel = { { 1, 0, 1 }, { 0, 1, 0 }, { 1, 0, 1 } };

    // Constructor for SplitTask
    SplitTask(Convolve convolve, int start, int end, int cols) {
        this.convolve = convolve;
        this.start = start;
        this.end = end;
        this.cols = cols;
    }

    @Override
    public void run() {
        // Perform convolution (multiply the input data with the kernel).
        for (int row = start; row < end; row++) {
            for (int col = 1; col < cols; col++) {
                long topLeft = kernel[0][0] * convolve.getArray()[row - 1][col - 1];
                long top = kernel[0][1] * convolve.getArray()[row - 1][col];
                long topRight = kernel[0][2] * convolve.getArray()[row - 1][col + 1];
                long left = kernel[1][0] * convolve.getArray()[row][col - 1];
                long centre = kernel[1][1] * convolve.getArray()[row][col];
                long right = kernel[1][2] * convolve.getArray()[row][col + 1];
                long bottomLeft = kernel[2][0] * convolve.getArray()[row + 1][col - 1];
                long bottom = kernel[2][1] * convolve.getArray()[row + 1][col];
                long bottomRight = kernel[2][2] * convolve.getArray()[row + 1][col + 1];
                long outputValue = topLeft + top + topRight + left + centre + right + bottomLeft + bottom + bottomRight;

                // Save output of convolution
                convolve.saveOutput(row - 1, col - 1, outputValue);
            }
        }
    }
}



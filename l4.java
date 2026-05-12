import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * ImageMeanFilter - A utility class for applying mean filtering to images.
 *
 * <p>This class provides functionality to apply a mean filter (also known as
 * averaging filter or box filter) to images for noise reduction and smoothing.
 * The filter replaces each pixel with the average value of its neighboring pixels
 * within a specified kernel size.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ImageMeanFilter.applyMeanFilter("input.jpg", "output.jpg", 3, 4);
 * }
 * </pre>
 *
 * @author Programming Course
 * @version 1.0
 * @since 2024
 */
public class ImageMeanFilter {
   
    /**
     * Applies mean filter to an image using multiple threads.
     *
     * @param inputPath  Path to input image
     * @param outputPath Path to output image
     * @param kernelSize Size of mean kernel
     * @param numThreads Number of threads used in the processing
     * @throws IOException If there is an error reading/writing
     */
    public static void applyMeanFilter(String inputPath, String outputPath,
            int kernelSize, int numThreads) throws IOException, InterruptedException {
        // Load image
        BufferedImage originalImage = ImageIO.read(new File(inputPath));
       
        if (originalImage == null) {
            throw new IOException("Could not load image: " + inputPath);
        }
       
        // Create output image
        BufferedImage filteredImage = new BufferedImage(
            originalImage.getWidth(),
            originalImage.getHeight(),
            originalImage.getType()
        );
       
        // Image processing
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Thread[] threads = new Thread[numThreads];
        long[] changedPixels = new long[numThreads];
        long[] unchangedPixels = new long[numThreads];

        int rowsPerThread = height / numThreads;
        int remainingRows = height % numThreads;
        int currentStart = 0;

        for (int i = 0; i < numThreads; i++) {
            int startY = currentStart;
            int extraRow = i < remainingRows ? 1 : 0;
            int endY = startY + rowsPerThread + extraRow;
            int threadIndex = i;

            threads[i] = new Thread(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        // Calculate neighborhood average
                        int[] avgColor = calculateNeighborhoodAverage(originalImage, x, y, kernelSize);

                        int newRgb =
                            (avgColor[0] << 16) |
                            (avgColor[1] << 8)  |
                            avgColor[2];

                        // Set filtered pixel
                        filteredImage.setRGB(x, y, newRgb);

                        int originalRgb = originalImage.getRGB(x, y) & 0xFFFFFF;
                        if (originalRgb == newRgb) {
                            unchangedPixels[threadIndex]++;
                        } else {
                            changedPixels[threadIndex]++;
                        }
                    }
                }
            });

            threads[i].start();
            currentStart = endY;
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }

        long totalChanged = 0;
        long totalUnchanged = 0;

        for (int i = 0; i < numThreads; i++) {
            totalChanged += changedPixels[i];
            totalUnchanged += unchangedPixels[i];
        }

        System.out.println("Pixels alterados: " + totalChanged);
        System.out.println("Pixels inalterados: " + totalUnchanged);
       
        // Save filtered image
        ImageIO.write(filteredImage, "jpg", new File(outputPath));
    }
   
    /**
     * Calculates the average RGB values for a pixel's neighborhood.
     *
     * @param image      The source image
     * @param centerX    X coordinate of center pixel
     * @param centerY    Y coordinate of center pixel
     * @param kernelSize Size of the kernel
     * @return Array containing average RGB values [red, green, blue]
     */
    private static int[] calculateNeighborhoodAverage(
            BufferedImage image,
            int centerX,
            int centerY,
            int kernelSize
    ) {
        int width = image.getWidth();
        int height = image.getHeight();
        int halfKernel = kernelSize / 2;
       
        long sumR = 0;
        long sumG = 0;
        long sumB = 0;
        int count = 0;
       
        for (int dy = -halfKernel; dy <= halfKernel; dy++) {
            for (int dx = -halfKernel; dx <= halfKernel; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
               
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int rgb = image.getRGB(x, y);
                   
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                   
                    sumR += r;
                    sumG += g;
                    sumB += b;
                    count++;
                }
            }
        }
       
        return new int[] {
            (int) (sumR / count),
            (int) (sumG / count),
            (int) (sumB / count)
        };
    }
   
    /**
     * Main method for demonstration.
     *
     * Usage: java ImageMeanFilter <input_file> <num_threads>
     *
     * Arguments:
     *   input_file - Path to the input image file to be processed
     *                Supported formats: JPG, PNG
     *   num_threads - Number of threads used to process the image
     *
     * Example:
     *   java ImageMeanFilter input.jpg 4
     *
     * The program will generate a filtered output image named "filtered_output.jpg"
     * using a 7x7 mean filter kernel.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ImageMeanFilter <input_file> <num_threads>");
            System.exit(1);
        }

        String inputFile = args[0];
        int numThreads = Integer.parseInt(args[1]);

        if (numThreads < 2) {
            System.err.println("Error: use at least 2 threads.");
            System.exit(1);
        }

        try {
            applyMeanFilter(inputFile, "filtered_output.jpg", 7, numThreads);
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Processing was interrupted.");
        }
    }
}

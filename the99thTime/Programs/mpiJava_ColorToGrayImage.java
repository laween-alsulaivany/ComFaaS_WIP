
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import mpi.* ;
//To compile: /home/bloodyanger31/OpenMPI/bin/mpijavac mpiJava_ColorToGrayImage.java


public class mpiJava_ColorToGrayImage {

    public static void main(String[] args) throws MPIException {
        String inputFolder = "Input/" ;
        String outputFolder = "Output/" ;

        MPI.Init(args) ;
        int rank = MPI.COMM_WORLD.getRank(),
            size = MPI.COMM_WORLD.getSize() ;
        File[] imageFiles = new File(inputFolder).listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
        //File imagefile : imageFiles
        if (rank < imageFiles.length) {
            for (int i = rank; i < imageFiles.length; i = i + size ) {
                try {
                    BufferedImage colorImage = ImageIO.read(imageFiles[i]) ;
                    BufferedImage grayImage = convertToGrayscale(colorImage) ;
                    saveGrayImage(grayImage, outputFolder, imageFiles[i].getName());
                }
                catch (IOException e) {
                    e.printStackTrace() ;
                }
            }
        }
        MPI.COMM_WORLD.barrier() ;
        if (rank == 0) System.out.println("All image processing completed. ");
        MPI.Finalize() ;
    }

    private static BufferedImage convertToGrayscale(BufferedImage colorImage) {
        int width = colorImage.getWidth();
        int height = colorImage.getHeight();

        // Create a new grayscale image with the same dimensions as the color image
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // Convert each pixel from color to grayscale
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the RGB values of the color pixel
                Color color = new Color(colorImage.getRGB(x, y));

                // Calculate the grayscale value using the luminosity method
                int grayValue = (int) (0.21 * color.getRed() + 0.72 * color.getGreen() + 0.07 * color.getBlue());

                // Create a new grayscale color with the same RGB value
                Color grayColor = new Color(grayValue, grayValue, grayValue);

                // Set the grayscale color to the corresponding pixel in the grayscale image
                grayImage.setRGB(x, y, grayColor.getRGB());
            }
        }
        return grayImage;
    }

    private static void saveGrayImage(BufferedImage grayImage, String outputFolder, String fileName) throws IOException {
        // Create the output folder if it doesn't exist
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Get the file extension
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);

        // Create the output file name with the same name and extension as the input file
        String outputFileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_gray." + fileExtension;
        File outputFile = new File(folder, outputFileName);

        // Save the grayscale image to disk
        ImageIO.write(grayImage, fileExtension, outputFile);
    }
}

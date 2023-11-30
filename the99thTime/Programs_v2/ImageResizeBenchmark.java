import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageResizeBenchmark {
    public static void main(String[] args) {
        String inputFolderPath = "Input";
        String outputFolderPath = "Output";
        int newWidth = 400;
        int newHeight = 400;

        File inputFolder = new File(inputFolderPath);
        File[] inputFiles = inputFolder.listFiles();

        if (inputFiles != null) {
            for (File inputFile : inputFiles) {
                try {
                    BufferedImage originalImage = ImageIO.read(inputFile);

                    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

                    Graphics2D g2d = resizedImage.createGraphics();
                    g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                    g2d.dispose();

                    String outputFilePath = outputFolderPath + File.separator + "resized" + inputFile.getName();
                    File outputFile = new File(outputFilePath);
                    ImageIO.write(resizedImage, "jpg", outputFile);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.exit(1) ;
    }
}

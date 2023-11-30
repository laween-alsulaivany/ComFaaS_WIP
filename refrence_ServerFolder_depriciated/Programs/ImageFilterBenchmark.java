import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageFilterBenchmark {
    public static void main(String[] args) {
        String inputFolderPath = "Input";
        String outputFolderPath = "Output";
        float[] kernelData = {1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f};
        int kernelSize = 7;

        File inputFolder = new File(inputFolderPath);
        File[] inputFiles = inputFolder.listFiles();

        if (inputFiles != null) {
            for (File inputFile : inputFiles) {
                try {
                    BufferedImage originalImage = ImageIO.read(inputFile);

                    Kernel kernel = new Kernel(kernelSize, kernelSize, kernelData);
                    ConvolveOp convolveOp = new ConvolveOp(kernel);

                    BufferedImage filteredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
                            originalImage.getType());
                    Graphics2D g2d = filteredImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.drawImage(originalImage, 0, 0, null);
                    g2d.dispose();

                    BufferedImage resultImage = convolveOp.filter(filteredImage, null);

                    String outputFilePath = outputFolderPath + File.separator + "filtered" + inputFile.getName();
                    File outputFile = new File(outputFilePath);
                    ImageIO.write(resultImage, "jpg", outputFile);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.exit(1) ;
    }
}

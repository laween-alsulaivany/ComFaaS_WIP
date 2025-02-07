// FileTypeDetector.java

package comfaas;

public class FileTypeDetector {

    public static String detectFileType(String fileName) {
        if (fileName.endsWith(".java")) {
            return "Java";
        } else if (fileName.endsWith(".py")) {
            return "Python";
        } else if (fileName.endsWith(".c")) {
            return "C";
        } else {
            return "Unknown file type";
        }
    }

}

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

    public static void main(String[] args) {
        String javaFile = "HelloWorld.java";
        String pythonFile = "script.py";
        String cFile = "program.c";
        String unknownFile = "readme.txt";

        // System.out.println(detectFileType(javaFile));
        // System.out.println(detectFileType(pythonFile));
        // System.out.println(detectFileType(cFile));
        // System.out.println(detectFileType(unknownFile));
    }
}

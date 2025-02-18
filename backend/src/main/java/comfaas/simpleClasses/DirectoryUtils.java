package comfaas.simpleClasses;


import java.io.File;
import java.io.IOException;

public class DirectoryUtils {

    /**
     * Checks if the 'child' directory (or file) is located inside the 'parent' directory.
     *
     * @param parent the parent directory
     * @param child  the child directory or file
     * @return true if child is inside parent, false otherwise
     * @throws IOException if an I/O error occurs while resolving paths
     */
    public static boolean isDirectoryInDirectory(File parent, File child) throws IOException {
        // Get canonical paths to resolve symlinks and relative path components
        String parentCanonicalPath = parent.getCanonicalPath();
        String childCanonicalPath = child.getCanonicalPath();
        
        // Ensure parent path ends with a file separator to prevent partial matching
        if (!parentCanonicalPath.endsWith(File.separator)) {
            parentCanonicalPath += File.separator;
        }
        
        return childCanonicalPath.startsWith(parentCanonicalPath);
    }
    
    // Example usage:
    public static void main(String[] args) {
        try {
            File parentDir = new File("/home/user/documents");
            File childDir = new File("/home/user/documents/project");
            File unrelatedDir = new File("/home/user/downloads");

            System.out.println("childDir in parentDir? " + isDirectoryInDirectory(parentDir, childDir));
            System.out.println("unrelatedDir in parentDir? " + isDirectoryInDirectory(parentDir, unrelatedDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

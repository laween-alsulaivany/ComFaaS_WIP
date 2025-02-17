public class StringManipulation {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // Create a big string and do repeated manipulations
        StringBuilder sb = new StringBuilder("ComFaaS");
        for (int i = 0; i < 10_000_000; i++) {
            sb.append("X");
        }
        // Convert to uppercase
        String result = sb.toString().toUpperCase();

        long endTime = System.currentTimeMillis();
        System.out.println("String length: " + result.length());
        System.out.println("Time elapsed: " + (endTime - startTime) + " ms");
    }
}

package comfaas;

import java.util.regex.*;

public class CommandLineProcessor {
    
    public static void processArguments(
        Edge client
        ) {

        // Loop through the arguments
        for (int i = 0; i < client.args.length; i++) {
            switch (client.args[i]) {
                case "-server":
                    if (i + 1 < client.args.length) {
                        client.server = client.args[++i];
                    } else {
                        System.err.println("Error: Missing value for -server");
                        return;
                    }
                    break;
                case "-p":
                    if (i + 1 < client.args.length) {
                        try {
                            client.port = Integer.parseInt(client.args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid port number");
                            return;
                        }
                    } else {
                        System.err.println("Error: Missing value for -p");
                        return;
                    }
                    break;
                case "-t":
                    if (i + 1 < client.args.length) {
                        client.type = client.args[++i];
                        if (!client.type.equals("cloud") && !client.type.equals("edge")) {
                            System.err.println("Error: Invalid value for -t. Must be 'cloud' or 'edge'");
                            return;
                        }
                    } else {
                        System.err.println("Error: Missing value for -t");
                        return;
                    }
                    break;
                case "-np":
                    if (i + 1 < client.args.length) {
                        try {
                            client.np = Integer.parseInt(client.args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid value for -np");
                            return;
                        }
                    } else {
                        System.err.println("Error: Missing value for -np");
                        return;
                    }
                    break;
                case "-tid":
                    if (i + 1 < client.args.length) {
                        try {
                            client.tid = Integer.parseInt(client.args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid value for -tid");
                            return;
                        }
                    } else {
                        System.err.println("Error: Missing value for -tid");
                        return;
                    }
                    break;
                case "-tn":
                    if (i + 1 < client.args.length) {
                        if (isValidFileName(client.args[++i])) {
                            client.tn = client.args[i] ;
                        }
                        else {
                            System.err.println("Error: Invalid value for -tn. Must be a valid fiename");
                            return;
                        }

                    } else {
                        System.err.println("Error: Missing value for -tn");
                        return;
                    }
                    break ;
                case "-k":
                    client.kFlag = true;
                    System.out.println("kflag flipped with "+client.args[i]);
                    break;
                case "-lang":
                    if (i + 1 < client.args.length) {
                        client.lang = client.args[++i];
                        if (!client.lang.equals("python") && !client.lang.equals("java") && !client.lang.equals("C")) {
                            System.err.println("Error: Invalid value for -lang. Must be 'python', 'java', or 'C'");
                            return;
                        }
                    } else {
                        System.err.println("Error: Missing value for -lang");
                        return;
                    }
                    break;
                default:
                    continue ;
                    // System.err.println("Error: Unknown argument " + client.args[i]);
                    // return;
            }
        }

        // Output the processed arguments
        // System.out.println("Server: " + client.server);
        // System.out.println("Port: " + client.port);
        // System.out.println("Type: " + client.type);
        // System.out.println("NP: " + client.np);
        // System.out.println("tn: " + client.tn);
        // System.out.println("K Flag: " + client.kFlag);
        // System.out.println("language: " + client.lang);

        if (client.server == null || client.port == -1 || client.type == null || client.np == -1 || client.tn == null || client.lang == null) {
            System.err.println("Error: Missing required arguments. Please provide -server, -p, -t, -np, -tid, and -lang.");
            return;
        }
    }
    public static boolean isValidFileName(String fileName) {
        // String regex = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*\\.[a-zA-Z]{2,4}$";
        // Pattern pattern = Pattern.compile(regex);
        // Matcher matcher = pattern.matcher(fileName);
        // return matcher.matches();
        return true ;
    }
}

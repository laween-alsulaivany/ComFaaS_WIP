// CommandLineProcessor.java
package comfaas;

// ------------------------------------------
// * The CommandLineProcessor class processes command-line arguments for the client.
// * It validates and parses the arguments to set the client properties.
// ------------------------------------------

public class CommandLineProcessor {

    // ------------------------------------------
    // Processes the command-line arguments and sets the client properties.
    // ------------------------------------------

    // TODO: Add missing arguments and remove unused ones
    public static void processArguments(Client client) {
        for (int i = 0; i < client.args.length; i++) {
            switch (client.args[i]) {
                case "-server" -> {
                    if (++i < client.args.length)
                        client.server = client.args[i];
                    else {
                        System.err.println("Error: Missing value for -server");
                        System.exit(1);
                    }
                }
                case "-p" -> {
                    if (++i < client.args.length) {
                        try {
                            client.port = Integer.valueOf(client.args[i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid port number");
                            System.exit(1);
                        }
                    } else {
                        System.err.println("Error: Missing value for -p");
                        System.exit(1);
                    }
                }
                case "-l" -> {
                    if (++i < client.args.length) {
                        client.location = client.args[i];
                        if (!client.location.equals("server")) {
                            System.err.println("Error: Invalid value for -l. Must be 'server'");
                            System.exit(1);
                        }
                    } else {
                        System.err.println("Error: Missing value for -l");
                        System.exit(1);
                    }
                }
                case "-np" -> {
                    if (++i < client.args.length) {
                        try {
                            client.np = Integer.valueOf(client.args[i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid value for -np");
                            System.exit(1);
                        }
                    } else {
                        System.err.println("Error: Missing value for -np");
                        System.exit(1);
                    }
                }
                case "-tn" -> {
                    if (++i < client.args.length)
                        client.tn = client.args[i];
                    else {
                        System.err.println("Error: Missing value for -tn");
                        System.exit(1);
                    }
                }
                case "-lang" -> {
                    if (++i < client.args.length)
                        client.lang = client.args[i];
                    else {
                        System.err.println("Error: Missing value for -lang");
                        System.exit(1);
                    }
                }
                case "-source" -> {
                    if (++i < client.args.length)
                        client.sourceFolder = client.args[i];
                    else {
                        System.err.println("Error: Missing value for -source");
                        System.exit(1);
                    }
                }
                case "-destination" -> {
                    if (++i < client.args.length)
                        client.destinationFolder = client.args[i];
                    else {
                        System.err.println("Error: Missing value for -destination");
                        System.exit(1);
                    }
                }
                default -> {
                    System.err.println("Error: Unknown argument " + client.args[i]);
                    System.exit(1);
                }
            }
        }

        // Validate only the absolutely required arguments.
        if (client.server == null || client.port == -1 || client.location == null) {
            System.err.println("Error: Missing required arguments. Provide at least -server, -p, and -l.");
            System.exit(1);
        }

        // Set defaults for optional parameters if they are not provided.
        if (client.np == -1)
            client.np = 1;
        if (client.tn == null)
            client.tn = "";
        if (client.lang == null)
            client.lang = "";
    }

}

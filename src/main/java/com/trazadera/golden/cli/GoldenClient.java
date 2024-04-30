package com.trazadera.golden.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import com.trazadera.golden.restclient.invoker.ApiClient;
import com.trazadera.golden.restclient.invoker.ApiException;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.lang.System.exit;

/**
 * Golden Client main.
 */
public class GoldenClient {

    private static final int EXIT_CODE_OK = 0;
    private static final int EXIT_CODE_ERROR_PARAMETERS = 1;
    private static final int EXIT_CODE_ERROR_GENERIC = 2;

    private static final String VARIABLE_TOKEN = "GOLDEN_TOKEN";
    private static final String VARIABLE_URL = "GOLDEN_URL";
    private static final String GOLDEN_FILE = ".golden";


    // Builders
    // =================================================================================================================

    GoldenClient() { }


    // Public methods
    // =================================================================================================================

    /**
     * Executes indicated command line.
     * @param args Command line
     * @return Exit code
     */
    public int execute(String[] args) {
        // Execute
        int exitCode = EXIT_CODE_OK;
        Context ctx = new Context();
        try {
            // Parse command line arguments
            GoldenParser.getInstance().parseCommandLine(ctx, args);

            // Execute global actions
            CommandLine cl = ctx.getGlobalCommandLine();
            if (cl==null || cl.hasOption("help")) {
                help(ctx, null);
            } else if (cl.hasOption("version")) {
                version();
            }

            // Execute command and print result
            else {
                Command cmd = ctx.getCommand();
                initializeApi(ctx);
                Call call = cmd.execute(ctx);
                if (call == null)
                    throw new IllegalArgumentException("invalid call");
                Response response = call.execute();
                if (response.isSuccessful())  {
                    printResult(ctx, response);
                } else {
                    exitCode = printError(ctx, response, null);
                }
            }
        } catch (IllegalArgumentException | ParseException e) {
            exitCode = EXIT_CODE_ERROR_PARAMETERS;
            help(ctx, e);
        } catch (Exception e) {
            exitCode = printError(ctx, null, e);
        }
        return exitCode;
    }

    /**
     * Main method. Allows for interactive usage if providing <code>--interactive</code>.
     * @param args
     */
    public static void main(String[] args) {
        boolean interactive = args.length == 1 && args[0] != null && args[0].trim().equalsIgnoreCase("--interactive");
        GoldenClient goldenClient = new GoldenClient();
        if (interactive) {
            goldenClient.version();
            System.out.println("Type 'exit' to quit");
            // System.console does not work from within IDE
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("golden> ");
                String line = scanner.nextLine().trim();
                if (line==null || line.length()==0) continue;
                if (line.toLowerCase().trim().equals("exit")) break;
                goldenClient.execute(line.split(" "));
            }
        } else {
            int exitCode = goldenClient.execute(args);
            exit(exitCode);
        }
    }


    // Private methods
    // =================================================================================================================

    /**
     * Initializes the API: retrieves API token and URL using any of the available methods and initializes the Golden API client.
     * @param context Context object.
     */
    private void initializeApi(Context context) {
        CommandLine cl = context.getGlobalCommandLine();

        // Locate golden file and load properties (if exists)
        Path path = Path.of(System.getProperty("user.home"), GOLDEN_FILE);
        Properties goldenFile = new Properties();
        File f = path.toFile();
        if (f.exists() && f.canRead()) {
            try (BufferedReader in = Files.newBufferedReader(path)) {
                goldenFile.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("error reading golden file: " + e.getMessage());
            }
        }

        // API token
        String token = cl.getOptionValue("token");
        if (token == null)
            token = System.getenv(VARIABLE_TOKEN);
        if (token == null)
            token = goldenFile.getProperty(VARIABLE_TOKEN);
        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("missing token, use --token or set environment variable GOLDEN_TOKEN or create a properties file $HOME/.golden with GOLDEN_TOKEN property.");

        // API URL
        String url = cl.getOptionValue("url");
        if (url == null)
            url = System.getenv(VARIABLE_URL);
        if (url == null)
            url = goldenFile.getProperty(VARIABLE_URL);
        if (url == null || url.isEmpty())
            throw new IllegalArgumentException("missing URL, use --url or set environment variable GOLDEN_URL or create a properties file $HOME/.golden with GOLDEN_URL property.");

        // Initialize API client
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(url);
        apiClient.setAccessToken(token);
        context.setApiClient(apiClient);
    }

    /**
     * Prints the result of the call.
     * @param ctx Context object.
     * @param response Response object.
     * @return Exit code.
     */
    private int printResult(Context ctx, Response response) {
        int exitCode = EXIT_CODE_OK;
        try {
            String output = GoldenPrinter.getInstance().formatOutput(ctx, response.body().string());
            System.out.println(output);
        } catch (IOException e) {
            exitCode = printError(ctx, null, e);
        }
        return exitCode;
    }

    /**
     * Handles error.
     * @param e Exception object.
     * @return Exit code.
     */
    private int printError(Context context, Response response, Exception e) {
        int exitCode = EXIT_CODE_ERROR_GENERIC;
        try {
            if (response != null && response.body() != null) {
                //System.out.println("--->" + response.body());
                Map<String, Object> map = new Gson().fromJson(response.body().string(), Map.class);
                System.out.println("Error [" + response.code() + "]: " + map.get("errors"));
                exitCode = response.code();
            } else if (e instanceof ApiException) {
                ApiException ae = (ApiException) e;
                System.out.println("  Code: " + ae.getCode());
                System.out.println("  Response: " + ae.getResponseBody());
            } else if (e != null) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            } else {
                System.err.println("Error (unknown)");
            }
        } catch (Exception critical) {
            System.err.println("Error (critical): " + critical.getMessage());
            critical.printStackTrace(); // this is a real error
        }
        return exitCode;
    }


    // Global commands
    // =================================================================================================================

    /**
     * Print version and exit.
     */
    private void version() {
        // This will work when not running in a jar file
        String version = "x.x.x";
        try (InputStream inputStream = this.getClass().getResourceAsStream("/application.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = properties.getProperty("app.version");
        } catch (IOException e) {
            // ignored
        }
        System.out.println("Golden Client version " + version);
    }

    /**
     * Prints contextualized help.
     * @param ctx Context object.
     * @param e Optional exception object.
     */
    private void help(Context ctx, Exception e) {
        // Header
        if (e != null) {
            System.out.println("Error: " + e.getMessage());
        } else {
            System.out.println("Golden Client - a command line interface for Golden API");
        }

        // Subcommand help
        if (ctx.getCommand() != null && ctx.getSubcommandInfo() != null) {
            Command cmd = ctx.getCommand();
            String c = cmd.info().getCommand();
            Command.SubcommandInfo sci = ctx.getSubcommandInfo();
            System.out.println("Usage: golden " + c + " " + sci.getSubcommand() + " [options] [global-options]");
            System.out.println();
            System.out.println(c  + " " + sci.getSubcommand() + " - " +  sci.getDescription());
            System.out.println();
            System.out.println("Options:");
            System.out.println(GoldenPrinter.getInstance().formatHelp(sci.getOptions()));
        }

        // Command help
        else if (ctx.getCommand() != null) {
            Command cmd = ctx.getCommand();
            String c = cmd.info().getCommand();
            System.out.println();
            System.out.println("Usage: golden " + c + " [subcommand] [options] [global-options]");
            System.out.println();
            System.out.println(c  + " - " + cmd.info().getDescription());
            System.out.println();
            System.out.println("Subcommands:");
            System.out.println();
            Map<String, String> helpMap = cmd.info().getSubcommandsInfo().stream().collect(HashMap::new,
                    (m, sci) -> m.put(sci.getSubcommand(), sci.getDescription()), HashMap::putAll);
            System.out.println(GoldenPrinter.getInstance().formatHelp(helpMap));
        }

        // Global help
        else {
            System.out.println();
            System.out.println("Usage: golden [command] [subcommand] [options] [global-options]");
            System.out.println();
            System.out.println("Commands:");
            Map<String, String> helpMap = GoldenCommands.getInstance().getCommands().stream()
                    .collect(LinkedHashMap::new, (m, c) -> m.put(c.info().getCommand(), c.info().getDescription()), LinkedHashMap::putAll);
            System.out.println();
            System.out.println(GoldenPrinter.getInstance().formatHelp(helpMap));
        }

        // Footer
        System.out.println();
        System.out.println("Global Options:");
        System.out.println(GoldenPrinter.getInstance().formatHelp(GoldenParser.getInstance().getGlobalOptions()));
        System.out.println();
        System.out.println("Use 'golden [command] --help' for more information about a command.");
        System.out.println("Use 'golden [command] [subcommand] --help' for more information about a subcommand.");
    }


}

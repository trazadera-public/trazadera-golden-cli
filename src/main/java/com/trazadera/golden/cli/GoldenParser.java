package com.trazadera.golden.cli;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parses the command line arguments: global and command specific.
 */
public class GoldenParser {

    private static final String OPTION_OUTPUT_FORMAT = "format";
    private static final String OPTION_OUTPUT_FILTER = "filter";

    private static final GoldenParser instance = new GoldenParser();

    private Options globalOptions = new Options();

    private GoldenParser() {
        // Build global options
        Option help = Option.builder(null).longOpt("help").desc("Show help and exit").required(false).build();
        Option version = Option.builder(null).longOpt("version").desc("Show version and exit").required(false).build();
        Option token = Option.builder(null).longOpt("token").desc("Golden API token to authenticate").required(false).hasArg().argName("token").build();
        Option url = Option.builder(null).longOpt("url").desc("Golden API URL").required(false).hasArg().argName("url").build();
        Option outputFormat = Option.builder(null).longOpt(OPTION_OUTPUT_FORMAT).desc("Output format: json (default), table, csv").required(false).hasArg().argName("format").build();
        Option outputFilter = Option.builder(null).longOpt(OPTION_OUTPUT_FILTER).desc("Output filter: a JSON path expression (https://github.com/json-path/JsonPath) to select what to print").required(false).hasArg().argName("expression").build();
        Option interactive = Option.builder(null).longOpt("interactive").desc("Interactive mode (ignores all other options and enters a prompt)").required(false).build();
        globalOptions.addOption(help);
        globalOptions.addOption(version);
        globalOptions.addOption(token);
        globalOptions.addOption(url);
        globalOptions.addOption(outputFormat);
        globalOptions.addOption(outputFilter);
        globalOptions.addOption(interactive);

        // Check basic command health
        // Make sure that command options do not overlap with global options
        for (Command c: GoldenCommands.getInstance().getCommands()) {
            if (c.info() == null)
                throw new IllegalStateException("Command information cannot be null: " + c.info());
            for (Command.SubcommandInfo sci : c.info().getSubcommandsInfo()) {
                for (Option o : sci.getOptions().getOptions()) {
                    if (globalOptions.hasOption(o.getOpt()) || globalOptions.hasOption(o.getLongOpt()))
                        throw new IllegalStateException("Command option '" + o.getOpt() + "' or '" + o.getLongOpt() + "' overlaps with global options");
                }
            }
        }
    }

    public static GoldenParser getInstance() {
        return instance;
    }

    public Options getGlobalOptions() {
        return globalOptions;
    }

    /**
     * Parses command line: global, command, subcommand and options.
     */
    public void parseCommandLine(Context ctx, String[] args) throws IllegalArgumentException, ParseException {
        // Null or empty arguments
        if (args==null || args.length==0)
            throw new IllegalArgumentException("no command provided");

        CommandLineParser parser = new DefaultParser();

        // Parse global command line
        // We have to extract the global options first to avoid unknown option errors (e.g. --help)
        // The Apache commons parser does not work when trying to ignore errors, that's why we extract the global options first
        try {
            CommandLine globalCommandLine = parser.parse(globalOptions, extractGlobalArgs(args), false);
            ctx.setGlobalCommandLine(globalCommandLine);
            if (globalCommandLine.hasOption(OPTION_OUTPUT_FORMAT))
                ctx.setFormat(globalCommandLine.getOptionValue("format").toUpperCase().trim());
            if (globalCommandLine.hasOption(OPTION_OUTPUT_FILTER))
                ctx.setOutputExpression(globalCommandLine.getOptionValue(OPTION_OUTPUT_FILTER));
        } catch (ParseException e) {
            // ignored
        }

        // Global actions flag
        boolean globalActions = ctx.getGlobalCommandLine() != null &&
                (ctx.getGlobalCommandLine().hasOption("help") || ctx.getGlobalCommandLine().hasOption("version"));

        // Retrieve command
        if (args.length > 0) {
            String cmd = args[0].toLowerCase().trim();
            if (!cmd.startsWith("-")) {
                Command c = GoldenCommands.getInstance().getCommand(cmd);
                if (c == null && !globalActions)
                    throw new IllegalArgumentException("unknown command '" + cmd + "'");
                ctx.setCommand(c);
                //System.out.println("--> Parsed command: " + c);
            }
        }
        if (!globalActions && ctx.getCommand()==null)
            throw new IllegalArgumentException("no command provided");

        // Retrieve subcommand
        if (args.length > 1) {
            String subcommand = args[1].toLowerCase().trim();
            if (!subcommand.startsWith("-") && ctx.getCommand() != null) {
                Command.SubcommandInfo sci = ctx.getCommand().info().getSubcommandInfo(subcommand);
                if (sci == null && !globalActions)
                    throw new IllegalArgumentException("unknown subcommand '" + subcommand + "'");
                ctx.setSubcommand(subcommand);
                //System.out.println("--> Parsed subcommand: " + subcommand);
            }
        }
        if (!globalActions && ctx.getSubcommand()==null)
            throw new IllegalArgumentException("no subcommand provided");

        // Parse subcommand arguments
        // Copy global options to subcommand options to avoid unknown option errors
        if (!globalActions) {
            String[] subcommandArgs = args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[0];
            Command.SubcommandInfo sci = ctx.getCommand().info().getSubcommandInfo(ctx.getSubcommand());
            Options temporalOptions = new Options();
            for (Option o: sci.getOptions().getOptions())
                temporalOptions.addOption(o);
            for (Option o: globalOptions.getOptions())
                temporalOptions.addOption(o);
            CommandLine commandLine = parser.parse(temporalOptions, subcommandArgs);
            ctx.setCommandLine(commandLine);
            //System.out.println("--> Parsed subcommand arguments: " + Arrays.toString(subcommandArgs));
        }
    }


    /**
     * Extracts global arguments from command line arguments.
     * @param args Command line arguments.
     * @return Global arguments.
     */
    private String[] extractGlobalArgs(String[] args) {
        List<String> globalArgs = new ArrayList<>();
        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith("-")) {
                String argName = args[i].replace("-", "").trim();
                if (globalOptions.hasOption(argName)) {
                    globalArgs.add(args[i]);
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        globalArgs.add(args[i + 1]);
                        i++;
                    }
                }
            }
        }
        // System.out.println("Global args: " + globalArgs);
        return globalArgs.toArray(new String[0]);
    }

}

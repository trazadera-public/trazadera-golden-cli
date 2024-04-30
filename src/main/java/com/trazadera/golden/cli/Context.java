package com.trazadera.golden.cli;

import com.trazadera.golden.restclient.invoker.ApiClient;
import org.apache.commons.cli.CommandLine;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Context object to pass information to commands.
 */
public class Context {

    // Defaults
    private static final GoldenFormat DEFAULT_OUTPUT_FORMAT = GoldenFormat.JSON;
    private static final String DEFAULT_CSV_SEPARATOR = ";";

    private ApiClient apiClient;
    private Command command;
    private String subcommand;
    private CommandLine commandLine;
    private CommandLine globalCommandLine;
    private GoldenFormat format = DEFAULT_OUTPUT_FORMAT;
    private String csvSeparator = DEFAULT_CSV_SEPARATOR;
    private boolean tableBorder = true;
    private String outputExpression = null;

    public GoldenFormat getFormat() {
        return format;
    }

    public Context setFormat(String format) throws IllegalArgumentException {
        try {
            setFormat(GoldenFormat.valueOf(format));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid format '" + globalCommandLine.getOptionValue("format")
                    + "' (valid formats: " + Arrays.stream(GoldenFormat.values()).map(f -> f.name().toLowerCase()).collect(Collectors.joining(", ")) + ")");
        }
        return this;
    }

    public Context setFormat(GoldenFormat format) {
        this.format = format;
        return this;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public Context setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        return this;
    }

    public Command getCommand() {
        return command;
    }

    public Context setCommand(Command command) {
        this.command = command;
        return this;
    }

    public String getSubcommand() {
        return subcommand;
    }

    public Context setSubcommand(String subcommand) {
        this.subcommand = subcommand;
        return this;
    }

    public Command.SubcommandInfo getSubcommandInfo() {
        return command.info().getSubcommandInfo(subcommand);
    }


    public CommandLine getCommandLine() {
        return commandLine;
    }

    public Context setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
        return this;
    }

    public CommandLine getGlobalCommandLine() {
        return globalCommandLine;
    }

    public Context setGlobalCommandLine(CommandLine globalCommandLine) {
        this.globalCommandLine = globalCommandLine;
        return this;
    }

    public String getCsvSeparator() {
        return csvSeparator;
    }

    public Context setCsvSeparator(String csvSeparator) {
        this.csvSeparator = csvSeparator;
        return this;
    }

    public boolean isTableBorder() {
        return tableBorder;
    }

    public Context setTableBorder(boolean tableBorder) {
        this.tableBorder = tableBorder;
        return this;
    }

    public String getOutputExpression() {
        return outputExpression;
    }

    public Context setOutputExpression(String outputExpression) {
        this.outputExpression = outputExpression;
        return this;
    }

    public enum GoldenFormat {
        TABLE,
        JSON,
        CSV
    }
}

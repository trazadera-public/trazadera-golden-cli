package com.trazadera.golden.cli;

import com.squareup.okhttp.Call;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract class that represents a command.
 */
public abstract class Command {

    final protected static Options EMPTY_OPTIONS = new Options();

    // Abstract methods
    // =================================================================================================================

    /**
     * Retrieves the command information.
     * @return Command information. Never null.
     */
    public abstract CommandInfo info();

    /**
     * Executes the subcommand using indicated context.
     * @param context Context object.
     * @exception Exception If an error occurs during command execution.
     */
    public abstract Call execute(Context context) throws Exception;


    // Command information
    // =================================================================================================================


    public class CommandInfo {
        String command;
        String description;
        Map<String, SubcommandInfo> subcommandsMap = new LinkedHashMap<>();
        List<String> subcommandsList = new ArrayList<>();
        List<SubcommandInfo> subcommandsInfo = new ArrayList<>();


        public CommandInfo(String command, String description) {
            if (command == null || command.isEmpty())
                throw new IllegalArgumentException("Command cannot be null or empty");
            if (description == null || description.isEmpty())
                throw new IllegalArgumentException("Description cannot be null or empty");
            this.command = command.trim().toLowerCase();
            this.description = description;
        }

        public String getCommand() {
            return command;
        }

        public String getDescription() {
            return description;
        }

        public void addSubcommandInfo(SubcommandInfo subcommand) {
            if (subcommand == null)
                throw new IllegalArgumentException("Subcommand cannot be null");
            this.subcommandsMap.put(subcommand.getSubcommand(), subcommand);
            this.subcommandsList.add(subcommand.getSubcommand());
            this.subcommandsInfo.add(subcommand);
        }

        public Map<String, SubcommandInfo> getSubcommandsMap() {
            return subcommandsMap;
        }

        public List<String> getSubcommands() {
            return this.subcommandsList;
        }

        public List<SubcommandInfo> getSubcommandsInfo() {
            return subcommandsInfo;
        }

        public SubcommandInfo getSubcommandInfo(String subcommand) {
            return this.subcommandsMap.get(subcommand);
        }
    }


    public class SubcommandInfo {
        String subcommand;
        String description;
        Options options;

        public SubcommandInfo(String subcommand, String description, Options options) {
            if (subcommand == null || subcommand.isEmpty())
                throw new IllegalArgumentException("Subcommand cannot be null or empty");
            if (description == null || description.isEmpty())
                throw new IllegalArgumentException("Description cannot be null or empty");
            if (options == null)
                throw new IllegalArgumentException("Options cannot be null");
            this.subcommand = subcommand.trim().toLowerCase();
            this.description = description;
            this.options = options;
        }

        public String getSubcommand() {
            return subcommand;
        }

        public String getDescription() {
            return description;
        }

        public Options getOptions() {
            return options;
        }
    }


}

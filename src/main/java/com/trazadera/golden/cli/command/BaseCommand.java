package com.trazadera.golden.cli.command;

import com.trazadera.golden.cli.Command;
import com.trazadera.golden.cli.Context;
import com.trazadera.golden.restclient.model.GoldenBucketFullResponseDto;
import org.apache.commons.cli.Option;

public abstract class BaseCommand extends Command {

    protected static final String OPTION_ENTITY = "entity";
    protected static final Option OPTION_OBJECT_ENTITY = Option.builder(null).longOpt(OPTION_ENTITY)
            .hasArg()
            .argName("id")
            .numberOfArgs(1)
            .required(true)
            .desc("Entity ID")
            .build();

    protected static final String OPTION_TABLE = "table";
    protected static final Option OPTION_OBJECT_TABLE = Option.builder(null).longOpt(OPTION_TABLE)
            .hasArg()
            .argName("id")
            .numberOfArgs(1)
            .required(true)
            .desc("Table ID")
            .build();

    protected static final String OPTION_RESOURCE = "resource";
    protected static final Option OPTION_OBJECT_RESOURCE = Option.builder(null).longOpt(OPTION_RESOURCE)
            .hasArg()
            .argName("id")
            .numberOfArgs(1)
            .required(true)
            .desc("Resource ID")
            .build();

    protected static final String OPTION_TASK = "task";
    protected static final Option OPTION_OBJECT_TASK = Option.builder(null).longOpt(OPTION_TASK)
            .hasArg()
            .argName("id")
            .numberOfArgs(1)
            .required(true)
            .desc("Task ID")
            .build();

    protected static final String OPTION_PAGE = "page";
    protected static final Option OPTION_OBJECT_PAGE = Option.builder(null).longOpt(OPTION_PAGE)
            .hasArg()
            .argName("number")
            .numberOfArgs(1)
            .required(false)
            .desc("Page number (integer). Default is 0.")
            .build();
    protected static final int OPTION_DEFAULT_PAGE = 0;

    protected static final String OPTION_PAGE_SIZE = "page";
    protected static final Option OPTION_OBJECT_PAGE_SIZE = Option.builder(null).longOpt(OPTION_PAGE_SIZE)
            .hasArg()
            .argName("number")
            .numberOfArgs(1)
            .required(false)
            .desc("Page size (positive integer). Default is 10.")
            .build();
    protected static final int OPTION_DEFAULT_PAGE_SIZE = 10;



    // Option methods
    // ================================================================================================================

    protected String getOption(Context context, String option) {
        if (context != null && context.getCommandLine() != null && context.getCommandLine().hasOption(option)) {
            return context.getCommandLine().getOptionValue(option);
        }
        return null;
    }

    protected int getIntegerOption(Context context, String option, int defaultValue) {
        if (context != null && context.getCommandLine() != null && context.getCommandLine().hasOption(option)) {
            try {
                return Integer.parseInt(context.getCommandLine().getOptionValue(option));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

}

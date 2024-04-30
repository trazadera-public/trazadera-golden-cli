package com.trazadera.golden.cli.command;

import com.squareup.okhttp.Call;
import com.trazadera.golden.cli.Context;
import com.trazadera.golden.restclient.api.TaskApi;

public class TaskCommand extends BaseCommand {

    private CommandInfo info;

    public TaskCommand() {
        info = new CommandInfo("task", "Manage tasks");
        info.addSubcommandInfo(new SubcommandInfo("list", "List tasks", EMPTY_OPTIONS));
    }

    @Override
    public CommandInfo info() {
        return info;
    }

    @Override
    public Call execute(Context context) throws Exception {
        TaskApi taskApi = new TaskApi(context.getApiClient());
        return switch (context.getSubcommand()) {
            case "list" -> {
                int page = getIntegerOption(context, OPTION_PAGE, OPTION_DEFAULT_PAGE);
                int pageSize = getIntegerOption(context, OPTION_PAGE_SIZE, OPTION_DEFAULT_PAGE_SIZE);
                String filterByStatus = null;
                String sortByDate = null;
                yield taskApi.getTaskInstancesCall(page, pageSize, filterByStatus, sortByDate, null, null);
            }
            default -> null;
        };
    }


}

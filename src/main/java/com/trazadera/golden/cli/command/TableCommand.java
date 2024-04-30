package com.trazadera.golden.cli.command;

import com.squareup.okhttp.Call;
import com.trazadera.golden.cli.Context;
import com.trazadera.golden.restclient.api.TableApi;
import org.apache.commons.cli.Options;

public class TableCommand extends BaseCommand {

    private CommandInfo info;

    public TableCommand() {
        info = new CommandInfo("table", "Manage tables");
        info.addSubcommandInfo(new SubcommandInfo("list", "List tables", EMPTY_OPTIONS));
        info.addSubcommandInfo(new SubcommandInfo("show", "Show table", new Options().addOption(OPTION_OBJECT_TABLE)));
    }

    @Override
    public CommandInfo info() {
        return info;
    }

    @Override
    public Call execute(Context context) throws Exception {
        TableApi tableApi = new TableApi(context.getApiClient());
        return switch (context.getSubcommand()) {
            case "list" -> tableApi.getAllTablesCall(null, null);
            case "show" -> tableApi.getTableCall(getOption(context, OPTION_TABLE), null, null);
            default -> null;
        };
    }
}

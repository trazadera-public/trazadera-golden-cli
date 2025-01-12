package com.trazadera.golden.cli.command;

import com.squareup.okhttp.Call;
import com.trazadera.golden.cli.Context;
import com.trazadera.golden.restclient.api.EntityApi;
import org.apache.commons.cli.Options;

public class EntityCommand extends BaseCommand {

    private CommandInfo info;

    public EntityCommand() {
        info = new CommandInfo("entity", "Manage entities");
        info.addSubcommandInfo(new SubcommandInfo("list", "List entities", EMPTY_OPTIONS));
        info.addSubcommandInfo(new SubcommandInfo("show", "Show entity", new Options().addOption(OPTION_OBJECT_ENTITY)));
    }

    @Override
    public CommandInfo info() {
        return info;
    }

    @Override
    public Call execute(Context context) throws Exception {
        EntityApi entityApi = new EntityApi(context.getApiClient());
        Call call = switch (context.getSubcommand()) {
            case "list" -> entityApi.getAllEntitiesCall(null, null);
            case "show" -> entityApi.getEntityCall(getOption(context, OPTION_ENTITY), null, null);
            default -> null;
        };
        return call;
    }
}

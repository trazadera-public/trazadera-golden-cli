package com.trazadera.golden.cli.command;

import com.squareup.okhttp.Call;
import com.trazadera.golden.cli.Context;
import com.trazadera.golden.restclient.api.ResourceApi;
import org.apache.commons.cli.Options;

public class ResourceCommand extends BaseCommand {

    private CommandInfo info;

    public ResourceCommand() {
        info = new CommandInfo("resource", "Manage resources");
        info.addSubcommandInfo(new SubcommandInfo("list", "List resources", EMPTY_OPTIONS));
        info.addSubcommandInfo(new SubcommandInfo("show", "Show resource", new Options().addOption(OPTION_OBJECT_RESOURCE)));
    }

    @Override
    public CommandInfo info() {
        return info;
    }

    @Override
    public Call execute(Context context) throws Exception {
        ResourceApi resourceApi = new ResourceApi(context.getApiClient());
        return switch (context.getSubcommand()) {
            case "list" -> resourceApi.getAllResourcesCall(null, null);
            case "show" -> resourceApi.getResourceCall(getOption(context, OPTION_RESOURCE), null, null);
            default -> null;
        };
    }
}

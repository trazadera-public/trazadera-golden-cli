package com.trazadera.golden.cli.command;

import com.squareup.okhttp.Call;
import com.trazadera.golden.cli.Context;
import com.trazadera.golden.restclient.api.SecurityApi;
import com.trazadera.golden.restclient.api.TaskApi;

public class UserCommand extends BaseCommand {

    private CommandInfo info;

    public UserCommand() {
        info = new CommandInfo("user", "Manage users");
        info.addSubcommandInfo(new SubcommandInfo("list", "List users", EMPTY_OPTIONS));
    }

    @Override
    public CommandInfo info() {
        return info;
    }

    @Override
    public Call execute(Context context) throws Exception {
        SecurityApi userApi = new SecurityApi(context.getApiClient());
        return switch (context.getSubcommand()) {
            case "list" -> userApi.findAllUsersCall(null, null);
            default -> null;
        };
    }


}

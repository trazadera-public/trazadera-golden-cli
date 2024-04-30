package com.trazadera.golden.cli.command;

import com.squareup.okhttp.Call;
import com.trazadera.golden.cli.Context;
import com.trazadera.golden.restclient.api.TaskApi;
import com.trazadera.golden.restclient.api.UserApi;

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
        UserApi userApi = new UserApi(context.getApiClient());
        return switch (context.getSubcommand()) {
            case "list" -> userApi.findAllUsersCall(null, null);
            default -> null;
        };
    }


}

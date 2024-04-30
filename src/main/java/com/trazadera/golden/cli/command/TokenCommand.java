package com.trazadera.golden.cli.command;

import com.squareup.okhttp.Call;
import com.trazadera.golden.cli.Context;
import com.trazadera.golden.restclient.api.TokenApi;
import com.trazadera.golden.restclient.api.UserApi;

public class TokenCommand extends BaseCommand {

    private CommandInfo info;

    public TokenCommand() {
        info = new CommandInfo("token", "Manage tokens");
        info.addSubcommandInfo(new SubcommandInfo("list", "List tokens", EMPTY_OPTIONS));
    }

    @Override
    public CommandInfo info() {
        return info;
    }

    @Override
    public Call execute(Context context) throws Exception {
        TokenApi tokenApi = new TokenApi(context.getApiClient());
        return switch (context.getSubcommand()) {
            case "list" -> tokenApi.findAllTokensCall(null, null);
            default -> null;
        };
    }


}

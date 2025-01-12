package com.trazadera.golden.cli.command;

import com.squareup.okhttp.Call;
import com.trazadera.golden.cli.Context;
import com.trazadera.golden.restclient.api.GoldenApi;
import com.trazadera.golden.restclient.model.GoldenBucketFullResponseDto;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.List;

public class GoldenCommand extends BaseCommand {

    private static final String OPTION_INDEX = "index";
    private static final String OPTION_CLASSIFICATION = "classification";
    private static final String OPTION_BUCKET = "bucket";
    private CommandInfo info;

    public GoldenCommand() {
        info = new CommandInfo("golden", "Manage golden records");

        // List
        Options listOptions = new Options();
        listOptions.addOption(OPTION_OBJECT_ENTITY);
        listOptions.addOption(Option.builder(null).longOpt(OPTION_INDEX)
                .hasArg()
                .argName("id")
                .numberOfArgs(1)
                .required(false)
                .desc("Index filter. Use filter ID or nothing to list all")
                .build());
        listOptions.addOption(Option.builder(null).longOpt(OPTION_CLASSIFICATION)
                .hasArg()
                .argName("outcome")
                .numberOfArgs(1)
                .required(false)
                .desc("Classification filter. Use classification outcome (" + GoldenBucketFullResponseDto.ClassificationEnum.values() + ") or nothing to list all")
                .build());
        listOptions.addOption(OPTION_OBJECT_PAGE);
        listOptions.addOption(OPTION_OBJECT_PAGE_SIZE);
        info.addSubcommandInfo(new SubcommandInfo("list", "List buckets", listOptions));

        // Show
        Options showOptions = new Options();
        showOptions.addOption(OPTION_OBJECT_ENTITY);
        showOptions.addOption(Option.builder(null).longOpt(OPTION_BUCKET)
                .hasArg()
                .argName("id")
                .numberOfArgs(1)
                .required(true)
                .desc("Bucket ID")
                .build());
        info.addSubcommandInfo(new SubcommandInfo("show", "Show bucket", showOptions));
    }

    @Override
    public CommandInfo info() {
        return info;
    }

    @Override
    public Call execute(Context context) throws Exception {
        GoldenApi goldenApi = new GoldenApi(context.getApiClient());
        return switch (context.getSubcommand()) {
            case "list" -> {
                String entity = getOption(context, OPTION_ENTITY);
                String index = getOption(context, OPTION_INDEX);
                String classification = getOption(context, OPTION_CLASSIFICATION);
                List<String> sorting = null;
                Integer page = getIntegerOption(context, OPTION_PAGE, 0);
                Integer pageSize = getIntegerOption(context, OPTION_PAGE_SIZE, 10);
                yield goldenApi.getBucketsCall(entity, page, pageSize, index, classification, sorting, null, null);
            }
            case "show" -> {
                String entity = getOption(context, OPTION_ENTITY);
                String bucket = getOption(context, OPTION_BUCKET);
                yield goldenApi.getBucketCall(entity, bucket, null, null);
            }
            default -> null;
        };
    }


}

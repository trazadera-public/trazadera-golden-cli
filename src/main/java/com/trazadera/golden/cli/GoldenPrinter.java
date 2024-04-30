package com.trazadera.golden.cli;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.ColumnData;
import com.github.freva.asciitable.HorizontalAlign;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.cli.Options;

import java.util.*;
import java.util.stream.Collectors;

public class GoldenPrinter {

    private static final int CMD_WIDTH = 50;
    private static final String CMD_SPACER = "  ";

    private static final GoldenPrinter instance = new GoldenPrinter();

    private GoldenPrinter() {
    }

    public static GoldenPrinter getInstance() {
        return instance;
    }

    /**
     * Formats a help output for indicated options.
     *
     * @param options Command line options.
     * @return A string formatted according to the usual command line help format.
     */
    public String formatHelp(Options options) {
        return AsciiTable.getTable(AsciiTable.NO_BORDERS, options.getOptions(), List.of(
                new Column().dataAlign(HorizontalAlign.LEFT).minWidth(CMD_WIDTH).with(o -> {
                    StringBuilder sb = new StringBuilder(CMD_SPACER);
                    boolean sep = false;
                    if (o.getOpt() != null && o.getOpt().trim().length() > 0) {
                        sb.append("-").append(o.getOpt());
                        sep = true;
                    }
                    if (o.getLongOpt() != null && o.getLongOpt().trim().length() > 0) {
                        if (sep) sb.append(", ");
                        sb.append("--").append(o.getLongOpt());
                    }
                    if (o.hasArg()) {
                        sb.append(" <").append(o.getArgName()).append(">");
                    }
                    return sb.toString();
                }),
                new Column().dataAlign(HorizontalAlign.LEFT).with(o -> {
                    StringBuilder sb = new StringBuilder(o.getDescription() + ".");
                    sb.append(o.isRequired() ?" Required." :" Optional.");
                    return sb.toString();
                }))
        );
    }

    public String formatHelp(Map map) {
        return AsciiTable.getTable(AsciiTable.NO_BORDERS, map.keySet(), List.of(
                new Column().dataAlign(HorizontalAlign.LEFT).minWidth(CMD_WIDTH).with(k -> CMD_SPACER + (k != null ?k.toString() :"")),
                new Column().dataAlign(HorizontalAlign.LEFT).with(k -> {
                    Object value = map.get(k);
                    return (value != null) ?value.toString() :"";
                }))
        );
    }

    public String formatOutput(Context context, String json) {
            //System.err.println("--> Result: " + response);
        if (json==null || json.isEmpty() || json.trim().isEmpty())
            return "";
        PrintColumns pc = PrintColumns.build(context, json);
        String output = switch (context.getFormat()) {
            case CSV -> csv(context, pc);
            case TABLE ->  table(context, pc);
            default -> json(context, pc);
        };
        if (output != null && !output.isEmpty()) {
            // trim all newlines and spaces at end of string
            output = output.replaceAll("[\n\\s]+$", "");
        }
        return output;
    }


    // Private methods
    // ================================================================================================================

    private String json(Context ctx, PrintColumns pc) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        return builder.create().toJson(pc.data);
    }

    private String csv(Context ctx, PrintColumns pc) {
        StringBuilder output = new StringBuilder();
        final String separator = ctx.getCsvSeparator();

        boolean header = true;
        for (int row=0; row<pc.data.size(); row++) {
            if (header) {
                output.append(pc.columns.stream().collect(Collectors.joining(separator)));
                output.append("\n");
                header = false;
            }
            for (int col = 0; col < pc.columns.size(); col++) {
                if (col > 0)
                    output.append(separator);
                Object value = pc.data.get(row).get(pc.columns.get(col));
                output.append(value==null ?"" :value.toString());
            }
            output.append("\n");
        }
        return output.toString();
    }

    private String table(Context ctx, PrintColumns pc) {
        Collection<Map> objects = pc.data;
        List<ColumnData<Map>> columns = new ArrayList<>();
        for (String col: pc.columns) {
            final String columnId = col;
            ColumnData<Map> cd = new Column().
                    dataAlign(HorizontalAlign.LEFT).
                    header(columnId).
                    with(m -> {
                        Object value = m.get(columnId);
                        return value==null ?"" :value.toString();
                    });
            columns.add(cd);
        }
        Character[] border = ctx.isTableBorder() ?AsciiTable.FANCY_ASCII :AsciiTable.NO_BORDERS;
        return AsciiTable.getTable(border, objects, columns);
    }

    private static class PrintColumns {
        List<Map> data = null;
        List<String> columns = null;

        PrintColumns() {
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PrintColumns{");
            sb.append("data=").append(data);
            sb.append(", columns=").append(columns);
            sb.append('}');
            return sb.toString();
        }

        /**
         * Prepares columns and data to be printed. Eventually applies a Json Path expression.
         *
         * @param ctx  Context object.
         * @param json JSON.
         * @return PrintColumns object with columns and data.
         */
        public static PrintColumns build(Context ctx, String json) {

            PrintColumns pc = new PrintColumns();

            // Initial parse
            pc.data = List.of(new GsonBuilder().create().fromJson(json, Map.class));

            // Json Path expression
            final String expression = ctx.getOutputExpression();
            if (expression != null && !expression.isEmpty()) {
                try {
                    Object result = JsonPath.read(json, expression);
                    // Create a columnName that removes from expression everything that is not a letter or number
                    String columnName = expression.replaceAll("[^a-zA-Z0-9]", "");
                    if (result==null) {
                        pc.data = List.of();
                    } else if (result instanceof Map) {
                        pc.data = List.of((Map) result);
                    } else  if (result instanceof List) {
                        List list = (List) result;
                        if (list==null || list.isEmpty()) {
                            pc.data = List.of();
                        } else if (list.get(0) instanceof Map) {
                            pc.data = list;
                        } else {
                            pc.data = list.stream().map(o -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put(columnName, o);
                                return map;
                            }).toList();
                        }
                    } else {
                        pc.data = List.of(Map.of(columnName, result));
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("invalid JSON path expression: " + e.getMessage());
                }
            }

            //        if (map.size()==1) {
            //            String key = map.keySet().iterator().next();
            //            Object value = map.get(key);
            //            if (value==null) {
            //                return PrintColumns.EMPTY;
            //            } else if (value instanceof List) {
            //                pc.data = (List<Map>)value;
            //            } else if (value instanceof Map) {
            //                pc.data = List.of((Map) value);
            //            }
            //        }

            // Calculate columns
            // By default include all in natural order
            LinkedHashSet<String> columns = new LinkedHashSet<>();
            for (Map item : pc.data) {
                for (Object col : item.keySet()) {
                    if (col != null)
                        columns.add(col.toString());
                }
            }
            pc.columns = columns.stream().toList();
            return pc;
        }
    }


}

package com.trazadera.golden.cli;

import com.trazadera.golden.cli.command.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Golden commands manager.
 */
public class GoldenCommands {

    private Map<String, Command> globalCommands = new LinkedHashMap<>();

    private static final GoldenCommands instance = new GoldenCommands();

    private GoldenCommands() {
        List<Command> commands = List.of(new GoldenCommand(),
                new EntityCommand(),
                new TableCommand(),
                new ResourceCommand(),
                new UserCommand(),
                new TokenCommand(),
                new TaskCommand());
        for (Command c : commands) {
            //System.out.println("--> Adding command: " + c.info().getCommand());
            globalCommands.put(c.info().getCommand(), c);
        }
    }

    public static GoldenCommands getInstance() {
        return instance;
    }

    public List<Command> getCommands() {
        return List.copyOf(globalCommands.values());
    }

    public Command getCommand(String command) {
        return globalCommands.get(command);
    }

}

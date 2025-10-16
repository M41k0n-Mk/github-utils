package me.m41k0n.menu;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.m41k0n.command.Command;

import java.util.HashMap;
import java.util.Map;

public class MenuController {
    private final Map<Integer, Command<?>> commands = new HashMap<>();

    public void registerCommand(int option, Command<?> command) {
        commands.put(option, command);
    }

    public void executeCommand(int option) throws JsonProcessingException {
        Command<?> command = commands.get(option);
        if (command != null) {
            command.execute();
        } else {
            System.out.println("Opção inválida. Digite apenas os números listados no menu.");
        }
    }
}
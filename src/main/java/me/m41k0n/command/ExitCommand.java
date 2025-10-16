package me.m41k0n.command;

public class ExitCommand implements Command<Void> {
    @Override
    public Void execute() {
        System.out.println("Saindo da aplicação...");
        return null;
    }
}
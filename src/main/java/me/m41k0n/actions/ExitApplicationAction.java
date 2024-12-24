package me.m41k0n.actions;

public class ExitApplicationAction implements MenuAction<Void> {
    @Override
    public Void execute() {
        System.out.println("Saindo da aplicação...");
        return null;
    }
}
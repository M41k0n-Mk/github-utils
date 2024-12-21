package me.m41k0n.actions;

public class ExitApplicationAction implements MenuAction {
    @Override
    public void execute() {
        System.out.println("Saindo da aplicação...");
    }
}
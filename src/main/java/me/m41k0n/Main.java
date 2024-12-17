package me.m41k0n;

import me.m41k0n.actions.Welcome;

public class Main {
    public static void main(String[] args) {
        MenuContext context = new MenuContext();

        context.setAction(1, new Welcome());
        context.executeAction(1);
    }
}
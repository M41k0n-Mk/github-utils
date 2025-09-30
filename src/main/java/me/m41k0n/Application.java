package me.m41k0n;

import me.m41k0n.actions.ExitApplicationAction;
import me.m41k0n.actions.NonFollowersCommand;
import me.m41k0n.actions.UnfollowCommand;
import me.m41k0n.actions.Welcome;
import me.m41k0n.service.APIConsume;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.http.HttpClient;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // Check if menu mode is requested
        if (args.length > 0 && args[0].equals("--menu")) {
            // Menu mode - don't start Spring Boot
            Main.runMenuMode();
        } else {
            // API mode - start Spring Boot
            SpringApplication.run(Application.class, args);
        }
    }
}

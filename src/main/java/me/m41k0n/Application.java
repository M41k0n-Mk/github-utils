package me.m41k0n;

import me.m41k0n.command.MenuFactory;
import me.m41k0n.menu.MenuDisplay;
import me.m41k0n.service.APIConsume;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.net.http.HttpClient;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Parse CLI flags for menu and dry-run
        boolean menuMode = false;
        boolean dryRun = false;
        for (String arg : args) {
            if ("--menu".equals(arg)) menuMode = true;
            if ("--dry-run".equals(arg)) dryRun = true;
        }

        if (dryRun) {
            // expose to Spring and manual instantiation paths via system property
            System.setProperty("app.dryRun", "true");
            System.out.println("\u2139\ufe0f Running in DRY-RUN mode: no writes will be performed");
        }

        if (menuMode) {
            runMenuMode(args);
        } else {
            runApiMode(args);
        }
    }

    private static void runMenuMode(String[] args) {
        System.out.println("🚀 Iniciando GitHub Utils em modo Menu...\n");

        showWelcome();

        // Bootstrap Spring without web server and use beans for services
        ApplicationContext context = new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.NONE)
                .run(args);

        MenuDisplay menu = MenuFactory.createMenu(context);
        menu.show();

        System.out.println("👋 Aplicação encerrada. Até logo!");
    }

    private static void runApiMode(String[] args) {
        System.out.println("🌐 Iniciando GitHub Utils em modo API...");
        SpringApplication.run(Application.class, args);
    }

    private static void showWelcome() {
        HttpClient client = HttpClient.newHttpClient();
        String token = System.getenv("GITHUB_TOKEN");
        if (token == null) {
            token = "";
        }
        APIConsume apiConsume = new APIConsume(client, token);

        try {
            String octocat = apiConsume.getData("https://api.github.com/octocat");
            System.out.println(octocat);
            System.out.println("\n" + "=".repeat(60) + "\n");
        } catch (Exception e) {
            System.out.println("⚠️ Não foi possível conectar à API do GitHub para exibir o Octocat.");
            System.out.println("Verifique sua conexão e token de acesso.\n");
        }
    }
}
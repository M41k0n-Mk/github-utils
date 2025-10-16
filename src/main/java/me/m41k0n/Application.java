package me.m41k0n;

import me.m41k0n.factory.MenuFactory;
import me.m41k0n.menu.MenuDisplay;
import me.m41k0n.service.APIConsume;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.http.HttpClient;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        if (args.length > 0 && "--menu".equals(args[0])) {
            runMenuMode();
        } else {
            runApiMode(args);
        }
    }

    private static void runMenuMode() {
        System.out.println("🚀 Iniciando GitHub Utils em modo Menu...\n");

        showWelcome();
        MenuDisplay menu = MenuFactory.createMenu();
        menu.show();

        System.out.println("👋 Aplicação encerrada. Até logo!");
    }

    private static void runApiMode(String[] args) {
        System.out.println("🌐 Iniciando GitHub Utils em modo API...");
        SpringApplication.run(Application.class, args);
    }

    private static void showWelcome() {
        HttpClient client = HttpClient.newHttpClient();
        APIConsume apiConsume = new APIConsume(client);

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
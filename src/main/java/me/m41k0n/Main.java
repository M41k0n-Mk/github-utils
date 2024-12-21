package me.m41k0n;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.m41k0n.actions.ExitApplicationAction;
import me.m41k0n.actions.NonFollowers;
import me.m41k0n.actions.Welcome;
import me.m41k0n.service.APIConsume;

import java.net.http.HttpClient;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final HttpClient client = HttpClient.newHttpClient();
        final APIConsume apiConsume = new APIConsume(client);

        new Welcome(apiConsume);

        MenuContext context = new MenuContext();
        context.setAction(1, new NonFollowers(apiConsume));
        context.setAction(0, new ExitApplicationAction());
        showMenu(context);
    }

    public static void showMenu(MenuContext context) {
        var choice = -1;
        Scanner read = new Scanner(System.in);

        while (choice != 0) {
            try {
                var menu = """
                        1 - Obter uma lista de quem você segue mas não segue você
                        
                        0 - Sair
                        """;

                System.out.println(menu);
                choice = read.nextInt();
                read.nextLine();

                context.executeAction(choice);
            } catch (IllegalArgumentException e) {
                System.out.println(e + "\n");
            } catch (InputMismatchException e) {
                System.out.println(e + " Digite somente os números listados para cada opção no menu abaixo:\n");
                read.nextLine();
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Ocorreu um erro na desserialização", e);
            }
        }
    }
}
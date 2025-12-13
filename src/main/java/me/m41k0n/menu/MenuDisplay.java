package me.m41k0n.menu;

import me.m41k0n.command.MenuController;

import java.util.InputMismatchException;
import java.util.Scanner;

public class MenuDisplay {

    private static final String MENU_TEXT = """
            ╔══════════════════════════════════════════════════════════════╗
            ║                    GitHub Utils - Menu                       ║
            ╠══════════════════════════════════════════════════════════════╣
            ║ 1 - Listar usuários que você segue mas não te seguem         ║
            ║ 2 - Dar unfollow em massa (não-seguidores)                   ║
            ║ 3 - Preview paginado e export (CSV/JSON)                     ║
            ║ 4 - Toggle dry-run (mostrar apenas o que seria feito)        ║
            ║ 0 - Sair                                                     ║
            ╚══════════════════════════════════════════════════════════════╝
            Digite sua opção:\s""";

    private final MenuController menuController;
    private final Scanner scanner = new Scanner(System.in);

    public MenuDisplay(MenuController menuController) {
        this.menuController = menuController;
    }

    public void show() {
        int choice = -1;

        while (choice != 0) {
            try {
                System.out.print(MENU_TEXT);
                choice = scanner.nextInt();
                scanner.nextLine();

                menuController.executeCommand(choice);

                if (choice != 0) {
                    System.out.println("\nPressione Enter para continuar...");
                    scanner.nextLine();
                    System.out.println("\n".repeat(2));
                }

            } catch (InputMismatchException e) {
                System.out.println("❌ Entrada inválida. Digite apenas números.\n");
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("❌ Erro: " + e.getMessage() + "\n");
            }
        }
    }
}

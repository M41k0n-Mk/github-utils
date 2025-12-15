package me.m41k0n.command;

import me.m41k0n.model.User;
import me.m41k0n.service.GitHubService;
import me.m41k0n.service.ListService;

import java.time.Instant;
import java.util.*;

/**
 * Comando interativo no console para:
 * - Paginar a lista de usuários que você segue (following)
 * - Marcar/desmarcar usuários por índice ou todos da página
 * - Salvar a seleção como lista nomeada
 * - Aplicar ação follow/unfollow na seleção, respeitando histórico (skipProcessed)
 */
public class SelectFollowingCommand implements Command<Void> {

    private final GitHubService gitHubService;
    private final ListService listService;

    public SelectFollowingCommand(GitHubService gitHubService, ListService listService) {
        this.gitHubService = gitHubService;
        this.listService = listService;
    }

    @Override
    public Void execute() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Page size (default 25): ");
        String s = sc.nextLine().trim();
        int pageSize = s.isEmpty() ? 25 : parsePositiveIntOrDefault(s, 25);

        int page = 1;
        Set<String> selected = new LinkedHashSet<>();
        String lastSavedListId = null;

        while (true) {
            List<User> users;
            try {
                users = gitHubService.getFollowing(page, pageSize);
            } catch (Exception ex) {
                System.out.println("❌ Erro ao buscar página: " + ex.getMessage());
                users = Collections.emptyList();
            }

            boolean dryRun = safeIsDryRun();
            System.out.println();
            System.out.println("════════════════════════════════════════════════════════");
            System.out.printf(" Following — página %d (tam=%d)  |  selecionados: %d  |  dry-run: %s%n",
                    page, pageSize, selected.size(), dryRun ? "ON" : "OFF");
            System.out.println("────────────────────────────────────────────────────────");

            if (users.isEmpty()) {
                System.out.println("(sem resultados nesta página)");
            } else {
                for (int i = 0; i < users.size(); i++) {
                    User u = users.get(i);
                    String mark = selected.contains(u.login()) ? "[x]" : "[ ]";
                    System.out.printf("%2d. %s %-20s %s%n", i + 1, mark, u.login(), nullToEmpty(u.html_url()));
                }
            }

            System.out.println();
            System.out.println("Comandos:");
            System.out.println("  n = próxima página   |  p = página anterior  |  g <n> = ir para página n");
            System.out.println("  m <idx>[,<idx>...] = marcar/desmarcar pelos índices da página (ex: m 1,3,5)");
            System.out.println("  ma = marcar todos da página  |  mu = desmarcar todos da página");
            System.out.println("  s = salvar seleção como lista nomeada");
            System.out.println("  a = aplicar ação (follow/unfollow) na seleção (cria lista efêmera se não houver)");
            System.out.println("  d/q = concluir e sair");
            System.out.print("> ");

            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1] : "";

            switch (cmd) {
                case "n" -> page++;
                case "p" -> page = Math.max(1, page - 1);
                case "g" -> {
                    int target = parsePositiveIntOrDefault(arg, page);
                    page = Math.max(1, target);
                }
                case "m" -> toggleMarksByIndex(arg, users, selected);
                case "ma" -> markAll(users, selected);
                case "mu" -> unmarkAll(users, selected);
                case "s" -> {
                    if (selected.isEmpty()) {
                        System.out.println("(nada selecionado para salvar)");
                        break;
                    }
                    System.out.print("Nome da lista: ");
                    String name = sc.nextLine().trim();
                    if (name.isEmpty()) name = "cli-" + Instant.now();
                    try {
                        var entity = listService.create(name, new ArrayList<>(selected));
                        lastSavedListId = entity.getId();
                        System.out.println("✅ Lista salva: " + entity.getName() + " (id=" + entity.getId() + ")");
                    } catch (Exception ex) {
                        System.out.println("❌ Erro ao salvar lista: " + ex.getMessage());
                    }
                }
                case "a" -> {
                    if (selected.isEmpty()) {
                        System.out.println("(nada selecionado para aplicar)");
                        break;
                    }
                    System.out.print("Ação (follow/unfollow) [unfollow]: ");
                    String action = sc.nextLine().trim().toLowerCase();
                    if (!action.equals("follow") && !action.equals("unfollow")) action = "unfollow";
                    System.out.print("Ignorar já processados pelo histórico? (Y/n): ");
                    String skip = sc.nextLine().trim().toLowerCase();
                    boolean skipProcessed = !"n".equals(skip);

                    String listIdToApply = lastSavedListId;
                    try {
                        if (listIdToApply == null) {
                            // cria lista efêmera apenas para aplicação
                            String tempName = "cli-apply-" + Instant.now();
                            var entity = listService.create(tempName, new ArrayList<>(selected));
                            listIdToApply = entity.getId();
                        }
                        Map<String, Object> result = listService.apply(listIdToApply, action, skipProcessed);
                        System.out.println("Resultado: applied=" + result.get("applied") + ", skipped=" + result.get("skipped") + ", dryRun=" + result.get("dryRun"));
                    } catch (Exception ex) {
                        System.out.println("❌ Erro ao aplicar ação: " + ex.getMessage());
                    }
                }
                case "d", "q" -> {
                    System.out.println("Saindo do modo de seleção.");
                    return null;
                }
                default -> System.out.println("Comando não reconhecido.");
            }
        }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private static int parsePositiveIntOrDefault(String s, int def) {
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static void toggleMarksByIndex(String arg, List<User> users, Set<String> selected) {
        if (users == null || users.isEmpty()) return;
        if (arg == null || arg.isBlank()) return;
        String[] tokens = arg.split(",");
        for (String t : tokens) {
            int idx = parsePositiveIntOrDefault(t, -1);
            if (idx >= 1 && idx <= users.size()) {
                String login = users.get(idx - 1).login();
                if (selected.contains(login)) selected.remove(login); else selected.add(login);
            }
        }
    }

    private static void markAll(List<User> users, Set<String> selected) {
        for (User u : users) selected.add(u.login());
    }

    private static void unmarkAll(List<User> users, Set<String> selected) {
        for (User u : users) selected.remove(u.login());
    }

    private boolean safeIsDryRun() {
        try {
            return gitHubService.isDryRunEnabled();
        } catch (Exception ex) {
            return false;
        }
    }
}

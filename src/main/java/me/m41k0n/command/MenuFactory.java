package me.m41k0n.command;

import me.m41k0n.menu.MenuDisplay;
import me.m41k0n.service.DryRunService;
import me.m41k0n.service.GitHubService;
import org.springframework.context.ApplicationContext;

public class MenuFactory {

    public static MenuDisplay createMenu(ApplicationContext context) {
        DryRunService dryRunService = context.getBean(DryRunService.class);
        GitHubService gitHubService = context.getBean(GitHubService.class);

        GetNonFollowersCommand getNonFollowersCommand = new GetNonFollowersCommand(gitHubService);
        MassUnfollowCommand massUnfollowCommand = new MassUnfollowCommand(gitHubService);
        PreviewCommand previewCommand = new PreviewCommand(gitHubService);
        ToggleDryRunCommand toggleDryRunCommand = new ToggleDryRunCommand(dryRunService);
        ExitCommand exitCommand = new ExitCommand();

        MenuController menuController = new MenuController();
        menuController.registerCommand(1, getNonFollowersCommand);
        menuController.registerCommand(3, previewCommand);
        menuController.registerCommand(4, toggleDryRunCommand);
        menuController.registerCommand(2, massUnfollowCommand);
        menuController.registerCommand(0, exitCommand);

        return new MenuDisplay(menuController);
    }
}
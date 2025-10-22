package me.m41k0n.factory;

import me.m41k0n.command.ExitCommand;
import me.m41k0n.command.GetNonFollowersCommand;
import me.m41k0n.command.MassUnfollowCommand;
import me.m41k0n.menu.MenuController;
import me.m41k0n.menu.MenuDisplay;
import me.m41k0n.service.APIConsume;
import me.m41k0n.service.DryRunService;
import me.m41k0n.service.GitHubService;

import java.net.http.HttpClient;

public class MenuFactory {

    public static MenuDisplay createMenu() {
        HttpClient httpClient = HttpClient.newHttpClient();
        APIConsume apiConsume = new APIConsume(httpClient);
        DryRunService dryRunService = new DryRunService();
        GitHubService gitHubService = new GitHubService(apiConsume, dryRunService);

        GetNonFollowersCommand getNonFollowersCommand = new GetNonFollowersCommand(gitHubService);
        MassUnfollowCommand massUnfollowCommand = new MassUnfollowCommand(gitHubService);
    me.m41k0n.command.PreviewCommand previewCommand = new me.m41k0n.command.PreviewCommand(gitHubService);
    me.m41k0n.command.ToggleDryRunCommand toggleDryRunCommand = new me.m41k0n.command.ToggleDryRunCommand(dryRunService);
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
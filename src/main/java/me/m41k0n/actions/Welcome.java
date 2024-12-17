package me.m41k0n.actions;

import me.m41k0n.service.APIConsume;

import java.net.http.HttpClient;

public class Welcome implements MenuAction {
    private final HttpClient client = HttpClient.newHttpClient();
    private final APIConsume apiConsume = new APIConsume(client);

    @Override
    public void execute() {
        String octocat = apiConsume.getData("https://api.github.com/octocat");
        System.out.println(octocat);
    }
}
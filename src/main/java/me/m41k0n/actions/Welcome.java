package me.m41k0n.actions;

import me.m41k0n.service.APIConsume;

public class Welcome {
    public Welcome(APIConsume apiConsume) {
        String octocat = apiConsume.getData("https://api.github.com/octocat");
        System.out.println(octocat);
    }
}
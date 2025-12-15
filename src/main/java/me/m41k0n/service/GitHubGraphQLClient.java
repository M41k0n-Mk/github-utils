package me.m41k0n.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Cliente mínimo para chamadas ao endpoint GraphQL do GitHub.
 * Mantém a responsabilidade de rede isolada do serviço de filtros.
 */
@Component
public class GitHubGraphQLClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubGraphQLClient.class);
    private static final String TOKEN = System.getenv("GITHUB_TOKEN");
    private static final String GRAPHQL_URL = "https://api.github.com/graphql";

    private final HttpClient httpClient;

    public GitHubGraphQLClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String execute(String graphqlQueryJson) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GRAPHQL_URL))
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .POST(HttpRequest.BodyPublishers.ofString(graphqlQueryJson))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                log.warn("[GraphQL] status={} body={}", resp.statusCode(), truncate(resp.body()));
                throw new RuntimeException("GitHub GraphQL returned status " + resp.statusCode());
            }
            return resp.body();
        } catch (Exception e) {
            throw new RuntimeException("GraphQL request failed: " + e.getMessage(), e);
        }
    }

    private String truncate(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "…" : s;
    }
}

package me.m41k0n.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class APIConsume {

    private final HttpClient client;
    private static final String TOKEN = System.getenv("GITHUB_TOKEN");

    public APIConsume(HttpClient client) {
        this.client = client;
    }

    private HttpRequest.Builder createRequestBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + TOKEN)
                .header("X-GitHub-Api-Version", "2022-11-28");
    }

    public String getData(String url) {
        HttpRequest request = createRequestBuilder(url).build();
        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(String.format(
                "A requisição HTTP falhou para URL: %s. Erro: %s", url, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format(
                "A thread foi interrompida durante a request HTTP para URL: %s", url), e);
        }

        return response.body();
    }

    public String deleteData(String url) {
        HttpRequest request = createRequestBuilder(url).DELETE().build();

        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(String.format(
                "A requisição HTTP DELETE falhou para URL: %s. Erro: %s", url, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format(
                "A thread foi interrompida durante a request HTTP DELETE para URL: %s", url), e);
        }

        return response.body();
    }

    public int deleteStatus(String url) {
        HttpRequest request = createRequestBuilder(url).DELETE().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (IOException e) {
            throw new RuntimeException(String.format(
                "A requisição HTTP DELETE falhou para URL: %s. Erro: %s", url, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format(
                "A thread foi interrompida durante a request HTTP DELETE para URL: %s", url), e);
        }
    }

    public int putEmpty(String url) {
        HttpRequest request = createRequestBuilder(url).PUT(HttpRequest.BodyPublishers.noBody()).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (IOException e) {
            throw new RuntimeException(String.format(
                "A requisição HTTP PUT falhou para URL: %s. Erro: %s", url, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format(
                "A thread foi interrompida durante a request HTTP PUT para URL: %s", url), e);
        }
    }
}
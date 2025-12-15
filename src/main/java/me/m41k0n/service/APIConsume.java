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
    private static final String HTTP_ERROR_MESSAGE = "A requisição HTTP %s falhou para URL: %s. Erro: %s";
    private static final String INTERRUPTED_ERROR_MESSAGE = "A thread foi interrompida durante a request HTTP %s para URL: %s";

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
            throw new RuntimeException(String.format(HTTP_ERROR_MESSAGE, "GET", url, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format(INTERRUPTED_ERROR_MESSAGE, "GET", url), e);
        }

        return response.body();
    }

    public String deleteData(String url) {
        HttpRequest request = createRequestBuilder(url).DELETE().build();

        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(String.format(HTTP_ERROR_MESSAGE, "DELETE", url, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format(INTERRUPTED_ERROR_MESSAGE, "DELETE", url), e);
        }

        return response.body();
    }

    public int deleteStatus(String url) {
        HttpRequest request = createRequestBuilder(url).DELETE().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (IOException e) {
            throw new RuntimeException(String.format(HTTP_ERROR_MESSAGE, "DELETE", url, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format(INTERRUPTED_ERROR_MESSAGE, "DELETE", url), e);
        }
    }

    public int putEmpty(String url) {
        HttpRequest request = createRequestBuilder(url).PUT(HttpRequest.BodyPublishers.noBody()).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (IOException e) {
            throw new RuntimeException(String.format(HTTP_ERROR_MESSAGE, "PUT", url, e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format(INTERRUPTED_ERROR_MESSAGE, "PUT", url), e);
        }
    }
}
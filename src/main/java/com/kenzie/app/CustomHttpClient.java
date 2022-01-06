package com.kenzie.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class CustomHttpClient {
    public static final String END_POINT_BASE_URL = "https://jservice.kenzie.academy";
    public static final String END_POINT_GET_CATEGORIES = "/api/categories";

    public String sendGET(String UrlString) {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(UrlString);
        HttpRequest request = HttpRequest.newBuilder().uri(uri)
                .header("Accept", "application/json").GET().build();
        try {
            HttpResponse<String> httpResponse = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            int statusCode = httpResponse.statusCode();
            if (statusCode == 200) {
                return httpResponse.body();
            } else {
                System.out.format("GET request failed: %d status code received with body: %s",
                        statusCode, httpResponse.body());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public CategoriesListDTO getCategories() {
        ObjectMapper objectMapper = new ObjectMapper();

        String httpResponse = sendGET(END_POINT_BASE_URL + END_POINT_GET_CATEGORIES);
        CategoriesListDTO categoriesListDTO = null;
        try {
            categoriesListDTO = objectMapper.readValue(httpResponse, CategoriesListDTO.class);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }

        return categoriesListDTO;
    }
}

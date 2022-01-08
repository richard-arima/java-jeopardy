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
    public static final String END_POINT_GET_CLUES = "/api/clues";

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

    public Object getListDTO(String URL, Class<?> cls) {
        ObjectMapper objectMapper = new ObjectMapper();

        String httpResponse = sendGET(URL);
        Object listDTO = null;
        try {
            listDTO = objectMapper.readValue(httpResponse, cls);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }

        return listDTO;
    }

    public CategoriesListDTO getCategories() {
        return (CategoriesListDTO)getListDTO(END_POINT_BASE_URL + END_POINT_GET_CATEGORIES, CategoriesListDTO.class);
    }

    public CluesListDTO getCluesWithParameters(String... args) {
        if((args.length & 1) == 1) {
            // maybe throw an exception for invalid amount of args
            return null;
        }
        StringBuilder sb = new StringBuilder(END_POINT_BASE_URL + END_POINT_GET_CLUES + "?");
        for (int i = 0; i < args.length; i += 2) {
            sb.append(args[i]);
            sb.append("=");
            sb.append(args[i + 1]);
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return (CluesListDTO)getListDTO(sb.toString(), CluesListDTO.class);
    }
}

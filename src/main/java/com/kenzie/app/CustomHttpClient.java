package com.kenzie.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (httpResponse == null) {
            return null;  // Catch this early on if that's the case
        }
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

    // Pulls home page of base url of end point and finds how many total clues are in the database.
    public int getTotalNumClues() {
        String homePageContent = sendGET(END_POINT_BASE_URL);
        String[] lines = homePageContent.split("\n");
        for (String line : lines) {
            if (line.contains("clues")) {
                Pattern intPattern = Pattern.compile("\\d+");
                Matcher intMatcher = intPattern.matcher(line);
                if (!intMatcher.find()) {
                    break;
                }
                return Integer.valueOf(intMatcher.group());
            }
        }
        return -1;
    }

    public CategoryListDTO getCategories() {
        return (CategoryListDTO)getListDTO(END_POINT_BASE_URL + END_POINT_GET_CATEGORIES, CategoryListDTO.class);
    }

    public ClueListDTO getCluesWithParameters(String... args) {
        if((args.length & 1) == 1) {
            // Maybe throw an exception for invalid amount of args
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
        return (ClueListDTO)getListDTO(sb.toString(), ClueListDTO.class);
    }
}

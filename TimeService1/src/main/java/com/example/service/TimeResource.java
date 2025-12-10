package com.example.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Path("/time")
public class TimeResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String currentTime = LocalTime.now().format(formatter);
        
        // Создание JSON ответа
        JsonObject json = Json.createObjectBuilder()
            .add("time", currentTime)
            .build();
            
        return json.toString();
    }
}
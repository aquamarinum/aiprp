/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Artemy
 */
@Path("/html")
public class HTMLResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getHtml() {
        return "<!DOCTYPE html>" + 
               "<html>" + 
               "<head>" + 
               "<title>HTML Head</title>" + 
               "</head>" + 
               "<body>" + 
               "<p>HTML Service</p>" + 
               "</body>" + 
               "</html>";
    }
}

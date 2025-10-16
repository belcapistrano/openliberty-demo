package com.example.demo.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    public Response health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "OpenLiberty Demo API");
        health.put("version", "1.0.0");

        return Response.ok(health).build();
    }

    @GET
    @Path("/ready")
    public Response ready() {
        Map<String, String> readiness = new HashMap<>();
        readiness.put("status", "READY");
        return Response.ok(readiness).build();
    }

    @GET
    @Path("/live")
    public Response live() {
        Map<String, String> liveness = new HashMap<>();
        liveness.put("status", "LIVE");
        return Response.ok(liveness).build();
    }
}
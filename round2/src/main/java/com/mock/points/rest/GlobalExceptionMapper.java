package com.mock.points.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * Translates validation failures into HTTP 400 with {"error": "..."} so
 * endpoints never need per-method try/catch.
 */
public class GlobalExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage() == null ? "invalid request" : e.getMessage()))
                .build();
    }
}

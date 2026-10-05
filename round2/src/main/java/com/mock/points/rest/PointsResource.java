package com.mock.points.rest;

import com.mock.points.service.PointsAward;
import com.mock.points.service.PointsService;
import com.mock.points.service.RentPayment;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.Map;

/**
 * POST /points/preview
 *
 * Request:
 * {
 *   "paymentId": "pay_123",
 *   "memberId": "mem_silver",
 *   "amountCents": 200000,
 *   "paymentDate": "2026-09-05",
 *   "dueDate": "2026-09-05"
 * }
 *
 * Response 200:
 * {
 *   "paymentId": "pay_123",
 *   "memberId": "mem_silver",
 *   "basePoints": 2000,
 *   "tierAdjustedPoints": 2500,
 *   "onTimeBonusApplied": true,
 *   "totalPoints": 3125
 * }
 *
 * Response 400 on invalid input: { "error": "..." }
 */
@Path("/points")
public class PointsResource {

    @Inject
    PointsService pointsService;

    public static class PreviewRequest {
        public String paymentId;
        public String memberId;
        public long amountCents;
        public String paymentDate;
        public String dueDate;
    }

    @POST
    @Path("/preview")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response preview(PreviewRequest req) {
        try {
            RentPayment payment = new RentPayment(
                    req.paymentId,
                    req.memberId,
                    req.amountCents,
                    LocalDate.parse(req.paymentDate),
                    LocalDate.parse(req.dueDate));

            PointsAward award = pointsService.calculateForPayment(payment);
            return Response.ok(award).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage() == null ? "invalid request" : e.getMessage()))
                    .build();
        }
    }
}

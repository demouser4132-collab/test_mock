package com.mock.points.rest;

import com.mock.points.entity.PointsAwardEntity;
import com.mock.points.service.AwardService;
import com.mock.points.service.RentPayment;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDate;
import java.util.List;

/**
 * POST /points/award            -> compute + persist (idempotent on paymentId)
 * GET  /points/history/{member} -> awards, most recent first
 *
 * NOTE: unlike Round 1's PointsResource, these endpoints deliberately have
 * no try/catch. Validation errors surface as IllegalArgumentException and
 * are translated to HTTP 400 by the global exception mapper.
 */
@Path("/points")
public class AwardResource {

    @Inject
    AwardService awardService;

    @POST
    @Path("/award")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PointsAwardEntity award(AwardRequest req) {
        RentPayment payment = new RentPayment(
                req.paymentId,
                req.memberId,
                req.amountCents,
                LocalDate.parse(req.paymentDate),
                LocalDate.parse(req.dueDate));
        return awardService.award(payment);
    }

    @GET
    @Path("/history/{memberId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PointsAwardEntity> history(@PathParam("memberId") String memberId) {
        return awardService.historyFor(memberId);
    }
}

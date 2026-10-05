package com.mock.points.service;

import com.mock.points.entity.PointsAwardEntity;
import com.mock.points.repo.AwardRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Comparator;
import java.util.List;

/**
 * Awarding (Round 2 spec):
 *  - POST /points/award computes points for a payment and PERSISTS the award.
 *  - Awarding must be IDEMPOTENT on paymentId: the payment processor retries
 *    webhooks, and a replayed payment must return the original award and
 *    must not create a second row or double-grant points.
 *  - awardedOn is the payment date (deterministic for replays).
 *  - GET /points/history/{memberId} returns that member's awards,
 *    MOST RECENT FIRST (ties broken by paymentId for stable ordering).
 *  - Unknown member on history -> IllegalArgumentException.
 */
@ApplicationScoped
public class AwardService {

    @Inject
    PointsService pointsService;

    @Inject
    AwardRepository awards;

    public PointsAwardEntity award(RentPayment payment) {
        PointsAward calc = pointsService.calculateForPayment(payment);
        PointsAwardEntity entity = new PointsAwardEntity(
                calc.paymentId(), calc.memberId(), calc.basePoints(),
                calc.tierAdjustedPoints(), calc.onTimeBonusApplied(),
                calc.totalPoints(), payment.getPaymentDate());
        awards.persist(entity);
        return entity;
    }

    public List<PointsAwardEntity> historyFor(String memberId) {
        List<PointsAwardEntity> all = awards.findAllForMember(memberId);
        return all.stream()
                .sorted(Comparator.comparing((PointsAwardEntity a) -> a.awardedOn)
                        .thenComparing(a -> a.paymentId))
                .toList();
    }
}

package com.mock.points.service;

import com.mock.points.entity.MemberEntity;
import com.mock.points.repo.MemberLookup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Points rules (Round 2 spec):
 *  - Base: 1 point per $1 (amountCents / 100).
 *  - Tier multiplier on lifetime points, boundaries INCLUSIVE:
 *      BLUE < 25,000 -> 1.0x | SILVER >= 25,000 -> 1.25x
 *      GOLD >= 50,000 -> 1.5x | PLATINUM >= 100,000 -> 2.0x
 *  - On-time bonus 1.25x: payment date on or before dueDate PLUS a
 *    3-day grace window (dueDate + 3 days, INCLUSIVE, still on time).
 *  - Monthly cap (points.monthly-cap) applies to the FINAL award.
 *  - Validation: null payment / non-positive amount / unknown member
 *    -> IllegalArgumentException.
 */
@ApplicationScoped
public class PointsService {

    public static final int GRACE_DAYS = 3;

    private final MemberLookup members;
    private final long monthlyCap;

    @Inject
    public PointsService(MemberLookup members,
                         @ConfigProperty(name = "points.monthly-cap") long monthlyCap) {
        this.members = members;
        this.monthlyCap = monthlyCap;
    }

    public PointsAward calculateForPayment(RentPayment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("payment is required");
        }
        if (payment.getAmountCents() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        MemberEntity member = members.findByMemberId(payment.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown member: " + payment.getMemberId()));
        return calculate(payment, member);
    }

    public PointsAward calculate(RentPayment payment, MemberEntity member) {
        long basePoints = payment.getAmountCents() / 100;
        long tierAdjusted = (long) Math.floor(basePoints * tierMultiplier(member));

        boolean onTime = payment.getPaymentDate()
                .isBefore(payment.getDueDate().plusDays(GRACE_DAYS));
        long withBonus = onTime ? (long) Math.floor(tierAdjusted * 1.25) : tierAdjusted;

        long total = Math.min(withBonus, monthlyCap);
        return new PointsAward(payment.getPaymentId(), payment.getMemberId(),
                basePoints, tierAdjusted, onTime, total);
    }

    private double tierMultiplier(MemberEntity member) {
        long lifetime = member.lifetimePoints;
        if (lifetime >= 100_000) return 2.0;
        if (lifetime >= 50_000)  return 1.5;
        if (lifetime >= 25_000)  return 1.25;
        return 1.0;
    }
}

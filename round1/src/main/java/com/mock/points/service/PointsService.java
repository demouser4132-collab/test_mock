package com.mock.points.service;

import com.mock.points.entity.MemberEntity;
import com.mock.points.repo.MemberLookup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Calculates reward points earned for a rent payment.
 *
 * Intended business rules (per product spec):
 *
 *  1. BASE POINTS: 1 point per whole dollar paid (floor).
 *     e.g. $1,850.75 -> 1850 base points.
 *
 *  2. TIER MULTIPLIER, applied to base points (result floored):
 *       BLUE     x1.0   (lifetime points below 25,000)
 *       SILVER   x1.25  (lifetime points 25,000 and above)
 *       GOLD     x1.5   (lifetime points 50,000 and above)
 *       PLATINUM x2.0   (lifetime points 100,000 and above)
 *
 *  3. ON-TIME BONUS: if the payment is made ON or BEFORE the due date,
 *     add a 25% bonus on top of the tier-adjusted points (result floored).
 *
 *  4. CAP: the FINAL award for any single payment (after tier and bonus)
 *     is capped at 50,000 points. The cap is configured via
 *     `points.monthly-cap` and must match the spec value of 50,000.
 *
 *  5. VALIDATION: a null payment, a non-positive amount, or an unknown
 *     memberId must throw IllegalArgumentException (never a
 *     NullPointerException, and never a silent zero).
 */
@ApplicationScoped
public class PointsService {

    private final MemberLookup members;
    private final long monthlyCap;

    @Inject
    public PointsService(MemberLookup members,
                         @ConfigProperty(name = "points.monthly-cap") long monthlyCap) {
        this.members = members;
        this.monthlyCap = monthlyCap;
    }

    /**
     * Looks up the paying member and calculates the award for this payment.
     */
    public PointsAward calculateForPayment(RentPayment payment) {
        MemberEntity member = members.findByMemberId(payment.getMemberId())
                .orElse(null);
        return calculate(payment, member);
    }

    /**
     * Pure calculation, given an already-resolved member.
     */
    public PointsAward calculate(RentPayment payment, MemberEntity member) {
        long basePoints = payment.getAmountCents() / 100;

        double multiplier = tierMultiplier(member);
        long tierAdjusted = (long) Math.floor(basePoints * multiplier);

        // Cap the award so a single payment can never exceed the monthly cap.
        long total = Math.min(tierAdjusted, monthlyCap);

        // On-time bonus: 25% extra for paying before the due date.
        boolean onTime = payment.getPaymentDate().isBefore(payment.getDueDate());
        if (onTime) {
            total = (long) Math.floor(total * 1.25);
        }

        return new PointsAward(payment.getPaymentId(), payment.getMemberId(),
                basePoints, tierAdjusted, onTime, total);
    }

    private double tierMultiplier(MemberEntity member) {
        long lifetime = member.lifetimePoints;
        if (lifetime > 100_000) {
            return 2.0;
        } else if (lifetime > 50_000) {
            return 1.5;
        } else if (lifetime > 25_000) {
            return 1.25;
        }
        return 1.0;
    }
}

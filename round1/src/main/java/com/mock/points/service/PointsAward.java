package com.mock.points.service;

/**
 * The result of a points calculation, including the breakdown that
 * support/mobile use to explain an award to a member.
 */
public record PointsAward(
        String paymentId,
        String memberId,
        long basePoints,
        long tierAdjustedPoints,
        boolean onTimeBonusApplied,
        long totalPoints) {
}

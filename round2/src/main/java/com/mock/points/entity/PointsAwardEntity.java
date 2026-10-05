package com.mock.points.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDate;

/**
 * A persisted points award. paymentId is the primary key on purpose:
 * the payment processor retries webhooks, and the database is the last
 * line of defense against awarding the same payment twice.
 */
@Entity
public class PointsAwardEntity extends PanacheEntityBase {

    @Id
    public String paymentId;

    public String memberId;
    public long basePoints;
    public long tierAdjustedPoints;
    public boolean onTimeBonusApplied;
    public long totalPoints;
    public LocalDate awardedOn;

    public PointsAwardEntity() {
    }

    public PointsAwardEntity(String paymentId, String memberId, long basePoints,
                             long tierAdjustedPoints, boolean onTimeBonusApplied,
                             long totalPoints, LocalDate awardedOn) {
        this.paymentId = paymentId;
        this.memberId = memberId;
        this.basePoints = basePoints;
        this.tierAdjustedPoints = tierAdjustedPoints;
        this.onTimeBonusApplied = onTimeBonusApplied;
        this.totalPoints = totalPoints;
        this.awardedOn = awardedOn;
    }
}

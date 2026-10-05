package com.mock.points.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * A rewards program member. lifetimePoints drives tier status
 * (see PointsService for thresholds).
 */
@Entity
public class MemberEntity extends PanacheEntityBase {

    @Id
    public String memberId;

    public long lifetimePoints;

    public MemberEntity() {
    }

    public MemberEntity(String memberId, long lifetimePoints) {
        this.memberId = memberId;
        this.lifetimePoints = lifetimePoints;
    }
}

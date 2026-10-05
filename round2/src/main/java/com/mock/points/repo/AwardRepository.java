package com.mock.points.repo;

import com.mock.points.entity.PointsAwardEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AwardRepository implements PanacheRepositoryBase<PointsAwardEntity, String> {

    public List<PointsAwardEntity> findAllForMember(String memberId) {
        return find("memberId", memberId).list();
    }
}

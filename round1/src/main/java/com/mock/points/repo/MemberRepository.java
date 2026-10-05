package com.mock.points.repo;

import com.mock.points.entity.MemberEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class MemberRepository implements PanacheRepositoryBase<MemberEntity, String>, MemberLookup {

    @Override
    public Optional<MemberEntity> findByMemberId(String memberId) {
        return find("member_id", memberId).firstResultOptional();
    }
}

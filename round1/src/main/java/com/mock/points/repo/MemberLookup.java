package com.mock.points.repo;

import com.mock.points.entity.MemberEntity;

import java.util.Optional;

/**
 * Abstraction over member lookup so PointsService can be unit tested
 * without booting the database.
 */
public interface MemberLookup {
    Optional<MemberEntity> findByMemberId(String memberId);
}

package com.mock.points;

import com.mock.points.repo.MemberRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the repository against the seeded H2 database (see import.sql).
 */
@QuarkusTest
class MemberRepositoryTest {

    @Inject
    MemberRepository repository;

    @Test
    @DisplayName("findByMemberId returns the seeded member with its lifetime points")
    void findsSeededMember() {
        var found = repository.findByMemberId("mem_gold");
        assertTrue(found.isPresent(), "expected seeded member mem_gold to be found");
        assertEquals(50_000, found.get().lifetimePoints);
    }

    @Test
    @DisplayName("findByMemberId returns empty for an unknown id")
    void unknownIdIsEmpty() {
        assertTrue(repository.findByMemberId("mem_nobody").isEmpty());
    }
}

package com.mock.points;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Guards the seed data contract: the README spec pins each seeded member's
 * lifetime balance. If a tier test fails here but GraceWindowTest is green,
 * the logic is fine — look at the DATA.
 */
@QuarkusTest
class SeedDataTest {

    @Test
    @DisplayName("Seeded GOLD member actually gets the 1.5x multiplier end to end")
    void goldSeedIsGold() {
        given()
            .contentType("application/json")
            .body("""
                    {
                      "payment_id": "pay_seed_gold",
                      "member_id": "mem_gold",
                      "amount_cents": 100000,
                      "payment_date": "2026-09-10",
                      "due_date": "2026-09-05"
                    }
                    """)
        .when()
            .post("/points/award")
        .then()
            .statusCode(200)
            .body("tierAdjustedPoints", equalTo(1500));
    }
}

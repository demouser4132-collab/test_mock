package com.mock.points;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * End-to-end checks through the REST layer against the seeded database.
 * These exercise the full path: resource -> service -> repository -> H2,
 * plus the points.monthly-cap configuration.
 */
@QuarkusTest
class PointsResourceTest {

    private String body(String memberId, long amountCents, String paid, String due) {
        return """
                {
                  "paymentId": "pay_9",
                  "memberId": "%s",
                  "amountCents": %d,
                  "paymentDate": "%s",
                  "dueDate": "%s"
                }
                """.formatted(memberId, amountCents, paid, due);
    }

    @Test
    @DisplayName("SILVER member paying $2,000 on the due date earns 3,125 points with full breakdown")
    void silverOnTimePreview() {
        given()
            .contentType("application/json")
            .body(body("mem_silver", 200_000, "2026-09-05", "2026-09-05"))
        .when()
            .post("/points/preview")
        .then()
            .statusCode(200)
            .body("basePoints", equalTo(2000))
            .body("tierAdjustedPoints", equalTo(2500))
            .body("onTimeBonusApplied", equalTo(true))
            .body("totalPoints", equalTo(3125));
    }

    @Test
    @DisplayName("Whale paying $30,000 early is capped at the spec cap of 50,000 points")
    void capIsSpecValueEndToEnd() {
        given()
            .contentType("application/json")
            .body(body("mem_whale", 3_000_000, "2026-09-04", "2026-09-05"))
        .when()
            .post("/points/preview")
        .then()
            .statusCode(200)
            .body("totalPoints", equalTo(50_000));
    }

    @Test
    @DisplayName("Unknown member returns 400, not 500")
    void unknownMemberIs400() {
        given()
            .contentType("application/json")
            .body(body("mem_ghost", 100_000, "2026-09-05", "2026-09-05"))
        .when()
            .post("/points/preview")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Negative amount returns 400, not 500")
    void negativeAmountIs400() {
        given()
            .contentType("application/json")
            .body(body("mem_blue", -100, "2026-09-05", "2026-09-05"))
        .when()
            .post("/points/preview")
        .then()
            .statusCode(400);
    }
}

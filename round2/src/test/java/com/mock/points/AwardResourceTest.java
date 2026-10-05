package com.mock.points;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * End-to-end tests for awarding + history through the REST layer.
 * The webhook payload uses the PROCESSOR'S contract (snake_case keys) —
 * that contract is fixed and cannot be changed on our side.
 */
@QuarkusTest
class AwardResourceTest {

    private String webhook(String paymentId, String memberId, long amountCents,
                           String paid, String due) {
        return """
                {
                  "payment_id": "%s",
                  "member_id": "%s",
                  "amount_cents": %d,
                  "payment_date": "%s",
                  "due_date": "%s"
                }
                """.formatted(paymentId, memberId, amountCents, paid, due);
    }

    @Test
    @DisplayName("Webhook contract: GOLD $2,000 on time awards 3,750 points")
    void webhookContractAwardsGold() {
        given()
            .contentType("application/json")
            .body(webhook("pay_wh1", "mem_gold", 200_000, "2026-09-05", "2026-09-05"))
        .when()
            .post("/points/award")
        .then()
            .statusCode(200)
            .body("paymentId", equalTo("pay_wh1"))
            .body("basePoints", equalTo(2000))
            .body("tierAdjustedPoints", equalTo(3000))
            .body("onTimeBonusApplied", equalTo(true))
            .body("totalPoints", equalTo(3750))
            .body("awardedOn", notNullValue());
    }

    @Test
    @DisplayName("Replayed webhook (same payment_id) returns the original award and stores ONE row")
    void replayedWebhookIsIdempotent() {
        String body = webhook("pay_dup", "mem_whale", 100_000, "2026-09-01", "2026-09-05");

        given().contentType("application/json").body(body)
            .when().post("/points/award")
            .then().statusCode(200).body("totalPoints", equalTo(2500));

        given().contentType("application/json").body(body)
            .when().post("/points/award")
            .then().statusCode(200).body("totalPoints", equalTo(2500));

        given()
        .when()
            .get("/points/history/mem_whale")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1));
    }

    @Test
    @DisplayName("Zero amount returns 400 with an error body, not a 500")
    void zeroAmountIsBadRequest() {
        given()
            .contentType("application/json")
            .body(webhook("pay_zero", "mem_blue", 0, "2026-09-05", "2026-09-05"))
        .when()
            .post("/points/award")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("History for an unknown member returns 400, not 500 or an empty 200")
    void unknownMemberHistoryIsBadRequest() {
        given()
        .when()
            .get("/points/history/mem_nobody")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("History returns awards MOST RECENT FIRST")
    void historyIsMostRecentFirst() {
        given().contentType("application/json")
            .body(webhook("pay_h1", "mem_plat", 100_000, "2026-07-01", "2026-07-05"))
            .when().post("/points/award").then().statusCode(200);
        given().contentType("application/json")
            .body(webhook("pay_h3", "mem_plat", 100_000, "2026-09-01", "2026-09-05"))
            .when().post("/points/award").then().statusCode(200);
        given().contentType("application/json")
            .body(webhook("pay_h2", "mem_plat", 100_000, "2026-08-01", "2026-08-05"))
            .when().post("/points/award").then().statusCode(200);

        given()
        .when()
            .get("/points/history/mem_plat")
        .then()
            .statusCode(200)
            .body("size()", equalTo(3))
            .body("[0].paymentId", equalTo("pay_h3"))
            .body("[1].paymentId", equalTo("pay_h2"))
            .body("[2].paymentId", equalTo("pay_h1"));
    }
}

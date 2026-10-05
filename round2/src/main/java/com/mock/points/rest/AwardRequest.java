package com.mock.points.rest;

/**
 * Inbound webhook payload from the payment processor.
 *
 * CONTRACT (fixed by the processor, cannot be changed):
 * {
 *   "payment_id":   "pay_123",
 *   "member_id":    "mem_gold",
 *   "amount_cents": 200000,
 *   "payment_date": "2026-09-05",
 *   "due_date":     "2026-09-05"
 * }
 */
public class AwardRequest {
    public String paymentId;
    public String memberId;
    public long amountCents;
    public String paymentDate;
    public String dueDate;
}

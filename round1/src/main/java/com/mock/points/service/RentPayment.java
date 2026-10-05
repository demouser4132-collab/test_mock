package com.mock.points.service;

import java.time.LocalDate;

/**
 * A single rent payment submitted by a member.
 * Amounts are in cents to avoid floating point issues.
 */
public class RentPayment {

    private final String paymentId;
    private final String memberId;
    private final long amountCents;
    private final LocalDate paymentDate;
    private final LocalDate dueDate;

    public RentPayment(String paymentId, String memberId, long amountCents,
                       LocalDate paymentDate, LocalDate dueDate) {
        this.paymentId = paymentId;
        this.memberId = memberId;
        this.amountCents = amountCents;
        this.paymentDate = paymentDate;
        this.dueDate = dueDate;
    }

    public String getPaymentId() { return paymentId; }
    public String getMemberId() { return memberId; }
    public long getAmountCents() { return amountCents; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public LocalDate getDueDate() { return dueDate; }
}

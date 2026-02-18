package com.kce.shopverse.payment.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String orderId;
    private String userEmail;
    private Double amount;

    private String paymentMethod;   // UPI / CARD / COD
    private String status;          // SUCCESS / FAILED / REFUNDED
    private String transactionId;

    private LocalDateTime paymentTime;

    public Payment() {}

    public Payment(String orderId, String userEmail,
                   Double amount, String paymentMethod) {

        this.orderId = orderId;
        this.userEmail = userEmail;
        this.amount = amount;
        this.paymentMethod = paymentMethod;

        this.status = "SUCCESS";   // simulate success
        this.transactionId = UUID.randomUUID().toString();
        this.paymentTime = LocalDateTime.now();
    }

    // ===== Getters =====

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getUserEmail() { return userEmail; }
    public Double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getPaymentTime() { return paymentTime; }

    // ===== Setters =====

    public void setId(String id) { this.id = id; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setStatus(String status) { this.status = status; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
}

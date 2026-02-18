package com.kce.shopverse.payment.service;

import com.kce.shopverse.payment.entity.Payment;
import com.kce.shopverse.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentService(PaymentRepository paymentRepository,
                          RazorpayClient razorpayClient) {
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
    }

    // ============================================================
    // 🔹 1. CREATE RAZORPAY ORDER
    // ============================================================
    public String createRazorpayOrder(Double amount) throws Exception {

        JSONObject options = new JSONObject();
        options.put("amount", amount * 100); // convert to paise
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = razorpayClient.orders.create(options);

        return order.get("id");
    }

    // ============================================================
    // 🔹 2. VERIFY SIGNATURE (VERY IMPORTANT)
    // ============================================================
    public boolean verifySignature(String razorpayOrderId,
                                   String razorpayPaymentId,
                                   String razorpaySignature) {

        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;

            return Utils.verifySignature(
                    payload,
                    razorpaySignature,
                    razorpayKeySecret
            );

        } catch (Exception e) {
            return false;
        }
    }

    // ============================================================
    // 🔹 3. SAVE PAYMENT ONLY AFTER SUCCESS
    // ============================================================
    public Payment saveSuccessfulPayment(String orderId,
                                         String userEmail,
                                         Double amount,
                                         String paymentMethod,
                                         String transactionId) {

        Payment payment = new Payment(orderId, userEmail, amount, paymentMethod);
        payment.setTransactionId(transactionId);
        payment.setStatus("SUCCESS");

        return paymentRepository.save(payment);
    }

    // ============================================================
    // 🔹 4. REFUND PAYMENT (REAL RAZORPAY REFUND)
    // ============================================================
    public Payment refundPayment(String paymentId) throws Exception {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Call Razorpay refund API
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("payment_id", payment.getTransactionId());

        Refund refund = razorpayClient.payments
                .refund(payment.getTransactionId(), refundRequest);

        payment.setStatus("REFUNDED");

        return paymentRepository.save(payment);
    }

    // ============================================================
    // 🔹 5. GET PAYMENTS BY USER
    // ============================================================
    public List<Payment> getUserPayments(String email) {
        return paymentRepository.findByUserEmail(email);
    }

    // ============================================================
    // 🔹 6. GET PAYMENTS BY ORDER
    // ============================================================
    public List<Payment> getOrderPayments(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}

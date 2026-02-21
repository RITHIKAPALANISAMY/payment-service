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

    // ================= CREATE ORDER =================
    public String createRazorpayOrder(Double amount) throws Exception {

        JSONObject options = new JSONObject();
        options.put("amount", amount * 100); // rupees → paise
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = razorpayClient.orders.create(options);

        return order.get("id");
    }

    // ================= VERIFY SIGNATURE =================
    public boolean verifySignature(String razorpayOrderId,
                                   String razorpayPaymentId,
                                   String razorpaySignature) {

        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;

            boolean result = Utils.verifySignature(
                    payload,
                    razorpaySignature,
                    razorpayKeySecret.trim()
            );

            System.out.println("Verification Result: " + result);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= SAVE SUCCESS PAYMENT =================
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

    // ================= REFUND =================
    public Payment refundPayment(String paymentId) throws Exception {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        JSONObject refundRequest = new JSONObject();

        Refund refund = razorpayClient.payments
                .refund(payment.getTransactionId(), refundRequest);

        payment.setStatus("REFUNDED");

        return paymentRepository.save(payment);
    }

    public List<Payment> getUserPayments(String email) {
        return paymentRepository.findByUserEmail(email);
    }

    public List<Payment> getOrderPayments(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
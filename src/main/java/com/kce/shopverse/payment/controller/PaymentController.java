package com.kce.shopverse.payment.controller;

import com.kce.shopverse.payment.entity.Payment;
import com.kce.shopverse.payment.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayClient razorpayClient;

    public PaymentController(PaymentService paymentService,
                             RazorpayClient razorpayClient) {
        this.paymentService = paymentService;
        this.razorpayClient = razorpayClient;
    }

    // ================= CREATE ORDER =================
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, String>> createOrder(
            @RequestBody Map<String, Object> request
    ) throws Exception {

        Double amount = Double.parseDouble(request.get("amount").toString());

        JSONObject options = new JSONObject();
        options.put("amount", amount * 100);
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = razorpayClient.orders.create(options);

        Map<String, String> response = new HashMap<>();
        response.put("orderId", order.get("id"));

        return ResponseEntity.ok(response);
    }

    // ================= VERIFY =================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, Object> request
    ) {
        try {

            String razorpayOrderId = request.get("razorpayOrderId").toString();
            String razorpayPaymentId = request.get("razorpayPaymentId").toString();
            String razorpaySignature = request.get("razorpaySignature").toString();
            String userEmail = request.get("userEmail").toString();
            Double amount = Double.parseDouble(request.get("amount").toString());
            String paymentMethod = request.get("paymentMethod").toString();

            boolean isValid = paymentService.verifySignature(
                    razorpayOrderId,
                    razorpayPaymentId,
                    razorpaySignature
            );

            if (!isValid) {
                return ResponseEntity.status(400).body("Invalid signature");
            }

            Payment savedPayment = paymentService.saveSuccessfulPayment(
                    razorpayOrderId,
                    userEmail,
                    amount,
                    paymentMethod,
                    razorpayPaymentId
            );

            return ResponseEntity.ok(savedPayment);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Verification failed");
        }
    }

    @PutMapping("/refund/{id}")
    public Payment refundPayment(@PathVariable String id) throws Exception {
        return paymentService.refundPayment(id);
    }

    @GetMapping("/user/{email}")
    public List<Payment> getUserPayments(@PathVariable String email) {
        return paymentService.getUserPayments(email);
    }

    @GetMapping("/order/{orderId}")
    public List<Payment> getOrderPayments(@PathVariable String orderId) {
        return paymentService.getOrderPayments(orderId);
    }
}
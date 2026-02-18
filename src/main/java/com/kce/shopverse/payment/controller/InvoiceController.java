package com.kce.shopverse.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class InvoiceController {

    @GetMapping("/invoice/{orderId}")
    public String generateInvoice(@PathVariable String orderId, Model model) {

        double amount = 1000.00;

        double gstRate = 0.18;
        double gst = amount * gstRate;
        double subtotal = amount - gst;

        String formattedDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));

        model.addAttribute("orderId", orderId);
        model.addAttribute("transactionId", "TXN" + System.currentTimeMillis());
        model.addAttribute("email", "buyer@gmail.com");
        model.addAttribute("date", formattedDate);
        model.addAttribute("subtotal", String.format("%.2f", subtotal));
        model.addAttribute("gst", String.format("%.2f", gst));
        model.addAttribute("total", String.format("%.2f", amount));
        model.addAttribute("status", "PAID");

        return "invoice";
    }
}

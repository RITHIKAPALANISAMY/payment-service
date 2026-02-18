package com.kce.shopverse.payment.repository;

import com.kce.shopverse.payment.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByUserEmail(String userEmail);

    List<Payment> findByOrderId(String orderId);
}

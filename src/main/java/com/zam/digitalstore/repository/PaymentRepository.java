package com.zam.digitalstore.repository;

import com.zam.digitalstore.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayPaymentIdAndProductIdAndStatus(
            String razorpayPaymentId,
            Long productId,
            String status);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    // Check if a product is already purchased by a user
    Optional<Payment> findByCustomerEmailAndProductIdAndStatus(
            String customerEmail,
            Long productId,
            String status);

    // Get all successful purchases of a user
    List<Payment> findByCustomerEmailAndStatus(
            String customerEmail,
            String status);
}
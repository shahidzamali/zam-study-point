package com.zam.digitalstore.controller;

import com.zam.digitalstore.model.Payment;
import com.zam.digitalstore.repository.PaymentRepository;
import com.zam.digitalstore.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("*")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public PaymentController(
            PaymentService paymentService,
            PaymentRepository paymentRepository) {

        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    // Create Razorpay Order
    @PostMapping("/create-order")
    public String createOrder(
            @RequestParam Long productId) {

        return paymentService.createOrder(productId);
    }

    // Verify Razorpay Payment
    @PostMapping("/verify")
    public String verifyPayment(
            @RequestParam String paymentId,
            @RequestParam String orderId,
            @RequestParam String signature,
            @RequestParam Long productId,
            @RequestParam String customerName,
            @RequestParam String customerEmail,
            @RequestParam String customerPhone) {

        return paymentService.verifyPayment(
                paymentId,
                orderId,
                signature,
                productId,
                customerName,
                customerEmail,
                customerPhone);
    }

    // Get all payment records
    @GetMapping("/all")
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Check whether user already purchased a product
    @GetMapping("/purchased")
    public boolean isProductPurchased(
            @RequestParam String email,
            @RequestParam Long productId) {

        return paymentRepository
                .findByCustomerEmailAndProductIdAndStatus(
                        email,
                        productId,
                        "SUCCESS")
                .isPresent();
    }

    // Get user's purchase history
    @GetMapping("/history")
    public List<Payment> getPurchaseHistory(
            @RequestParam String email) {

        return paymentRepository
                .findByCustomerEmailAndStatus(
                        email,
                        "SUCCESS");
    }
}
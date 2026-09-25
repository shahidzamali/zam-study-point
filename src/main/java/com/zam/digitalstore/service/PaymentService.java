package com.zam.digitalstore.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import com.zam.digitalstore.model.Payment;
import com.zam.digitalstore.model.Product;

import com.zam.digitalstore.repository.PaymentRepository;
import com.zam.digitalstore.repository.ProductRepository;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final ProductRepository productRepository;

        @Value("${razorpay.key.id}")
        private String keyId;

        @Value("${razorpay.key.secret}")
        private String keySecret;

        public PaymentService(
                        PaymentRepository paymentRepository,
                        ProductRepository productRepository) {

                this.paymentRepository = paymentRepository;
                this.productRepository = productRepository;
        }

        // =====================================================
        // CREATE RAZORPAY ORDER
        // =====================================================

        public String createOrder(Long productId) {

                try {

                        // -------------------------------------------------
                        // Find product from database
                        // -------------------------------------------------

                        Product product = productRepository
                                        .findById(productId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Product not found with id: " + productId));

                        // -------------------------------------------------
                        // Check product price
                        // -------------------------------------------------

                        if (product.getPrice() == null) {

                                throw new RuntimeException(
                                                "Product price is not set");
                        }

                        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {

                                throw new RuntimeException(
                                                "Product price must be greater than zero");
                        }

                        // -------------------------------------------------
                        // Database price
                        // -------------------------------------------------

                        BigDecimal productPrice = product.getPrice();

                        // Razorpay uses paise
                        int amountInPaise = productPrice
                                        .multiply(BigDecimal.valueOf(100))
                                        .intValueExact();

                        // -------------------------------------------------
                        // Razorpay client
                        // -------------------------------------------------

                        RazorpayClient razorpayClient = new RazorpayClient(
                                        keyId,
                                        keySecret);

                        // -------------------------------------------------
                        // Create Razorpay order
                        // -------------------------------------------------

                        JSONObject orderRequest = new JSONObject();

                        orderRequest.put(
                                        "amount",
                                        amountInPaise);

                        orderRequest.put(
                                        "currency",
                                        "INR");

                        orderRequest.put(
                                        "receipt",
                                        "product_"
                                                        + productId
                                                        + "_"
                                                        + System.currentTimeMillis());

                        Order order = razorpayClient.orders
                                        .create(orderRequest);

                        // -------------------------------------------------
                        // Response to frontend
                        // -------------------------------------------------

                        JSONObject response = new JSONObject();

                        response.put(
                                        "orderId",
                                        order.get("id").toString());

                        response.put(
                                        "amount",
                                        amountInPaise);

                        response.put(
                                        "currency",
                                        "INR");

                        response.put(
                                        "keyId",
                                        keyId);

                        response.put(
                                        "productId",
                                        productId);

                        return response.toString();

                } catch (Exception e) {

                        e.printStackTrace();

                        throw new RuntimeException(
                                        "Razorpay order creation failed: "
                                                        + e.getMessage());
                }
        }

        // =====================================================
        // VERIFY RAZORPAY PAYMENT
        // =====================================================

        public String verifyPayment(
                        String paymentId,
                        String orderId,
                        String signature,
                        Long productId,
                        String customerName,
                        String customerEmail,
                        String customerPhone) {

                try {

                        // -------------------------------------------------
                        // Find product
                        // -------------------------------------------------

                        Product product = productRepository
                                        .findById(productId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Product not found with id: "
                                                                        + productId));

                        // -------------------------------------------------
                        // Check product price
                        // -------------------------------------------------

                        if (product.getPrice() == null) {

                                throw new RuntimeException(
                                                "Product price is not set");
                        }

                        BigDecimal productPrice = product.getPrice();

                        // -------------------------------------------------
                        // Verify Razorpay signature
                        // -------------------------------------------------

                        JSONObject options = new JSONObject();

                        options.put(
                                        "razorpay_order_id",
                                        orderId);

                        options.put(
                                        "razorpay_payment_id",
                                        paymentId);

                        options.put(
                                        "razorpay_signature",
                                        signature);

                        boolean isValid = Utils.verifyPaymentSignature(
                                        options,
                                        keySecret);

                        if (!isValid) {

                                throw new RuntimeException(
                                                "Invalid Razorpay payment signature");
                        }

                        // -------------------------------------------------
                        // Prevent duplicate payment
                        // -------------------------------------------------

                        boolean alreadyExists = paymentRepository
                                        .findByRazorpayPaymentId(
                                                        paymentId)
                                        .isPresent();

                        if (alreadyExists) {

                                return "Payment already verified. Payment ID: "
                                                + paymentId;
                        }

                        // -------------------------------------------------
                        // Create payment record
                        // -------------------------------------------------

                        Payment payment = new Payment();

                        payment.setProductId(
                                        productId);

                        payment.setCustomerName(
                                        customerName);

                        payment.setCustomerEmail(
                                        customerEmail);

                        payment.setCustomerPhone(
                                        customerPhone);

                        payment.setRazorpayOrderId(
                                        orderId);

                        payment.setRazorpayPaymentId(
                                        paymentId);

                        payment.setRazorpaySignature(
                                        signature);

                        payment.setAmount(
                                        productPrice);

                        payment.setStatus(
                                        "SUCCESS");

                        // -------------------------------------------------
                        // Save payment
                        // -------------------------------------------------

                        paymentRepository.save(
                                        payment);

                        return "Payment verified and saved successfully. "
                                        + "Payment ID: "
                                        + paymentId;

                } catch (Exception e) {

                        e.printStackTrace();

                        throw new RuntimeException(
                                        "Payment verification failed: "
                                                        + e.getMessage());
                }
        }
}
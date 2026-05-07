package com.apimonitor.ambackend.controller;

import com.apimonitor.ambackend.service.UserService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final UserService userService;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.pro-price-id}")
    private String proPriceId;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    public PaymentController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a Stripe Checkout Session for the Pro plan subscription.
     * Body: { "email": "user@example.com" }
     * Returns: { "url": "https://checkout.stripe.com/..." }
     */
    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        Stripe.apiKey = secretKey;

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomerEmail(email)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPrice(proPriceId)
                            .build())
                    .build();

            Session session = Session.create(params);
            log.info("Checkout session created for: {}", email);
            return ResponseEntity.ok(Map.of("url", session.getUrl()));
        } catch (StripeException e) {
            log.error("Stripe error creating checkout session: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Stripe webhook endpoint. Receives events from Stripe and processes them.
     * On checkout.session.completed, marks the user as PRO in the database.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject().ifPresent(stripeObject -> {
                Session session = (Session) stripeObject;
                String email = session.getCustomerEmail();
                String customerId = session.getCustomer();
                if (email != null) {
                    try {
                        userService.upgradeToPro(email, customerId);
                        log.info("Webhook: upgraded {} to PRO (customer: {})", email, customerId);
                    } catch (IllegalArgumentException e) {
                        log.warn("Webhook: user not found for email: {}", email);
                    }
                }
            });
        }

        return ResponseEntity.ok("OK");
    }
}

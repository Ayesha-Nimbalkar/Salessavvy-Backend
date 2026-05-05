package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.PaymentService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
public class PaymentController {

	@Value("${cashfree.client.id}")
	private String clientId;

	@Value("${cashfree.client.secret}")
	private String clientSecret;

	@Autowired
	private AuthService authService;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private PaymentService paymentService;

	// 🔥 CREATE ORDER
	@PostMapping("/create-order")
	public ResponseEntity<?> createOrder(HttpServletRequest request) {

		try {
			// 🔥 GET USER FROM TOKEN
			String token = null;
			if (request.getCookies() != null) {
				for (Cookie c : request.getCookies()) {
					if ("authToken".equals(c.getName())) {
						token = c.getValue();
					}
				}
			}

			if (token == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing");
			}

			String username = authService.extractUsername(token);
			User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

			// 🔥 CREATE ORDER ID
			String orderId = "order_" + System.currentTimeMillis();

			// 🔥 SAVE ORDER IN DB
			paymentService.createOrderInDB(user.getUserId(), orderId, new BigDecimal(500));

			// 🔥 CASHFREE API
			String url = "https://sandbox.cashfree.com/pg/orders";
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("x-client-id", clientId);
			headers.set("x-client-secret", clientSecret);
			headers.set("x-api-version", "2022-09-01");

			Map<String, Object> body = new HashMap<>();
			body.put("order_id", orderId);
			body.put("order_amount", 500);
			body.put("order_currency", "INR");

			Map<String, Object> customer = new HashMap<>();
			customer.put("customer_id", "cust_" + user.getUserId());
			customer.put("customer_email", user.getEmail());
			customer.put("customer_phone", "9999999999");

			body.put("customer_details", customer);

			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

			ResponseEntity<Map> cfResponse = restTemplate.postForEntity(url, entity, Map.class);

			String sessionId = (String) cfResponse.getBody().get("payment_session_id");

			// 🔥 SEND TO FRONTEND
			return ResponseEntity.ok(Map.of("paymentSessionId", sessionId, "orderId", orderId));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(e.getMessage());
		}
	}

	// 🔥 VERIFY + SUCCESS FLOW
	@PostMapping("/verify")
	public ResponseEntity<?> verify(@RequestParam String orderId, HttpServletRequest request) {

		try {
			String token = null;

			if (request.getCookies() != null) {
				for (Cookie c : request.getCookies()) {
					if ("authToken".equals(c.getName())) {
						token = c.getValue();
					}
				}
			}

			if (token == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing");
			}

			String username = authService.extractUsername(token);
			User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

			// 🔥 PAYMENT SUCCESS
			paymentService.markPaymentSuccess(orderId, user.getUserId());

			System.out.println("🔥 ORDER SUCCESS + CART CLEARED FOR: " + username);

			return ResponseEntity.ok(Map.of("status", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Verification failed");
		}
	}
}
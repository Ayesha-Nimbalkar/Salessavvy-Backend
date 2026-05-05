package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

		User user = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

		String token = authService.generateToken(user);

		// ✅ CORRECT COOKIE (DEV SAFE)
		Cookie cookie = new Cookie("authToken", token);
		cookie.setHttpOnly(true);
		cookie.setSecure(false); // HTTP dev
		cookie.setPath("/");
		cookie.setMaxAge(3600);

		response.addCookie(cookie);

		Map<String, Object> body = new HashMap<>();
		body.put("message", "Login successful");
		body.put("role", user.getRole().name());
		body.put("username", user.getUsername());

		return ResponseEntity.ok(body);
	}
}
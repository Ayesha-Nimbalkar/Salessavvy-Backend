package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.JWTToken;
import com.example.demo.entity.User;
import com.example.demo.repository.JWTTokenRepository;
import com.example.demo.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService {

	private final Key SIGNING_KEY;
	private final UserRepository userRepository;
	private final JWTTokenRepository jwtRepo;
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	public AuthService(UserRepository userRepository, JWTTokenRepository jwtRepo,
			@Value("${jwt.secret}") String jwtSecret) {

		this.userRepository = userRepository;
		this.jwtRepo = jwtRepo;

		this.SIGNING_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	public User authenticate(String username, String password) {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Invalid username or password"));

		if (!encoder.matches(password, user.getPassword())) {
			throw new RuntimeException("Invalid username or password");
		}
		return user;
	}

	public String generateToken(User user) {

		String token = Jwts.builder().setSubject(user.getUsername()).claim("role", user.getRole().name())
				.setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 3600000))
				.signWith(SIGNING_KEY, SignatureAlgorithm.HS512).compact();

		jwtRepo.save(new JWTToken(user, token, LocalDateTime.now().plusHours(1)));

		return token;
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(SIGNING_KEY).build().parseClaimsJws(token);
			return jwtRepo.findByToken(token).map(t -> t.getExpiresAt().isAfter(LocalDateTime.now())).orElse(false);
		} catch (Exception e) {
			return false;
		}
	}

	public String extractUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(SIGNING_KEY).build().parseClaimsJws(token).getBody().getSubject();
	}
}
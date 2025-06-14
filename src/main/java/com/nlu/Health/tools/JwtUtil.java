package com.nlu.Health.tools;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.security.Key;

public class JwtUtil {

    private static final String SECRET_KEY = "x9kPqW3zT7mY2rN8vL4jF6hB0cD5aE1gJ3iK2uM9nQ"; // Chuỗi mới, 32 ký tự
    private static final long EXPIRATION_TIME = 86400000; // 1 ngày

    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Tạo token
    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email.toLowerCase()) // Chuẩn hóa email thành chữ thường
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }
}
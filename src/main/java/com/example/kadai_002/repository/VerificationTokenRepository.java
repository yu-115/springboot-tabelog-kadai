package com.example.kadai_002.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kadai_002.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
	public VerificationToken findByToken(String token);
	public VerificationToken findByUserId(Integer userId);
}

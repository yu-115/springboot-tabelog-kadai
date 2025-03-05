package com.example.kadai_002.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kadai_002.entity.UsersPaid;

public interface UsersPaidRepository extends JpaRepository<UsersPaid, Integer>{
	public UsersPaid findByUserId(Integer userId);
	public UsersPaid findByPaymentId(String paymentMethodId);
}

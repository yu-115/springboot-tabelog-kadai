package com.example.kadai_002.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kadai_002.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
	List<Review> findByShopId(Integer shopId);
	Page<Review> findByShopId(Integer shopId, Pageable pageable);
	Review findByShopIdAndUserId(Integer shopId, Integer userId);
	void deleteByShopId(Integer shopId);
}

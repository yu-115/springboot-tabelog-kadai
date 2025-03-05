package com.example.kadai_002.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kadai_002.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
	List<Favorite> findByUserId(Integer userId);
	List<Favorite> findByUserIdAndShopId(Integer userId, Integer shopId);
	Page<Favorite> findByUserId(Integer userId, Pageable pageable);
	void deleteByUserIdAndShopId(Integer userId, Integer shopId);
	void deleteByShopId(Integer shopId);
}

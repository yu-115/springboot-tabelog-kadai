package com.example.kadai_002.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.kadai_002.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Integer> {
	public Page<Shop> findByNameLike(String keyword, Pageable pageable);
	
	public Page<Shop> findByPriceLessThanEqual(Integer price, Pageable pageable); 

	public List<Shop> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT s, AVG(CAST(r.score AS float)) as avgScore FROM Shop s JOIN Review r ON s.id = r.shopId GROUP BY s.id ORDER BY avgScore DESC")
    List<Object[]> findTop10ByAverageScore();
	
	@Query("SELECT s FROM Shop s JOIN ShopCategory sc ON s.id = sc.shopId WHERE sc.categoryId = :categoryId")
	public Page<Shop> findByCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);
}

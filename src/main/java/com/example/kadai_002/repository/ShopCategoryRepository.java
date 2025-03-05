package com.example.kadai_002.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.kadai_002.entity.ShopCategory;

public interface ShopCategoryRepository extends JpaRepository<ShopCategory, Integer> {
	@Query("SELECT sc FROM ShopCategory sc JOIN FETCH sc.category WHERE sc.shopId = :shopId")
	public List<ShopCategory> findByShopId(@Param("shopId") Integer shopId);
	
	@Query("SELECT sc FROM ShopCategory sc JOIN FETCH sc.category WHERE sc.shopId = :shopId AND sc.category.id = :categoryId")
    ShopCategory findByShopIdAndCategoryId(@Param("shopId") Integer shopId, @Param("categoryId") Integer categoryId);
	//sc(shopCategoryの別名。任意に設定可能)=ShopCategoryエンティティからデータを選択し、それに関連するcategoryエンティティを結合して一度に取得。
	//shopIdが指定された値に一致するデータをフィルタリングし、categoryIdが指定された値に一致するデータをさらにフィルタリングする。
	
	void deleteByCategoryId(Integer categoryId);
	void deleteByShopId(Integer shopId);
}

package com.example.kadai_002.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "shop_categories")
@Data
public class ShopCategory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "shop_id")
	private Integer shopId;
	
	@Column(name = "category_id")
	private Integer categoryId;
	
	@ManyToOne(fetch = FetchType.EAGER) // フェッチモードを EAGER に設定
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
}

package com.example.kadai_002.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reviews")
@Data
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "shop_id")
	private Integer shopId;
	
	@Column(name = "user_id")
	private Integer userId;
	
	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "review_comment")
	private String reviewComment;
	
	@Column(name = "score")
	private String score;
	
	@Column(name = "enabled")
	private Boolean enabled;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Column(name = "updated_at", insertable = false, updatable = false)
	private Timestamp updatedAt;
}
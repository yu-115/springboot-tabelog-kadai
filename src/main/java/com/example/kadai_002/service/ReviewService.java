package com.example.kadai_002.service;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kadai_002.entity.Review;
import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.form.ReviewEditForm;
import com.example.kadai_002.form.ReviewPostForm;
import com.example.kadai_002.repository.ReviewRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.security.UserDetailsImpl;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final ShopRepository shopRepository;
	private final UserRepository userRepository;
	
	public ReviewService(ReviewRepository reviewRepository, ShopRepository shopRepository, UserRepository userRepository) {
		this.reviewRepository = reviewRepository;
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public void create(ReviewPostForm reviewPostForm, Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Review review = new Review();
		Shop shop = shopRepository.getReferenceById(id);
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		
		review.setShopId(shop.getId());
		review.setUserId(user.getId());
		review.setUserName(user.getName());
		review.setReviewComment(reviewPostForm.getReviewComment());
		review.setScore(reviewPostForm.getScore());
		review.setEnabled(true);
		
		reviewRepository.save(review);
	}
	
	@Transactional
	public void update(ReviewEditForm reviewEditForm, Integer id, Integer userId) {
		Review review = reviewRepository.findByShopIdAndUserId(id, userId);

		review.setReviewComment(reviewEditForm.getReviewComment());
		review.setScore(reviewEditForm.getScore());
		
		reviewRepository.save(review);
	}
	
	@Transactional
	public void changePrivate(Integer id) {
		Review review = reviewRepository.getReferenceById(id);

		review.setEnabled(false);
		
		reviewRepository.save(review);
	}
	
	@Transactional
	public void changePublic(Integer id) {
		Review review = reviewRepository.getReferenceById(id);

		review.setEnabled(true);
		
		reviewRepository.save(review);
	}
}

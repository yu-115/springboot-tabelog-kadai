package com.example.kadai_002.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kadai_002.entity.Review;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.repository.ReviewRepository;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.security.UserDetailsImpl;
import com.example.kadai_002.service.ReviewService;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {
	private final ReviewRepository reviewRepository;
	private final ReviewService reviewService;
	private final UserRepository userRepository;
	
	public AdminReviewController(ReviewRepository reviewRepository, ReviewService reviewService, UserRepository userRepository) {
		this.reviewRepository = reviewRepository;
		this.reviewService = reviewService;
		this.userRepository = userRepository;
	}
	
	@GetMapping
	public String index(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Page<Review> reviewPage = reviewRepository.findAll(pageable);
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		Integer userId = user.getId();
		
		Map<Integer, String> userNameMap = new HashMap<>();
	    for (Review review : reviewPage) {
	        User user2 = userRepository.findById(review.getUserId()).orElse(null);
	        if (user2 != null) {
	            userNameMap.put(review.getId(), user2.getName());
	        }
	    }
		
		model.addAttribute("reviewPage", reviewPage);
		model.addAttribute("userNameMap", userNameMap);
		model.addAttribute("userId", userId);
		
		return "admin/reviews/index";
	}
	
	@PostMapping("/{id}/changePrivate")
	public String changePrivate(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		reviewService.changePrivate(id);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを非公開設定しました。");
		
		return "redirect:/admin/reviews";
	}
	
	@PostMapping("/{id}/changePublic")
	public String changePublic(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		reviewService.changePublic(id);
		redirectAttributes.addFlashAttribute("successMessage", "レビューの非公開設定を解除しました。");
		
		return "redirect:/admin/reviews";
	}
}

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
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kadai_002.entity.Review;
import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.form.ReviewEditForm;
import com.example.kadai_002.form.ReviewPostForm;
import com.example.kadai_002.repository.ReviewRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.security.UserDetailsImpl;
import com.example.kadai_002.service.ReviewService;

@Controller
@RequestMapping("/shops/{id}/reviews")
public class ReviewController {
	private final ReviewRepository reviewRepository;
	private final ReviewService reviewService;
	private final ShopRepository shopRepository;
	private final UserRepository userRepository;
	
	public ReviewController(ReviewRepository reviewRepository, ReviewService reviewService, ShopRepository shopRepository, UserRepository userRepository) {
		this.reviewRepository = reviewRepository;
		this.reviewService = reviewService;
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
	}
	
	@GetMapping
	public String index(@PathVariable(name = "id") Integer id, Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Page<Review> reviewPage = reviewRepository.findByShopId(id, pageable);
		Shop shop = shopRepository.getReferenceById(id);
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
		model.addAttribute("shop", shop);
		model.addAttribute("userId", userId);
		
		return "reviews/index";
	}
	
	@GetMapping("/post")
	public String post(@PathVariable(name = "id") Integer id, Model model) {
		Shop shop = shopRepository.getReferenceById(id);
		
		model.addAttribute("shop", shop);
		model.addAttribute("reviewPostForm", new ReviewPostForm());
		
		return "reviews/post";
	}
	
	@PostMapping("/create")
	public String create(@PathVariable(name = "id") Integer id, @ModelAttribute @Validated ReviewPostForm reviewPostForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		Shop shop = shopRepository.findById(id).orElse(null);
		if (bindingResult.hasErrors()) {
			model.addAttribute("shop", shop);
			return "reviews/post";
		}
		
		reviewService.create(reviewPostForm, id, userDetailsImpl);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");
		
		return "redirect:/shops/{id}/reviews";
	}
	
	@GetMapping("/edit")
	public String edit(@PathVariable(name = "id") Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		Shop shop = shopRepository.getReferenceById(id);
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		Review review = reviewRepository.findByShopIdAndUserId(id, user.getId());
		
		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getId(), review.getScore(), review.getReviewComment());
		
		model.addAttribute("shop", shop);
		model.addAttribute("review", review);
		model.addAttribute("reviewEditForm", reviewEditForm);
		
		return "reviews/edit";
	}
	
	@PostMapping("/update")
	public String update(@PathVariable(name = "id") Integer id, @ModelAttribute @Validated ReviewEditForm reviewEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		Shop shop = shopRepository.findById(id).orElse(null);
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		if (bindingResult.hasErrors()) {
			model.addAttribute("shop", shop);
			return "reviews/edit";
		}
		
		reviewService.update(reviewEditForm, id, user.getId());
		redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");
		
		return "redirect:/shops/{id}/reviews";
	}
	
	@PostMapping("/delete")
	public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		Review review = reviewRepository.findByShopIdAndUserId(id, user.getId());
		reviewRepository.deleteById(review.getId());
		
		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
		
		return "redirect:/shops/{id}";
	}
}

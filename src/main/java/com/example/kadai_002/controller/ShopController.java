package com.example.kadai_002.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.kadai_002.entity.Category;
import com.example.kadai_002.entity.Favorite;
import com.example.kadai_002.entity.Review;
import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.ShopCategory;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.form.ReservationInputForm;
import com.example.kadai_002.repository.CategoryRepository;
import com.example.kadai_002.repository.FavoriteRepository;
import com.example.kadai_002.repository.ReviewRepository;
import com.example.kadai_002.repository.ShopCategoryRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.security.UserDetailsImpl;

@Controller
@RequestMapping("/shops")
public class ShopController {
	private final ShopRepository shopRepository;
	private final ShopCategoryRepository shopCategoryRepository;
	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;
	private final FavoriteRepository favoriteRepository;
	private final ReviewRepository reviewRepository;
	
	public ShopController(ShopRepository shopRepository, ShopCategoryRepository shopCategoryRepository, CategoryRepository categoryRepository, UserRepository userRepository, FavoriteRepository favoriteRepository, ReviewRepository reviewRepository) {
		this.shopRepository = shopRepository;
		this.shopCategoryRepository = shopCategoryRepository;
		this.categoryRepository = categoryRepository;
		this.userRepository = userRepository;
		this.favoriteRepository = favoriteRepository;
		this.reviewRepository = reviewRepository;
	}
	
	@GetMapping
	public String index(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
						@RequestParam(name = "keyword", required = false) String keyword,
						@RequestParam(name = "price", required = false) Integer price,
						@RequestParam(name = "category", required = false) Integer categoryId) {
		Page<Shop> shopPage;
		List<Category> categoryList = categoryRepository.findAll();
		
		if (keyword != null && !keyword.isEmpty()) {
			shopPage = shopRepository.findByNameLike("%" + keyword + "%", pageable);
		} else if (price != null) {
			shopPage = shopRepository.findByPriceLessThanEqual(price, pageable);
		} else if (categoryId != null) {
			shopPage = shopRepository.findByCategoryId(categoryId, pageable);
		} else {
			shopPage = shopRepository.findAll(pageable);
		}
		
		Map<Integer, String> shopCategoriesMap = new HashMap<>();
	    	for (Shop shop : shopPage) {
	    		String categoryNames = shopCategoryRepository.findByShopId(shop.getId()).stream()
	                    .map(sc -> sc.getCategory().getName())
	                    .collect(Collectors.joining(", "));
	            shopCategoriesMap.put(shop.getId(), categoryNames);
	        }
		
		model.addAttribute("shopPage", shopPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("price", price);
		model.addAttribute("categoryList", categoryList);
		model.addAttribute("categoryId", categoryId);
        model.addAttribute("shopCategoriesMap", shopCategoriesMap);
		
		return "shops/index";
	}
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model,  @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Shop shop = shopRepository.getReferenceById(id);
		List<ShopCategory> shopCategories = shopCategoryRepository.findByShopId(id);
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		List<Favorite> favorites = favoriteRepository.findByUserIdAndShopId(user.getId(), id);
		
		List<Review> reviews = reviewRepository.findByShopId(id);
		List<Review> cutReviews;
		boolean doubleCheck = false;
		
		for (Review review : reviews) {
			if (review.getShopId().equals(id) && review.getUserId().equals(user.getId())) {
				doubleCheck = true;
				break;
			}
		}
		
		Map<Integer, String> userNameMap = new HashMap<>();
	    for (Review review : reviews) {
	        User user2 = userRepository.findById(review.getUserId()).orElse(null);
	        if (user2 != null) {
	            userNameMap.put(review.getId(), user2.getName());
	        }
	    }
		
		//リストのサイズをチェックして、10件に制限する
		if (reviews.size() > 10) {
			cutReviews = reviews.subList(0, 10);
			model.addAttribute("cutReviews", cutReviews);
		}
		
		model.addAttribute("shop", shop);
		model.addAttribute("shopCategories", shopCategories);
		model.addAttribute("favorites", favorites);
		model.addAttribute("reviews", reviews);
		model.addAttribute("userNameMap", userNameMap);
		model.addAttribute("userId", user.getId());
		model.addAttribute("doubleCheck", doubleCheck);
		model.addAttribute("reservationInputForm", new ReservationInputForm());
		return "shops/show";
	}
}

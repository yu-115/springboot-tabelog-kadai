package com.example.kadai_002.controller;

import java.util.HashMap;
import java.util.List;
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

import com.example.kadai_002.entity.Favorite;
import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.repository.FavoriteRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.security.UserDetailsImpl;
import com.example.kadai_002.service.FavoriteService;

@Controller
@RequestMapping("/")
public class FavoriteController {
	private final FavoriteRepository favoriteRepository;
	private final FavoriteService favoriteService;
	private final ShopRepository shopRepository;
	private final UserRepository userRepository;
	
	public FavoriteController(FavoriteRepository favoriteRepository, FavoriteService favoriteService, ShopRepository shopRepository, UserRepository userRepository) {
		this.favoriteRepository = favoriteRepository;
		this.favoriteService = favoriteService;
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
	}
	
	@GetMapping("/favorite")
	public String index(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		Integer userId = user.getId();
		Page<Favorite> favoritePage = favoriteRepository.findByUserId(userId, pageable);
		
		Map<Integer, Shop> shopMap = new HashMap<>();
		for (Favorite favorite : favoritePage) {
			Shop shop = shopRepository.findById(favorite.getShopId()).orElse(null);
			if (shop != null) {
				shopMap.put(favorite.getShopId(), shop);
			}
		}
		
		model.addAttribute("favoritePage", favoritePage);
		model.addAttribute("shopMap", shopMap);

		return "favorites/index";
	}
	
	@PostMapping("/shops/{id}/favoriteRegister")
	public String favoriteRegister(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		List<Favorite> favorites = favoriteRepository.findByUserIdAndShopId(user.getId(), id);
		
		//二重窓などで重複してお気に入り登録されない為の処理。既に登録済みの場合はお気に入り登録せずエラーメッセージを表示する。
		if (!favorites.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "既にお気に入りに登録済みです。");
			return "redirect:/shops/{id}";
		}
		
		favoriteService.favoriteRegister(id, userDetailsImpl);
		redirectAttributes.addFlashAttribute("successMessage", "お気に入りに登録しました。");
		
		return "redirect:/shops/{id}";
	}
	
	@PostMapping("/shops/{id}/favoriteDelete")
	public String favoriteDelete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		
		favoriteService.favoriteDelete(id, userDetailsImpl);
		redirectAttributes.addFlashAttribute("successMessage", "お気に入りを解除しました。");
		
		return "redirect:/shops/{id}";
	}
}

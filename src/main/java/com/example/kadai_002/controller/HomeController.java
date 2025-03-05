package com.example.kadai_002.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.repository.ShopCategoryRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.security.UserDetailsImpl;
import com.example.kadai_002.service.ShopService;

@Controller
public class HomeController {
	private final ShopRepository shopRepository;
	private final ShopService shopService;
	private final ShopCategoryRepository shopCategoryRepository;
	private final UserRepository userRepository;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	public HomeController(ShopRepository shopRepository, ShopService shopService, ShopCategoryRepository shopCategoryRepository, UserRepository userRepository) {
		this.shopRepository = shopRepository;
		this.shopService = shopService;
		this.shopCategoryRepository = shopCategoryRepository;
		this.userRepository = userRepository;
	}
	
	@GetMapping("/")
	public String index(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, @RequestParam(name = "keyword", required = false) String keyword, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Page<Shop> shopPage;
		
		if (keyword != null && !keyword.isEmpty()) {
			shopPage = shopRepository.findByNameLike("%" + keyword + "%", pageable);
		} else {
			shopPage = shopRepository.findAll(pageable);
		}
		
		Map<Integer, String> shopCategoriesMap = new HashMap<>();
		List<Shop> shopList = shopRepository.findAll();
    	for (Shop shop : shopList) {
    		String categoryNames = shopCategoryRepository.findByShopId(shop.getId()).stream()
                    .map(sc -> sc.getCategory().getName())
                    .collect(Collectors.joining(", "));
            shopCategoriesMap.put(shop.getId(), categoryNames);
        }
		
		List<Shop> newShops = shopRepository.findTop10ByOrderByCreatedAtDesc();
        model.addAttribute("newShops", newShops);
        
        List<Shop> topShops = shopService.getTop10ShopsByAverageScore();
        model.addAttribute("topShops", topShops);
		
		model.addAttribute("shopPage", shopPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("shopCategoriesMap", shopCategoriesMap);
		
		//ログイン済みの場合のみ実行（未ログインの場合、ユーザー情報を読み込まない
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
			// 認証（ロール）情報の更新
			User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
			UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(user.getEmail());
		    UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(updatedUserDetails, updatedUserDetails.getPassword(), updatedUserDetails.getAuthorities());
		    
		    // 新旧の認証情報から、特定ロールが含まれるかを確認し、メッセージの有無・内容を分岐させる
	        boolean oldRoleIsMember = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEMBER"));
	        boolean oldRoleIsPaid = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PAID"));
	        boolean newRoleIsMember = newAuthentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEMBER"));
	        boolean newRoleIsPaid = newAuthentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PAID"));
	        
	        if (oldRoleIsMember && newRoleIsPaid) {
	            model.addAttribute("successMessage", "有料会員登録が成功しました。");
	        } else if (oldRoleIsPaid && newRoleIsMember) {
	            model.addAttribute("successMessage", "有料会員を解約しました。");
	        }
		    
		    SecurityContextHolder.getContext().setAuthentication(newAuthentication);
		}
		
		return "index";
	}
}

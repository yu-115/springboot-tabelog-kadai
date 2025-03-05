package com.example.kadai_002.service;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kadai_002.entity.Favorite;
import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.repository.FavoriteRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.security.UserDetailsImpl;

@Service
public class FavoriteService {
	private final FavoriteRepository favoriteRepository;
	private final ShopRepository shopRepository;
	private final UserRepository userRepository;
	
	public FavoriteService(FavoriteRepository favoriteRepository, ShopRepository shopRepository, UserRepository userRepository) {
		this.favoriteRepository = favoriteRepository;
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
	}
	
	@Transactional
	public void favoriteRegister(Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Favorite favorite = new Favorite();
		Shop shop = shopRepository.getReferenceById(id);
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		
		favorite.setShopId(shop.getId());
		favorite.setUserId(user.getId());
		
		favoriteRepository.save(favorite);
	}
	
	@Transactional
	public void favoriteDelete(Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Integer userId = userDetailsImpl.getUser().getId();
		
		favoriteRepository.deleteByUserIdAndShopId(userId, id);
	}
}

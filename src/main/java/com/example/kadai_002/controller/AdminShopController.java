package com.example.kadai_002.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kadai_002.entity.Category;
import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.ShopCategory;
import com.example.kadai_002.form.ShopCategoryRegisterForm;
import com.example.kadai_002.form.ShopEditForm;
import com.example.kadai_002.form.ShopRegisterForm;
import com.example.kadai_002.repository.CategoryRepository;
import com.example.kadai_002.repository.FavoriteRepository;
import com.example.kadai_002.repository.ReviewRepository;
import com.example.kadai_002.repository.ShopCategoryRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.service.ShopService;

@Controller
@RequestMapping("/admin/shops")
public class AdminShopController {
	private final ShopRepository shopRepository;
	private final ShopCategoryRepository shopCategoryRepository;
	private final ShopService shopService;
	private final CategoryRepository categoryRepository;
	private final ReviewRepository reviewRepository;
	private final FavoriteRepository favoriteRepository;
	
	public AdminShopController(ShopRepository shopRepository, ShopCategoryRepository shopCategoryRepository, ShopService shopService, CategoryRepository categoryRepository, ReviewRepository reviewRepository, FavoriteRepository favoriteRepository) {
		this.shopRepository = shopRepository;
		this.shopCategoryRepository = shopCategoryRepository;
		this.shopService = shopService;
		this.categoryRepository = categoryRepository;
		this.reviewRepository = reviewRepository;
		this.favoriteRepository = favoriteRepository;
	}
	
	@GetMapping
	public String index(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, @RequestParam(name = "keyword", required = false) String keyword) {
		Page<Shop> shopPage;
		
		if (keyword != null && !keyword.isEmpty()) {
			shopPage = shopRepository.findByNameLike("%" + keyword + "%", pageable);
		} else {
			shopPage = shopRepository.findAll(pageable);
		}
		
		model.addAttribute("shopPage", shopPage);
		model.addAttribute("keyword", keyword);
		
		return "admin/shops/index";
	}
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model) {
		Shop shop = shopRepository.getReferenceById(id);
		List<ShopCategory> shopCategories = shopCategoryRepository.findByShopId(id);
		List<Category> categoryList = categoryRepository.findAll();
		
		model.addAttribute("shop", shop);
		model.addAttribute("shopCategories", shopCategories);
		model.addAttribute("shopCategoryRegisterForm", new ShopCategoryRegisterForm());
		model.addAttribute("categoryList", categoryList);
		
		return "admin/shops/show";
	}
	
	@GetMapping("/register")
	public String register(Model model) {
		List<Category> categoryList = categoryRepository.findAll();
		
		model.addAttribute("categoryList", categoryList);
		model.addAttribute("shopRegisterForm", new ShopRegisterForm());
		return "admin/shops/register";
	}
	
	 @PostMapping("/create")
	    public String create(@ModelAttribute @Validated ShopRegisterForm shopRegisterForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {        
	        if (bindingResult.hasErrors()) {
	            return "admin/shops/register";
	        }
	        
	        shopService.create(shopRegisterForm);
	        redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");    
	        
	        return "redirect:/admin/shops";
	    } 
	 
	 @GetMapping("/{id}/edit")
	    public String edit(@PathVariable(name = "id") Integer id, Model model) {
	        Shop shop = shopRepository.getReferenceById(id);
	        String imageName = shop.getImageName();
	        ShopEditForm shopEditForm = new ShopEditForm(shop.getId(), shop.getName(), null, shop.getDescription(), shop.getPrice(), shop.getCapacity(), shop.getAddress(), shop.getPhoneNumber());
	        
	        model.addAttribute("imageName", imageName);
	        model.addAttribute("shopEditForm", shopEditForm);
	        
	        return "admin/shops/edit";
	    }
	 
	 @PostMapping("/{id}/update")
	    public String update(@ModelAttribute @Validated ShopEditForm shopEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		 if (bindingResult.hasErrors()) {
	            return "admin/shops/edit";
	        }

	        shopService.update(shopEditForm);
	        redirectAttributes.addFlashAttribute("successMessage", "店舗情報を編集しました。");

	        return "redirect:/admin/shops";
	    }
	 
	 @Transactional
	 @PostMapping("/{id}/delete")
	    public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		 	reviewRepository.deleteByShopId(id);
		 	favoriteRepository.deleteByShopId(id);
		 	shopCategoryRepository.deleteByShopId(id);
		 	shopRepository.deleteById(id);
	        
	        redirectAttributes.addFlashAttribute("successMessage", "店舗情報を削除しました。");
	        
	        return "redirect:/admin/shops";
	    }
}

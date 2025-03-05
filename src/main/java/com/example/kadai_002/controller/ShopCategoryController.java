package com.example.kadai_002.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
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

import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.ShopCategory;
import com.example.kadai_002.form.ShopCategoryRegisterForm;
import com.example.kadai_002.repository.ShopCategoryRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.service.ShopService;

@Controller
@RequestMapping("/admin/shopcategories")
public class ShopCategoryController {
	private final ShopRepository shopRepository;
	private final ShopCategoryRepository shopCategoryRepository;
	private final ShopService shopService;
	
	public ShopCategoryController(ShopRepository shopRepository, ShopCategoryRepository shopCategoryRepository, ShopService shopService) {
		this.shopRepository = shopRepository;
		this.shopCategoryRepository = shopCategoryRepository;
		this.shopService = shopService;
	}

	@PostMapping("/{id}/create")
    public String create(@PathVariable(name = "id") Integer id, @ModelAttribute @Validated ShopCategoryRegisterForm shopCategoryRegisterForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {        
        if (bindingResult.hasErrors()) {
        	Shop shop = shopRepository.getReferenceById(id);
    		List<ShopCategory> shopCategories = shopCategoryRepository.findByShopId(id);
    		
    		model.addAttribute("shop", shop);
    		model.addAttribute("shopCategories", shopCategories);
            return "admin/shops/show"; // エラーがある場合は店舗詳細ページを再表示
        }
        
        //カテゴリが登録済みでないかチェックする
        ShopCategory checkCategory = shopCategoryRepository.findByShopIdAndCategoryId(id, shopCategoryRegisterForm.getCategoryId());
        if (checkCategory != null) {
        	String categoryName = checkCategory.getCategory().getName();
        	redirectAttributes.addFlashAttribute("errorMessage", "カテゴリ：" + categoryName + "は登録済みです。"); 
            
            return "redirect:/admin/shops/{id}";
        }
        
        shopService.addCategory(id, shopCategoryRegisterForm);
        redirectAttributes.addFlashAttribute("successMessage", "カテゴリを追加しました。"); 
        
        return "redirect:/admin/shops/{id}";
    }
	
	@GetMapping("/{id}/remove")
	public String remove(@PathVariable(name = "id") Integer id, Model model) {
		List<ShopCategory> shopCategories = shopCategoryRepository.findByShopId(id);

        model.addAttribute("shopCategories", shopCategories);
		
		return "admin/shops/remove";
	}
	
	@PostMapping("/{id}/remove")
	public String delete(@RequestParam("deleteSC") List<Integer> deleteSC, RedirectAttributes redirectAttributes) {
		try {
			for (Integer id : deleteSC) {
				shopCategoryRepository.deleteById(id); //（複数データが選択される場合もあるため）繰り返し表示でidの一致するshopcategoryエンティティのデータを削除する。
			}
			redirectAttributes.addFlashAttribute("successMessage", "カテゴリを削除しました。"); 
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "カテゴリ削除中にエラーが発生しました。"); 
		}
		
		return "redirect:/admin/shops/{id}";
	}
}

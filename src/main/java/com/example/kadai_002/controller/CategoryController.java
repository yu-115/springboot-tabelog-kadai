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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kadai_002.entity.Category;
import com.example.kadai_002.form.CategoryEditForm;
import com.example.kadai_002.form.CategoryRegisterForm;
import com.example.kadai_002.repository.CategoryRepository;
import com.example.kadai_002.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
	private final CategoryRepository categoryRepository;
	private final CategoryService categoryService;
	
	public CategoryController(CategoryRepository categoryRepository, CategoryService categoryService) {
		this.categoryRepository = categoryRepository;
		this.categoryService = categoryService;
	}

	@GetMapping
	public String index(Model model) {
		List<Category> categories = categoryRepository.findAll();
		
		model.addAttribute("categories", categories);
		model.addAttribute("categoryRegisterForm", new CategoryRegisterForm());
		
		return "admin/categories/index";
	}
	
	@PostMapping("/create")
    public String create(@ModelAttribute @Validated CategoryRegisterForm categoryRegisterForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {        
        if (bindingResult.hasErrors()) {
        	List<Category> categories = categoryRepository.findAll();
        	model.addAttribute("categories", categories);
            return "admin/categories/index"; // エラーがある場合はカテゴリ管理ページを再表示
        }
        
        categoryService.create(categoryRegisterForm);
        redirectAttributes.addFlashAttribute("successMessage", "カテゴリを登録しました。"); 
        
        return "redirect:/admin/categories";
    } 
	
	@GetMapping("/{id}/edit")
    public String edit(@PathVariable(name = "id") Integer id, Model model) {
        Category category = categoryRepository.getReferenceById(id);
        if (category == null) {
            // カテゴリが存在しない場合のエラーハンドリング
            return "redirect:/admin/categories";
        }

        CategoryEditForm categoryEditForm = new CategoryEditForm(category.getId(), category.getName());
        
        model.addAttribute("categoryEditForm", categoryEditForm);
        
        return "admin/categories/edit";
    }
 
	@PostMapping("/{id}/update")
    public String update(@PathVariable(name = "id") Integer id, @ModelAttribute @Validated CategoryEditForm categoryEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
	 if (bindingResult.hasErrors()) {
            return "admin/categories/edit";
        }
        
        categoryService.update(categoryEditForm);
        redirectAttributes.addFlashAttribute("successMessage", "カテゴリ名を編集しました。");    
        
        return "redirect:/admin/categories";
    }
 
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
	 	categoryService.deleteCategoryAndShopCategory(id);
     
	 	redirectAttributes.addFlashAttribute("successMessage", "カテゴリを削除しました。");    
     
	 	return "redirect:/admin/categories";
 }
}

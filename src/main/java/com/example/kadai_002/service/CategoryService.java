package com.example.kadai_002.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kadai_002.entity.Category;
import com.example.kadai_002.form.CategoryEditForm;
import com.example.kadai_002.form.CategoryRegisterForm;
import com.example.kadai_002.repository.CategoryRepository;
import com.example.kadai_002.repository.ShopCategoryRepository;

@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final ShopCategoryRepository shopCategoryRepository;
	
	public CategoryService(CategoryRepository categoryRepository, ShopCategoryRepository shopCategoryRepository) {
		this.categoryRepository = categoryRepository;
		this.shopCategoryRepository = shopCategoryRepository;
	}
	
	@Transactional
	   public void create(CategoryRegisterForm categoryRegisterForm) {
	       Category category = new Category();
	       
	       category.setName(categoryRegisterForm.getName());

	       categoryRepository.save(category);
	   }
	
	@Transactional
	   public void update(CategoryEditForm categoryEditForm) {
	       Category category = categoryRepository.getReferenceById(categoryEditForm.getId());
	       
	       category.setName(categoryEditForm.getName());

	       categoryRepository.save(category);
	   }
	
	@Transactional
	public void deleteCategoryAndShopCategory(Integer categoryId) {
		//categoryテーブルの削除を行う際、shop_categoriesテーブルの関連データも一緒に削除する
		shopCategoryRepository.deleteByCategoryId(categoryId);
		categoryRepository.deleteById(categoryId);
	}
}

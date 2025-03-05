package com.example.kadai_002.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.ShopCategory;
import com.example.kadai_002.form.ShopCategoryRegisterForm;
import com.example.kadai_002.form.ShopEditForm;
import com.example.kadai_002.form.ShopRegisterForm;
import com.example.kadai_002.repository.ShopCategoryRepository;
import com.example.kadai_002.repository.ShopRepository;

@Service
public class ShopService {
	private final ShopRepository shopRepository;
	private final ShopCategoryRepository shopCategoryRepository;
	
	public ShopService(ShopRepository shopRepository, ShopCategoryRepository shopCategoryRepository) {
		this.shopRepository = shopRepository;
		this.shopCategoryRepository = shopCategoryRepository;
	}
	
	 @Transactional
	   public void create(ShopRegisterForm shopRegisterForm) {
	       Shop shop = new Shop();
	       MultipartFile imageFile = shopRegisterForm.getImageFile();
	       
	       if (!imageFile.isEmpty()) {
	           String imageName = imageFile.getOriginalFilename(); 
	           String hashedImageName = generateNewFileName(imageName);
	           Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
	           copyImageFile(imageFile, filePath);
	           shop.setImageName(hashedImageName);
	       }
	       
	       shop.setName(shopRegisterForm.getName());
	       shop.setDescription(shopRegisterForm.getDescription());
	       shop.setPrice(shopRegisterForm.getPrice());
	       shop.setCapacity(shopRegisterForm.getCapacity());
	       shop.setAddress(shopRegisterForm.getAddress());
	       shop.setPhoneNumber(shopRegisterForm.getPhoneNumber());
	       
	       shopRepository.save(shop); //先にshopを生成する
	       
	       // CategoryIdの値が空でない場合のみshopCategoryを保存
	       if (shopRegisterForm.getCategoryId() != null) {
		       ShopCategory shopCategory = new ShopCategory();
		       
	    	   shopCategory.setShopId(shop.getId()); //先に生成したshopのIDを使用する
	    	   shopCategory.setCategoryId(shopRegisterForm.getCategoryId());
	    	   
	    	   shopCategoryRepository.save(shopCategory);
	       }
	   }
	 
	 @Transactional
	   public void addCategory(Integer id, ShopCategoryRegisterForm shopCategoryRegisterForm) {
		 	ShopCategory shopCategory = new ShopCategory();
	       
		 	shopCategory.setShopId(id);
		 	shopCategory.setCategoryId(shopCategoryRegisterForm.getCategoryId());
  	   
  	   	 	shopCategoryRepository.save(shopCategory);
	   }
	 
	 @Transactional
	   public void update(ShopEditForm shopEditForm) {
	       Shop shop = shopRepository.getReferenceById(shopEditForm.getId());
	       MultipartFile imageFile = shopEditForm.getImageFile();
	       
	       if (!imageFile.isEmpty()) {
	           String imageName = imageFile.getOriginalFilename(); 
	           String hashedImageName = generateNewFileName(imageName);
	           Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
	           copyImageFile(imageFile, filePath);
	           shop.setImageName(hashedImageName);
	       }
	       
	       shop.setName(shopEditForm.getName());
	       shop.setDescription(shopEditForm.getDescription());
	       shop.setPrice(shopEditForm.getPrice());
	       shop.setCapacity(shopEditForm.getCapacity());
	       shop.setAddress(shopEditForm.getAddress());
	       shop.setPhoneNumber(shopEditForm.getPhoneNumber());
	                   
	       shopRepository.save(shop);
	   }

	   public List<Shop> getTop10ShopsByAverageScore() {
	        List<Object[]> results = shopRepository.findTop10ByAverageScore();
	        return results.stream().map(result -> (Shop) result[0]).collect(Collectors.toList());
	    }

	   // UUIDを使って生成したファイル名を返す
	   public String generateNewFileName(String fileName) {
	       String[] fileNames = fileName.split("\\.");
	       for (int i = 0; i < fileNames.length - 1; i++) {
	           fileNames[i] = UUID.randomUUID().toString();
	       }
	       String hashedFileName = String.join(".", fileNames);
	       return hashedFileName;
	   }
	   
	   // 画像ファイルを指定したファイルにコピーする
	   public void copyImageFile(MultipartFile imageFile, Path filePath) {
	       try {
	           Files.copy(imageFile.getInputStream(), filePath);
	       } catch (IOException e) {
	           e.printStackTrace();
	       }
	   } 
}

package com.example.kadai_002.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopCategoryRegisterForm {
	@NotNull(message = "カテゴリを選択してください。")
	private Integer categoryId;
}

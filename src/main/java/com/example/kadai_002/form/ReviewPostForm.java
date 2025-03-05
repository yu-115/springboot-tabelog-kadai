package com.example.kadai_002.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewPostForm {
	@NotBlank(message = "評価点数を選択してください。")
	private String score;
	
	@NotBlank(message = "コメントを入力してください。")
	private String reviewComment;

}

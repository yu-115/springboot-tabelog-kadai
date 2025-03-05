package com.example.kadai_002.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.kadai_002.entity.User;
import com.example.kadai_002.repository.UserRepository;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
	private final UserRepository userRepository;
	
	public AdminUserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping
	public String index(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, @RequestParam(name = "keyword", required = false) String keyword) {
		Page<User> userPage;
		
		if (keyword != null && !keyword.isEmpty()) {
			userPage = userRepository.findByEmailLike("%" + keyword + "%", pageable);
		} else {
			userPage = userRepository.findAll(pageable);
		}
		
		long totalUsers = userRepository.count();
		long memberUsers = userRepository.countByRoleId(1);
		long paidUsers = userRepository.countByRoleId(2);
		
		model.addAttribute("userPage", userPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("totalUsers", totalUsers);
		model.addAttribute("memberUsers", memberUsers);
		model.addAttribute("paidUsers", paidUsers);
		
		return "admin/users/index";
	}
}

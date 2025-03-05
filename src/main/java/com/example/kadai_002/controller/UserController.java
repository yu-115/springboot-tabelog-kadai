package com.example.kadai_002.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kadai_002.entity.User;
import com.example.kadai_002.entity.UsersPaid;
import com.example.kadai_002.form.UserEditForm;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.repository.UsersPaidRepository;
import com.example.kadai_002.security.UserDetailsImpl;
import com.example.kadai_002.service.StripeService;
import com.example.kadai_002.service.UserService;
import com.stripe.exception.StripeException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UserController {
	private final UserRepository userRepository;
	private final UserService userService;
	private final UsersPaidRepository usersPaidRepository;
	private final StripeService stripeService;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	public UserController(UserRepository userRepository, UserService userService, UsersPaidRepository usersPaidRepository, StripeService stripeService) {
		this.userRepository = userRepository;
		this.userService = userService;
		this.usersPaidRepository = usersPaidRepository;
		this.stripeService = stripeService;
	}
	
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		
		model.addAttribute("user", user);
		
		return "users/index";
	}
	
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		UserEditForm userEditForm = new UserEditForm(user.getId(), user.getName(), user.getFurigana(), user.getPhoneNumber(), user.getEmail());

		model.addAttribute("userEditForm", userEditForm);
		
		return "users/edit";
	}
	
	@PostMapping("/update")
	public String update(@ModelAttribute @Validated UserEditForm userEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		//メールアドレスが変更されており、かつ登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
		if (userService.isEmailChanged(userEditForm) && userService.isEmailRegistered(userEditForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済のメールアドレスです。");
			bindingResult.addError(fieldError);
		}
		
		if (bindingResult.hasErrors()) {
			return "users/edit";
		}
		
		userService.update(userEditForm);
		
		// 認証情報の更新
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(user.getEmail());
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(updatedUserDetails, updatedUserDetails.getPassword(), updatedUserDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
			    
		redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");
		
		return "redirect:/users";
	}
	
	@GetMapping("/paid")
	public String paid(HttpServletRequest httpServletRequest, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		String sessionId = stripeService.createStripeSession(httpServletRequest, user.getId());
		
		model.addAttribute("sessionId", sessionId);
		return "users/upgrade";
	}
	
	@GetMapping("/update-card")
	public String showUpdateCardForm(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model, HttpSession session) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		Integer userId = user.getId();
		userService.saveUserIdToSession(session, userId);
		
		model.addAttribute("userId", userId);
		
	    return "users/update-card";
	}
	
	@GetMapping("/cancellation")
	public String cancellation(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) throws StripeException {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		Integer userId = user.getId();
		UsersPaid usersPaid = usersPaidRepository.findByUserId(userId);
		
		String paymentMethodId = usersPaid.getPaymentId();
		String customerId = usersPaid.getCustomerId();
		String subscriptionId = stripeService.getActiveSubscriptionIdByCustomerId(customerId);
		
		
		model.addAttribute("paymentMethodId", paymentMethodId);
		model.addAttribute("subscriptionId", subscriptionId);
		
		return "users/downgrade";
	}
}

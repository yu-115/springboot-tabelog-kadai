package com.example.kadai_002.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kadai_002.entity.User;
import com.example.kadai_002.entity.VerificationToken;
import com.example.kadai_002.event.ResetEventPublisher;
import com.example.kadai_002.event.SignupEventPublisher;
import com.example.kadai_002.form.PasswordResetForm;
import com.example.kadai_002.form.ResetForm;
import com.example.kadai_002.form.SignupForm;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.service.UserService;
import com.example.kadai_002.service.VerificationTokenService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {
	private final UserService userService;
	private final UserRepository userRepository;
	private final SignupEventPublisher signupEventPublisher;
	private final ResetEventPublisher resetEventPublisher;
	private final VerificationTokenService verificationTokenService;
	
	public AuthController(UserService userService, UserRepository userRepository, SignupEventPublisher signupEventPublisher, ResetEventPublisher resetEventPublisher, VerificationTokenService verificationTokenService) {
		this.userService = userService;
		this.userRepository = userRepository;
		this.signupEventPublisher = signupEventPublisher;
		this.resetEventPublisher = resetEventPublisher;
		this.verificationTokenService = verificationTokenService;
	}
	
	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("signupForm", new SignupForm());
		return "auth/signup";
	}
	
	@PostMapping("/signup")
    public String signup(@ModelAttribute @Validated SignupForm signupForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {      
        // メールアドレスが登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
        if (userService.isEmailRegistered(signupForm.getEmail())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
            bindingResult.addError(fieldError);
        }    
        
        // パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResultオブジェクトにエラー内容を追加する
        if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
            bindingResult.addError(fieldError);
        }        
        
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }
        
        User createdUser = userService.create(signupForm);
        String requestUrl = new String(httpServletRequest.getRequestURL());
        signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
        redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。"); 

        return "redirect:/";
    }
	
	@GetMapping("/signup/verify")
    public String verify(@RequestParam(name = "token") String token, Model model) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        
        if (verificationToken != null) {
            User user = verificationToken.getUser();  
            userService.enableUser(user);
            String successMessage = "会員登録が完了しました。";
            model.addAttribute("successMessage", successMessage);            
        } else {
            String errorMessage = "トークンが無効です。";
            model.addAttribute("errorMessage", errorMessage);
        }
        
        return "auth/verify";         
    }
	
	@GetMapping("/reset")
	public String reset(Model model) {
		model.addAttribute("resetForm", new ResetForm());
		return "auth/reset";
	}
	
	@PostMapping("/reset")
    public String reset(@ModelAttribute @Validated ResetForm resetForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {      
        // 登録済みのメールアドレスでなければ、BindingResultオブジェクトにエラー内容を追加する
        if (userService.isSameEmail(resetForm.getEmail())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "登録されていないメールアドレスです。");
            bindingResult.addError(fieldError);                       
        }
        
        if (bindingResult.hasErrors()) {
            return "auth/reset";
        }
        
        User user = userRepository.findByEmail(resetForm.getEmail());
        String requestUrl = new String(httpServletRequest.getRequestURL());
        resetEventPublisher.publishResetEvent(user, requestUrl);
        redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスにパスワード再設定用のメールを送信しました。メールに記載されているリンクをクリックし、パスワード再設定を完了してください。"); 

        return "redirect:/";
    }
	
	@GetMapping("/reset/password")
    public String resetverify(@RequestParam(name = "token") String token, Model model) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        
        if (verificationToken != null) {
            User user = verificationToken.getUser();
            PasswordResetForm passwordResetForm = new PasswordResetForm(user.getId(), null, null);
            model.addAttribute("passwordResetForm", passwordResetForm);
        } else {
            String errorMessage = "トークンが無効です。";
            model.addAttribute("errorMessage", errorMessage);
        }
        
        return "auth/password";         
    }
	
	@PostMapping("/reset/password/{id}")
	public String passwordReset(@ModelAttribute @Validated PasswordResetForm passwordResetForm, BindingResult bindingResult, @PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
		// パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResultオブジェクトにエラー内容を追加する
        if (!userService.isSamePassword(passwordResetForm.getPassword(), passwordResetForm.getPasswordConfirmation())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
            bindingResult.addError(fieldError);
        }  
        
		if (bindingResult.hasErrors()) {
			model.addAttribute("passwordResetForm", passwordResetForm);
            return "auth/password";
        }

        userService.passwordReset(passwordResetForm);
        redirectAttributes.addFlashAttribute("successMessage", "パスワードを再設定しました。");

        return "redirect:/";
	}
	
}

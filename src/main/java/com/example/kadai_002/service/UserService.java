package com.example.kadai_002.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kadai_002.entity.Role;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.form.PasswordResetForm;
import com.example.kadai_002.form.SignupForm;
import com.example.kadai_002.form.UserEditForm;
import com.example.kadai_002.repository.RoleRepository;
import com.example.kadai_002.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService {
	private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;        
        this.passwordEncoder = passwordEncoder;
    }    
    
    @Transactional
    public User create(SignupForm signupForm) {
        User user = new User();
        Role role = roleRepository.findByName("ROLE_MEMBER");
        
        user.setName(signupForm.getName());
        user.setFurigana(signupForm.getFurigana());
        user.setPhoneNumber(signupForm.getPhoneNumber());
        user.setEmail(signupForm.getEmail());
        user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
        user.setRole(role);
        user.setEnabled(false);        
        
        return userRepository.save(user);
    } 
    
    @Transactional
    public void update(UserEditForm userEditForm) {
        User user = userRepository.getReferenceById(userEditForm.getId());
        
        user.setName(userEditForm.getName());
        user.setFurigana(userEditForm.getFurigana());
        user.setPhoneNumber(userEditForm.getPhoneNumber());
        user.setEmail(userEditForm.getEmail());      
        
        userRepository.save(user);
    }  
    
    @Transactional
    public void paidUpgrade(Map<String, String> paymentIntentObject) {
    	Integer userId = Integer.valueOf(paymentIntentObject.get("userId"));
    	Optional<User> optionalUser = userRepository.findById(userId);
    	
    	if (optionalUser.isPresent()) {
    		Role role = roleRepository.findByName("ROLE_PAID");
    		User user = optionalUser.get();
    		user.setRole(role);
    		userRepository.save(user);
    	System.out.println("ユーザーID " + userId + " のロールを有料会員に変更しました。");
        } else {
            System.out.println("ユーザーID " + userId + " が見つかりませんでした。");
        }
    }
    
    @Transactional
    public void paidDowngrade(User user) {
    	Role role = roleRepository.findByName("ROLE_MEMBER");
    	
    	user.setRole(role);
    	
    	userRepository.save(user);
    }
    
    //メールアドレスが登録済みかどうかチェックする
    public boolean isEmailRegistered(String email) {
    	User user = userRepository.findByEmail(email);
    	return user != null;
    }
    
    //パスワードとパスワード（確認用）の入力値が一致するかどうかをチェックする
    public boolean isSamePassword(String password, String passwordConfirmation) {
    	return password.equals(passwordConfirmation);
    }
    
    //ユーザーを有効にする
    @Transactional
    public void enableUser(User user) {
    	user.setEnabled(true);
    	userRepository.save(user);
    }
    
    // メールアドレスが変更されたかどうかをチェックする
    public boolean isEmailChanged(UserEditForm userEditForm) {
        User currentUser = userRepository.getReferenceById(userEditForm.getId());
        return !userEditForm.getEmail().equals(currentUser.getEmail());      
    }  
    
    //メール再設定用。登録済みのメールアドレスかどうかチェックする
    public boolean isSameEmail(String email) {
    	User user = userRepository.findByEmail(email);
    	return user == null;
    }
    
    @Transactional
	public void passwordReset(PasswordResetForm passwordResetForm) {
    	User user = userRepository.getReferenceById(passwordResetForm.getId());
	       
	    user.setPassword(passwordEncoder.encode(passwordResetForm.getPassword()));
	       
	    userRepository.save(user);
	}
    
    //セッションにユーザーIDを保存する
    public void saveUserIdToSession(HttpSession session, Integer userId) {
        session.setAttribute("userId", userId);
        System.out.println("セッションにuserIdを保存: " + userId);
    }
    
    public Integer getUserIdFromSession(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId instanceof Integer) {
            System.out.println("セッションから取得したuserId: " + userId);
            return (Integer) userId;
        } else {
            throw new IllegalStateException("セッションにuserIdが存在しません");
        }
    }
}

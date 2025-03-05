package com.example.kadai_002.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.kadai_002.entity.UsersPaid;
import com.example.kadai_002.repository.UsersPaidRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;

import jakarta.servlet.http.HttpSession;

@Service
public class UsersPaidService {
	private final UsersPaidRepository usersPaidRepository;
	private final UserService userService;
	
	public UsersPaidService(UsersPaidRepository usersPaidRepository, UserService userService) {
		this.usersPaidRepository = usersPaidRepository;
		this.userService = userService;
	}
	
	@Transactional
	public UsersPaid create(Integer userId, String paymentId, String customerId) {
		UsersPaid usersPaid = new UsersPaid();
		
		usersPaid.setUserId(userId);
		usersPaid.setPaymentId(paymentId);
		usersPaid.setCustomerId(customerId);
		
		return usersPaidRepository.save(usersPaid);
	}
	
	@Transactional
	public void update(Integer userId, String paymentId, String customerId) {
		UsersPaid usersPaid = usersPaidRepository.findByUserId(userId);
		
		usersPaid.setPaymentId(paymentId);
		usersPaid.setCustomerId(customerId);
		
		usersPaidRepository.save(usersPaid);
	}
	
	@Transactional
	public ResponseEntity<String> saveCard(HttpSession session, @RequestBody Map<String, Object> payload, String customerId) {
		System.out.println("saveCardメソッドが実行されました。");
	    String subscriptionId = (String) payload.get("subscriptionId");
	    String sessionId = (String) payload.get("sessionId");

	    try {
	        // Subscriptionから支払い方法を取得
	        Subscription subscription = Subscription.retrieve(subscriptionId);
	        String paymentMethodId = subscription.getDefaultPaymentMethod();

	        // セッションのメタデータからuserIdを取得
	        Integer userId = Integer.parseInt(Session.retrieve(sessionId).getMetadata().get("userId"));

	        if (usersPaidRepository.findByUserId(userId) == null) {
	            create(userId, paymentMethodId, customerId);
	            System.out.println("データベース：users_paidテーブルに新規登録しました。");
	        } else {
	            update(userId, paymentMethodId, customerId);
	            System.out.println("データベース：users_paidテーブルを更新しました。");
	        }

	        return ResponseEntity.ok("カード情報を保存しました");
	    } catch (StripeException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("カード情報の保存に失敗しました");
	    }
	}
	
	@Transactional
	public Map<String, String> getCustomerAndPaymentMethodIds(Integer userId) {
	    UsersPaid usersPaid = usersPaidRepository.findByUserId(userId);
	    Map<String, String> ids = new HashMap<>();
	    ids.put("customerId", usersPaid.getCustomerId());
	    ids.put("paymentMethodId", usersPaid.getPaymentId());
	    return ids;
	}
	
	//処理分岐の為のフラグ管理メソッド
	private final ConcurrentHashMap<Integer, Boolean> userFlags = new ConcurrentHashMap<>();

    // フラグをセット
    public void setSaveCardDirectlyFlag(Integer userId, boolean flag) {
        userFlags.put(userId, flag);
        System.out.println("setSaveCardDirectlyFlag: userId=" + userId + ", flag=" + flag);
    }

    // フラグを取得
    public boolean getSaveCardDirectlyFlag(Integer userId) {
        Boolean flag = userFlags.get(userId);
        System.out.println("getSaveCardDirectlyFlag: userId=" + userId + ", flag=" + flag);
        return flag != null && flag;
    }

    // フラグをリセット
    public void resetSaveCardDirectlyFlag(Integer userId) {
        userFlags.remove(userId);
        System.out.println("resetSaveCardDirectlyFlag: userId=" + userId);
    }

    @Transactional
    public ResponseEntity<String> saveCardDirectly(@RequestBody Map<String, Object> payload, HttpSession session) {
        System.out.println("saveCardDirectlyメソッドが実行されました。");
        setSaveCardDirectlyFlag(userService.getUserIdFromSession(session), true);
        
        return ResponseEntity.ok("saveCardDirectlyが正常に実行されました");
    }
	
    @Transactional
    public void deleteCard(String paymentMethodId) {
    	try {
	        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
	        paymentMethod.detach();  // カード情報を削除
	        System.out.println("カード情報が削除されました: " + paymentMethodId);
	    } catch (StripeException e) {
	        e.printStackTrace();
	        System.out.println("カード情報の削除に失敗しました: " + paymentMethodId);
	    }
    }
    
    @Transactional
    public void cancelSubscription(String subscriptionId) {
    	try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscription.cancel();
            System.out.println("サブスクリプションがキャンセルされました: " + subscriptionId);
        } catch (StripeException e) {
            e.printStackTrace();
            System.out.println("サブスクリプションのキャンセルに失敗しました: " + subscriptionId);
        }
    }
}

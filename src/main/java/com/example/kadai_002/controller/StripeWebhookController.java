package com.example.kadai_002.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kadai_002.service.StripeService;
import com.example.kadai_002.service.UsersPaidService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class StripeWebhookController {
	private final StripeService stripeService;
	private final UsersPaidService usersPaidService;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public StripeWebhookController(StripeService stripeService, UsersPaidService usersPaidService) {
        this.stripeService = stripeService;
        this.usersPaidService = usersPaidService;
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader, RedirectAttributes redirectAttributes, HttpServletRequest request) {
    	System.out.println("Webhookメソッドが実行されました");
    	Stripe.apiKey = stripeApiKey;
        Event event = null;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            System.out.println("Payload: " + payload);
            System.out.println("Stripe-Signature: " + sigHeader);
            System.out.println("Event Type: " + event.getType());
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if ("checkout.session.completed".equals(event.getType())) {
        	System.out.println("チェックアウトセッション完了イベントが受信されました");
        	
        	// saveCardメソッドの呼び出し
        	StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(() -> new IllegalArgumentException("Sessionオブジェクトが取得できません"));
        	if (stripeObject instanceof Session) {
        		Session stripeSession = (Session) stripeObject;
        		
        		Map<String, String> metadata = stripeSession.getMetadata();
                if (metadata == null || !metadata.containsKey("userId")) {
                    System.out.println("メタデータにuserIdが含まれていません");
                    return ResponseEntity.badRequest().body("Metadata is missing userId");
                }
                
                Integer userId = Integer.parseInt(metadata.get("userId"));
                System.out.println("取得したuserId: " + userId);
        	
                // フラグを確認して処理を分岐
                if (usersPaidService.getSaveCardDirectlyFlag(userId)) {
                	System.out.println("直前にsaveCardDirectlyが実行されたため処理をスキップします。");
                	usersPaidService.resetSaveCardDirectlyFlag(userId); // フラグをリセット
                	return ResponseEntity.ok("Skipped processing due to saveCardDirectly execution.");
                } else {
                	stripeService.processSessionCompleted(event);

            		// subscription、customerIdを取得
            		String subscriptionId = stripeSession.getSubscription();
            		String customerId = stripeSession.getCustomer();
               
            		Map<String, Object> payloadData = new HashMap<>();
            		payloadData.put("subscriptionId", subscriptionId);
            		payloadData.put("sessionId", stripeSession.getId());

            		// HttpSessionを取得
            		HttpSession httpSession = request.getSession();
                
            		System.out.println("Payload: " + payload);
            		System.out.println("Payload Data: " + payloadData);

            		usersPaidService.saveCard(httpSession, payloadData, customerId);
            	}
        	}
        } else {
            System.out.println("イベントタイプが一致しません: " + event.getType());
        }

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
    
    @GetMapping("/create-update-card-session")
    @ResponseBody
    public String createUpdateCardSession(HttpServletRequest request, @RequestParam Integer userId) {
        return stripeService.createUpdateCardSession(request, userId);
    }
    
    @PostMapping("/custom/save-card")
    public ResponseEntity<String> saveCardDirectly(@RequestBody Map<String, Object> payload, HttpSession session) {
        return usersPaidService.saveCardDirectly(payload, session);
    }
    
    @PostMapping("/stripe/cancel-subscription")
    public ResponseEntity<String> cancelSubscription(@RequestBody Map<String, String> request) {
        try {
            stripeService.cancelSubscription(request);
            return ResponseEntity.ok("サブスクリプションとカード情報が正常にキャンセルされました");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("サーバーエラー: " + e.getMessage());
        }
    }
}

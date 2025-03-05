package com.example.kadai_002.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kadai_002.entity.User;
import com.example.kadai_002.entity.UsersPaid;
import com.example.kadai_002.repository.UserRepository;
import com.example.kadai_002.repository.UsersPaidRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.SetupIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.SetupIntentCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import com.stripe.param.checkout.SessionCreateParams.SetupIntentData;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class StripeService {
	@Value("${stripe.api-key}")
    private String stripeApiKey;
	
	@PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey; // 一度だけ設定すれば他の箇所では不要
    }
	
	private final UserRepository userRepository;
	private final UserService userService;
	private final UsersPaidRepository usersPaidRepository;
	private final UsersPaidService usersPaidService;
	
	public StripeService(UserRepository userRepository, UserService userService, UsersPaidRepository usersPaidRepository, UsersPaidService usersPaidService) {
        this.userRepository = userRepository;
		this.userService = userService;
        this.usersPaidRepository = usersPaidRepository;
        this.usersPaidService = usersPaidService;
    }
	
	// セッションを作成し、Stripeに必要な情報を返す
    public String createStripeSession(HttpServletRequest httpServletRequest, Integer userId) {
    	System.out.println("Stripeセッションを作成します: ユーザーID " + userId);
    	Stripe.apiKey = stripeApiKey;
    	String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":" + httpServletRequest.getServerPort();
    	
    	String priceId = "price_1Qv1ZD08B8D45f99PwZp6gWH";
    	
        SessionCreateParams params =
            SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                    	.setPrice(priceId)
                        .setQuantity(1L)
                        .build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .putMetadata("userId", String.valueOf(userId))
                .setSuccessUrl(baseUrl + "/")
                .setCancelUrl(baseUrl + "/")
                .build();
        try {
            Session session = Session.create(params);
            return session.getId();
        } catch (StripeException e) {
            e.printStackTrace();
            return "";
        }
    } 
    
    // セッションからユーザー情報を取得し、UserServiceクラスを介してデータベースを編集する  
    public void processSessionCompleted(Event event) {
        Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
        optionalStripeObject.ifPresentOrElse(stripeObject -> {
        	if (stripeObject instanceof com.stripe.model.checkout.Session) {
                com.stripe.model.checkout.Session session = (com.stripe.model.checkout.Session) stripeObject;
                Map<String, String> metadata = session.getMetadata();

            if (metadata != null && metadata.containsKey("userId")) {
                userService.paidUpgrade(metadata);
                System.out.println("ユーザーの有料会員登録が成功しました。");
            } else {
                System.out.println("メタデータが取得できませんでした。");
            }
            
        	} else {
        		System.out.println("Stripe API Version: " + event.getApiVersion());	System.out.println("予期しないオブジェクトタイプ: " + stripeObject.getClass().getName());
        		}

            System.out.println("Stripe API Version: " + event.getApiVersion());
            System.out.println("stripe-java Version: " + Stripe.VERSION);
        },
        () -> {
            System.out.println("ユーザーの有料会員登録が失敗しました。");
            System.out.println("Stripe API Version: " + event.getApiVersion());
            System.out.println("stripe-java Version: " + Stripe.VERSION);
        });
    }
    
    // カード情報の更新
    public String createSetupIntent(String customerId) throws StripeException {
        SetupIntentCreateParams params = SetupIntentCreateParams.builder()
            .setCustomer(customerId)
            .addPaymentMethodType("card")
            .build();

        SetupIntent setupIntent = SetupIntent.create(params);
        return setupIntent.getClientSecret();
    }
    
    // カード情報編集のためのセッションを作成するメソッド
    public String createUpdateCardSession(HttpServletRequest httpServletRequest, Integer userId) {
        System.out.println("カード情報編集のためのStripeセッションを作成します: ユーザーID " + userId);
        Stripe.apiKey = stripeApiKey;
        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":" + httpServletRequest.getServerPort();

        // ユーザーのカスタマーIDを取得
        UsersPaid usersPaid = usersPaidRepository.findByUserId(userId);
        String customerId = usersPaid.getCustomerId();
        
        SessionCreateParams params =
            SessionCreateParams.builder()
                .setCustomer(customerId)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(Mode.SETUP)
                .putMetadata("userId", userId.toString())
                .setSetupIntentData(SetupIntentData.builder().build())
                .setSuccessUrl(baseUrl + "/")
                .setCancelUrl(baseUrl + "/")
                .build();

        try {
            Session session = Session.create(params);
            return session.getId();
        } catch (StripeException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public List<Subscription> getSubscriptionsByCustomerId(String customerId) throws StripeException {
    	SubscriptionListParams params = SubscriptionListParams.builder()
                .setCustomer(customerId)
                .build();
        
    	List<Subscription> subscriptions = Subscription.list(params).getData();
    	System.out.println("取得したサブスクリプション: " + subscriptions);
    	
    	for (Subscription subscription : subscriptions) {
            System.out.println("サブスクリプションID: " + subscription.getId() + ", ステータス: " + subscription.getStatus());
        }
    	
        return subscriptions;
    }

    public String getActiveSubscriptionIdByCustomerId(String customerId) throws StripeException {
        List<Subscription> subscriptions = getSubscriptionsByCustomerId(customerId);

        // アクティブなサブスクリプションを見つける
        for (Subscription subscription : subscriptions) {
            if ("active".equals(subscription.getStatus())) {
            	System.out.println("アクティブなサブスクリプションが見つかりました: " + subscription.getId());
                return subscription.getId();
            }
        }

        System.out.println("アクティブなサブスクリプションが見つかりませんでした");
        return null; // アクティブなサブスクリプションが見つからない場合
    }
    
    @Transactional
    public void cancelSubscription(Map<String, String> request) throws StripeException {
    	String paymentMethodId = request.get("paymentMethodId");
    	UsersPaid usersPaid = usersPaidRepository.findByPaymentId(paymentMethodId);
    	
    	String customerId = usersPaid.getCustomerId();
        System.out.println("顧客ID: " + customerId);
	    String subscriptionId = getActiveSubscriptionIdByCustomerId(customerId);
	    
	    Optional<User> optionalUser = userRepository.findById(usersPaid.getUserId());
	    User user = optionalUser.orElseThrow(() -> new IllegalArgumentException("User not found"));
	    
	    usersPaidService.deleteCard(paymentMethodId);
        usersPaidService.cancelSubscription(subscriptionId);
        userService.paidDowngrade(user);
        usersPaidRepository.deleteById(usersPaid.getId());
        System.out.println("カード情報が削除され、サブスクリプションが正常にキャンセルされました");
    }
}

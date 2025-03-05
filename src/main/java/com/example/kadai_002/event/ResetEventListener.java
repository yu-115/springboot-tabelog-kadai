package com.example.kadai_002.event;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.kadai_002.entity.User;
import com.example.kadai_002.entity.VerificationToken;
import com.example.kadai_002.repository.VerificationTokenRepository;

@Component
public class ResetEventListener {
	private final VerificationTokenRepository verificationTokenRepository;
    private final JavaMailSender javaMailSender;
    
    public ResetEventListener(VerificationTokenRepository verificationTokenRepository, JavaMailSender mailSender) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.javaMailSender = mailSender;
    }

    @EventListener
    private void onResetEvent(ResetEvent resetEvent) {
    	 User user = resetEvent.getUser();
         VerificationToken verificationToken = verificationTokenRepository.findByUserId(user.getId());

         String recipientAddress = user.getEmail();
         String subject = "メール認証";
         String confirmationUrl = resetEvent.getRequestUrl() + "/password?token=" + verificationToken.getToken();
         String message = "以下のリンクからパスワード再設定を行ってください。";

         SimpleMailMessage mailMessage = new SimpleMailMessage(); 
         mailMessage.setTo(recipientAddress);
         mailMessage.setSubject(subject);
         mailMessage.setText(message + "\n" + confirmationUrl);
         javaMailSender.send(mailMessage);
    }
}

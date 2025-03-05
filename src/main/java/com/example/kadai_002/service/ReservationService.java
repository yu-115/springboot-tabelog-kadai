package com.example.kadai_002.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kadai_002.entity.Reservation;
import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.form.ReservationRegisterForm;
import com.example.kadai_002.repository.ReservationRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.repository.UserRepository;

@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;  
    private final ShopRepository shopRepository;  
    private final UserRepository userRepository;  
    
    public ReservationService(ReservationRepository reservationRepository, ShopRepository shopRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;  
        this.shopRepository = shopRepository;  
        this.userRepository = userRepository;  
    }    
    
    @Transactional
    public void create(ReservationRegisterForm reservationRegisterForm) { 
        Reservation reservation = new Reservation();
        Shop shop = shopRepository.getReferenceById(reservationRegisterForm.getShopId());
        User user = userRepository.getReferenceById(reservationRegisterForm.getUserId());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime checkinDateTime = LocalDateTime.parse(reservationRegisterForm.getCheckinDateTime(), formatter);
                
        reservation.setShop(shop);
        reservation.setUser(user);
        reservation.setCheckIn(checkinDateTime);
        reservation.setNumberOfPeople(reservationRegisterForm.getNumberOfPeople());
        
        reservationRepository.save(reservation);
    }
	
	// 予約人数が定員以下かどうかをチェックする
    public boolean isWithinCapacity(Integer numberOfPeople, Integer capacity) {
        return numberOfPeople <= capacity;
    }
}

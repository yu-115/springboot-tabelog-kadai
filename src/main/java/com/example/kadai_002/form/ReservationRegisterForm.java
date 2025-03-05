package com.example.kadai_002.form;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReservationRegisterForm {
	private Integer shopId;
    
    private Integer userId;    
        
    private String checkinDateTime;
    
    private Integer numberOfPeople;
    
    private Integer price;
}

package com.example.kadai_002.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kadai_002.entity.Reservation;
import com.example.kadai_002.entity.Shop;
import com.example.kadai_002.entity.User;
import com.example.kadai_002.form.ReservationInputForm;
import com.example.kadai_002.form.ReservationRegisterForm;
import com.example.kadai_002.repository.ReservationRepository;
import com.example.kadai_002.repository.ShopRepository;
import com.example.kadai_002.security.UserDetailsImpl;
import com.example.kadai_002.service.ReservationService;

@Controller
public class ReservationController {
	private final ReservationRepository reservationRepository;
	private final ReservationService reservationService;
	private final ShopRepository shopRepository;
    
    public ReservationController(ReservationRepository reservationRepository, ReservationService reservationService, ShopRepository shopRepository) {        
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.shopRepository = shopRepository;
    }    

    @GetMapping("/reservations")
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {
        User user = userDetailsImpl.getUser();
        Page<Reservation> reservationPage = reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        model.addAttribute("reservationPage", reservationPage);         
        
        return "reservations/index";
    }
    
    @GetMapping("/shops/{id}/reservations/input")
    public String input(@PathVariable(name = "id") Integer id,
                        @ModelAttribute @Validated ReservationInputForm reservationInputForm,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model)
    {   
        Shop shop = shopRepository.getReferenceById(id);
        Integer numberOfPeople = reservationInputForm.getNumberOfPeople();   
        Integer capacity = shop.getCapacity();
        
        if (numberOfPeople != null) {
            if (!reservationService.isWithinCapacity(numberOfPeople, capacity)) {
                FieldError fieldError = new FieldError(bindingResult.getObjectName(), "numberOfPeople", "予約人数が定員を超えています。");
                bindingResult.addError(fieldError);
            }
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            model.addAttribute("errorMessage", "予約内容に不備があります。"); 
            return "shops/show";
        }
        
        redirectAttributes.addFlashAttribute("reservationInputForm", reservationInputForm);           
        
        return "redirect:/shops/{id}/reservations/confirm";
    }
    
    @GetMapping("/shops/{id}/reservations/confirm")
    public String confirm(@PathVariable(name = "id") Integer id,
                          @ModelAttribute ReservationInputForm reservationInputForm,
                          @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                          Model model) 
    {        
        Shop shop = shopRepository.getReferenceById(id);
        User user = userDetailsImpl.getUser(); 
        
        ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm(shop.getId(), user.getId(), reservationInputForm.getFromCheckinDateTime(), reservationInputForm.getNumberOfPeople(), reservationInputForm.getNumberOfPeople() * shop.getPrice());
        
        model.addAttribute("shop", shop);
        model.addAttribute("reservationRegisterForm", reservationRegisterForm);
        
        return "reservations/confirm";
    }
    
    @PostMapping("/shops/{id}/reservations/create")
    public String create(@ModelAttribute ReservationRegisterForm reservationRegisterForm) {                
        reservationService.create(reservationRegisterForm);        
        
        return "redirect:/reservations?reserved";
    }
    
    @PostMapping("/shops/{id}/reservations/{reservationId}/delete")
	public String delete(@PathVariable(name = "id") Integer id, @PathVariable(name = "reservationId") Integer reservationId, RedirectAttributes redirectAttributes) {
	 	reservationRepository.deleteById(reservationId);
     
	 	redirectAttributes.addFlashAttribute("successMessage", "予約をキャンセルしました。");    
     
	 	return "redirect:/reservations?reserved";
 }

}

package com.api.ticketshop.Controllers;

import com.api.ticketshop.DTOs.TicketDTO;
import com.api.ticketshop.Models.*;
import com.api.ticketshop.Services.EventService;
import com.api.ticketshop.Services.PurchaseService;
import com.api.ticketshop.Services.SeatService;
import com.api.ticketshop.Services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/v1/tickets")
public class TicketController {

    final TicketService ticketService;
    final SeatService seatService;

    final PurchaseService purchaseService;
    final EventService eventService;


    public TicketController(TicketService ticketService, SeatService seatService, PurchaseService purchaseService, EventService eventService) {
        this.ticketService = ticketService;
        this.seatService = seatService;
        this.purchaseService = purchaseService;
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Object> newTicket(@RequestBody @Valid TicketDTO ticketDTO){
        TicketModel ticketModel = new TicketModel();

        Optional<SeatModel> seatModelOptional = seatService.getSeatModelByEventIdAndId(ticketDTO.getSeat_event_id(), ticketDTO.getSeat_id());
        Optional<PurchaseModel> purchaseModelOptional = purchaseService.getPurchaseModelByUserIdAndUserBillingAddressIdAndId(ticketDTO.getPurchase_user_id(), ticketDTO.getPurchase_user_billing_address_id(), ticketDTO.getPurchase_id());
        Optional<EventModel> eventModelOptional = eventService.getEventByID(ticketDTO.getSeat_event_id());

        if(seatModelOptional.isPresent() && purchaseModelOptional.isPresent()) {
            if(ticketService.getTicketModelBySeatId(ticketDTO.getSeat_id()).isPresent() || eventModelOptional.get().getAvailable_seates() == 0 ){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("This seat is already in use or there are no seats available");
            }
            ticketModel.setSeatId(ticketDTO.getSeat_id());
            ticketModel.setSeatEventId(ticketDTO.getSeat_event_id());
            ticketModel.setPurchaseId(ticketDTO.getPurchase_id());
            ticketModel.setPurchaseUserId(ticketDTO.getPurchase_user_id());
            ticketModel.setPurchaseUserBillingAddressId(ticketDTO.getPurchase_user_billing_address_id());
            ticketModel.setType(ticketDTO.getType());
            ticketModel.setHash(UUID.randomUUID().toString());
            ticketModel.setStatus(ticketDTO.getStatus());

            //Updating the number of available seats
            eventModelOptional.get().setAvailable_seates(eventModelOptional.get().getAvailable_seates()-1);

            return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createNewTicket(ticketModel));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found with the given input");

    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getTicketByUserID(@PathVariable(value = "userId") Integer userId) {

        List<TicketModel> ticketModelList = ticketService.getTicketModelByUserId(userId);

        if(ticketModelList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No ticket found with the given user id");
        }

        return ResponseEntity.status(HttpStatus.OK).body(ticketModelList);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<Object> getTicketByEventID(@PathVariable(value = "eventId") Integer eventId) {

        List<TicketModel> ticketModelList = ticketService.getTicketModelByEventId(eventId);

        if(ticketModelList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No ticket found with the given event id");
        }

        return ResponseEntity.status(HttpStatus.OK).body(ticketModelList);
    }

    @GetMapping("/purchase/{purchaseId}")
    public ResponseEntity<Object> getTicketByPurchaseID(@PathVariable(value = "purchaseId") Integer purchaseId) {

        List<TicketModel> ticketModelList = ticketService.getTicketModelByPurchaseId(purchaseId);

        if(ticketModelList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No ticket found with the given purchase id");
        }

        return ResponseEntity.status(HttpStatus.OK).body(ticketModelList);
    }

    /**
     * Method to delete a specific ticket by its id.
     */
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Object> deleteEventById(@PathVariable(value = "ticketId") Integer ticketId){

        Optional<TicketModel> ticketModelOptional = ticketService.getTicketModelById(ticketId);

        if(ticketModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No ticket found with the given id");
        }

        ticketService.deleteEvent(ticketModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Ticket deleted successfully");
    }

    @PatchMapping("/{ticketId}")
    public TicketModel updateTicketModel(@PathVariable(value = "ticketId") Integer ticketId, @RequestBody Map<Object, Object> fields) {
        return ticketService.updateTicketModel(ticketId, fields);
    }

}

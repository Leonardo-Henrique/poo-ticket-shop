package com.api.ticketshop.Controllers;

import com.api.ticketshop.DTOs.SeatDTO;
import com.api.ticketshop.Models.EventModel;
import com.api.ticketshop.Models.SeatModel;
import com.api.ticketshop.Services.EventService;
import com.api.ticketshop.Services.SeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/v1/seats")
public class SeatController {

    final SeatService seatService;
    final EventService eventService;

    public SeatController(SeatService seatService, EventService eventService) {
        this.seatService = seatService;
        this.eventService = eventService;
    }

    @PostMapping
    /**
     * Method to save seats. The number of seats is given by 'available_seates' attribute of EventModel
     */
    public ResponseEntity<Object> newSeat(@RequestBody @Valid SeatDTO seatDTO){
        SeatModel seatModel = new SeatModel();

        Optional<EventModel> eventModelOptional = eventService.getEventByID(seatDTO.getEventID());

        if(eventModelOptional.isPresent()) {
            EventModel eventModel = eventModelOptional.get();
            seatModel.setName(seatDTO.getName());
            seatModel.setStatus(seatDTO.getStatus());
            seatModel.setPrice(seatDTO.getPrice());
            seatModel.setEvent(eventModel);

            return ResponseEntity.status(HttpStatus.CREATED).body(seatService.createNewSeat(seatModel, eventModel.getAvailable_seates()));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No event found with the given id");

    }

    /**
     * Method to list all seats by its eventID. Each event has its own seat list.
     */
    @GetMapping(value = "/event/{eventID}")
    public List<SeatModel> getAllEventSeats(@PathVariable String eventID){
        return seatService.getAllSeatsByEventID(Integer.parseInt(eventID));
    }

    /**
     * Method to delete a specific seat by its id.
     */
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteSeatByID(@PathVariable String id) {
        seatService.deleteSeatByID(Integer.parseInt(id));
    }

}

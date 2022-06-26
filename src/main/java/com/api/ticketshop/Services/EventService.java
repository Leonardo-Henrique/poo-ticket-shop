package com.api.ticketshop.Services;

import com.api.ticketshop.Models.EventModel;
import com.api.ticketshop.Repositories.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventService {
    final EventRepository eventRepository;

    /**
     * Class constructor that receives the Repository Interface.
     * This constructor is actually a Dependency Injection Point.
     */
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    /**
     * Method to create an Event in the database.
     */
    public EventModel createNewEvent(EventModel eventModel) {
        return eventRepository.save(eventModel);
    }

    /**
     * Method to list all existing events in the database.
     */
    public List<EventModel> listAllEvents() {
        return (List<EventModel>) eventRepository.findAll();
    }

    /**
     * Method to find an event by its id.
     */
    public Optional<EventModel> getEventByID(Integer id) {
        return eventRepository.findById(id);
    }

    /**
     * Method to delete an event.
     */
    @Transactional
    public void deleteEvent(EventModel eventModel) {
        eventRepository.delete(eventModel);
    }

    @Transactional
    public EventModel updateEventModel(Integer eventId, Map<Object, Object> fields){
        EventModel eventModel = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No events found with this id"));

        fields.forEach((key, value) -> {
            if (fields.containsKey("date")){
                DateTimeFormatter parserDate = new DateTimeFormatterBuilder()
                        .appendPattern("dd/MM/yyyy")
                        .parseDefaulting(ChronoField.ERA, 1)
                        .toFormatter()
                        .withResolverStyle(ResolverStyle.STRICT);
                try{
                    LocalDate parsedDate = LocalDate.parse(value.toString(), parserDate);
                    eventModel.setDate(parsedDate);

                } catch (java.time.DateTimeException e){
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Date/time format is not acceptable");
                }

            } else if (fields.containsKey("start_time")) {
                DateTimeFormatter parserTime = DateTimeFormatter.ofPattern("HH:mm");

                try{
                    LocalTime parsedStartTime = LocalTime.parse(value.toString(), parserTime);
                    eventModel.setStart_time(parsedStartTime);
                } catch (java.time.DateTimeException e){
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Time format is not acceptable");
                }

            } else if (fields.containsKey("finish_time")) {
                DateTimeFormatter parserTime = DateTimeFormatter.ofPattern("HH:mm");
                try{
                    LocalTime parsedFinishTime = LocalTime.parse(value.toString(), parserTime);
                    eventModel.setFinish_time(parsedFinishTime);
                } catch (java.time.DateTimeException e){
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Time format is not acceptable");
                }
            }
            else {
                Field field = ReflectionUtils.findField(EventModel.class, key.toString());
                assert field != null;
                field.setAccessible(true);
                ReflectionUtils.setField(field, eventModel, value);
            }
        });

        return eventRepository.save(eventModel);
    }
}

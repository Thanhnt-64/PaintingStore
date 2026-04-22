package com.vn.ManageStore.mapper;

import org.springframework.stereotype.Component;

import com.vn.ManageStore.domain.Event;
import com.vn.ManageStore.dto.EventDTO;

@Component
public class EventMapper {

    /**
     * Map Event entity to EventDTO
     */
    public EventDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }

        return EventDTO.builder()
                .event_id(event.getEventId())
                .title(event.getTitle())
                .start_date(event.getStartDate())
                .end_date(event.getEndDate())
                .image_url(event.getImageUrl())
                .build();
    }
}

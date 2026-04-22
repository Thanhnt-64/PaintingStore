package com.vn.ManageStore.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTO {
    private Integer event_id;
    private String title;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private String image_url;
}

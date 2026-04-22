package com.vn.ManageStore.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.ManageStore.domain.Artwork;
import com.vn.ManageStore.domain.Event;
import com.vn.ManageStore.dto.ArtistDTO;
import com.vn.ManageStore.dto.EventDTO;
import com.vn.ManageStore.dto.FeaturedArtworkDTO;
import com.vn.ManageStore.dto.PaginatedResponse;
import com.vn.ManageStore.repository.ArtworkRepository;
import com.vn.ManageStore.repository.EventRepository;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicController {

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private EventRepository eventRepository;

    /**
     * GET /api/public/artworks/featured
     * Lấy tác phẩm nổi bật cho landing page
     */
    @GetMapping("/artworks/featured")
    public PaginatedResponse<FeaturedArtworkDTO> getFeaturedArtworks() {
        List<Artwork> artworks = artworkRepository.findFeaturedWithDetails();

        List<FeaturedArtworkDTO> data = artworks.stream()
                .map(this::mapToFeaturedArtworkDTO)
                .collect(Collectors.toList());

        return PaginatedResponse.<FeaturedArtworkDTO>builder()
                .data(data)
                .total(data.size())
                .build();
    }

    /**
     * GET /api/public/events
     * Lấy danh sách sự kiện sắp tới (từ hôm nay trở đi)
     */
    @GetMapping("/events")
    public PaginatedResponse<EventDTO> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByStartDateAfterOrderByStartDateAsc(now);

        List<EventDTO> data = events.stream()
                .map(this::mapToEventDTO)
                .collect(Collectors.toList());

        return PaginatedResponse.<EventDTO>builder()
                .data(data)
                .total(data.size())
                .build();
    }

    /**
     * Helper method: map Artwork entity to FeaturedArtworkDTO
     */
    private FeaturedArtworkDTO mapToFeaturedArtworkDTO(Artwork artwork) {
        String primaryImageUrl = null;
        if (artwork.getImages() != null && !artwork.getImages().isEmpty()) {
            primaryImageUrl = artwork.getImages()
                    .stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .findFirst()
                    .map(img -> img.getImageUrl())
                    .orElseGet(() -> artwork.getImages().get(0).getImageUrl());
        }

        ArtistDTO artistDTO = null;
        if (artwork.getArtist() != null) {
            artistDTO = ArtistDTO.builder()
                    .artist_id(artwork.getArtist().getArtistId())
                    .name(artwork.getArtist().getName())
                    .build();
        }

        int viewCount = 0;
        if (artwork.getViewCount() != null) {
            viewCount = artwork.getViewCount();
        }
        return FeaturedArtworkDTO.builder()
                .artwork_id(artwork.getArtworkId())
                .title(artwork.getTitle())
                .artist(artistDTO)
                .price(artwork.getPrice())
                .primary_image(primaryImageUrl)
                .view_count(viewCount)
                .slug(artwork.getSlug())
                .build();
    }

    /**
     * Helper method: map Event entity to EventDTO
     */
    private EventDTO mapToEventDTO(Event event) {
        return EventDTO.builder()
                .event_id(event.getEventId())
                .title(event.getTitle())
                .start_date(event.getStartDate())
                .end_date(event.getEndDate())
                .image_url(event.getImageUrl())
                .build();
    }
}

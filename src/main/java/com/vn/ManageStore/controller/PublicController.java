package com.vn.ManageStore.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vn.ManageStore.domain.Artwork;
import com.vn.ManageStore.domain.Event;
import com.vn.ManageStore.dto.ArtistDTO;
import com.vn.ManageStore.dto.ArtistDetailDTO;
import com.vn.ManageStore.dto.ArtworkBrowseDTO;
import com.vn.ManageStore.dto.ArtworkDetailDTO;
import com.vn.ManageStore.dto.ArtworkImageDTO;
import com.vn.ManageStore.dto.BrowseArtworksResponse;
import com.vn.ManageStore.dto.CategoryDTO;
import com.vn.ManageStore.dto.EventDTO;
import com.vn.ManageStore.dto.FeaturedArtworkDTO;
import com.vn.ManageStore.dto.PaginatedResponse;
import com.vn.ManageStore.dto.PaginationMetaDTO;
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
     * GET /api/public/artworks
     * Browse artworks with pagination, filtering by category/artist, and sorting
     * Query Params:
     *   - page: int (default: 1)
     *   - limit: int (default: 12)
     *   - category_id: int (optional)
     *   - artist_id: int (optional)
     *   - sort: string (popular|newest|price_asc|price_desc, default: popular)
     */
    @GetMapping("/artworks")
    public BrowseArtworksResponse getArtworks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer limit,
            @RequestParam(required = false) Integer category_id,
            @RequestParam(required = false) Integer artist_id,
            @RequestParam(defaultValue = "popular") String sort) {

        // Build sort order
        Sort.Order sortOrder;
        switch (sort) {
            case "newest":
                sortOrder = Sort.Order.desc("createdAt");
                break;
            case "price_asc":
                sortOrder = Sort.Order.asc("price");
                break;
            case "price_desc":
                sortOrder = Sort.Order.desc("price");
                break;
            default:
                // popular
                sortOrder = Sort.Order.desc("viewCount");
        }

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(sortOrder));

        Page<Artwork> artworksPage;
        if (category_id != null) {
            artworksPage = artworkRepository.findByCategoryIdWithDetails(category_id, pageable);
        } else if (artist_id != null) {
            artworksPage = artworkRepository.findByArtistIdWithDetails(artist_id, pageable);
        } else {
            artworksPage = artworkRepository.findAllPublishedWithDetails(pageable);
        }

        List<ArtworkBrowseDTO> data = artworksPage.getContent()
                .stream()
                .map(this::mapToArtworkBrowseDTO)
                .collect(Collectors.toList());

        PaginationMetaDTO pagination = PaginationMetaDTO.builder()
                .page(page)
                .limit(limit)
                .total((int) artworksPage.getTotalElements())
                .total_pages(artworksPage.getTotalPages())
                .build();

        return BrowseArtworksResponse.builder()
                .data(data)
                .pagination(pagination)
                .build();
    }

    /**
     * GET /api/public/artworks/{artwork_id}
     * Get artwork detail with all related data
     */
    @GetMapping("/artworks/{artwork_id}")
    public ArtworkDetailDTO getArtworkDetail(@PathVariable("artwork_id") Integer artworkId) {
        Artwork artwork = artworkRepository.findByIdWithDetails(artworkId)
                .orElseThrow(() -> new RuntimeException("Artwork not found with id: " + artworkId));

        return mapToArtworkDetailDTO(artwork);
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
     * Helper method: map Artwork entity to ArtworkBrowseDTO
     */
    private ArtworkBrowseDTO mapToArtworkBrowseDTO(Artwork artwork) {
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

        return ArtworkBrowseDTO.builder()
                .artwork_id(artwork.getArtworkId())
                .title(artwork.getTitle())
                .slug(artwork.getSlug())
                .price(artwork.getPrice())
                .primary_image(primaryImageUrl)
                .view_count(viewCount)
                .year_created(artwork.getYearCreated())
                .artist(artistDTO)
                .build();
    }

    /**
     * Helper method: map Artwork entity to ArtworkDetailDTO
     */
    private ArtworkDetailDTO mapToArtworkDetailDTO(Artwork artwork) {
        String primaryImageUrl = null;
        if (artwork.getImages() != null && !artwork.getImages().isEmpty()) {
            primaryImageUrl = artwork.getImages()
                    .stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .findFirst()
                    .map(img -> img.getImageUrl())
                    .orElseGet(() -> artwork.getImages().get(0).getImageUrl());
        }

        ArtistDetailDTO artistDetailDTO = null;
        if (artwork.getArtist() != null) {
            artistDetailDTO = ArtistDetailDTO.builder()
                    .artist_id(artwork.getArtist().getArtistId())
                    .name(artwork.getArtist().getName())
                    .slug(artwork.getArtist().getSlug())
                    .birth_year(artwork.getArtist().getBirthYear())
                    .death_year(artwork.getArtist().getDeathYear())
                    .nationality(artwork.getArtist().getNationality())
                    .biography(artwork.getArtist().getBiography())
                    .image_url(artwork.getArtist().getImageUrl())
                    .build();
        }

        List<ArtworkImageDTO> images = null;
        if (artwork.getImages() != null && !artwork.getImages().isEmpty()) {
            images = artwork.getImages()
                    .stream()
                    .map(img -> ArtworkImageDTO.builder()
                            .image_id(img.getImageId())
                            .image_url(img.getImageUrl())
                            .is_primary(img.getIsPrimary())
                            .build())
                    .collect(Collectors.toList());
        }

        List<CategoryDTO> categories = null;
        if (artwork.getCategories() != null && !artwork.getCategories().isEmpty()) {
            categories = artwork.getCategories()
                    .stream()
                    .map(cat -> CategoryDTO.builder()
                            .category_id(cat.getCategoryId())
                            .name(cat.getName())
                            .slug(cat.getSlug())
                            .build())
                    .collect(Collectors.toList());
        }

        int viewCount = 0;
        if (artwork.getViewCount() != null) {
            viewCount = artwork.getViewCount();
        }

        return ArtworkDetailDTO.builder()
                .artwork_id(artwork.getArtworkId())
                .title(artwork.getTitle())
                .slug(artwork.getSlug())
                .description(artwork.getDescription())
                .price(artwork.getPrice())
                .year_created(artwork.getYearCreated())
                .type(artwork.getType())
                .view_count(viewCount)
                .is_featured(artwork.getIsFeatured())
                .artist(artistDetailDTO)
                .images(images)
                .categories(categories)
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

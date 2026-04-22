package com.vn.ManageStore.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

@Service
public class PublicService {

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private EventRepository eventRepository;

    /**
     * Get featured artworks for landing page
     */
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
     * Get upcoming events
     */
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
     * Browse artworks with pagination, filtering, and sorting
     */
    public BrowseArtworksResponse getArtworks(
            Integer page,
            Integer limit,
            Integer categoryId,
            Integer artistId,
            String sort) {

        // Build sort order
        Sort.Order sortOrder = buildSortOrder(sort);
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(sortOrder));

        // Fetch artworks based on filter criteria
        Page<Artwork> artworksPage = fetchArtworksPage(categoryId, artistId, pageable);

        // Map to DTOs
        List<ArtworkBrowseDTO> data = artworksPage.getContent()
                .stream()
                .map(this::mapToArtworkBrowseDTO)
                .collect(Collectors.toList());

        // Build pagination metadata
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
     * Get artwork detail with all related data
     */
    public ArtworkDetailDTO getArtworkDetail(Integer artworkId) {
        Artwork artwork = artworkRepository.findByIdWithDetails(artworkId)
                .orElseThrow(() -> new RuntimeException("Artwork not found with id: " + artworkId));

        return mapToArtworkDetailDTO(artwork);
    }

    /**
     * Helper: Build sort order based on sort parameter
     */
    private Sort.Order buildSortOrder(String sort) {
        switch (sort) {
            case "newest":
                return Sort.Order.desc("createdAt");
            case "price_asc":
                return Sort.Order.asc("price");
            case "price_desc":
                return Sort.Order.desc("price");
            default:
                // popular
                return Sort.Order.desc("viewCount");
        }
    }

    /**
     * Helper: Fetch artworks page based on filter criteria
     */
    private Page<Artwork> fetchArtworksPage(Integer categoryId, Integer artistId, Pageable pageable) {
        if (categoryId != null) {
            return artworkRepository.findByCategoryIdWithDetails(categoryId, pageable);
        } else if (artistId != null) {
            return artworkRepository.findByArtistIdWithDetails(artistId, pageable);
        } else {
            return artworkRepository.findAllPublishedWithDetails(pageable);
        }
    }

    /**
     * Mapper: Artwork → FeaturedArtworkDTO
     */
    private FeaturedArtworkDTO mapToFeaturedArtworkDTO(Artwork artwork) {
        String primaryImageUrl = getPrimaryImageUrl(artwork);

        ArtistDTO artistDTO = null;
        if (artwork.getArtist() != null) {
            artistDTO = ArtistDTO.builder()
                    .artist_id(artwork.getArtist().getArtistId())
                    .name(artwork.getArtist().getName())
                    .build();
        }

        int viewCount = artwork.getViewCount() != null ? artwork.getViewCount() : 0;

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
     * Mapper: Artwork → ArtworkBrowseDTO
     */
    private ArtworkBrowseDTO mapToArtworkBrowseDTO(Artwork artwork) {
        String primaryImageUrl = getPrimaryImageUrl(artwork);

        ArtistDTO artistDTO = null;
        if (artwork.getArtist() != null) {
            artistDTO = ArtistDTO.builder()
                    .artist_id(artwork.getArtist().getArtistId())
                    .name(artwork.getArtist().getName())
                    .build();
        }

        int viewCount = artwork.getViewCount() != null ? artwork.getViewCount() : 0;

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
     * Mapper: Artwork → ArtworkDetailDTO
     */
    private ArtworkDetailDTO mapToArtworkDetailDTO(Artwork artwork) {
        String primaryImageUrl = getPrimaryImageUrl(artwork);

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

        int viewCount = artwork.getViewCount() != null ? artwork.getViewCount() : 0;

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
     * Mapper: Event → EventDTO
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

    /**
     * Helper: Extract primary image URL from artwork images
     */
    private String getPrimaryImageUrl(Artwork artwork) {
        if (artwork.getImages() == null || artwork.getImages().isEmpty()) {
            return null;
        }

        return artwork.getImages()
                .stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .map(img -> img.getImageUrl())
                .orElseGet(() -> artwork.getImages().get(0).getImageUrl());
    }
}

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
import com.vn.ManageStore.dto.ArtworkBrowseDTO;
import com.vn.ManageStore.dto.ArtworkDetailDTO;
import com.vn.ManageStore.dto.BrowseArtworksResponse;
import com.vn.ManageStore.dto.EventDTO;
import com.vn.ManageStore.dto.FeaturedArtworkDTO;
import com.vn.ManageStore.dto.PaginatedResponse;
import com.vn.ManageStore.dto.PaginationMetaDTO;
import com.vn.ManageStore.mapper.ArtworkMapper;
import com.vn.ManageStore.mapper.EventMapper;
import com.vn.ManageStore.repository.ArtworkRepository;
import com.vn.ManageStore.repository.EventRepository;

@Service
public class PublicService {

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtworkMapper artworkMapper;

    @Autowired
    private EventMapper eventMapper;

    /**
     * Get featured artworks for landing page
     */
    public PaginatedResponse<FeaturedArtworkDTO> getFeaturedArtworks() {
        List<Artwork> artworks = artworkRepository.findFeaturedWithDetails();
        List<FeaturedArtworkDTO> data = artworkMapper.toFeaturedDTOList(artworks);

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
                .map(eventMapper::toDTO)
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
        List<ArtworkBrowseDTO> data = artworkMapper.toBrowseDTOList(artworksPage.getContent());

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

        return artworkMapper.toDetailDTO(artwork);
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
}

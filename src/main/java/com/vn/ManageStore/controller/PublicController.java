package com.vn.ManageStore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vn.ManageStore.dto.ArtworkDetailDTO;
import com.vn.ManageStore.dto.BrowseArtworksResponse;
import com.vn.ManageStore.dto.EventDTO;
import com.vn.ManageStore.dto.FeaturedArtworkDTO;
import com.vn.ManageStore.dto.PaginatedResponse;
import com.vn.ManageStore.service.PublicService;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicController {

    @Autowired
    private PublicService publicService;

    /**
     * GET /api/public/artworks/featured
     * Lấy tác phẩm nổi bật cho landing page
     */
    @GetMapping("/artworks/featured")
    public PaginatedResponse<FeaturedArtworkDTO> getFeaturedArtworks() {
        return publicService.getFeaturedArtworks();
    }

    /**
     * GET /api/public/events
     * Lấy danh sách sự kiện sắp tới (từ hôm nay trở đi)
     */
    @GetMapping("/events")
    public PaginatedResponse<EventDTO> getUpcomingEvents() {
        return publicService.getUpcomingEvents();
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

        return publicService.getArtworks(page, limit, category_id, artist_id, sort);
    }

    /**
     * GET /api/public/artworks/{artwork_id}
     * Get artwork detail with all related data (artist, images, categories)
     */
    @GetMapping("/artworks/{artwork_id}")
    public ArtworkDetailDTO getArtworkDetail(@PathVariable("artwork_id") Integer artworkId) {
        return publicService.getArtworkDetail(artworkId);
    }
}

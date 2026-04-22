package com.vn.ManageStore.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vn.ManageStore.domain.Artwork;
import com.vn.ManageStore.dto.ArtistDTO;
import com.vn.ManageStore.dto.ArtistDetailDTO;
import com.vn.ManageStore.dto.ArtworkBrowseDTO;
import com.vn.ManageStore.dto.ArtworkDetailDTO;
import com.vn.ManageStore.dto.ArtworkImageDTO;
import com.vn.ManageStore.dto.CategoryDTO;
import com.vn.ManageStore.dto.FeaturedArtworkDTO;

@Component
public class ArtworkMapper {

    /**
     * Map Artwork entity to FeaturedArtworkDTO
     * Used for landing page featured section
     */
    public FeaturedArtworkDTO toFeaturedDTO(Artwork artwork) {
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
     * Map Artwork entity to ArtworkBrowseDTO
     * Used for browse/list pages
     */
    public ArtworkBrowseDTO toBrowseDTO(Artwork artwork) {
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
     * Map Artwork entity to ArtworkDetailDTO
     * Used for detail/single page view with all related data
     */
    public ArtworkDetailDTO toDetailDTO(Artwork artwork) {
        String primaryImageUrl = getPrimaryImageUrl(artwork);

        ArtistDetailDTO artistDetailDTO = mapArtistToDetailDTO(artwork.getArtist());
        List<ArtworkImageDTO> images = mapImagesToDTO(artwork.getImages());
        List<CategoryDTO> categories = mapCategoriesToDTO(artwork.getCategories());

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
     * Map list of Artwork to list of ArtworkBrowseDTO
     */
    public List<ArtworkBrowseDTO> toBrowseDTOList(List<Artwork> artworks) {
        return artworks.stream()
                .map(this::toBrowseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map list of Artwork to list of FeaturedArtworkDTO
     */
    public List<FeaturedArtworkDTO> toFeaturedDTOList(List<Artwork> artworks) {
        return artworks.stream()
                .map(this::toFeaturedDTO)
                .collect(Collectors.toList());
    }

    /**
     * Helper: Extract primary image URL from artwork images list
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

    /**
     * Helper: Map Artist entity to ArtistDetailDTO
     */
    private ArtistDetailDTO mapArtistToDetailDTO(com.vn.ManageStore.domain.Artist artist) {
        if (artist == null) {
            return null;
        }

        return ArtistDetailDTO.builder()
                .artist_id(artist.getArtistId())
                .name(artist.getName())
                .slug(artist.getSlug())
                .birth_year(artist.getBirthYear())
                .death_year(artist.getDeathYear())
                .nationality(artist.getNationality())
                .biography(artist.getBiography())
                .image_url(artist.getImageUrl())
                .build();
    }

    /**
     * Helper: Map list of ArtworkImages to DTOs
     */
    private List<ArtworkImageDTO> mapImagesToDTO(List<com.vn.ManageStore.domain.ArtworkImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .map(img -> ArtworkImageDTO.builder()
                        .image_id(img.getImageId())
                        .image_url(img.getImageUrl())
                        .is_primary(img.getIsPrimary())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Helper: Map list of Categories to DTOs
     */
    private List<CategoryDTO> mapCategoriesToDTO(List<com.vn.ManageStore.domain.Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        return categories.stream()
                .map(cat -> CategoryDTO.builder()
                        .category_id(cat.getCategoryId())
                        .name(cat.getName())
                        .slug(cat.getSlug())
                        .build())
                .collect(Collectors.toList());
    }
}

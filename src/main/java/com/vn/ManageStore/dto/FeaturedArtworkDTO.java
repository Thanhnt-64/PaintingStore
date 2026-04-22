package com.vn.ManageStore.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturedArtworkDTO {
    private Integer artwork_id;
    private String title;
    private ArtistDTO artist;
    private BigDecimal price;
    private String primary_image;
    private Integer view_count;
    private String slug;
}

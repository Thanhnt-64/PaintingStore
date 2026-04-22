package com.vn.ManageStore.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkDetailDTO {
    private Integer artwork_id;
    private String title;
    private String slug;
    private String description;
    private BigDecimal price;
    private Integer year_created;
    private String type;
    private Integer view_count;
    private Boolean is_featured;
    private ArtistDetailDTO artist;
    private List<ArtworkImageDTO> images;
    private List<CategoryDTO> categories;
}

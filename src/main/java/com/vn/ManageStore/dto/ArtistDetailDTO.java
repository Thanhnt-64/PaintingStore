package com.vn.ManageStore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistDetailDTO {
    private Integer artist_id;
    private String name;
    private String slug;
    private Integer birth_year;
    private Integer death_year;
    private String nationality;
    private String biography;
    private String image_url;
}

package com.vn.ManageStore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkImageDTO {
    private Integer image_id;
    private String image_url;
    private Boolean is_primary;
}

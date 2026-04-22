package com.vn.ManageStore.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrowseArtworksResponse {
    private List<ArtworkBrowseDTO> data;
    private PaginationMetaDTO pagination;
}

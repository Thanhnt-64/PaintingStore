package com.vn.ManageStore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationMetaDTO {
    private Integer page;
    private Integer limit;
    private Integer total;
    private Integer total_pages;
}

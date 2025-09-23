package com.kunfeng2002.be002.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    @NotBlank(message = "Query không được để trống")
    private String query;

    @NotBlank(message = "Network không được để trống")
    private String network;

    @Min(value = 0, message = "Page phải >= 0")
    private int page = 0;

    @Min(value = 1, message = "Size phải >= 1")
    @Max(value = 100, message = "Size phải <= 100")
    private int size = 20;

    private String sortBy = "timestamp";
    private String sortDirection = "desc";
}

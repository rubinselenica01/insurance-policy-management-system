package com.rubin.insurance.policy_management_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Schema(description = "Paginated list response with content and pagination metadata")
public class PageResponse<T> {

    @Schema(description = "List of items for the current page")
    private final List<T> content;
    private final PaginationMetadata metadata;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.metadata = new  PaginationMetadata(page);
    }

    @JsonProperty("pagination")
    public PaginationMetadata getMetadata() {return this.metadata;}


    @Getter
    @Schema(description = "Pagination metadata for the current page")
    public static class PaginationMetadata {
        @Schema(description = "Current page number (1-based)", example = "1")
        private final int page;
        @Schema(description = "Page size", example = "10")
        private final int size;
        @Schema(description = "Total number of elements across all pages", example = "42")
        private final long totalElements;
        @Schema(description = "Total number of pages", example = "5")
        private final int totalPages;
        @Schema(description = "True if this is the first page")
        private final boolean first;
        @Schema(description = "True if this is the last page")
        private final boolean last;
        @Schema(description = "True if the page has no content")
        private final boolean empty;


        public PaginationMetadata(Page<?> page) {
            this.page = page.getNumber() + 1;
            this.size = page.getSize();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.first = page.isFirst();
            this.last = page.isLast();
            this.empty = page.isEmpty();
        }
    }
}

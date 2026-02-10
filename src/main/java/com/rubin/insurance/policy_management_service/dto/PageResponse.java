package com.rubin.insurance.policy_management_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Paginated list response with content and pagination metadata")
public record PageResponse<T>(

        @Schema(description = "List of items for the current page")
        List<T> content,

        @Schema(description = "Pagination metadata for the current page")
        PaginationMetadata pagination
) {
    public PageResponse(Page<T> page) {
        this(page.getContent(), new PaginationMetadata(page));
    }

    @Schema(description = "Pagination metadata for the current page")
    public record PaginationMetadata(

            @Schema(description = "Current page number (1-based)", example = "1")
            int page,

            @Schema(description = "Page size", example = "10")
            int size,

            @Schema(description = "Total number of elements across all pages", example = "42")
            long totalElements,

            @Schema(description = "Total number of pages", example = "5")
            int totalPages,

            @Schema(description = "True if this is the first page", example = "true")
            boolean first,

            @Schema(description = "True if this is the last page", example = "false")
            boolean last,

            @Schema(description = "True if the page has no content", example = "false")
            boolean empty
    ) {
        public PaginationMetadata(Page<?> page) {
            this(
                    page.getNumber() + 1,
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast(),
                    page.isEmpty()
            );
        }
    }
}

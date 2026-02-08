package com.rubin.insurance.policy_management_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final PaginationMetadata metadata;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.metadata = new  PaginationMetadata(page);
    }

    @JsonProperty("pagination")
    public PaginationMetadata getMetadata() {return this.metadata;}


    @Getter
    public static class PaginationMetadata {
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean first;
        private final boolean last;
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

package com.rubin.insurance.policy_management_service.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


public abstract class PaginationUtils {

    public static final int DEFAULT_PAGE_SIZE = 10;

    public static Pageable createPageable(int page, int elementsSize){
        int pageIndex = Math.max(0, page - 1);
        int pageSize = elementsSize <= 0 ?  DEFAULT_PAGE_SIZE : elementsSize;

        return PageRequest.of(pageIndex, pageSize);
    }
}

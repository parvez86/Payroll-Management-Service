package org.sp.payroll_service.domain.common.dto.response;

import org.springframework.data.domain.Page;

/**
 * Page metadata record for pagination.
 * @param page current page number (0-based)
 * @param size page size
 * @param totalElements total number of elements
 * @param totalPages total number of pages
 * @param hasNext whether there's a next page
 * @param hasPrevious whether there's a previous page
 */
public record PageMetadata(
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
    /**
     * Creates page metadata from Spring Page.
     * @param page Spring page object
     * @return page metadata
     */
    public static PageMetadata from(Page<?> page) {
        return new PageMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}
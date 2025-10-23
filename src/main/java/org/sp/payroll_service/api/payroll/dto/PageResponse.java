package org.sp.payroll_service.api.payroll.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Generic pagination wrapper for API responses.
 * Encapsulates Spring Data Page metadata with content.
 * 
 * Industry practice: Provides consistent pagination structure across all paginated endpoints.
 * Supports sorting, filtering, and deterministic ordering.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    /**
     * The actual content/items for the current page.
     */
    @JsonProperty("content")
    private List<T> content;

    /**
     * Total number of elements across all pages.
     */
    @JsonProperty("totalElements")
    private long totalElements;

    /**
     * Total number of pages.
     */
    @JsonProperty("totalPages")
    private int totalPages;

    /**
     * Current page size (number of items per page).
     */
    @JsonProperty("size")
    private int size;

    /**
     * Current page number (0-indexed).
     */
    @JsonProperty("number")
    private int number;

    /**
     * Number of elements in the current page.
     */
    @JsonProperty("numberOfElements")
    private int numberOfElements;

    /**
     * Whether this is the first page.
     */
    @JsonProperty("first")
    private boolean first;

    /**
     * Whether this is the last page.
     */
    @JsonProperty("last")
    private boolean last;

    /**
     * Whether the page is empty.
     */
    @JsonProperty("empty")
    private boolean empty;

    /**
     * Sort information for the current page.
     */
    @JsonProperty("sort")
    private SortInfo sort;

    /**
     * Pageable information (offset, page number, page size, etc.).
     */
    @JsonProperty("pageable")
    private PageableInfo pageable;

    /**
     * Factory method to create PageResponse from Spring Data Page.
     * Automatically extracts and maps all pagination metadata.
     *
     * @param page Spring Data Page object
     * @param <T>  Type of content
     * @return PageResponse with all metadata populated
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .number(page.getNumber())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .sort(SortInfo.from(page.getSort()))
                .pageable(PageableInfo.from(page.getPageable()))
                .build();
    }

    public static <T> PageResponse<T> from(List<T> itemList, long totalElements, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.max(0, (totalElements + pageSize - 1) / (long) pageSize);

        return PageResponse.<T>builder()
                .content(itemList)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .size(pageSize)
                .number(pageable.getPageNumber())
                .numberOfElements(itemList != null ? itemList.size() : 0)
                .first(pageable.getPageNumber() == 0)
                .last(pageable.getPageNumber() == totalPages - 1)
                .empty(itemList == null || itemList.isEmpty())
                .sort(SortInfo.from(pageable.getSort()))
                .pageable(PageableInfo.from(pageable))
                .build();
    }

    /**
     * Nested DTO for sort information.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SortInfo {
        @JsonProperty("empty")
        private boolean empty;

        @JsonProperty("sorted")
        private boolean sorted;

        @JsonProperty("unsorted")
        private boolean unsorted;

        @JsonProperty("orders")
        private List<OrderInfo> orders;

        public static SortInfo from(Sort sort) {
            if (sort == null) {
                return SortInfo.builder()
                        .empty(true)
                        .sorted(false)
                        .unsorted(true)
                        .build();
            }

            return SortInfo.builder()
                    .empty(sort.isEmpty())
                    .sorted(sort.isSorted())
                    .unsorted(sort.isUnsorted())
                    .orders(sort.stream()
                            .map(OrderInfo::from)
                            .toList())
                    .build();
        }
    }

    /**
     * Nested DTO for individual sort order.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderInfo {
        @JsonProperty("property")
        private String property;

        @JsonProperty("direction")
        private String direction;

        @JsonProperty("ignoreCase")
        private boolean ignoreCase;

        @JsonProperty("nullHandling")
        private String nullHandling;

        public static OrderInfo from(Sort.Order order) {
            return OrderInfo.builder()
                    .property(order.getProperty())
                    .direction(order.getDirection().name())
                    .ignoreCase(order.isIgnoreCase())
                    .nullHandling(order.getNullHandling().name())
                    .build();
        }
    }

    /**
     * Nested DTO for pageable information.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageableInfo {
        @JsonProperty("offset")
        private long offset;

        @JsonProperty("pageNumber")
        private int pageNumber;

        @JsonProperty("pageSize")
        private int pageSize;

        @JsonProperty("paged")
        private boolean paged;

        @JsonProperty("unpaged")
        private boolean unpaged;

        @JsonProperty("sort")
        private SortInfo sort;

        public static PageableInfo from(org.springframework.data.domain.Pageable pageable) {
            if (pageable == null) {
                return PageableInfo.builder()
                        .paged(false)
                        .unpaged(true)
                        .build();
            }

            return PageableInfo.builder()
                    .offset(pageable.getOffset())
                    .pageNumber(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .paged(pageable.isPaged())
                    .unpaged(!pageable.isPaged())
                    .sort(SortInfo.from(pageable.getSort()))
                    .build();
        }
    }
}

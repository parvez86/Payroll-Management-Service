package org.sp.payroll_service.utils;

import lombok.experimental.UtilityClass;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@UtilityClass
public class ObjectMapperUtils {
    private static final ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        // Configure ModelMapper
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
    }

    /**
     * Maps a single source object to a single destination object.
     *
     * @param source The object to map.
     * @param destinationType The class of the destination object.
     * @param <S> The source type.
     * @param <D> The destination type.
     * @return The mapped destination object, or null if the source is null.
     */
    public static <S, D> D map(S source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    /**
     * Maps a list of source objects to a list of destination objects.
     *
     * @param sourceList The list of objects to map.
     * @param destinationType The class of the destination objects.
     * @param <S> The source type.
     * @param <D> The destination type.
     * @return A list of mapped destination objects, or an empty list if the source list is null or empty.
     */
    public static <S, D> List<D> mapAll(List<S> sourceList, Class<D> destinationType) {
        if (sourceList == null || sourceList.isEmpty()) return List.of();
        return sourceList.stream()
                .map(source -> map(source, destinationType))
                .toList();
    }

    /**
     * Converts a Page of source entities into a Page of destination DTOs.
     * Preserves all pagination metadata (total elements, page number, page size).
     *
     * @param sourcePage The Page of source entities (S).
     * @param destinationType The Class of the destination DTO (D).
     * @param <S> The source entity type.
     * @param <D> The destination DTO type.
     * @return A new Page<D> containing the mapped DTOs.
     */
    public static <S, D> Page<D> mapPage(Page<S> sourcePage, Class<D> destinationType) {
        if (sourcePage == null) {
            // Returns an empty Page preserving the Pageable state if available, or just an empty PageImpl
            return new PageImpl<>(List.of(), Pageable.unpaged(), 0);
        }

        List<D> dtoList = mapAll(sourcePage.getContent(), destinationType);

        return new PageImpl<>(
                dtoList,
                sourcePage.getPageable(),
                sourcePage.getTotalElements()
        );
    }

    /**
     * Returns a sublist based on Spring Data Pageable object, used when in-memory pagination is necessary.
     *
     * @param list The full list to sublist.
     * @param pageable Pagination information.
     * @param <T> The list element type.
     * @return The sublist corresponding to the pageable range, or an empty list if range is invalid.
     */
    public static <T> List<T> sublistByPage(List<T> list, Pageable pageable) {
        if (list == null || list.isEmpty()) return List.of();

        int total = list.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        return start < end ? list.subList(start, end) : List.of();
    }

    /**
     * Maps properties from a source object onto an existing destination object.
     *
     * @param source The source object.
     * @param destination The existing object to be modified.
     */
    public static void updateEntityFromDto(Object source, Object destination) {
        modelMapper.map(source, destination);
    }
}
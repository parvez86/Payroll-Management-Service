package org.sp.payroll_service.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collection manipulation utilities.
 */
public final class CollectionUtils {
    
    private CollectionUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Checks if collection is null or empty.
     * @param collection collection to check
     * @return true if null or empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * Checks if collection has elements.
     * @param collection collection to check
     * @return true if has elements
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
    
    /**
     * Gets first element from collection safely.
     * @param collection source collection
     * @return optional first element
     */
    public static <T> Optional<T> getFirst(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Optional.empty();
        }
        return Optional.of(collection.iterator().next());
    }
    
    /**
     * Gets last element from list safely.
     * @param list source list
     * @return optional last element
     */
    public static <T> Optional<T> getLast(List<T> list) {
        if (isEmpty(list)) {
            return Optional.empty();
        }
        return Optional.of(list.get(list.size() - 1));
    }
    
    /**
     * Partitions collection into chunks of specified size.
     * @param collection source collection
     * @param chunkSize chunk size
     * @return list of chunks
     */
    public static <T> List<List<T>> partition(Collection<T> collection, int chunkSize) {
        if (isEmpty(collection) || chunkSize <= 0) {
            return List.of();
        }
        
        List<T> list = new ArrayList<>(collection);
        List<List<T>> partitions = new ArrayList<>();
        
        for (int i = 0; i < list.size(); i += chunkSize) {
            partitions.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        
        return partitions;
    }
    
    /**
     * Creates frequency map from collection.
     * @param collection source collection
     * @return frequency map
     */
    public static <T> Map<T, Long> frequency(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Map.of();
        }
        
        return collection.stream()
            .collect(Collectors.groupingBy(
                Function.identity(),
                Collectors.counting()
            ));
    }
    
    /**
     * Finds duplicates in collection.
     * @param collection source collection
     * @return set of duplicates
     */
    public static <T> Set<T> findDuplicates(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Set.of();
        }
        
        Set<T> seen = new HashSet<>();
        return collection.stream()
            .filter(item -> !seen.add(item))
            .collect(Collectors.toSet());
    }
    
    /**
     * Safely gets element at index from list.
     * @param list source list
     * @param index target index
     * @return optional element
     */
    public static <T> Optional<T> safeGet(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(index));
    }
}
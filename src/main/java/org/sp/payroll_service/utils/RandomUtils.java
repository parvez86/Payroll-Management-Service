package org.sp.payroll_service.utils;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * Random value generation utilities.
 */
@UtilityClass
public class RandomUtils {
    
    /**
     * Generates a random 8-character string.
     *
     * @return Random string of 8 characters.
     */
    public static String getRandom() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

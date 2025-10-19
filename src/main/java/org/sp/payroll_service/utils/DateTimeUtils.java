package org.sp.payroll_service.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Date and time utility functions.
 */
public final class DateTimeUtils {
    
    private static final ZoneId SYSTEM_ZONE = ZoneId.of("Asia/Dhaka");
    private static final DateTimeFormatter ISO_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
    
    private DateTimeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Gets current system time.
     * @return current instant in system timezone
     */
    public static Instant now() {
        return Instant.now();
    }
    
    /**
     * Gets current system date.
     * @return current local date in system timezone
     */
    public static LocalDate today() {
        return LocalDate.now(SYSTEM_ZONE);
    }
    
    /**
     * Converts instant to system timezone.
     * @param instant instant to convert
     * @return zoned date time in system timezone
     */
    public static ZonedDateTime toSystemZone(Instant instant) {
        return instant.atZone(SYSTEM_ZONE);
    }
    
    /**
     * Gets start of day for given date.
     * @param date target date
     * @return start of day instant
     */
    public static Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(SYSTEM_ZONE).toInstant();
    }
    
    /**
     * Gets end of day for given date.
     * @param date target date
     * @return end of day instant
     */
    public static Instant endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX).atZone(SYSTEM_ZONE).toInstant();
    }
    
    /**
     * Formats instant for display.
     * @param instant instant to format
     * @return formatted date time string
     */
    public static String formatForDisplay(Instant instant) {
        return DISPLAY_DATE_TIME.format(toSystemZone(instant));
    }
    
    /**
     * Checks if date is in current month.
     * @param date date to check
     * @return true if date is in current month
     */
    public static boolean isCurrentMonth(LocalDate date) {
        LocalDate today = today();
        return date.getYear() == today.getYear() && date.getMonth() == today.getMonth();
    }
    
    /**
     * Gets business days between two dates (excluding weekends).
     * @param startDate start date (inclusive)
     * @param endDate end date (exclusive)
     * @return number of business days
     */
    public static long getBusinessDaysBetween(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate)
            .filter(date -> !isWeekend(date))
            .count();
    }
    
    /**
     * Checks if date is weekend.
     * @param date date to check
     * @return true if weekend
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * Gets next payroll date based on current date and payroll day.
     * @param currentDate current date
     * @param payrollDay day of month for payroll (1-31)
     * @return next payroll date
     */
    public static LocalDate getNextPayrollDate(LocalDate currentDate, int payrollDay) {
        LocalDate nextPayroll = currentDate.withDayOfMonth(
            Math.min(payrollDay, currentDate.lengthOfMonth())
        );
        
        if (!nextPayroll.isAfter(currentDate)) {
            nextPayroll = nextPayroll.plusMonths(1)
                .withDayOfMonth(Math.min(payrollDay, nextPayroll.lengthOfMonth()));
        }
        
        return nextPayroll;
    }
}
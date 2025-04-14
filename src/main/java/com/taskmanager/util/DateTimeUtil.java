package com.taskmanager.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Utility class for handling date and time operations.
 */
public class DateTimeUtil {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String TIME_ZONE = "UTC";
    
    static {
        DATE_FORMAT.setTimeZone(java.util.TimeZone.getTimeZone(TIME_ZONE));
        DATE_TIME_FORMAT.setTimeZone(java.util.TimeZone.getTimeZone(TIME_ZONE));
    }
    
    /**
     * Formats a Date object to a date string (yyyy-MM-dd).
     * 
     * @param date The date to format
     * @return Formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }
    
    /**
     * Formats a Date object to a date-time string (yyyy-MM-dd HH:mm:ss).
     * 
     * @param date The date to format
     * @return Formatted date-time string
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (DATE_TIME_FORMAT) {
            return DATE_TIME_FORMAT.format(date);
        }
    }
    
    /**
     * Parses a date string to a Date object.
     * 
     * @param dateStr The date string in yyyy-MM-dd format
     * @return Parsed Date object
     * @throws ParseException If the string cannot be parsed
     */
    public static Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.parse(dateStr);
        }
    }
    
    /**
     * Parses a date-time string to a Date object.
     * 
     * @param dateTimeStr The date-time string in yyyy-MM-dd HH:mm:ss format
     * @return Parsed Date object
     * @throws ParseException If the string cannot be parsed
     */
    public static Date parseDateTime(String dateTimeStr) throws ParseException {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        synchronized (DATE_TIME_FORMAT) {
            return DATE_TIME_FORMAT.parse(dateTimeStr);
        }
    }
    
    /**
     * Gets the current date and time.
     * 
     * @return Current Date object
     */
    public static Date getCurrentDateTime() {
        return new Date();
    }
    
    /**
     * Calculates the difference between two dates in days.
     * 
     * @param start The start date
     * @param end The end date
     * @return The number of days between the dates
     */
    public static long getDaysBetween(Date start, Date end) {
        if (start == null || end == null) {
            return 0;
        }
        
        LocalDateTime startDateTime = LocalDateTime.ofInstant(start.toInstant(), ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(end.toInstant(), ZoneId.systemDefault());
        
        return Duration.between(startDateTime, endDateTime).toDays();
    }
    
    /**
     * Formats an elapsed time in friendly text format.
     * 
     * @param timestamp The timestamp to compare against current time
     * @return A string like "2 hours ago", "3 days ago", etc.
     */
    public static String getTimeAgo(Date timestamp) {
        if (timestamp == null) {
            return "";
        }
        
        long now = System.currentTimeMillis();
        long timePassed = now - timestamp.getTime();
        
        // Convert to seconds
        long seconds = timePassed / 1000;
        
        if (seconds < 60) {
            return "just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        }
    }
}
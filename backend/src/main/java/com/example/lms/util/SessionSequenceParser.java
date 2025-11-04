package com.example.lms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing session sequence numbers from filenames.
 * Supports patterns like: session_1, session 2, session-3, session - 4, Session01, etc.
 */
public class SessionSequenceParser {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionSequenceParser.class);
    
    private static final Pattern SESSION_PATTERN = Pattern.compile(
        "(?i)session\\s*[-_]?\\s*(\\d+)",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Extracts session sequence number from filename.
     * 
     * @param filename Filename to parse (e.g., "session_1.mp4", "Session - 2.mp4")
     * @return Session number if found, null otherwise
     */
    public static Integer extractSessionNumber(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        
        try {
            Matcher matcher = SESSION_PATTERN.matcher(filename);
            if (matcher.find()) {
                String numberStr = matcher.group(1);
                Integer sessionNumber = Integer.parseInt(numberStr);
                logger.debug("Extracted session number {} from filename: {}", sessionNumber, filename);
                return sessionNumber;
            }
            
            logger.debug("No session number found in filename: {}", filename);
            return null;
            
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse session number from filename: {}", filename, e);
            return null;
        }
    }
    
    /**
     * Removes file extension from filename.
     * 
     * @param filename Filename with extension (e.g., "video.mp4")
     * @return Filename without extension (e.g., "video")
     */
    public static String removeExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return filename;
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        
        return filename;
    }
}

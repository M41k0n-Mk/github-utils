package me.m41k0n.service;

import me.m41k0n.entity.HistoryEntity;
import me.m41k0n.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for exporting user data to different formats.
 * Converted from static utility class to proper Spring service.
 */
@Service
public class ExportService {
    /**
     * Exports users list to JSON format with pretty printing.
     * Delegates to ExportUtils for consistency.
     */
    public String exportToJson(List<User> users) throws Exception {
        return ExportUtils.toJson(users);
    }
    
    /**
     * Exports users list to CSV format with proper escaping.
     * Delegates to ExportUtils for consistency.
     */
    public String exportToCsv(List<User> users) {
        return ExportUtils.toCsv(users);
    }

    /**
     * Exports users list to specified format.
     */
    public String exportToFormat(List<User> users, ExportFormat format) throws Exception {
        return switch (format) {
            case CSV -> exportToCsv(users);
            case JSON -> exportToJson(users);
        };
    }

    public String exportHistoryToJson(List<HistoryEntity> items) throws Exception {
        return ExportUtils.historyToJson(items);
    }

    public String exportHistoryToCsv(List<HistoryEntity> items) {
        return ExportUtils.historyToCsv(items);
    }

    public String exportHistoryToFormat(List<HistoryEntity> items, ExportFormat format) throws Exception {
        return switch (format) {
            case CSV -> exportHistoryToCsv(items);
            case JSON -> exportHistoryToJson(items);
        };
    }

    /**
     * Supported export formats
     */
    public enum ExportFormat {
        CSV("text/csv"),
        JSON("application/json");

        private final String mimeType;

        ExportFormat(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMimeType() {
            return mimeType;
        }

        public static ExportFormat fromString(String format) {
            if (format == null) return null;
            return switch (format.toLowerCase()) {
                case "csv" -> CSV;
                case "json" -> JSON;
                default -> null;
            };
        }
    }
}
package me.m41k0n.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.m41k0n.entity.HistoryEntity;
import me.m41k0n.model.User;

import java.io.StringWriter;
import java.util.List;

public class ExportUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(List<User> users) throws Exception {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(users);
    }

    public static String toCsv(List<User> users) {
        StringWriter sw = new StringWriter();
        sw.append("login,html_url\n");
        for (User u : users) {
            sw.append(escape(u.login())).append(",").append(escape(u.html_url())).append("\n");
        }
        return sw.toString();
    }

    public static String historyToJson(List<HistoryEntity> items) throws Exception {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(items);
    }

    public static String historyToCsv(List<HistoryEntity> items) {
        StringWriter sw = new StringWriter();
        sw.append("username,action,timestamp,source_list_id,dry_run\n");
        for (HistoryEntity h : items) {
            sw.append(escape(h.getUsername())).append(",")
              .append(escape(h.getAction())).append(",")
              .append(escape(h.getTimestamp())).append(",")
              .append(escape(h.getSourceListId())).append(",")
              .append(String.valueOf(h.getDryRun()))
              .append("\n");
        }
        return sw.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }
}

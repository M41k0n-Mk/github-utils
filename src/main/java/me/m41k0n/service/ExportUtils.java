package me.m41k0n.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }
}

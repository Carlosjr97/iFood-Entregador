package com.biometric.capture.service;

import com.biometric.capture.domain.Session;
import com.biometric.capture.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@Transactional(readOnly = true)
public class CsvExportService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_INSTANT;

    private final SessionRepository sessionRepository;

    public CsvExportService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public String exportSessionsCsv() {
        StringBuilder csv = new StringBuilder("id,user_name,user_email,score,result,created_at\n");
        for (Session session : sessionRepository.findAllWithUser()) {
            csv.append(session.getId()).append(',')
                    .append(escape(session.getUser().getName())).append(',')
                    .append(escape(session.getUser().getEmail())).append(',')
                    .append(session.getScore()).append(',')
                    .append(session.getResult()).append(',')
                    .append(TIMESTAMP_FORMAT.format(session.getCreatedAt()))
                    .append('\n');
        }
        return csv.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }
}

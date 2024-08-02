package com.stibo.demo.report.controller;

import com.stibo.demo.report.model.Datastandard;
import com.stibo.demo.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.util.Strings.dquote;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
public class ReportController {
    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ReportController(ReportService reportService, ObjectMapper objectMapper) {
        this.reportService = reportService;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/report/{categoryId}", produces = "text/csv")
    public String report(@PathVariable String categoryId)  {
        var datastandard = loadDatastandard();
        return reportService.report(datastandard, categoryId)
            .map(row -> row.map(ReportController::escape).collect(joining(","))).collect(joining("\n"));
    }

    private Datastandard loadDatastandard() {
        // Load acme datastandard from classpath
        try (InputStream is = getClass().getResourceAsStream("/acme-datastandard.json")) {
            return objectMapper.readValue(is, Datastandard.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load datastandard", e);
        }
    }

    private static String escape(String cell) {
        if (isNull(cell)) {
            return "";
        } else if (cell.contains("\"")) {
            return dquote(cell.replace("\"", "\"\""));
        } else if (cell.contains(",") || cell.contains("\n")) {
            return dquote(cell);
        } else {
            return cell;
        }
    }
}

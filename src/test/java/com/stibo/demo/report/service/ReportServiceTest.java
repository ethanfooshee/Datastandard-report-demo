package com.stibo.demo.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stibo.demo.report.model.Datastandard;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportService.class, ObjectMapper.class})
public class ReportServiceTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ReportService reportService;

    private Datastandard datastandard;

    @BeforeEach
    public void before() throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("datastandard.json");
        this.datastandard = objectMapper.readValue(stream, Datastandard.class);
    }

    @Test
    public void testReport() {
        List<List<String>> report = reportService.report(datastandard, "leaf").map(row -> row.collect(toList())).collect(toList());
    }
}

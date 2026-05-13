package com.jost.invernadero.tests.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportGeneratorTest {

    @TempDir
    private Path tempDir;

    @Test
    void generatesHtmlReportWithSummaryAndRows() throws IOException {
        Path reportPath = tempDir.resolve("test-results.html");

        Path generatedPath = new ReportGenerator(reportPath).generate(List.of(
                TestResult.passed("Sensor", 1234),
                TestResult.failed("Location", 2000, "Campo #name no encontrado"),
                TestResult.error("Greenhouse", 500, "Boom <script>")
        ));

        String html = Files.readString(generatedPath);

        assertAll(
                () -> assertEquals(reportPath.toAbsolutePath().normalize(), generatedPath),
                () -> assertTrue(html.contains("1/3 pruebas pasadas")),
                () -> assertTrue(html.contains("<span>PASSED</span><strong>1</strong>")),
                () -> assertTrue(html.contains("<span>FAILED</span><strong>1</strong>")),
                () -> assertTrue(html.contains("<span>ERROR</span><strong>1</strong>")),
                () -> assertTrue(html.contains("<td>Sensor</td>")),
                () -> assertTrue(html.contains("<td>1.234s</td>")),
                () -> assertTrue(html.contains("Campo #name no encontrado")),
                () -> assertTrue(html.contains("Boom &lt;script&gt;"))
        );
    }
}

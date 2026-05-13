package com.jost.invernadero.tests.report;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ReportGenerator {

    public static final Path DEFAULT_REPORT_PATH = Path.of("test-results.html");

    private final Path reportPath;

    public ReportGenerator() {
        this(DEFAULT_REPORT_PATH);
    }

    public ReportGenerator(Path reportPath) {
        this.reportPath = Objects.requireNonNull(reportPath, "reportPath");
    }

    public Path generate(List<TestResult> results) {
        Objects.requireNonNull(results, "results");

        try {
            Path normalizedPath = reportPath.toAbsolutePath().normalize();
            Path parent = normalizedPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(normalizedPath, html(results), StandardCharsets.UTF_8);
            return normalizedPath;
        } catch (IOException exception) {
            throw new UncheckedIOException("No se pudo generar el reporte HTML: " + reportPath, exception);
        }
    }

    private String html(List<TestResult> results) {
        long passed = count(results, TestResult.Status.PASSED);
        long failed = count(results, TestResult.Status.FAILED);
        long errors = count(results, TestResult.Status.ERROR);
        long totalDurationMillis = results.stream().mapToLong(TestResult::getDurationMillis).sum();

        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Resultados Selenium CRUD</title>
                  <style>
                    :root {
                      color-scheme: light;
                      font-family: Arial, Helvetica, sans-serif;
                      background: #f6f8f7;
                      color: #1f2933;
                    }
                    body {
                      margin: 0;
                      padding: 32px;
                    }
                    main {
                      max-width: 1080px;
                      margin: 0 auto;
                    }
                    h1 {
                      margin: 0 0 8px;
                      font-size: 28px;
                    }
                    .muted {
                      color: #5c6b73;
                      margin: 0 0 24px;
                    }
                    .summary {
                      display: grid;
                      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
                      gap: 12px;
                      margin: 24px 0;
                    }
                    .metric {
                      background: #ffffff;
                      border: 1px solid #d9e2df;
                      border-radius: 8px;
                      padding: 16px;
                    }
                    .metric span {
                      display: block;
                      color: #5c6b73;
                      font-size: 12px;
                      text-transform: uppercase;
                      letter-spacing: 0.04em;
                    }
                    .metric strong {
                      display: block;
                      margin-top: 8px;
                      font-size: 24px;
                    }
                    table {
                      width: 100%%;
                      border-collapse: collapse;
                      background: #ffffff;
                      border: 1px solid #d9e2df;
                      border-radius: 8px;
                      overflow: hidden;
                    }
                    th, td {
                      padding: 12px 14px;
                      text-align: left;
                      border-bottom: 1px solid #e6ecea;
                      vertical-align: top;
                    }
                    th {
                      background: #eef4f1;
                      font-size: 13px;
                    }
                    tr:last-child td {
                      border-bottom: 0;
                    }
                    .status {
                      display: inline-block;
                      min-width: 72px;
                      padding: 4px 8px;
                      border-radius: 999px;
                      font-size: 12px;
                      font-weight: 700;
                      text-align: center;
                    }
                    .PASSED {
                      color: #0f5132;
                      background: #d1e7dd;
                    }
                    .FAILED {
                      color: #842029;
                      background: #f8d7da;
                    }
                    .ERROR {
                      color: #664d03;
                      background: #fff3cd;
                    }
                    .message {
                      white-space: pre-wrap;
                      word-break: break-word;
                    }
                  </style>
                </head>
                <body>
                  <main>
                    <h1>Resultados Selenium CRUD</h1>
                    <p class="muted">%s/%s pruebas pasadas. Tiempo total: %ss.</p>
                    <section class="summary" aria-label="Resumen ejecutivo">
                      <div class="metric"><span>Total</span><strong>%s</strong></div>
                      <div class="metric"><span>PASSED</span><strong>%s</strong></div>
                      <div class="metric"><span>FAILED</span><strong>%s</strong></div>
                      <div class="metric"><span>ERROR</span><strong>%s</strong></div>
                      <div class="metric"><span>Tiempo total</span><strong>%ss</strong></div>
                    </section>
                    <table>
                      <thead>
                        <tr>
                          <th>Modelo</th>
                          <th>Estado</th>
                          <th>Duracion</th>
                          <th>Mensaje</th>
                        </tr>
                      </thead>
                      <tbody>
                        %s
                      </tbody>
                    </table>
                  </main>
                </body>
                </html>
                """.formatted(
                passed,
                results.size(),
                seconds(totalDurationMillis),
                results.size(),
                passed,
                failed,
                errors,
                seconds(totalDurationMillis),
                rows(results)
        );
    }

    private String rows(List<TestResult> results) {
        StringBuilder rows = new StringBuilder();

        for (TestResult result : results) {
            rows.append("""
                    <tr>
                      <td>%s</td>
                      <td><span class="status %s">%s</span></td>
                      <td>%ss</td>
                      <td class="message">%s</td>
                    </tr>
                    """.formatted(
                    escape(result.getName()),
                    result.getStatus(),
                    result.getStatus(),
                    seconds(result.getDurationMillis()),
                    escape(result.getErrorMessage().orElse(""))
            ));
        }

        return rows.toString();
    }

    private long count(List<TestResult> results, TestResult.Status status) {
        return results.stream()
                .filter(result -> result.getStatus() == status)
                .count();
    }

    private String seconds(long millis) {
        return String.format(Locale.US, "%.3f", millis / 1000.0);
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

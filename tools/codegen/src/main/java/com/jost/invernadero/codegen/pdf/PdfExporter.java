package com.jost.invernadero.codegen.pdf;

import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.FieldDef;
import com.jost.invernadero.codegen.model.LocaleLabels;
import com.jost.invernadero.codegen.model.Options;
import com.jost.invernadero.codegen.model.RelationDef;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class PdfExporter {

    public record EntityWithSource(String sourceFileName, EntityDefinition definition) {}

    private static final float MARGIN = 50f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final float MIN_Y = MARGIN + 20f;

    public void export(List<EntityWithSource> entities, Path output) throws IOException {
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        try (PDDocument doc = new PDDocument()) {
            PageWriter w = new PageWriter(doc);
            writeHeader(w);
            for (EntityWithSource e : entities) {
                writeEntitySection(w, e);
            }
            w.close();
            doc.save(output.toFile());
        }
    }

    private void writeHeader(PageWriter w) throws IOException {
        w.writeLine("Definiciones de Entidades", PDType1Font.HELVETICA_BOLD, 16f);
        w.writeLine("Exportado: " + LocalDate.now(), PDType1Font.HELVETICA, 9f);
        w.skip(12f);
    }

    private void writeEntitySection(PageWriter w, EntityWithSource e) throws IOException {
        EntityDefinition d = e.definition();

        w.writeLine("Fuente: " + e.sourceFileName(), PDType1Font.HELVETICA_OBLIQUE, 9f);
        w.writeLine(d.name(), PDType1Font.HELVETICA_BOLD, 13f);
        w.writeLine("Tabla: " + d.tableName(), PDType1Font.HELVETICA, 10f);
        w.skip(4f);

        w.writeLine("Campos:", PDType1Font.HELVETICA_BOLD, 10f);
        for (FieldDef f : d.fields()) {
            w.writeWrapped("  * " + f.name() + " [" + f.type() + "]" + fieldMeta(f),
                    PDType1Font.HELVETICA, 10f);
        }

        if (!d.relations().isEmpty()) {
            w.skip(4f);
            w.writeLine("Relaciones:", PDType1Font.HELVETICA_BOLD, 10f);
            for (RelationDef r : d.relations()) {
                StringBuilder line = new StringBuilder("  * ")
                        .append(r.name())
                        .append(" (").append(r.type()).append(" -> ").append(r.target()).append(")");
                if (r.joinColumn() != null) line.append(" joinColumn=").append(r.joinColumn());
                if (r.mappedBy() != null) line.append(" mappedBy=").append(r.mappedBy());
                w.writeWrapped(line.toString(), PDType1Font.HELVETICA, 10f);
            }
        }

        w.skip(4f);
        Options o = d.options();
        w.writeLine(
                "Opciones: controller=" + o.generateController()
                        + "  frontend=" + o.generateFrontend()
                        + "  auditable=" + o.auditable(),
                PDType1Font.HELVETICA, 10f);

        if (!d.i18n().locales().isEmpty()) {
            w.skip(4f);
            w.writeLine("Etiquetas i18n:", PDType1Font.HELVETICA_BOLD, 10f);
            for (Map.Entry<String, LocaleLabels> entry : new TreeMap<>(d.i18n().locales()).entrySet()) {
                String locale = entry.getKey();
                LocaleLabels labels = entry.getValue();
                w.writeLine(
                        "  [" + locale + "] " + nvl(labels.singular()) + " / " + nvl(labels.plural()),
                        PDType1Font.HELVETICA, 10f);
                if (!labels.fields().isEmpty()) {
                    w.writeWrapped("    campos: " + labels.fields(), PDType1Font.HELVETICA, 9f);
                }
                if (!labels.relations().isEmpty()) {
                    w.writeWrapped("    relaciones: " + labels.relations(), PDType1Font.HELVETICA, 9f);
                }
            }
        }

        w.skip(16f);
    }

    private String fieldMeta(FieldDef f) {
        StringBuilder sb = new StringBuilder();
        if (!Boolean.TRUE.equals(f.nullable())) sb.append(", not null");
        if (Boolean.TRUE.equals(f.unique())) sb.append(", unique");
        if (f.length() != null) sb.append(", length=").append(f.length());
        if (f.precision() != null) sb.append(", precision=").append(f.precision());
        if (f.scale() != null) sb.append(", scale=").append(f.scale());
        if (f.defaultValue() != null) sb.append(", default=").append(f.defaultValue());
        if (!f.enumValues().isEmpty()) sb.append(", values=").append(String.join("|", f.enumValues()));
        return sb.toString();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    // Keeps only WinAnsi-safe characters (covers full Spanish alphabet).
    static String sanitize(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= 0x20 && c <= 0x7E) || (c >= 0xA0 && c <= 0xFF)) {
                sb.append(c);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    private static class PageWriter {
        private final PDDocument doc;
        private PDPageContentStream cs;
        private float y;

        PageWriter(PDDocument doc) throws IOException {
            this.doc = doc;
            newPage();
        }

        void close() throws IOException {
            if (cs != null) cs.close();
        }

        private void newPage() throws IOException {
            if (cs != null) cs.close();
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        void skip(float amount) throws IOException {
            y -= amount;
            if (y < MIN_Y) newPage();
        }

        void writeLine(String text, PDFont font, float size) throws IOException {
            float lineHeight = size * 1.4f;
            if (y - size < MIN_Y) newPage();
            drawText(sanitize(text), font, size, MARGIN, y - size);
            y -= lineHeight;
        }

        void writeWrapped(String text, PDFont font, float size) throws IOException {
            float lineHeight = size * 1.4f;
            for (String line : wrap(sanitize(text), font, size, CONTENT_WIDTH)) {
                if (y - size < MIN_Y) newPage();
                drawText(line, font, size, MARGIN, y - size);
                y -= lineHeight;
            }
        }

        private void drawText(String text, PDFont font, float size, float x, float posY)
                throws IOException {
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(x, posY);
            cs.showText(text);
            cs.endText();
        }

        private static List<String> wrap(String text, PDFont font, float size, float maxWidth)
                throws IOException {
            List<String> result = new ArrayList<>();
            String[] words = text.split(" ", -1);
            StringBuilder current = new StringBuilder();
            for (String word : words) {
                if (current.length() == 0) {
                    current.append(word);
                } else {
                    String candidate = current + " " + word;
                    if (font.getStringWidth(candidate) / 1000f * size > maxWidth) {
                        result.add(current.toString());
                        current = new StringBuilder(word);
                    } else {
                        current.append(' ').append(word);
                    }
                }
            }
            if (!current.isEmpty()) result.add(current.toString());
            return result.isEmpty() ? List.of("") : result;
        }
    }
}

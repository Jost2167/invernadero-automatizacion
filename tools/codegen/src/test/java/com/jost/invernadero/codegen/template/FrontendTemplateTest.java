package com.jost.invernadero.codegen.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.jost.invernadero.codegen.model.EntityDefinition;
import com.jost.invernadero.codegen.model.EntityDefinitionObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FrontendTemplateTest {

    private final TemplateModelBuilder builder = new TemplateModelBuilder();
    private final Handlebars handlebars = new Handlebars(new ClassPathTemplateLoader("/templates/frontend", ".hbs"));

    @Test
    void rendersApiClientSnapshot() throws IOException {
        String output = render("api-client", "fixtures/valid/with-enum.json");

        assertThat(output).isEqualTo("""
                import api from './client.js'

                const RESOURCE = '/api/alert'

                export const alertApi = {
                  list() {
                    return api.get(RESOURCE).then((response) => response.data)
                  },

                  getById(id) {
                    return api.get(`${RESOURCE}/${id}`).then((response) => response.data)
                  },

                  create(payload) {
                    return api.post(RESOURCE, payload).then((response) => response.data)
                  },

                  update(id, payload) {
                    return api.put(`${RESOURCE}/${id}`, payload).then((response) => response.data)
                  },

                  remove(id) {
                    return api.delete(`${RESOURCE}/${id}`).then((response) => response.data)
                  },
                }

                export default alertApi
                """.stripIndent());
    }

    @Test
    void rendersListPageSnapshot() throws IOException {
        String output = render("list-page", "fixtures/valid/with-enum.json");

        assertThat(output)
                .contains("export default function AlertListPage()")
                .contains("import alertApi from '../../api/alert.js'")
                .contains("{t('alert.list.fields.message')}")
                .contains("{t('alert.list.fields.severity')}")
                .contains("<TableCell colSpan={ 4 }>{t('alert.list.empty')}</TableCell>")
                .contains("navigate('/alert/new')")
                .contains("alertApi.remove(id)");
    }

    @Test
    void rendersFormPageSnapshot() throws IOException {
        String output = render("form-page", "fixtures/valid/with-enum.json");

        assertThat(output)
                .contains("export default function AlertFormPage()")
                .contains("import alertApi from '../../api/alert.js'")
                .contains("message: ''")
                .contains("severity: ''")
                .contains("sensorId: ''")
                .contains("select")
                .contains("<MenuItem value=\"CRITICAL\">CRITICAL</MenuItem>")
                .contains("await alertApi.update(id, form)")
                .contains("await alertApi.create(form)");
    }

    private String render(String template, String fixture) throws IOException {
        Map<String, Object> model = builder.build(readFixture(fixture));
        return handlebars.compile(template).apply(model).replace("\r\n", "\n");
    }

    private EntityDefinition readFixture(String resourcePath) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertThat(input)
                    .as("fixture %s exists", resourcePath)
                    .isNotNull();
            return EntityDefinitionObjectMapper.create().readValue(input, EntityDefinition.class);
        }
    }
}

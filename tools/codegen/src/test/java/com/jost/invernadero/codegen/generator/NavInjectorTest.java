package com.jost.invernadero.codegen.generator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NavInjectorTest {

    private static final String SIDEBAR = """
            export const NAV_MODULES = [
              { key: 'dashboard', path: '/', exact: true },
              // codegen:nav
            ]
            """.stripIndent();

    @Test
    void injectsEntryBeforeMarker() {
        InjectionResult result = new NavInjector().inject(SIDEBAR, "location", "location");

        assertThat(result.success()).isTrue();

        String content = result.content();
        assertThat(content).contains("{ key: 'location', path: '/location' }");
        assertThat(content).contains("// codegen:nav");

        int entryIndex = content.indexOf("{ key: 'location', path: '/location' }");
        int markerIndex = content.indexOf("// codegen:nav");
        assertThat(entryIndex).isLessThan(markerIndex);
    }

    @Test
    void isIdempotentWhenRouteAlreadyPresent() {
        String alreadyInjected = """
                export const NAV_MODULES = [
                  { key: 'dashboard', path: '/', exact: true },
                  { key: 'location', path: '/location' },
                  // codegen:nav
                ]
                """.stripIndent();

        InjectionResult result = new NavInjector().inject(alreadyInjected, "location", "location");

        assertThat(result.success()).isTrue();
        assertThat(result.content()).isEqualTo(alreadyInjected);
    }

    @Test
    void returnsErrorWhenMarkerIsMissing() {
        String noMarker = """
                export const NAV_MODULES = [
                  { key: 'dashboard', path: '/', exact: true },
                ]
                """.stripIndent();

        InjectionResult result = new NavInjector().inject(noMarker, "location", "location");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).contains("// codegen:nav");
    }
}

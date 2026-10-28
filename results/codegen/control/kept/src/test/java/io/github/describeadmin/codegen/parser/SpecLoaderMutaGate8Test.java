package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.Layout;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpecLoaderMutaGate8Test {

    @Test
    void invalidValuesAreReportedWithOriginAndShortCircuit() {
        // illegal CLI value wins over valid lower-priority sources; only one error, naming the CLI origin
        List<String> errors = new ArrayList<>();
        SpecLoader.LayoutChoice cli = SpecLoader.resolveLayout("bogus", "flat", "flat", errors);
        assertThat(cli.origin()).isEqualTo("--layout 参数");
        assertThat(cli.layout()).isEqualTo(Layout.NESTED);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("\"bogus\"")
                .contains("来自 --layout 参数")
                .contains("只能是 nested 或 flat")
                .doesNotContain("spec 的 layout 键")
                .doesNotContain("CODEGEN_LAYOUT");

        // non-string spec value is stringified and reported
        List<String> specErrors = new ArrayList<>();
        SpecLoader.LayoutChoice spec = SpecLoader.resolveLayout(null, 123, "flat", specErrors);
        assertThat(spec.origin()).isEqualTo("spec 的 layout 键");
        assertThat(specErrors).hasSize(1);
        assertThat(specErrors.get(0))
                .contains("\"123\"")
                .contains("来自 spec 的 layout 键");

        // env value is trimmed in the message
        List<String> envErrors = new ArrayList<>();
        SpecLoader.LayoutChoice env = SpecLoader.resolveLayout(null, null, "  bogus ", envErrors);
        assertThat(env.origin()).isEqualTo("CODEGEN_LAYOUT 环境变量");
        assertThat(envErrors).hasSize(1);
        assertThat(envErrors.get(0)).contains("\"bogus\"").contains("来自 CODEGEN_LAYOUT 环境变量");

        // errors accumulate in the caller's list rather than being replaced
        List<String> shared = new ArrayList<>();
        shared.add("existing");
        SpecLoader.resolveLayout("bad1", null, null, shared);
        SpecLoader.resolveLayout(null, "bad2", null, shared);
        assertThat(shared).hasSize(3);
        assertThat(shared.get(0)).isEqualTo("existing");
        assertThat(shared.get(1)).contains("bad1");
        assertThat(shared.get(2)).contains("bad2");

        // valid input leaves the list untouched
        List<String> clean = new ArrayList<>();
        assertThat(SpecLoader.resolveLayout(null, null, null, clean).origin()).isEqualTo("默认");
        assertThat(clean).isEmpty();
    }
}

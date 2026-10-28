package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.Layout;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpecLoaderMutaGate14Test {

    @Test
    void invalidHigherPriorityValueIsReportedAndNotSkipped() {
        List<String> errors = new ArrayList<>();

        // 非法的命令行取值：不会回落到合法的 spec / env，只记录一条错误，且错误里的值已去除首尾空白
        SpecLoader.LayoutChoice cli = SpecLoader.resolveLayout(" sideways ", "flat", "flat", errors);
        assertThat(cli.origin()).isEqualTo("--layout 参数");
        assertThat(cli.layout()).isEqualTo(Layout.NESTED);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("\"sideways\"")
                .contains("来自 --layout 参数")
                .contains("只能是 nested 或 flat");

        // 非字符串的 spec 值（如 YAML 里的数字）经 String.valueOf 转换后同样被拦下，错误累积到同一个列表
        SpecLoader.LayoutChoice spec = SpecLoader.resolveLayout(null, 123, "flat", errors);
        assertThat(spec.origin()).isEqualTo("spec 的 layout 键");
        assertThat(spec.layout()).isEqualTo(Layout.NESTED);
        assertThat(errors).hasSize(2);
        assertThat(errors.get(1))
                .contains("\"123\"")
                .contains("来自 spec 的 layout 键");
    }
}

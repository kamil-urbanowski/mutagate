package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.Layout;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate2Test {

    @Test
    void invalidValuesAreReportedWithOriginAndDoNotFallThrough() {
        // 1) 非法的命令行取值：只记一条错误，来源为 --layout 参数，
        //    不会因为 spec / 环境变量合法而继续往下取值；错误信息里的取值已 trim
        List<String> errors = new ArrayList<>();
        SpecLoader.LayoutChoice cli = SpecLoader.resolveLayout("  bogus  ", "flat", "flat", errors);
        assertEquals("--layout 参数", cli.origin());
        assertEquals(Layout.NESTED, cli.layout());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("\"bogus\""), errors.get(0));
        assertTrue(errors.get(0).contains("来自 --layout 参数"), errors.get(0));
        assertTrue(errors.get(0).contains("只能是 nested 或 flat"), errors.get(0));

        // 2) 非法的 spec 取值（非字符串类型，经 String.valueOf 转换）：
        //    只记一条错误，来源为 spec 的 layout 键，不会改用合法的环境变量
        errors = new ArrayList<>();
        SpecLoader.LayoutChoice spec = SpecLoader.resolveLayout(null, 123, "flat", errors);
        assertEquals("spec 的 layout 键", spec.origin());
        assertEquals(Layout.NESTED, spec.layout());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("\"123\""), errors.get(0));
        assertTrue(errors.get(0).contains("来自 spec 的 layout 键"), errors.get(0));

        // 3) 合法的命令行取值不会被后面非法的 spec / 环境变量影响，也不产生错误
        errors = new ArrayList<>();
        SpecLoader.LayoutChoice ok = SpecLoader.resolveLayout("flat", "sideways", "bogus", errors);
        assertEquals(Layout.FLAT, ok.layout());
        assertEquals("--layout 参数", ok.origin());
        assertTrue(errors.isEmpty(), errors.toString());

        // 4) 环境变量非法时，错误信息中的取值同样 trim
        errors = new ArrayList<>();
        SpecLoader.LayoutChoice env = SpecLoader.resolveLayout(null, null, " weird ", errors);
        assertEquals("CODEGEN_LAYOUT 环境变量", env.origin());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("\"weird\""), errors.get(0));
    }
}

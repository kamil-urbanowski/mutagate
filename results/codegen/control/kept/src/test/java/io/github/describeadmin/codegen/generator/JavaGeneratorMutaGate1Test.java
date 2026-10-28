package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaGeneratorMutaGate1Test {

    @Test
    void controllerDocImportsAndPermPrefix() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String src = JavaGenerator.controller(spec);

        List<FieldSpec> queryFields = spec.queryFields();
        assertFalse(queryFields.isEmpty());

        // Javadoc lists each query condition with its mode, inside a <ul>
        StringBuilder expectedDoc = new StringBuilder("\n *\n * <p>列表查询支持的条件：\n * <ul>\n");
        for (int i = 0; i < queryFields.size(); i++) {
            FieldSpec f = queryFields.get(i);
            if (i > 0) {
                expectedDoc.append("\n");
            }
            expectedDoc.append(" *   <li>{@code ").append(f.name()).append("} —— ")
                    .append(f.comment()).append("（")
                    .append(f.query().name().toLowerCase()).append("）</li>");
        }
        expectedDoc.append("\n * </ul>");
        assertTrue(src.contains(expectedDoc.toString()), src);

        // class comment followed by the full-width period
        assertTrue(src.contains(" * " + spec.comment() + "。"), src);

        // permPrefix returns the raw module name
        assertTrue(src.contains("public String permPrefix() {\n        return \""
                + spec.module() + "\";\n    }"), src);

        // getService returns the injected field
        assertTrue(src.contains("return " + spec.entityVar() + "Service;"), src);

        // imports are emitted in sorted order and include the query-related ones
        List<String> imports = new ArrayList<>();
        for (String line : src.split("\n")) {
            if (line.startsWith("import ")) {
                imports.add(line.substring("import ".length(), line.length() - 1));
            }
        }
        List<String> sorted = new ArrayList<>(imports);
        sorted.sort(null);
        assertEquals(sorted, imports);
        assertTrue(imports.contains("com.baomidou.mybatisplus.core.conditions.Wrapper"));
        assertTrue(imports.contains("com.baomidou.mybatisplus.core.conditions.query.QueryWrapper"));
        assertTrue(imports.contains("java.util.Map"));
        assertTrue(imports.contains("io.github.describeadmin.mybatis.api.BaseController"));
        assertTrue(imports.contains("org.springframework.web.bind.annotation.RestController"));
    }
}

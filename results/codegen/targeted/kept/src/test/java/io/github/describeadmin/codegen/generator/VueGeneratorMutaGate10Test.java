package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate10Test {

    private static final String PLACEHOLDER = "  // spec 中没有 query 字段，无查询条件";

    @Test
    void searchModelPlaceholderOnlyWhenNoQueryFields() throws Exception {
        ModuleSpec withQuery = SpecLoader.load(Path.of("examples/project.yaml"));

        List<FieldSpec> noQueryFields = withQuery.fields().stream()
                .map(f -> new FieldSpec(f.name(), f.column(), f.type(), f.length(),
                        f.precision(), f.scale(), f.nullable(), f.comment(),
                        FieldSpec.QueryMode.NONE, f.indexed()))
                .toList();
        ModuleSpec withoutQuery = new ModuleSpec(withQuery.basePackage(), withQuery.module(),
                withQuery.entity(), withQuery.table(), withQuery.comment(),
                withQuery.apiPrefix(), withQuery.layout(), withQuery.layoutOrigin(),
                noQueryFields);

        assertFalse(withQuery.queryFields().isEmpty());
        assertTrue(withoutQuery.queryFields().isEmpty());

        String pageWith = VueGenerator.page(withQuery);
        assertFalse(pageWith.contains("无查询条件"));
        assertTrue(pageWith.contains("  startDateStart: undefined as string | undefined,"));

        String pageWithout = VueGenerator.page(withoutQuery);
        assertTrue(pageWithout.contains("const search = reactive({\n" + PLACEHOLDER + "\n});"));
    }
}

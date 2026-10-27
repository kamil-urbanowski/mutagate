package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaGeneratorMutaGate1Test {

    @Test
    void controllerQueryDocAppearsOnlyWhenQueryFieldsExist() throws Exception {
        ModuleSpec withQuery = SpecLoader.load(Path.of("examples/project.yaml"));
        assertFalse(withQuery.queryFields().isEmpty());

        String withDoc = JavaGenerator.controller(withQuery);
        assertTrue(withDoc.contains("<p>列表查询支持的条件："));
        assertTrue(withDoc.contains("<li>{@code projectName}"));
        assertTrue(withDoc.contains("（like）</li>"));
        assertTrue(withDoc.contains(" * </ul>"));

        List<FieldSpec> noQueryFields = withQuery.fields().stream()
                .map(f -> new FieldSpec(f.name(), f.column(), f.type(), f.length(),
                        f.precision(), f.scale(), f.nullable(), f.comment(),
                        FieldSpec.QueryMode.NONE, f.indexed()))
                .toList();
        ModuleSpec withoutQuery = new ModuleSpec(
                withQuery.basePackage(), withQuery.module(), withQuery.entity(),
                withQuery.table(), withQuery.comment(), withQuery.apiPrefix(),
                withQuery.layout(), withQuery.layoutOrigin(), noQueryFields);
        assertTrue(withoutQuery.queryFields().isEmpty());

        String withoutDoc = JavaGenerator.controller(withoutQuery);
        assertFalse(withoutDoc.contains("列表查询支持的条件"));
        assertFalse(withoutDoc.contains("<ul>"));
        assertFalse(withoutDoc.contains("<li>"));
        assertFalse(withoutDoc.contains("buildListWrapper"));
    }
}

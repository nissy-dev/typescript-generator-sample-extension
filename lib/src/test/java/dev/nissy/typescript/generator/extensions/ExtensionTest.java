package dev.nissy.typescript.generator.extensions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TypeScriptGenerator;

import lombok.Getter;
import lombok.Setter;

class ExtensionTest {
    @Test
    public void test() {
        final Settings settings = TestUtils.settings();
        settings.extensions.add(new DefaultValueNonNullable());
        settings.extensions.add(new JsonDeserializeDecorator());
        final String output = new TypeScriptGenerator(settings).generateTypeScript(Input.from(SampleClass.class));
        System.out.println(output);
        assertTrue(output.contains("text0: string | null;"));
        assertTrue(output.contains("text1: string;"));
        assertTrue(output.contains("text2: string;"));
        assertTrue(output.contains("fieldList: { [index: string]: FieldForm };"));
    }

    public static class SampleClass {

        // Check default value
        public String text0;
        public String text1 = "hello";
        @NotNull
        public String text2;

        // Check Jackson decorators
        @JsonDeserialize(using = FieldFormDeserializer.class)
        public List<FieldForm> fieldList = new ArrayList<>();

    }

    @Getter
    @Setter
    public static class FieldForm {

        @NotNull
        public String field0;
        @NotNull
        public Long field1;

    }

    public static class FieldFormDeserializer extends StdDeserializer<FieldForm> {
        private static final long serialVersionUID = 1L;

        public FieldFormDeserializer() {
            super(FieldForm.class);
        }

        @Override
        public FieldForm deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
            FieldForm fieldForm = new FieldForm();
            return fieldForm;
        }
    }
}

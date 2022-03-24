package dev.nissy.typescript.generator.extensions;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TypeScriptGenerator;

class ExtensionTest {
    @Test
    public void test() {
        final Settings settings = TestUtils.settings();
        settings.extensions.add(new DefaultValueNonNullableExtension());
        settings.extensions.add(new CustomSerializerExtension());
        final String output = new TypeScriptGenerator(settings).generateTypeScript(Input.from(SampleClass.class));
        assertTrue(output.contains("text0: string | null;"));
        assertTrue(output.contains("text1: string;"));
        assertTrue(output.contains("intValue: string;"));
    }

    public static class SampleClass {

        // Check default value
        public String text0;
        public String text1 = "hello";

        // Check custom serializer
        @JsonSerialize(using = CustomSerializer.class)
        public Integer intValue = 0;

    }

    public static class CustomSerializer extends StdSerializer<Integer> {
        public CustomSerializer() {
            super(Integer.class);
        }

        @Override
        public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }
}

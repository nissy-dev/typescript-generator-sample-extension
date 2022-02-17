
package dev.nissy.typescript.generator.extensions;

import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TypeScriptFileType;
import cz.habarta.typescript.generator.TypeScriptOutputKind;
import cz.habarta.typescript.generator.JsonLibrary;
import cz.habarta.typescript.generator.OptionalPropertiesDeclaration;

public class TestUtils {

  public static Settings settings() {
    final Settings settings = new Settings();
    settings.outputKind = TypeScriptOutputKind.module;
    settings.jsonLibrary = JsonLibrary.jackson2;
    settings.noFileComment = true;
    settings.noTslintDisable = true;
    settings.noEslintDisable = true;
    settings.newline = "\n";
    settings.requiredAnnotations.add(jakarta.validation.constraints.NotNull.class);
    settings.optionalPropertiesDeclaration = OptionalPropertiesDeclaration.nullableType;
    settings.outputFileType = TypeScriptFileType.implementationFile;
    return settings;
  }
}

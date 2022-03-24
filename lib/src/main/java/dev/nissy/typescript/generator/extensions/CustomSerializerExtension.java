package dev.nissy.typescript.generator.extensions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.TsBeanModel;
import cz.habarta.typescript.generator.emitter.TsModel;
import cz.habarta.typescript.generator.emitter.TsPropertyModel;
import cz.habarta.typescript.generator.emitter.EmitterExtensionFeatures;
import cz.habarta.typescript.generator.parser.BeanModel;
import cz.habarta.typescript.generator.TypeScriptGenerator;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.lang.reflect.Field;

public class CustomSerializerExtension extends Extension {

  @Override
  public EmitterExtensionFeatures getFeatures() {
    final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
    return features;
  }

  @Override
  public List<TransformerDefinition> getTransformers() {
    return Arrays
        .asList(new TransformerDefinition(ModelCompiler.TransformationPhase.BeforeSymbolResolution,
            this::transformModel));
  }

  protected TsModel transformModel(TsModelTransformer.Context context, TsModel model) {
    final List<TsBeanModel> beans = model.getBeans().stream()
        .map(bean -> transformBean(context, bean))
        .collect(Collectors.toList());
    return model.withBeans(beans);
  }

  protected TsBeanModel transformBean(TsModelTransformer.Context context, TsBeanModel tsBean) {
    try {
      final BeanModel bean = context.getBeanModelOrigin(tsBean);
      List<TsPropertyModel> properties = tsBean.getProperties().stream().map((TsPropertyModel tsProperty) -> {
        try {
          final Class<?> originClass = bean.getOrigin();
          final Field field = originClass.getDeclaredField(tsProperty.getName());
          field.setAccessible(true);
          JsonSerialize jsonDeserialize = field.getDeclaredAnnotation(JsonSerialize.class);
          if (jsonDeserialize != null
              && (jsonDeserialize.using().getSuperclass() == StdSerializer.class
                  || jsonDeserialize.contentUsing().getSuperclass() == StdSerializer.class)) {
            final TsType newType = transformType(tsProperty.tsType);
            return tsProperty.withTsType(newType);
          }

          return tsProperty;
        } catch (Exception e) {
          return tsProperty;
        }
      }).collect(Collectors.toList());
      return tsBean.withProperties(properties);
    } catch (Exception e) {
      TypeScriptGenerator.getLogger()
          .verbose(String.format("CustomSerializerExtension raised error: ", e.getMessage()));
      return tsBean;
    }
  }

  protected TsType transformType(TsType tsType) {
    if (tsType instanceof TsType.UnionType) {
      final TsType.UnionType unionType = (TsType.UnionType) tsType;
      final List<TsType> newTypes = unionType.types.stream().map(
          (TsType type) -> {
            if (type instanceof TsType.BasicType && ((TsType.BasicType) type) == TsType.Number) {
              return TsType.String;
            }
            return type;
          }).collect(Collectors.toList());
      return new TsType.UnionType(newTypes);
    } else if (tsType instanceof TsType.BasicType) {
      final TsType.BasicType basicType = (TsType.BasicType) tsType;
      if (basicType == TsType.Number) {
        return TsType.String;
      }
      return tsType;
    } else {
      return tsType;
    }
  }
}

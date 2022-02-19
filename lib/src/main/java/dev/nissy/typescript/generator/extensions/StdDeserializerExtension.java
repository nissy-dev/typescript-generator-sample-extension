package dev.nissy.typescript.generator.extensions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.ModelTransformer;
import cz.habarta.typescript.generator.compiler.SymbolTable;
import cz.habarta.typescript.generator.emitter.TsModel;
import cz.habarta.typescript.generator.emitter.EmitterExtensionFeatures;
import cz.habarta.typescript.generator.parser.BeanModel;
import cz.habarta.typescript.generator.parser.Model;
import cz.habarta.typescript.generator.parser.PropertyModel;
import cz.habarta.typescript.generator.TypeScriptGenerator;
import cz.habarta.typescript.generator.type.JParameterizedType;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

public class StdDeserializerExtension extends Extension {

  @Override
  public EmitterExtensionFeatures getFeatures() {
    final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
    return features;
  }

  @Override
  public List<TransformerDefinition> getTransformers() {
    return Arrays.asList(
        new TransformerDefinition(ModelCompiler.TransformationPhase.BeforeTsModel, new ModelTransformer() {
          @Override
          public TsModel transformModel(SymbolTable symbolTable, TsModel tsModel) {
            return tsModel;
          }

          @Override
          public Model transformModel(SymbolTable symbolTable, Model model) {
            final List<BeanModel> beans = model.getBeans();
            beans.replaceAll(bean -> transformBean(bean));
            return model;
          }
        }));
  }

  protected BeanModel transformBean(BeanModel bean) {
    try {
      List<PropertyModel> properties = bean.getProperties().stream().map(property -> transformProperty(property))
          .collect(Collectors.toList());
      return new BeanModel(
          bean.getOrigin(),
          bean.getParent(),
          bean.getTaggedUnionClasses(),
          bean.getDiscriminantProperty(),
          bean.getDiscriminantLiteral(),
          bean.getInterfaces(),
          properties,
          bean.getComments());
    } catch (Exception e) {
      TypeScriptGenerator.getLogger()
          .verbose(String.format("StdDeserializerExtension raised error: ", e.getMessage()));
      return bean;
    }
  }

  protected PropertyModel transformProperty(PropertyModel property) {
    try {
      Member member = property.getOriginalMember();
      Type originalType = property.getType();

      if (member instanceof Field && originalType instanceof JParameterizedType) {
        Field field = (Field) member;
        JParameterizedType parameterizedType = (JParameterizedType) originalType;
        JsonDeserialize jsonDeserialize = field.getAnnotation(JsonDeserialize.class);
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        if (jsonDeserialize != null
            && jsonDeserialize.using().getSuperclass() == StdDeserializer.class
            && actualTypeArguments.length == 1) {

          Type[] newArgument = new Type[] { String.class, actualTypeArguments[0] };
          JParameterizedType newType = new JParameterizedType(Map.class, newArgument, null);

          return new PropertyModel(
              property.getName(),
              newType,
              property.isOptional(),
              property.getAccess(),
              property.getOriginalMember(),
              property.getPullProperties(),
              property.getContext(),
              property.getComments());
        }
      }

      return property;
    } catch (Exception e) {
      return property;
    }
  }
}

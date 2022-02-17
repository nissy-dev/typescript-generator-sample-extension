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

import java.lang.reflect.Field;

public class SampleExtension extends Extension {

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
                    final Object instance = originClass.getConstructor().newInstance();
                    final Field field = originClass.getField(tsProperty.getName());
                    field.setAccessible(true);
                    if (field.get(instance) != null && tsProperty.tsType instanceof TsType.UnionType) {
                        final TsType.UnionType unionType = (TsType.UnionType) tsProperty.tsType;
                        return tsProperty.withTsType(unionType.remove(Arrays.asList(TsType.Null)));
                    }

                    return tsProperty;
                } catch (Exception e) {
                    return tsProperty;
                }
            }).collect(Collectors.toList());
            return tsBean.withProperties(properties);
        } catch (Exception e) {
            TypeScriptGenerator.getLogger().verbose(String.format("SampleExtension raised error: ", e.getMessage()));
            return tsBean;
        }
    }

}

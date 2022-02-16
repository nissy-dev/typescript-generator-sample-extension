package dev.nissy.typescript.generator.extensions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cz.habarta.typescript.generator.Extension;
import cz.habarta.typescript.generator.compiler.ModelCompiler;
import cz.habarta.typescript.generator.compiler.TsModelTransformer;
import cz.habarta.typescript.generator.emitter.TsBeanModel;
import cz.habarta.typescript.generator.emitter.TsModel;
import cz.habarta.typescript.generator.emitter.EmitterExtensionFeatures;

public class SampleExtension extends Extension {

    @Override
    public EmitterExtensionFeatures getFeatures() {
        final EmitterExtensionFeatures features = new EmitterExtensionFeatures();
        return features;
    }

    @Override
    public List<TransformerDefinition> getTransformers() {

        return Arrays
                .asList(new TransformerDefinition(ModelCompiler.TransformationPhase.AfterDeclarationSorting,
                        this::transformModel));
    }

    protected TsModel transformModel(TsModelTransformer.Context context, TsModel model) {
        final List<TsBeanModel> beans = model.getBeans().stream()
                .map(bean -> transformBean(context, bean))
                .collect(Collectors.toList());
        return model.withBeans(beans);
    }

    protected TsBeanModel transformBean(TsModelTransformer.Context context, TsBeanModel tsBean) {
        return tsBean;
    }

}

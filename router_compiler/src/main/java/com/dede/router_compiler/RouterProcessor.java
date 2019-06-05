package com.dede.router_compiler;

import com.dede.router.annotations.Route;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Router注解解析
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({"debug"})
@SupportedAnnotationTypes({"com.dede.router.annotations.OnIntercept", "com.dede.router.annotations.OnIntercept"})
public final class RouterProcessor extends AbstractProcessor {

    private final static String PACKAGE_NAME = "com.dede.router";

    private HashMap<String, CompontInfo> routerMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        log("RouterProcessor init");
    }

    private void log(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log("RouterProcessor process");
        findAndParseRouter(roundEnv);
        buildRouterFile();

        if (getSupportedOptions().contains("debug")) {
            throw new IllegalStateException("Router debug mode");
        }
        return true;
    }

    private void buildRouterFile() {
        log("buildRouterFile");

        CodeBlock.Builder staticCodeBuilder = CodeBlock.builder();// 静态代码块

        ClassName builderClass = ClassName.get(PACKAGE_NAME, "Compont.Builder");
        staticCodeBuilder.addStatement("$T c", ClassName.get(PACKAGE_NAME, "Compont"));// 声明一个Compont引用
        for (Map.Entry<String, CompontInfo> entry : routerMap.entrySet()) {
            CompontInfo info = entry.getValue();

            String format = "c = new $T(%s)\n" +
                    ".target(%s.class)\n" +
                    ".parserParams(%b)\n" +
                    ".typeCase(%b)\n" +
                    ".build()";

            StringBuilder sb = new StringBuilder();
            for (String router : info.route.route()) {
                sb.append("\"")
                        .append(router)
                        .append("\"")
                        .append(" ,");
            }
            sb.deleteCharAt(sb.length() - 1);
            String code = String.format(format, sb.toString(), info.className, info.route.parserParams(), info.route.typeCase());

            staticCodeBuilder.addStatement(code, builderClass);
            staticCodeBuilder.addStatement("compontList.add(c)");
        }


        FieldSpec fieldSpec = FieldSpec.builder(ArrayList.class, "compontList",
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)// 静态ArrayList
                .initializer("new $T()", ArrayList.class)
                .build();
        TypeSpec typeSpec = TypeSpec.classBuilder("Router_Inject")// 生成Router_Inject.java
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Generated code from Router. Do not modify!\n")
                .addField(fieldSpec)// 添加属性
                .addStaticBlock(staticCodeBuilder.build())// 添加静态代码块
                .build();
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, typeSpec)
                .build();
        try {
            javaFile.writeTo(this.processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findAndParseRouter(RoundEnvironment roundEnv) {
        log("findAndParseRouter");
        routerMap.clear();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Route.class);
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getQualifiedName().toString();// 完整类名
            Route route = element.getAnnotation(Route.class);

            CompontInfo info = new CompontInfo();
            info.className = className;
            info.route = route;
            info.typeElement = typeElement;

            routerMap.put(className, info);
        }
    }

}

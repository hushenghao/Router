package com.dede.router_compiler

import com.dede.router.annotations.Route
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * Router注解解析
 */
@AutoService(Processor::class)
//@SupportedOptions("debug")
@SupportedAnnotationTypes("com.dede.router.annotations.Route", "com.dede.router.annotations.OnIntercept")
class RouterProcessor : AbstractProcessor() {

    private val routerMap = HashMap<String, CompontInfo>()

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        log("RouterProcessor init")
    }

    private fun log(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, msg)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        log("RouterProcessor process")
        findAndParseRouter(roundEnv)

        findAndParseOnIntercept(roundEnv)

        buildRouterFile()

        if (supportedOptions.contains("debug")) {
            throw IllegalStateException("Router debug mode")
        }
        return true
    }

    private fun findAndParseOnIntercept(roundEnv: RoundEnvironment) {

    }

    private fun buildRouterFile() {
        log("buildRouterFile")

        val staticCodeBuilder = CodeBlock.builder()// 静态代码块

        staticCodeBuilder.addStatement("\$T c", COMPONT_NAME)// 声明一个Compont引用
        val format = "c = new \$T(%s)\n" +
                ".target(%s)\n" +
                ".parserParams(%b)\n" +
                ".typeCase(%b)\n" +
                ".build()"
        for ((_, info) in routerMap) {
            val code = String.format(
                format,
                info.route.route.joinToString { "\"$it\"" },
                "${info.className}.class",
                info.route.parserParams,
                info.route.typeCase
            )

            staticCodeBuilder.addStatement(code, COMPONT_BUILDER_NAME)
            staticCodeBuilder.addStatement("compontList.add(c)")
        }


        val fieldSpec = FieldSpec.builder(
            ArrayList::class.java, "compontList",
            Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL
        )// 静态ArrayList
            .initializer("new \$T()", ArrayList::class.java)
            .build()
        val typeSpec = TypeSpec.classBuilder("Router_Inject")// 生成Router_Inject.java
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addJavadoc("Generated code from Router. Do not modify!\n")
            .addField(fieldSpec)// 添加属性
            .addStaticBlock(staticCodeBuilder.build())// 添加静态代码块
            .build()
        val javaFile = JavaFile.builder(PACKAGE_NAME, typeSpec)
            .build()
        try {
            javaFile.writeTo(this.processingEnv.filer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun findAndParseRouter(roundEnv: RoundEnvironment) {
        log("findAndParseRouter")
        routerMap.clear()
        val elements = roundEnv.getElementsAnnotatedWith(Route::class.java)
        for (element in elements) {
            val typeElement = element as TypeElement
            val className = typeElement.qualifiedName.toString()// 完整类名
            val route = element.getAnnotation(Route::class.java)

            val info = CompontInfo()
            info.className = className
            info.route = route

            routerMap[className] = info
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.dede.router"
        private val COMPONT_BUILDER_NAME = ClassName.get(PACKAGE_NAME, "Compont.Builder")
        private val COMPONT_NAME = ClassName.get(PACKAGE_NAME, "Compont")
    }

}

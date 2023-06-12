package com.zlib.plugin

import com.zlib.annotation_processor.ZLibAnnotationProcessor
import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

import javax.annotation.processing.AbstractProcessor
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

class HelperPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.task('hello') {
            dependsOn('build');
            doLast {
                println("Hello world")
            }
        }

        project.android.sourceSets.all { sourceSet ->
            println(sourceSet.name)
            if(!sourceSet.name.toString().startsWith("test")) {
                def sourceSetNameFormatted = sourceSet.name.toString().replaceAll("([a-z])([A-Z])", "${1}/${2}").toLowerCase()
                def buildDir = project.buildDir.path + "/generated/res/zlib-services/" + sourceSetNameFormatted
                sourceSet.res.srcDirs += buildDir
            }
        }

        project.afterEvaluate {

            project.android.applicationVariants.all { variant ->
                def buildDir = project.buildDir.path + "/generated/res/lib-services/"
                def splitDirByBuildVariant = variant.name.toString().replaceAll("([a-z])([A-Z])", "${1}/${2}").toLowerCase()
                buildDir = buildDir + splitDirByBuildVariant + "/values"
                if(!project.file(buildDir).exists()) {
                    new File(buildDir).mkdirs()
                }
                buildDir = buildDir + File.separator + "strings.xml"
                project.file(buildDir).withWriter { writer ->
                    def xml = new MarkupBuilder(new IndentPrinter(writer, "\t", true))

                    xml.doubleQuotes = true
                    xml.mkp.xmlDeclaration(version: '1.0', encoding: 'utf-8')
                    xml.resources() {
                        string(name: "project_id", "123456789")
                        mkp.yield('\n    ')
                    }
                }

                project.android.sourceSets.all { sourceSet ->
                    if (sourceSet.name == variant.name) {
                        sourceSet.res.srcDirs += buildDir
                    }
                }

            }

            def javaCompileTask = project.tasks.getByName("compileDebugJavaWithJavac")
//            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler()
//            compiler.run(null, null, null, "-proc:only", "-processor")
//            javaCompileTask.configure(t -> {
//                t.sourceSets.main.java.srcDirs("src/main/java")
//                t.options.annotationProcessorPath = project.configurations.annotationProcessor
//                t.options.compilerArgs += [
//                        '-proc:only',
//                        "-AannotationName=com.zlib.annotation.AppModule",
//                        "-s", project.buildDir.path + '/generated/sources/annotation-processor'
//                ]
//
//                t.options.compilerArgs += [
//                        '-processor', ZLibAnnotationProcessor.canonicalName
//                ]
//            })

//            def scan = project.tasks.register("scanWithAnnotation", ScanWithAnnotationTask) {
//                annotationProcessor.set(new ZLibAnnotationProcessor())
//                annotationName.set("com.zlib.annotation.AppModule")
//
//            }
//            javaCompileTask.dependsOn(scan.get())
//            def jvaCompileTask = project.tasks.register("scanCustomTask", JavaCompile) {
//                it.source = project.sourceSets.main.java
//                it.classpath = project.configurations.compile +
//            }
            def compileDebugTask = project.tasks.getByName("compileDebugJavaWithJavac")
            def scanWithAnnotation = project.tasks.register("scanWithAnnotation", ScanWithAnnotationTask) {
                annotationProcessor.set(new ZLibAnnotationProcessor())
                annotationName.set("com.zlib.annotation.AppModule")
                compileTask.set(compileDebugTask)
            }
            compileDebugTask.dependsOn(scanWithAnnotation)

        }
//        project.plugins.withType(JavaPlugin.class, javaPlugin -> {
//            JavaCompile javaCompileTask = (JavaCompile) project.getTasks().getByName("compileJava");
//            javaCompileTask.doLast(task -> {
//                ZLibAnnotationProcessor annotationProcessor = new ZLibAnnotationProcessor();
//                annotationProcessor.init(javaCompileTask.getProcessingEnvironment());
//                annotationProcessor.process(javaCompileTask.getAnnotationProcessor().getSupportedAnnotationTypes(),
//                        javaCompileTask.getAnnotationProcessor().getRoundEnvironment());
//            });
//
//        })
    }

}



abstract class ScanWithAnnotationTask extends DefaultTask {
    @Input
    abstract Property<AbstractProcessor> getAnnotationProcessor()

    @Input
    abstract Property<String> getAnnotationName()

    @Internal
    abstract Property<Task> getCompileTask()

    @TaskAction
    void scanClassesWithAnnotation() {
        println ZLibAnnotationProcessor.class

        def annotationProcessor = annotationProcessor.get()
        def annotationName = annotationName.get()

        println "Scanning classes with annotation: $annotationName"
        def compileTask = compileTask.get()
        def compileJavaOptions = compileTask.options
        compileJavaOptions.compilerArgs += [
                '-processor', "${project.buildDir}tmp/buildSrc/com/zlib/annotation_processor/ZLibAnnotationProcessor",
                '-AannotationName=' + annotationName
        ]
//
//        def javaCompileTask = project.tasks.getByName("compileDebugJavaWithJavac")
//        javaCompileTask.doLast {
//            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler()
//            compiler.run(null, null, null, "-proc:only", "-processor", annotationProcessor.get().getClass().getName(), "-AannotationName=" + annotationName.get())
//        }

    }
}
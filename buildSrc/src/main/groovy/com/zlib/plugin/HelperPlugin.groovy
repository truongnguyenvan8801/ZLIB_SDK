package com.zlib.plugin

import groovy.xml.MarkupBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

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

            TaskProvider<JavaCompile> javaCompileTask = project.buildDir.resolve("generated/source/processors");
            javaCompileTask = project.tasks.named("compileJava", JavaCompile)
            javaCompileTask.configure(t -> {
                options.annotationProcessorGeneratedSourcesDirectory = project.buildDir.resolve
            })
        }
    }
}
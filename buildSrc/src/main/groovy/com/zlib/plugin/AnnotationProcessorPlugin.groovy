package com.zlib.plugin

import io.github.classgraph.ClassGraph
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class AnnotationProcessorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.afterEvaluate {
            if(project.plugins.hasPlugin("com.android.application")) {
                project.android.applicationVariants.all { variant ->
                    def variantNameCapitalize = variant.name.toString().capitalize()
                    Task scanTask = project.tasks.create("scanClasses${variantNameCapitalize}WithAnnotation", ScanClassesWithAnnotationTask) {
                        buildVariant.set(variant.name)
                        packageName.set(project.android.namespace)
                        annotationName.set("com.zlib.library01.AppModule")
                    }
                    project.tasks.getByName("compile${variantNameCapitalize}JavaWithJavac").dependsOn(scanTask)
                }
            }
        }
    }
}

abstract class ScanClassesWithAnnotationTask extends DefaultTask {

    @Input
    abstract Property<String> getBuildVariant()

    @Input
    abstract Property<String> getAnnotationName()

    @Input
    abstract Property<String> getPackageName()


    @TaskAction
    def scan() {
        def buildVariantCapitalize = buildVariant.get().toString().capitalize()
        def compileTask = project.tasks.getByName("compile${buildVariantCapitalize}JavaWithJavac")
        def mergeTask = project.tasks.getByName("merge${buildVariantCapitalize}Resources")
        compileTask.doLast {
            def packageToScan = getPackageName().get()
            def annotationName = getAnnotationName().get()
            def simpleNameAnnotation = getAnnotationName().get().toString().split("\\.")
            if(simpleNameAnnotation.size() == 0) {
                simpleNameAnnotation = getAnnotationName().get()
            } else {
                simpleNameAnnotation = simpleNameAnnotation.getAt(simpleNameAnnotation.size() - 1)
            }
            def scanResultFileName = "scanClassesWithType${simpleNameAnnotation}.txt"

            def scanResultDir = compileTask.temporaryDir.toString() + File.separator + "res" + File.separator + "raw" + File.separator
            new File(scanResultDir).mkdirs()
            def scanResultPath = scanResultDir + scanResultFileName

            def classPackageRoot = compileTask.destinationDir

            println("Scanning $classPackageRoot")
            new ClassGraph().verbose().enableAllInfo().acceptPackages(packageToScan).overrideClasspath(classPackageRoot).scan().withCloseable { scanResult ->
                def resultList = scanResult.getClassesWithAnnotation(annotationName)
                def outputFile = new File(scanResultPath)
                outputFile.withWriter { writer ->
                    resultList.each {ci ->
                        writer.println(ci.getName())
                        println "Found annotated class: ${ci.getName()}"
                    }
                }

                println("Wrote scan result to $outputFile ; size = ${outputFile.length()}")
            }

            def aapt2Path = project.android.getSdkDirectory().toPath().resolve("build-tools")
                                .resolve(project.android.buildToolsVersion).resolve("aapt2")
            def flatFileOutputPrefix = mergeTask.outputDir.get().toString() + "/"
            def cmd = [aapt2Path, "compile", "-o", flatFileOutputPrefix, scanResultPath]
            println "Executing: " + cmd.join(" ")
            def exec = cmd.execute()
            exec.in.eachLine {line -> println line}
            exec.err.eachLine {line -> System.err.println "ERROR: " + line}
            exec.waitFor()
            println "Wrote compiled resource as a .flat file to " + flatFileOutputPrefix
        }

    }
}

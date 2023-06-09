package com.zlib.plugin

import io.github.classgraph.ClassGraph
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

class AnnotationProcessorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.afterEvaluate {
            if(project.plugins.hasPlugin("com.android.application")) {
                project.android.applicationVariants.all { variant ->
                    def variantNameCapitalize = variant.name.toString().capitalize()
                    def compileTask = project.tasks.getByName("compile${variantNameCapitalize}JavaWithJavac")
                    def mergeTask = project.tasks.getByName("merge${variantNameCapitalize}Resources")
                    def annotation = "com.zlib.library01.AppModule"
                    def simpleNameAnnotation = annotation.split("\\.")
                    if(simpleNameAnnotation.size() == 0) {
                        simpleNameAnnotation = annotation
                    } else {
                        simpleNameAnnotation = simpleNameAnnotation.getAt(simpleNameAnnotation.size() - 1)
                    }
                    def scanResultFileName = "scanClassesWithType${simpleNameAnnotation}.txt"
                    def scanResultDir = compileTask.temporaryDir.toString() + File.separator + "res" + File.separator + "raw" + File.separator
                    new File(scanResultDir).mkdirs()
                    def scanResultPath = scanResultDir + scanResultFileName
                    if(!project.file(scanResultPath).exists()) {
                        project.file(scanResultPath).createNewFile()
                    }
                    TaskProvider<ScanClassesWithAnnotationTask> scanTask = project.tasks.register("scanClasses${variantNameCapitalize}WithAnnotation", ScanClassesWithAnnotationTask) {
                        buildVariant.set(variant.name)
                        packageName.set(project.android.namespace)
                        annotationName.set("com.zlib.library01.AppModule")
                        inputFile.set(new File(scanResultPath))
                        outputDirectory.set(new File(mergeTask.outputDir.get().toString() + "/"))
                    }

                    project.tasks.getByName("compile${variantNameCapitalize}JavaWithJavac").dependsOn(scanTask.get())
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

    @InputFile
    abstract RegularFileProperty getInputFile()

    @OutputDirectory
    abstract DirectoryProperty getOutputDirectory()

    @TaskAction
    def scan() {
        def buildVariantCapitalize = buildVariant.get().toString().capitalize()
        def compileTask = project.tasks.getByName("compile${buildVariantCapitalize}JavaWithJavac")
        compileTask.doLast {
            def packageToScan = getPackageName().get()
            def annotationName = getAnnotationName().get()

            def classPackageRoot = compileTask.destinationDir

            println("Scanning $classPackageRoot")
            new ClassGraph().verbose().enableAllInfo().acceptPackages(packageToScan).overrideClasspath(classPackageRoot).scan().withCloseable { scanResult ->
                def resultList = scanResult.getClassesWithAnnotation(annotationName)
                def outputFile = inputFile.get().asFile
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
            def flatFileOutputPrefix = outputDirectory.get()
            def cmd = [aapt2Path, "compile", "-o", flatFileOutputPrefix.asFile.path, inputFile.get().asFile.path]
            println "Executing: " + cmd.join(" ")
            def exec = cmd.execute()
            exec.in.eachLine {line -> println line}
            exec.err.eachLine {line -> System.err.println "ERROR: " + line}
            exec.waitFor()
            println "Wrote compiled resource as a .flat file to " + flatFileOutputPrefix.asFile.path
        }

    }
}

package com.zlib.plugin

import io.github.classgraph.ClassGraph
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

class AnnotationProcessorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        Task scanTask = project.tasks.create("scanClassesWithAnnotation", ScanClassesWithAnnotationTask)
        project.tasks.register("processFlatFile", ProcessFlatFileTask)
        project.afterEvaluate {
            project.tasks.compileDebugJavaWithJavac.doLast {
                scanTask.dependsOn "compileDebugJavaWithJavac"
            }
//            def compileTask = project.tasks.findByName("compileDebugJavaWithJavac")
//            def mergeTask = project.tasks.findByName("mergeDebugResources")
//            def scanTask = project.tasks.findByName("scanClasses")
//            def processTask = project.tasks.findByName("processFlatFile")
//            if(compileTask && mergeTask && scanTask && processTask) {
//                scanTask.dependsOn compileTask, mergeTask
//                scanTask.finalizedBy processTask
//                project.gradle.taskGraph.whenReady {taskGraph ->
//                    if(scanTask in taskGraph.getAllTasks() && processTask in taskGraph.getAllTasks()) {
//                        scanTask.mustRunAfter mergeTask
//                        processTask.mustRunAfter scanTask
//                    }
//                }
//            }
//            def compileTask = project.tasks.compileDebugJavaWithJavac
//            def mergeTask = project.tasks.mergeDebugResources
//            def scanResultFilename = "scan_result.txt"
//
//            def packageToScan = getPackageName()
//            def annotationName = getAnnotationName()
//            def scanResultDir = compileTask.temporaryDir.toString() + File.separator + "res" + File.separator + "raw" + File.separator
//            new File(scanResultDir).mkdirs()
//
//            def scanResultPath = scanResultDir + scanResultFilename
//
//            def classPackageRoot = compileTask.destinationDir
//            println("Scanning $classPackageRoot")
//
//            new ClassGraph().verbose().enableAllInfo().acceptPackages(packageToScan).overrideClasspath(classPackageRoot).scan().withCloseable { scanResult ->
//                def resultList = scanResult.getClassesWithAnnotation(annotationName)
////                resultList.each { ci ->
////                    println "Found annotated class: ${ci.getName()}"
////                }
//                def outputFile = new File(scanResultPath)
//                outputFile.withWriter { writer ->
//                    resultList.each {ci ->
//                        writer.println(ci.getName())
//                        println "Found annotated class: ${ci.getName()}"
//                    }
//                }
//
//                println("Wrote scan result to $outputFile ; size = ${outputFile.length()}")
//            }
//
//            def aapt2Path = project.android.getSdkDirectory().toPath().resolve("build-tools")
//                                .resolve(project.android.buildToolsVersion).resolve("aapt2")
//            def flatFileOutputPrefix = mergeTask.outputDir.get().toString() + "/"
//            def cmd = [aapt2Path, "compile", "-o", flatFileOutputPrefix, scanResultPath]
//            println "Executing: " + cmd.join(" ")
//            def exec = cmd.execute()
//            exec.in.eachLine {line -> println line}
//            exec.err.eachLine {line -> System.err.println "ERROR: " + line}
//            exec.waitFor()
//            println "Wrote compiled resource as a .flat file to " + flatFileOutputPrefix

        }
    }

    static def getPackageName() {
        return "com.zlib"
    }

    static def getAnnotationName() {
        return "com.zlib.library01.AppModule"
    }
}

class ScanClassesWithAnnotationTask extends DefaultTask {

    @TaskAction
    def scan() {
        doLast {
            def packageToScan = AnnotationProcessorPlugin.getPackageName()
            def annotationName = AnnotationProcessorPlugin.getAnnotationName()
            def compileTask = project.tasks.findByName("compileDebugJavaWithJavac")
            def scanResultDir = compileTask.temporaryDir.toString() + File.separator + "res" + File.separator + "raw" + File.separator
            new File(scanResultDir).mkdirs()

            def scanResultPath = scanResultDir + scanResultFilename

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
        }

    }
}

class ProcessFlatFileTask extends DefaultTask {

    @TaskAction
    void process() {
        def mergeTask = project.tasks.findByName("mergeDebugResources")
        if (mergeTask) {
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
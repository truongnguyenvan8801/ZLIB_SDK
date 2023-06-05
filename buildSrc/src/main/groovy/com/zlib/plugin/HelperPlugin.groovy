package com.zlib.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class HelperPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('hello') {
            dependsOn('build');
            doLast {
                println("Hello world")
            }
        }
        project.afterEvaluate {
//            project.dependencies.add("implementation", project.dependencies.create(JavaPlugin.))
            project.dependencies.add("implementation", project.files("libs/library01-debug.aar"))
        }
    }
}
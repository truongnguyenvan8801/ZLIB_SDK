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
    }
}
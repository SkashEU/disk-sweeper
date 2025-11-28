import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class InternalComposeLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            applyPluginsByName(
                "sweeper-internal-kmp-module",
                "composeMultiplatform",
                "composeCompiler"
            )

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain {
                    dependencies {
                        implementation(composeExtension.dependencies.runtime)
                        implementation(composeExtension.dependencies.foundation)
                        implementation(composeExtension.dependencies.ui)
                        api(findLibraryByName("material3"))
                        api(composeExtension.dependencies.materialIconsExtended)
                        implementation(composeExtension.dependencies.animation)
                        implementation(composeExtension.dependencies.animationGraphics)
                        implementation(findLibraryByName("androidx-lifecycle-runtimeCompose"))

                        implementation(composeExtension.dependencies.components.resources)
                        implementation(composeExtension.dependencies.components.uiToolingPreview)
                    }
                }
            }
        }
    }
}

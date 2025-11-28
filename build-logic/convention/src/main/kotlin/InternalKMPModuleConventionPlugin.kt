import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class InternalKMPModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            applyPluginsByName(
                "kotlinMultiplatform",
            )

            extensions.configure<KotlinMultiplatformExtension> {
                jvmToolchain(JVM_TOOLCHAIN_VERSION)

                jvm()

                sourceSets.commonMain {
                    dependencies {
                        implementation(findLibraryByName("kotlin-stdlib"))
                        implementation(findLibraryByName("kotlinx-coroutines-core"))
                    }
                }

                sourceSets.jvmMain {
                    dependencies {
                        implementation(findLibraryByName("kotlinx-coroutines-swing"))
                    }
                }

                compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

plugins {
    `kotlin-dsl`
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("internalKmpLibrary") {
            id =
                libs.plugins.sweeper.internal.kmp.module
                    .get()
                    .pluginId
            implementationClass = "InternalKMPModuleConventionPlugin"
        }
        register("internalComposeLibrary") {
            id =
                libs.plugins.sweeper.internal.compose.module
                    .get()
                    .pluginId
            implementationClass = "InternalComposeLibraryConventionPlugin"
        }
    }
}

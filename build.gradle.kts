import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.binaryCompat)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

apply(from = "gradle/publishing.gradle.kts")

yarn.lockFileDirectory = file("gradle/kotlin-js-store")

val testApp: String? by extra

repositories {
    mavenCentral()
}

kotlin {
    js {
        nodejs()
    }
    jvm()
    linuxX64()
    mingwX64()
    macosArm64()
    macosX64 {
        if (testApp?.toBoolean() == true) {
            binaries {
                executable()
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val posixMain by creating {
            dependsOn(commonMain)
        }
        val macosX64Main by getting {
            dependsOn(posixMain)
            if (testApp?.toBoolean() == true) {
                kotlin.srcDirs("src/macosX64Runner/kotlin")
            }
        }
        val macosArm64Main by getting {
            dependsOn(macosX64Main)
        }
        val linuxX64Main by getting {
            dependsOn(posixMain)
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-js"))
            }
        }
    }
}

spotless {
    kotlin {
        target("**/**.kt")
        ktlint(libs.versions.ktlint.get())
            .setUseExperimental(true)
            .editorConfigOverride(mapOf(
                "disabled_rules" to "no-wildcard-imports,no-unused-imports,trailing-comma"
            ))
    }
}

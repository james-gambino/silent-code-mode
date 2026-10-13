plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.10.4"
}

group = "dev.silentmode"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1")
    }
}

kotlin { jvmToolchain(21) }

intellijPlatform {
    signing {
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
    }
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
    pluginVerification {
        ides {
            create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.Rider, "2025.1")
            create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.WebStorm, "2025.1")
            create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.PyCharm, "2025.1")
            create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.Rider, "2025.3")
            create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.WebStorm, "2025.3")
            create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.PyCharm, "2025.3")
            latest {
                types = listOf(
                    org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.Rider,
                    org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.WebStorm,
                    org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.PyCharm
                )
            }
        }
    }
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }
    }
}

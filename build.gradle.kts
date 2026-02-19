plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "io.lcalmsky.github"
version = "0.1.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val platformType = providers.gradleProperty("platformType")
        val platformVersion = providers.gradleProperty("platformVersion")
        create(platformType, platformVersion)
        // instrumentationTools() - no longer needed in 2.x
    }
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks {
    patchPluginXml {
        version = "0.1.1"
        sinceBuild = "243"
        untilBuild = "253.*"
    }

    test {
        useJUnitPlatform()
    }
}

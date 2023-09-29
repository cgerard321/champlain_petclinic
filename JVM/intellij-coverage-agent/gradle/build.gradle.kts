import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    id("org.jetbrains.kotlinx.kover") version "0.7.2"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

koverReport {
    filters {
        includes {
            classes("org.example.*")
        }
    }
    // uncomment to fall back to XML reports. Not supported by Qodana IDE plugin
    /*
    defaults {
        xml {
            onCheck = false
            setReportFile(layout.projectDirectory.file(".qodana/code-coverage/result.xml").asFile)
        }
    }
    */
}

tasks.test {
    useJUnitPlatform()
    // uncomment to fall back to XML reports. Also need to comment finalizedBy(tasks.koverVerify) out.
    // finalizedBy(tasks.koverXmlReport)
    finalizedBy(tasks.koverVerify) // you will find the report in build/kover/bin-reports
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}

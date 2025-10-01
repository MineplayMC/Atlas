import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    group = "be.esmay"
    version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "maven-publish")

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.38")
        annotationProcessor("org.projectlombok:lombok:1.18.38")

        implementation("org.spongepowered:configurate-yaml:4.2.0")

    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }
    }
    tasks.withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    tasks.withType<ShadowJar> {
        archiveClassifier.set("")
        mergeServiceFiles()
    }
}
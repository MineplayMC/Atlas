import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("maven-publish")
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


    if (findProperty("mineplayUsername") != null && findProperty("mineplayPassword") != null) {
        publishing {
            repositories {
                maven {
                    name = "mineplay"
                    url = uri("https://repo.mineplay.nl/private")
                    credentials {
                        username = findProperty("mineplayUsername") as String
                        password = findProperty("mineplayPassword") as String
                    }
                    authentication {
                        create<BasicAuthentication>("basic")
                    }
                }
            }
            publications {
                create<MavenPublication>("maven") {
                    groupId = "be.esmay"
                    artifactId = project.name
                    version = version
                    from(components["java"])
                }
            }
        }
    }

}
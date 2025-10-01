import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":atlas-common"))
    implementation("com.github.thebathduck.modulemanager:velocity:7b2cd5708b")

    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.2")
    implementation("com.github.Jazzkuh:storm:34f9f28684")

    compileOnly("io.netty:netty-all:4.1.100.Final")
}

tasks.named("compileJava") {
    dependsOn(":atlas-common:shadowJar")
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(":atlas-common:shadowJar")

    exclude("io/netty/**")
    exclude("com/google/gson/**")
    exclude("org/slf4j/**")
    exclude("ch/qos/logback/**")
}
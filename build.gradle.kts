plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
}

group = "com.zenith"
version = "1.0.0"

repositories {
    mavenLocal()
    maven("https://maven.parchmentmc.org")
    maven("https://maven.2b2t.vc/releases")
    maven("https://maven.2b2t.vc/remote")
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

loom {
    accessWidenerPath = file("src/main/resources/zenithproxy.accesswidener")
    runs {
        getByName("server") {
            ideConfigGenerated(true)
            server()
            property("data.dir", project.layout.buildDirectory.file("data").get().asFile.absolutePath)
        }
    }
}
val lombokVersion = "1.18.42"

dependencies {
    minecraft("com.mojang:minecraft:26.1.1")
    implementation("net.fabricmc:fabric-loader:0.18.5")
    implementation("net.fabricmc.fabric-api:fabric-api:0.144.4+26.1")
    implementation("com.palantir.javapoet:javapoet:0.10.0")
    implementation("com.github.rfresh2:MCProtocolLib:26.1.1.1") {
        exclude(group = "io.netty")
    }
    implementation("org.cloudburstmc.math:immutable:2.0")
    implementation("com.zenith:ZenithProxy:26.1.0-SNAPSHOT") {
        isTransitive = false
    }
    api(platform("tools.jackson:jackson-bom:3.1.0"))
    api("tools.jackson.core:jackson-databind")
    api("tools.jackson.dataformat:jackson-dataformat-smile")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

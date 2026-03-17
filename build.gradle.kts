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
    minecraft("com.mojang:minecraft:26.1-pre-3")
//    mappings(loom.layered {
//        officialMojangMappings()
//        parchment("org.parchmentmc.data:parchment-1.21.11:2025.12.20@zip")
//    })
    implementation("net.fabricmc:fabric-loader:0.18.4")
    implementation("net.fabricmc.fabric-api:fabric-api:0.143.14+26.1")
    implementation("com.palantir.javapoet:javapoet:0.10.0")
    implementation("com.github.rfresh2:MCProtocolLib:1.21.11.8") {
        exclude(group = "io.netty")
    }
    implementation("org.cloudburstmc.math:immutable:2.0")
    implementation("com.zenith:ZenithProxy:1.21.11-SNAPSHOT") {
        isTransitive = false
    }
    api(platform("tools.jackson:jackson-bom:3.0.4"))
    api("tools.jackson.core:jackson-databind")
    api("tools.jackson.dataformat:jackson-dataformat-smile")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

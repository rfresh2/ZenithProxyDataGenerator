plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
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
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

loom {
    accessWidenerPath = file("src/main/resources/zenithproxy.accesswidener")
    runs {
        getByName("server") {
            generateRunConfig = true
            server()
            systemProperties.put("data.dir", project.layout.buildDirectory.file("data").get().asFile.absolutePath)
        }
    }
}
val lombokVersion = "1.18.46"

dependencies {
    minecraft("com.mojang:minecraft:1.21.4")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.4:2025.03.23@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:0.19.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.119.4+1.21.4")
    implementation("com.palantir.javapoet:javapoet:0.19.0")
    implementation("com.github.rfresh2:MCProtocolLib:1.21.4.57") {
        exclude(group = "io.netty")
    }
    implementation("org.cloudburstmc.math:immutable:2.0")
    implementation("com.zenith:ZenithProxy:1.21.4-SNAPSHOT") {
        isTransitive = false
    }
    api(platform("tools.jackson:jackson-bom:3.2.2"))
    api("tools.jackson.core:jackson-databind")
    api("tools.jackson.dataformat:jackson-dataformat-smile")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

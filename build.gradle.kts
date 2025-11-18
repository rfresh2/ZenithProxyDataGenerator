plugins {
    id("fabric-loom") version "1.13-SNAPSHOT"
}

group = "com.zenith"
version = "1.0.0"

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://maven.2b2t.vc/releases")
    maven("https://maven.2b2t.vc/remote")
    mavenCentral()
    mavenLocal()
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
    minecraft("com.mojang:minecraft:1.21.10")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.10:2025.10.12@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:0.17.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.138.3+1.21.10")
    implementation("com.palantir.javapoet:javapoet:0.8.0")
    implementation("com.github.rfresh2:MCProtocolLib:1.21.10.8") {
        isTransitive = false
    }
    implementation("org.cloudburstmc.math:immutable:2.0")
    implementation("com.zenith:ZenithProxy:1.21.10-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

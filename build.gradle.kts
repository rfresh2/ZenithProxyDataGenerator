plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
}

group = "com.zenith"
version = "1.0.0"

repositories {
    mavenLocal()
    maven("https://maven.parchmentmc.org")
    maven("https://maven.2b2t.vc/releases")
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
val lombokVersion = "1.18.38"

dependencies {
    minecraft("com.mojang:minecraft:1.21")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:2024.11.10@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:0.16.10")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.100.1+1.21")
    implementation("com.palantir.javapoet:javapoet:0.7.0")
    implementation("com.github.rfresh2:MCProtocolLib:1.21.0.48") {
        isTransitive = false
    }
    implementation("org.cloudburstmc.math:immutable:2.0")
    implementation("com.zenith:ZenithProxy:1.21.0-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
}

group = "com.zenith"
version = "1.0.0"

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://maven.2b2t.vc/releases") {
        content {
            includeGroupByRegex("com.github.rfresh2.*")
            includeGroup("com.zenith")
        }
    }
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
    minecraft("com.mojang:minecraft:1.21.5")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.4:2025.03.16@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:0.16.10")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.124.2+1.21.5")
    implementation("com.palantir.javapoet:javapoet:0.7.0")
    implementation("com.github.rfresh2:MCProtocolLib:1.21.5.13") {
        isTransitive = false
    }
    implementation("com.zenith:ZenithProxy:1.21.5-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

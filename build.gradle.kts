plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
}

group = "com.zenith"
version = "1.0.0"

repositories {
    mavenLocal {
        content { includeGroup("com.zenith") }
    }
    mavenCentral()
    maven("https://maven.2b2t.vc/releases") {
        content { includeGroupByRegex("com.github.rfresh2.*") }
    }
    maven("https://maven.parchmentmc.org")
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
val lombokVersion = "1.18.34"

dependencies {
    minecraft("com.mojang:minecraft:1.21")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:2024.07.28@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:0.15.11")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.100.1+1.21")
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.github.rfresh2:MCProtocolLib:1.21.0.7") {
        exclude("*")
    }
    implementation("com.zenith:ZenithProxy:1.21.0") {
        exclude("*")
    }
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

plugins {
    kotlin("jvm") version "2.2.20"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.vhoyd"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("dev.vhoyd:Blockworks:1.0-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

}

tasks {
    runServer {
        minecraftVersion("1.21.8")
    }
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set("PrisonServerKT")
        archiveVersion.set("1.0-SNAPSHOT")
        minimize()
        doLast {
            println("Shadow jar built at: ${archiveFile.get().asFile.absolutePath}")
        }
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

val exportPluginThingy by tasks.registering(Copy::class) {
    dependsOn(tasks.build)
    from(tasks.shadowJar)
    into(File(System.getProperty("user.home"), "Desktop/Paper 1.21.8 server/plugins/"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

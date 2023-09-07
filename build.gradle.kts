plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.1"
}

group = "me.prouge"
version = "0.2"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.2.1")
    type.set("IC")
    updateSinceUntilBuild.set(false)
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    signPlugin {
        certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(providers.environmentVariable("PUBLISH_TOKEN"))
    }
}

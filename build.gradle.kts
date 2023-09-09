plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "me.prouge"
version = "0.3"

repositories {
    mavenCentral()
}

intellij {
    version.set("2022.3")
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

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3.2")

        bundledPlugin("com.intellij.java")

        pluginVerifier()
        zipSigner()
    }

}



intellijPlatform {
    projectName = project.name
    buildSearchableOptions = true

    pluginConfiguration {

        ideaVersion {
            sinceBuild = "233.3"
        }

    }

}
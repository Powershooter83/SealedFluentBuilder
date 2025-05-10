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
    instrumentCode = false

    pluginConfiguration {

        ideaVersion {
            sinceBuild = "233.3"
            untilBuild = "251.*"
        }

    }

}
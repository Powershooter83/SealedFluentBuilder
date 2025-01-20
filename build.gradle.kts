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
        id = "me.prouge.sealedfluentbuilder"
        name = "SealedFluentBuilder"
        version = "0.8"

        ideaVersion {
            sinceBuild = "243"
            untilBuild = "243.*"
        }


        vendor {
            name = "Yanick Romere"
            email = "justcrout@gmail.com"
            url = "https://github.com/Powershooter83"
        }
    }

}
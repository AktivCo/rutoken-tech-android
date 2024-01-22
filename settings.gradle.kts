pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://nexus.aktivco.ru/repository/maven-public")
    }
}

rootProject.name = "rutoken-tech-android"

include(":module.rutoken-tech", ":test.use-cases-tests")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "jiaa-backend"
include(":analysis-service")
include(":auth-service")
include(":common-lib")
include(":discovery-service")
include(":gateway-service")
include(":goal-service")
include(":user-service")

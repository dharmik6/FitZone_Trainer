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
        //noinspection JcenterRepositoryObsolete
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "FitZone(Trainer)"
include(":app")
 
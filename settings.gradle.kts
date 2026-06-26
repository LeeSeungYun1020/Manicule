pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Manicule"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// 1단계 — 기반 시스템 (Foundation): 현재 등록된 모듈
include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:model")

// 후속 단계에서 활성화
include(":core:data")
include(":core:database")
include(":core:datastore")
//include(":core:domain")
include(":core:network")
//include(":core:notifications")
//include(":core:scanner")
//include(":core:ui")
//include(":feature:bookdetail")
//include(":feature:home")
//include(":feature:library")
//include(":feature:scanner")
//include(":feature:search")
//include(":feature:settings")
//include(":feature:stats")

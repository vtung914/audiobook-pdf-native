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
        mavenCentral() // Đây là kho lưu trữ chính thống, không lỗi
    }
}
rootProject.name = "AudioBook EPUB"
include(":app")

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

rootProject.name = "areashop-parent"

// Interfaces
include(":areashop-interface")
include(":areashop-nms")

// Adapters
include(":adapters:platform:paper")
include(":adapters:platform:platform-interface")

include(":adapters:plugins:worldedit")
include(":adapters:plugins:worldguard")
include(":adapters:plugins:fastasyncworldedit")

if (!providers.environmentVariable("JITPACK").isPresent) {
    include(":adapters:platform:bukkit-1-17")
}

include(":adapters:platform:bukkit-modern")


// Main project
include(":areashop")

project(":areashop").projectDir = file("AreaShop")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

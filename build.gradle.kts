// Gradle 9.3.1 + Android Gradle Plugin 9.1.1 (AGP 9.1 requires Gradle >= 9.3.1).
// AGP 9 compiles Kotlin itself ("built-in Kotlin"), so org.jetbrains.kotlin.android is not applied.
// The Compose and serialization compiler plugins are still applied and pin the Kotlin version.
plugins {
    id("com.android.application") version "9.1.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20" apply false
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "org.news.data"

    compileSdk = libs.versions.android.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    jvmToolchain(11)
}


dependencies {
    api(project(":core:common"))
    api(project(":core:network"))

    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.retrofit.core)
    implementation(libs.kotlinx.serialization.json)

}
plugins {
    id("java-library")
    id("kotlin")
    id("com.google.devtools.ksp")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Hilt
    implementation("com.google.dagger:hilt-core:2.49")
    ksp("com.google.dagger:hilt-compiler:2.48.1")

    // moshi
    implementation("com.squareup.moshi:moshi:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}
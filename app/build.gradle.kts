plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.projeto"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projeto"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

   // Biblioteca para montar o gráfico de barra usando MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Biblioteca que transforma as tarefas em JSON
    implementation("com.google.code.gson:gson:2.10.1")
}
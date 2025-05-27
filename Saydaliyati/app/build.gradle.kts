plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.saydaliyati"
    compileSdk = 34

    defaultConfig {

        applicationId = "com.example.saydaliyati"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        resConfigs("fr", "ar")
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
    implementation("androidx.activity:activity:1.8.2")
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // OSMDroid - Bibliothèque principale
    implementation("org.osmdroid:osmdroid-android:6.1.16")

    // Room components
    implementation("androidx.room:room-runtime:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")

    // SecurityCrypto pour le hachage des mots de passe
    implementation("androidx.security:security-crypto:1.0.0")

    // Dépendances supplémentaires
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
}
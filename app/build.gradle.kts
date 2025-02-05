plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.googleServices) // Plugin do Google Services
    id("org.jetbrains.kotlin.kapt") // Habilita o plugin Kotlin KAPT
    id("kotlin-parcelize") // Ativa o suporte ao @Parcelize
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.quest.food"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.quest.food"
        minSdk = 26
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // Firebase BOM para gerenciar versões
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))

    // Dependências do Firebase
    implementation("com.google.firebase:firebase-auth-ktx") // Autenticação do Firebase
    implementation("com.google.firebase:firebase-database-ktx") // Banco de dados em tempo real
    implementation("com.google.firebase:firebase-storage-ktx") // Armazenamento Firebase
    implementation("com.google.firebase:firebase-messaging-ktx") // Mensagens Firebase
    implementation("com.google.firebase:firebase-appcheck-playintegrity") // AppCheck

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0") // Google Sign-In

    // Glide para carregamento de imagens
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.firebase.firestore.ktx) // Firestore do Firebase
    kapt("com.github.bumptech.glide:compiler:4.15.1") // Glide KAPT

    // Material Design
    implementation("com.google.android.material:material:1.8.0")

    // Jetpack Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}

// Aplica o plugin google-services no módulo app
apply(plugin = "com.google.gms.google-services")

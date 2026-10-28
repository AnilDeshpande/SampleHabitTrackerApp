plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.codetutor.samplehabittrackerapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.codetutor.samplehabittrackerapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.codetutor.samplehabittrackerapp.HabitTrackerTestRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            // Robolectric API 36 accesses FileDescriptor through JDK 21 SharedSecrets.
            it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")
        }
        managedDevices {
            localDevices {
                create("pixel2Api36") {
                    device = "Pixel 2"
                    apiLevel = 36
                    systemImageSource = "aosp"
                }
            }
        }
    }
    sourceSets {
        getByName("test").kotlin.directories.add("src/sharedTest/java")
        getByName("androidTest").kotlin.directories.add("src/sharedTest/java")
    }
    buildFeatures {
        compose = true
    }
}

room { schemaDirectory("$projectDir/schemas") }

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.work.runtime)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.hilt.testing)
    kspTest(libs.hilt.compiler)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.work.testing)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.testing)
    kspAndroidTest(libs.hilt.compiler)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

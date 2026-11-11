plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = providers.gradleProperty("GROUP_ID").getOrElse("com.github.e-hai")
version = resolvePublishVersion()

android {
    namespace = "com.kit.crashreport"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    api(platform(libs.firebase.bom))
    api(libs.firebase.crashlytics)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = providers.gradleProperty("ARTIFACT_ID").getOrElse("crashreport")
            version = project.version.toString()

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("CrashReportKit-Android")
                description.set("Android crash reporting SDK facade (Firebase Crashlytics backend).")
                url.set("https://github.com/e-hai/CrashReportKit-Android")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("e-hai")
                        name.set("e-hai")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/e-hai/CrashReportKit-Android.git")
                    developerConnection.set("scm:git:ssh://github.com/e-hai/CrashReportKit-Android.git")
                    url.set("https://github.com/e-hai/CrashReportKit-Android")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/e-hai/CrashReportKit-Android")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

fun resolvePublishVersion(): String {
    val fromEnv = System.getenv("VERSION")
    val raw = when {
        !fromEnv.isNullOrBlank() -> fromEnv
        else -> providers.gradleProperty("VERSION_NAME").orNull ?: "1.0.0"
    }
    return raw.removePrefix("v").removePrefix("V")
}

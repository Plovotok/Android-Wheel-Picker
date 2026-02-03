plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.publishing)
    alias(libs.plugins.nmcp)
}

android {
    namespace = "github.plovotok.wheelpicker"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.create("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        explicitApi()
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

version = properties["VERSION_NAME"].toString()
description = properties["POM_DESCRIPTION"].toString()

dependencies {
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
//    implementation(libs.androidx.compose.ui)
}

afterEvaluate {
    mavenPublishing {
        publishToMavenCentral()

        signAllPublications()

        coordinates(
            project.properties["GROUP_ID"].toString(),
            project.properties["POM_ARTIFACT_ID"].toString(),
            project.properties["VERSION_NAME"].toString()
        )

        val pomUrl = project.properties["POM_URL"].toString()

        pom {
            name.set(project.properties["POM_NAME"].toString())
            description.set(project.properties["POM_DESCRIPTION"].toString())
            url.set(pomUrl)
            inceptionYear.set(project.properties["POM_INCEPTION_YEAR"].toString())

            issueManagement {
                url.set("$pomUrl/issues")
            }

            scm {
                url.set(project.properties["POM_SCM_URL"].toString())
                connection.set(project.properties["POM_SCM_CONNECTION"].toString())
                developerConnection.set(project.properties["POM_SCM_DEV_CONNECTION"].toString())
            }

            licenses {
                license {
                    name.set(project.properties["POM_LICENSE_NAME"].toString())
                    url.set(project.properties["POM_LICENSE_URL"].toString())
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set(project.properties["POM_DEVELOPER_ID"].toString())
                    name.set(project.properties["POM_DEVELOPER_NAME"].toString())
                    url.set(project.properties["POM_DEVELOPER_URL"].toString())
                }
            }
        }
    }
}

nmcp {
    publishAllPublicationsToCentralPortal {
        val keyUsername = "SONATYPE_USERNAME"
        val keyPassword = "SONATYPE_PASSWORD"
        username = findProperty(keyUsername)?.toString() ?: System.getenv(keyUsername)
        password = findProperty(keyPassword)?.toString() ?: System.getenv(keyPassword)
        this.publishingType = "USER_MANAGED"
    }
}

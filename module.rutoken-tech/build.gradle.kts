import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

plugins {
    alias(libs.plugins.android.app)
    alias(libs.plugins.jetbrains.kotlinAndroid)
    alias(libs.plugins.ksp)
}

base {
    archivesName.set("rutoken-tech")
}

android {
    namespace = "ru.rutoken.tech"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.rutoken.tech"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "COMMIT_HASH", "\"${getCommitHash()}\"")

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    signingConfigs {
        create("defaultSigningConfig") {
            storeFile = if (project.hasProperty("keystorePath")) file(project.property("keystorePath")!!) else null
            storePassword = project.findProperty("keystorePass") as String?
            keyAlias = project.findProperty("keyAlias") as String?
            keyPassword = project.findProperty("keyPass") as String?
        }
    }

    buildTypes {
        val hasSigningParameters = project.hasProperty("keystorePath") && project.property("keystorePath") != "" &&
                project.hasProperty("keyAlias") && project.property("keyAlias") != "" &&
                project.hasProperty("keystorePass") && project.hasProperty("keyPass")

        release {
            buildConfigField("int", "LOG_LEVEL", "android.util.Log.ERROR")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (hasSigningParameters) signingConfigs.getByName("defaultSigningConfig") else null
        }

        debug {
            buildConfigField("int", "LOG_LEVEL", "android.util.Log.VERBOSE")
            if (hasSigningParameters)
                signingConfig = signingConfigs.getByName("defaultSigningConfig")
        }
    }

    applicationVariants.all {
        val copyJniLibsTask = tasks.register("${name}CopyExternalJniLibs") {
            doLast { architectures.forEach { arch -> copyJniLibs(project, arch) } }
        }
        tasks.named("merge${name.firstCharToUpper()}JniLibFolders") {
            dependsOn(copyJniLibsTask)
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "META-INF/versions/21/OSGI-INF/MANIFEST.MF"
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugarJdkLibs)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    releaseImplementation(libs.bouncycastle.bcpkix)
    debugImplementation(libs.bundles.bouncycastle.debug) {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk18on")
        exclude(group = "org.bouncycastle", module = "bcutil-jdk18on")
    }
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.jna) { artifact { type = "aar" } }
    implementation(libs.koin)
    implementation(libs.kstatemachine)
    implementation(libs.bundles.room)
    implementation(libs.rutoken.pkcs11jna) { isTransitive = false }
    implementation(libs.rutoken.pkcs11wrapper) { isTransitive = false }
    implementation(libs.rutoken.rtpcscbridge)
    implementation(libs.android.pdf.viewer)

    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotest.assertions.core)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.room.testing)

    debugImplementation(libs.bundles.compose.debug)
}

val architectures = listOf(
    Architecture("armv7a", "armeabi-v7a"),
    Architecture("arm64", "arm64-v8a"),
)

data class Architecture(val depArch: String, val jniArch: String)

fun String.firstCharToUpper() = replaceFirstChar { it.uppercase() }

fun copyJniLibs(proj: Project, architecture: Architecture) {
    val jniLibs = "${proj.projectDir}/src/main/jniLibs/${architecture.jniArch}"
    val dependencyArch = "android-${architecture.depArch}"

    copyFromBinaryDeps(rootDir.absolutePath, "pkcs11ecp", dependencyArch, "librtpkcs11ecp.so", jniLibs)
}

fun requireFileInDirectory(directory: String, file: String) =
    check(File(directory, file).exists()) { "Not found $file in directory $directory" }

fun copyFile(file: String, sourcePath: String, destinationPath: String, destinationFile: String = file) {
    requireFileInDirectory(sourcePath, file)
    Files.createDirectories(Path.of(destinationPath))
    Files.copy(
        Path.of(sourcePath, file),
        Path.of(destinationPath, destinationFile),
        StandardCopyOption.REPLACE_EXISTING
    )
}

fun copyFromBinaryDeps(
    rootDir: String,
    projectName: String,
    architecture: String,
    file: String,
    destinationPath: String
) {
    val sourcePath = "$rootDir/external/$projectName/$architecture/lib"
    copyFile(file, sourcePath, destinationPath)
}

tasks.named("clean") {
    doLast {
        delete("src/main/jniLibs")
    }
}

fun getCommitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

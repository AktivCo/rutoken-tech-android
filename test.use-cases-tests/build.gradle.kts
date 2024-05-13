import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

plugins {
    alias(libs.plugins.android.lib)
    alias(libs.plugins.jetbrains.kotlinAndroid)
}

android {
    namespace = "ru.rutoken.tech.usecasestests"

    compileSdk = 34
    defaultConfig.minSdk = 24
    testOptions.targetSdk = 34
    lint.targetSdk = 34

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions.jvmTarget = "11"

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    libraryVariants.all {
        val copyJniLibsTask = tasks.register("${name}CopyExternalJniLibs") {
            doLast { architectures.forEach { arch -> copyJniLibs(project, arch) } }
        }
        tasks.named("merge${name.firstCharToUpper()}JniLibFolders") {
            dependsOn(copyJniLibsTask)
        }
    }

    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "META-INF/versions/21/OSGI-INF/MANIFEST.MF"
        }
    }
}

dependencies {
    androidTestImplementation(project(":module.rutoken-tech"))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.uiAutomator)
    androidTestImplementation(libs.jna) { artifact { type = "aar" } }
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.rutoken.pkcs11jna) { isTransitive = false }
    androidTestImplementation(libs.rutoken.pkcs11wrapper) { isTransitive = false }
    androidTestImplementation(libs.rutoken.rtpcscbridge)
    androidTestImplementation(libs.bundles.bouncycastle.debug) {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk18on")
        exclude(group = "org.bouncycastle", module = "bcutil-jdk18on")
    }
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

[Russian/Русский](README_RUS.md)

# Description

Rutoken Technologies is an application for demonstrating the capabilities of the Rutoken ECP line on mobile devices
based on Android 7 and newer.

The application contains Certification Authority section designed to demonstrate the capabilities of the test
certification authority. Here you can generate a key pair and issue a test certificate on a mobile device.

The project consists of two Gradle modules:

* `module.rutoken-tech` is the main module containing all the business logic and UI of the application;
* `test.use-cases-tests` is a module of instrumental tests in which the correctness of performing various operations
  with the token is checked.

# Requirements

Rutoken Technologies application should be built using Android SDK Platform 34 or newer and launched on devices with
Android 7 (API level 24) and newer.

External dependencies required to build the application can be found in
the [Rutoken SDK](https://www.rutoken.ru/developers/sdk/). Required libraries:

* librtpkcs11ecp.so for the architectures: `armeabi-v7a` and `arm64-v8a`.

# How to build

1. Before building the project, copy the downloaded external dependencies to the
   directory `module.rutoken-tech/src/main/jniLibs/<arch>`, where `<arch>` is the library architecture. Files location
   example is shown below:

    ```Text
    module.rutoken-tech
    | - src
    |   | - main
    |   |   | - jniLibs
    |   |   |   | - arm64-v8a
    |   |   |   |   | - librtpkcs11ecp.so
    |   |   |   | - armeabi-v7a
    |   |   |   |   | - librtpkcs11ecp.so
    ```

2. You can build the project in the following ways:

    * Using Gradle from terminal. In this case, you should run the command in the terminal:

   ```shell
   ./gradlew :module.rutoken-tech:assemble
   ```

   After that, the apk files of the application can be found in the
   directory `module.rutoken-tech/build/outputs/apk/<build_variant>`, where `<build_variant>` is the build variant
   (debug or release).

    * Using Android Studio. The build instructions can be found in the
      article [Build and run your app](https://developer.android.com/studio/run) in Google's Android documentation.

# Restrictions

Rutoken Technologies application can only be run on physical devices, not on emulators.

# Licenses

The project source code is distributed under the [BSD-2-Clause License](LICENSE).

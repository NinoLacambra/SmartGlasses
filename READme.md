# Smart Glasses Android Application

This is a mobile application built with **Android Studio** and written in **Java/Kotlin** (update as needed). It was downloaded from GitHub and is designed to run on Android devices using Gradle as the build system.

---

## 💼 Project Structure

* `app/` - Main application source code.
* `gradle/` - Gradle wrapper for consistent builds.
* `build.gradle` - Build configuration files.
* `AndroidManifest.xml` - App manifest file.

---

## 🚀 How to Run the Project Locally

### 1. Clone or Download the Project

If not already done, download the ZIP or clone via Git:

```bash
git clone https://github.com/NinoLacambra/SmartGlasses.git
```

### 2. Open in Android Studio

* Launch Android Studio.
* Choose **"Open an existing project"**.
* Navigate to the extracted folder.

### 3. Sync Gradle

* Android Studio will try to sync the Gradle files.
* If prompted, update Gradle and plugin versions.

### 4. Build the Project

* Go to **Build > Make Project** or click the hammer icon.
* If you get errors, follow the Gradle fix instructions below.

### 5. Run the App

* Connect an Android device or start an emulator.
* Press the green **Run** button.

---

## 🛠️ Fixing Common Gradle Issues

If you encounter errors like:

> `Unable to find method 'void org.gradle.api.internal.DefaultDomainObjectSet.<init>(java.lang.Class)'`
> or
> `Unable to load class 'org.gradle.initialization.BuildCompletionListener'`

### Try the following:

1. **Delete the `.gradle` folders** in both the project and user directories.
2. **Update `gradle-wrapper.properties`:**

   ```properties
   distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-all.zip
   ```
3. **Update Gradle Plugin in `build.gradle`:**

   ```groovy
   classpath 'com.android.tools.build:gradle:8.0.2'
   ```
4. **Sync the project again** in Android Studio.
5. **Rebuild and run.**

---

## 🧩 Requirements

* Android Studio (latest stable version)
* Gradle 8.0+ (auto-handled via wrapper)
* JDK 11 or later
* Internet connection (for initial dependency sync)

---

## 📝 ESP32 Source Code

The Android app works in conjunction with an ESP32 microcontroller. You can find the ESP32 source code repository here:

🔗 [ESP32 Smart Glasses Controller Code](https://github.com/NinoLacambra/SmartGlasses-ESP32.git)

---

## ✍️ Author

Niño C. Lacambra

* [lacambranino12@gmail.com](mailto:lacambranino12@gmail.com)
* [www.linkedin.com/in/niño-lacambra](http://www.linkedin.com/in/niño-lacambra)

---

## License

MIT License - Free for personal and commercial use


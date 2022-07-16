# Kotlin File IO

![Maven Central](https://img.shields.io/maven-central/v/org.drewcarlson/kotlin-file-io?label=maven&color=blue)
![](https://github.com/DrewCarlson/kotlin-file-io/workflows/Tests/badge.svg)

Kotlin Multiplatform File IO library.

## Download

![Maven Central](https://img.shields.io/maven-central/v/org.drewcarlson/kotlin-file-io?label=maven&color=blue)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/org.drewcarlson/kotlin-file-io?server=https%3A%2F%2Fs01.oss.sonatype.org)

![](https://img.shields.io/static/v1?label=&message=Platforms&color=grey)
![](https://img.shields.io/static/v1?label=&message=Js&color=blue)
![](https://img.shields.io/static/v1?label=&message=Jvm&color=blue)
![](https://img.shields.io/static/v1?label=&message=Linux&color=blue)
![](https://img.shields.io/static/v1?label=&message=macOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=Windows&color=blue)

```kotlin
repositories {
    mavenCentral()
    // (Optional) For Snapshots:
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.drewcarlson:ktfio:$fileIoVersion")

    implementation("org.drewcarlson:ktfio:$fileIoVersion")
    implementation("org.drewcarlson:ktfio-js:$fileIoVersion") // Node.js only
    implementation("org.drewcarlson:ktfio-linuxx64:$fileIoVersion")
    implementation("org.drewcarlson:ktfio-macosx64:$fileIoVersion")
    implementation("org.drewcarlson:ktfio-macosArm64:$fileIoVersion")
    implementation("org.drewcarlson:ktfio-mingwx64:$fileIoVersion")
}
```

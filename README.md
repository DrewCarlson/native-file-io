# Kotlin File IO

![Maven Central](https://img.shields.io/maven-central/v/org.drewcarlson/kotlin-file-io?label=maven&color=blue)
![](https://github.com/DrewCarlson/kotlin-file-io/workflows/Tests/badge.svg)

Kotlin Multiplatform File IO library.

## Usage

Full API documentation is available at https://drewcarlson.github.io/kotlin-file-io/

```Kotlin
val file = File("~/Downloads/file.txt")

println(file.readText()) // Hello World!

if (file.exists()) {
    file.writeText("Goodbye!")
}

file.delete()
```

Note for JVM targets, `ktfio.File` is a typealias for `java.io.File`.

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
    implementation("org.drewcarlson:ktfio:$ktfioVersion")

    // For a single platform:
    implementation("org.drewcarlson:ktfio:$ktfioVersion")
    implementation("org.drewcarlson:ktfio-jvm:$ktfioVersion")
    implementation("org.drewcarlson:ktfio-js:$ktfioVersion") // Node.js only
    implementation("org.drewcarlson:ktfio-linuxx64:$ktfioVersion")
    implementation("org.drewcarlson:ktfio-macosx64:$ktfioVersion")
    implementation("org.drewcarlson:ktfio-macosArm64:$ktfioVersion")
    implementation("org.drewcarlson:ktfio-mingwx64:$ktfioVersion")
}
```

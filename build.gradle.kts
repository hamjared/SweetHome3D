plugins {
    java
    application
}

group = "com.eteks"
version = "8.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    // Java 3D from JogAmp - use correct URLs
    maven {
        url = uri("https://www.jogamp.org/deployment/maven")
    }
}

sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("src")
    }
    test {
        java.srcDirs("test")
    }
}

dependencies {
    // Java 3D from lib/ (shipped with project)
    implementation(files("lib/java3d-1.6/j3dcore.jar"))
    implementation(files("lib/java3d-1.6/j3dutils.jar"))
    implementation(files("lib/java3d-1.6/vecmath.jar"))
    implementation(files("lib/java3d-1.6/gluegen-rt.jar"))
    implementation(files("lib/java3d-1.6/jogl-all.jar"))

    // SVG and PDF
    implementation("com.lowagie:itext:2.1.7")

    // Local/custom dependencies
    implementation(files("lib/batik-svgpathparser-1.7.jar"))
    implementation(files("lib/jeksparser-calculator.jar"))
    implementation(files("lib/jmf.jar"))
    implementation(files("lib/freehep-vectorgraphics-svg-2.1.1c.jar"))
    implementation(files("lib/sunflow-0.07.3i.jar"))

    // Compile-only (not needed at runtime on modern Java)
    compileOnly(files("libtest/AppleJavaExtensions.jar"))
    // JNLP needed only for applet/web start (gracefully degrades if missing)
    implementation(files("libtest/jnlp.jar"))

    // Test dependencies
    testImplementation(files("libtest/abbot.jar"))
    testImplementation(files("libtest/gnu-regexp-1.1.4.jar"))
    testImplementation("org.jdom:jdom:1.1.1")
    testImplementation("jdepend:jdepend:2.9")
}

tasks.test {
    useJUnit()
}

// Fat JAR task - bundles all dependencies and native libraries
tasks.register<Jar>("jarExecutable") {
    group = "build"
    description = "Build self-contained executable JAR with all dependencies"

    archiveFileName.set("SweetHome3D-${version}.jar")
    destinationDirectory.set(file("$buildDir/libs"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to "com.eteks.sweethome3d.SweetHome3DBootstrap",
            "Add-opens" to "java.desktop/java.awt java.desktop/sun.awt java.desktop/com.apple.eio java.desktop/com.apple.eawt",
            "Implementation-Title" to "Sweet Home 3D",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "Space Mushrooms"
        )
    }

    // Include all compiled classes
    from(sourceSets.main.get().output)

    // Include all runtime dependencies
    val runtimeClasspath = configurations.runtimeClasspath.get()
    from(runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) }) {
        // Prevent duplicates of manifest files and signatures
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/MANIFEST.MF")
    }

    // Include native libraries
    from("lib/java3d-1.6/linux/amd64") {
        into("lib/java3d-1.6/linux/amd64")
        include("*.so")
    }
    from("lib/java3d-1.6/linux/i586") {
        into("lib/java3d-1.6/linux/i586")
        include("*.so")
    }
    from("lib/java3d-1.6/windows/x64") {
        into("lib/java3d-1.6/windows/x64")
        include("*.dll")
    }
    from("lib/java3d-1.6/windows/i386") {
        into("lib/java3d-1.6/windows/i386")
        include("*.dll")
    }
    from("lib/java3d-1.6/macosx") {
        into("lib/java3d-1.6/macosx")
        include("*.jnilib", "*.dylib")
    }

    // Include YafaRay native libraries
    from("lib/yafaray/linux/x64") {
        into("lib/yafaray/linux/x64")
        include("*.so")
    }
    from("lib/yafaray/windows/x64") {
        into("lib/yafaray/windows/x64")
        include("*.dll")
    }
    from("lib/yafaray/macosx") {
        into("lib/yafaray/macosx")
        include("*.jnilib", "*.dylib")
    }

    doLast {
        // Copy main JAR to install directory
        val installDir = file("install")
        installDir.mkdirs()
        copy {
            from(archiveFile)
            into(installDir)
        }
        println("✓ Built: ${installDir}/${archiveFileName.get()}")
    }
}

// Cost Estimator Plugin JAR
tasks.register<Jar>("costEstimatorPlugin") {
    group = "build"
    description = "Build Cost Estimator plugin JAR"

    dependsOn("classes", "processResources")

    archiveFileName.set("CostEstimatorPlugin.jar")
    destinationDirectory.set(file("$buildDir/plugins"))

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Include plugin classes
    from(sourceSets.main.get().output.classesDirs) {
        include("com/eteks/sweethome3d/plugin/costestimator/**")
    }

    // Include plugin resources in package directory (except ApplicationPlugin.properties)
    from(sourceSets.main.get().output.resourcesDir) {
        include("com/eteks/sweethome3d/plugin/costestimator/**")
        exclude("com/eteks/sweethome3d/plugin/costestimator/ApplicationPlugin.properties")
    }

    // Copy ApplicationPlugin.properties to root for plugin discovery
    from(sourceSets.main.get().output.resourcesDir) {
        include("com/eteks/sweethome3d/plugin/costestimator/ApplicationPlugin.properties")
        into("")  // Empty string puts file at root, stripping directory structure
    }

    manifest {
        attributes(
            "Implementation-Title" to "Cost Estimator Plugin",
            "Implementation-Version" to version
        )
    }

    doLast {
        val pluginDir = file("$buildDir/plugins")
        val installPluginDir = file("install/plugins")
        installPluginDir.mkdirs()
        copy {
            from(archiveFile)
            into(installPluginDir)
        }
        println("✓ Built: ${pluginDir}/${archiveFileName.get()}")
        println("✓ Copied to: ${installPluginDir}/${archiveFileName.get()}")
    }
}

// Make jarExecutable depend on costEstimatorPlugin
tasks.named("jarExecutable") {
    dependsOn("costEstimatorPlugin")
}

// Bundled JAR with plugin included
tasks.register<Jar>("jarExecutableWithPlugin") {
    group = "build"
    description = "Build self-contained JAR with Cost Estimator plugin bundled"

    archiveFileName.set("SweetHome3D-${version}-with-plugin.jar")
    destinationDirectory.set(file("$buildDir/libs"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to "com.eteks.sweethome3d.SweetHome3DBootstrap",
            "Add-opens" to "java.desktop/java.awt java.desktop/sun.awt java.desktop/com.apple.eio java.desktop/com.apple.eawt",
            "Implementation-Title" to "Sweet Home 3D",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "Space Mushrooms"
        )
    }

    // Include all compiled classes (including plugin)
    from(sourceSets.main.get().output)

    // Include all runtime dependencies
    val runtimeClasspath = configurations.runtimeClasspath.get()
    from(runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/MANIFEST.MF")
    }

    // Include native libraries
    from("lib/java3d-1.6/linux/amd64") {
        into("lib/java3d-1.6/linux/amd64")
        include("*.so")
    }
    from("lib/java3d-1.6/linux/i586") {
        into("lib/java3d-1.6/linux/i586")
        include("*.so")
    }
    from("lib/java3d-1.6/windows/x64") {
        into("lib/java3d-1.6/windows/x64")
        include("*.dll")
    }
    from("lib/java3d-1.6/windows/i386") {
        into("lib/java3d-1.6/windows/i386")
        include("*.dll")
    }
    from("lib/java3d-1.6/macosx") {
        into("lib/java3d-1.6/macosx")
        include("*.jnilib", "*.dylib")
    }

    // Include YafaRay native libraries
    from("lib/yafaray/linux/x64") {
        into("lib/yafaray/linux/x64")
        include("*.so")
    }
    from("lib/yafaray/windows/x64") {
        into("lib/yafaray/windows/x64")
        include("*.dll")
    }
    from("lib/yafaray/macosx") {
        into("lib/yafaray/macosx")
        include("*.jnilib", "*.dylib")
    }

    doLast {
        val installDir = file("install")
        installDir.mkdirs()
        copy {
            from(archiveFile)
            into(installDir)
        }
        println("✓ Built: ${installDir}/${archiveFileName.get()}")
    }
}

// Copy jarExecutable output to install/ directory
tasks.build {
    dependsOn("jarExecutable")
}

// Windows installer with bundled JRE using jpackage
tasks.register<Exec>("createWindowsInstaller") {
    group = "build"
    description = "Create Windows .exe installer with bundled JRE (Windows + JDK 16+ only)"

    dependsOn("jarExecutableWithPlugin")

    // Only run on Windows
    onlyIf {
        System.getProperty("os.name").lowercase().contains("windows")
    }

    val appVersion = version.toString()
    val outputDir = file("build/installer").absolutePath

    file(outputDir).mkdirs()

    val javaHome = System.getenv("JAVA_HOME") ?: System.getProperty("java.home")
    commandLine(
        "$javaHome/bin/jpackage.exe",
        "--input", file("install").absolutePath,
        "--name", "SweetHome3D",
        "--main-jar", "SweetHome3D-${appVersion}-with-plugin.jar",
        "--main-class", "com.eteks.sweethome3d.SweetHome3DBootstrap",
        "--type", "exe",
        "--dest", outputDir,
        "--app-version", appVersion,
        "--vendor", "Space Mushrooms",
        "--description", "Sweet Home 3D - Interior 2D design application with Cost Estimator",
        "--win-menu",
        "--win-menu-group", "SweetHome3D",
        "--win-shortcut"
    )

    doLast {
        println("✓ Windows installer created in: $outputDir")
        val exeFile = file("$outputDir/SweetHome3D-${appVersion}.exe")
        if (exeFile.exists()) {
            val sizeMB = exeFile.length() / (1024 * 1024)
            println("✓ Installer: ${exeFile.name} ($sizeMB MB)")
        }
    }
}

application {
    mainClass.set("com.eteks.sweethome3d.SweetHome3DBootstrap")
}

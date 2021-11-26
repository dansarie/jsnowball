/* Copyright (c) 2021 Marcus Dansarie

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program. If not, see <http://www.gnu.org/licenses/>. */

plugins {
    application
    id("org.panteleyev.jpackageplugin") version "1.3.1"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("org.json:json:20210307")
}

application {
    mainClass.set("se.dansarie.jsnowball.JSnowball")
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "JSnowball",
            "Implementation-Version" to project.property("archiveVersion") as String?
        )
    }
}

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$buildDir/jars")
}

task("copyJar", Copy::class) {
    from(tasks.jar).into("$buildDir/jars")
}

tasks.jpackage {
    dependsOn("build", "copyDependencies", "copyJar")

    input  = "$buildDir/jars"
    destination = "$buildDir/dist"

    appName = "JSnowball"
    appVersion = project.property("archiveVersion") as String?
    copyright = "Copyright (c) 2021 Marcus Dansarie"
    vendor = "Marcus Dansarie"
    mainClass = "se.dansarie.jsnowball.JSnowball"
    mainJar = tasks.jar.get().archiveFileName.get()

    windows {
        winMenu = true
        winDirChooser = true
        winMenuGroup = "JSnowball"
        winShortcut = true
        winPerUserInstall = true
    }
}

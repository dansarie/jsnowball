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
    id("com.xcporter.jpkg") version "0.0.8"
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
            "Implementation-Version" to archiveVersion
        )
    }
}

jpkg {
    useVersionFromGit = false
    mainClass = "se.dansarie.jsnowball.JSnowball"
    packageName = "JSnowball"
    vendor = "Marcus Dansarie"
    copyright = "2021"
    menuGroup = "JSnowball"
    windows {
        winDirChooser = true
        winPerUser = true
        winMenu = true
        shortcut = true
    }
}
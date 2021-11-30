# JSnowball
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://travis-ci.com/github/dansarie/jsnowball.svg?branch=master)](https://travis-ci.com/dansarie/jsnowball)

JSnowball is a program for performing systematic literature studies through snowballing and for visualization of citation relations between academic papers.

## Dependencies

* [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/)
* [Google Guava](https://github.com/google/guava)
* Java 17
* [JPackage Gradle Plugin](https://github.com/petr-panteleyev/jpackage-gradle-plugin)
* [JSON](https://www.json.org/json-en.html)

All dependencies, except for the JDK and JRE are automatically installed by the Gradle wrapper build script.

## Build

```
./gradlew build
```

### Building Windows executable

Run the following commands in Powershell.
```powershell
Set-ExecutionPolicy -ExecutionPolicy Unrestricted -Scope CurrentUser
.\WindowsBuild.ps1
```
The script automatically downloads a JDK and the WiX installer and then runs the Gradle wrapper build script. An installer bundle will appear in app\build\dist.

## Test

```
./gradlew test
```

## Run

```
./gradlew run
```

## Contributing

Reports on bugs and other issues are welcome. Please don't hesitate to open a new
[issue](https://github.com/dansarie/jsnowball/issues).

Likewise, contrubutions to code or documentation in the form of
[pull requests](https://github.com/dansarie/jsnowball/pulls) are welcomed.

## License and Copyright

Copyright 2021 [Marcus Dansarie](https://github.com/dansarie).

This project is licensed under the GNU General Public License – see the [LICENSE](LICENSE)
file for details.
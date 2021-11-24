Start-BitsTransfer -Source https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.zip
Expand-Archive -Path microsoft-jdk-17-windows-x64.zip
$Env:JAVA_HOME = (Get-ChildItem "jdk-17*" -Recurse | % {$_.FullName})
.\gradlew.bat build
.\gradlew.bat test

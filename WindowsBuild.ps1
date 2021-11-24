Start-BitsTransfer -Source https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.zip, https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip
Expand-Archive -Path microsoft-jdk-17-windows-x64.zip
Expand-Archive -Path wix311-binaries.zip
$Env:JAVA_HOME = (Get-ChildItem -Filter "jdk-17*" -Recurse | % {$_.FullName})
$Env:PATH = (Get-ChildItem -Filter "wix311-binaries" -Recurse | % {$_.FullName})
.\gradlew.bat test jpackagebuild

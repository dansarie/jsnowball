# Copyright (c) 2021 Marcus Dansarie
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

Start-BitsTransfer -Source https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.zip, https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip
Expand-Archive -Path microsoft-jdk-17-windows-x64.zip
Expand-Archive -Path wix311-binaries.zip
$Env:JAVA_HOME = (Get-ChildItem -Filter "jdk-17*" -Recurse | % {$_.FullName})
$Env:PATH = (Get-ChildItem -Filter "wix311-binaries" -Recurse | % {$_.FullName})
.\gradlew.bat test jpackagebuild

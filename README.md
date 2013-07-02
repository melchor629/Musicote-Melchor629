Musicote App
====================
[![Build Status](https://travis-ci.org/melchor629/Musicote-Melchor629.png?branch=ActionBarSherlock)](https://travis-ci.org/melchor629/Musicote-Melchor629) [![Coverage Status](https://coveralls.io/repos/melchor629/Musicote-Melchor629/badge.png)](https://coveralls.io/r/melchor629/Musicote-Melchor629)

Una sencilla aplicación para android 2.3, montado con el SDK de android **17** (platform 17)
Descarga el contenido emitido por un servidor web del archivo _"api.py"_, lo procesa y carga en una lista en el que puedes apretar una canción, ver su información básica y reproducir. También incluye Scrobbler para Last.FM, disponible en el menú de ajustes.
Para editar este projecto se recomienda usar Eclipse, con el plugin de Android instalado
Aqui guardaré todo lo avanzado sobre esta app

An app for android 2.3 and higger, writted with the android **SDK 17**
It downloads the content created by the _"api.py"_ and a web server then it process the content and loads it in a list. You can touch a song in the list and see some basic information and listen to it. Also it includes a Last.FM Scrobbler available in the settings menu.
To edit this project, use Eclipse with android's plugin or maven
Here I will save all my changes of this app

##How to compile
You can compile the project by Eclipse, easy way, or by Maven. In Eclipse you will need to import the project and have installed the android SDK 17. If you use maven, before compile you need to write this command:
```
mvn install:install-file -Dfile=$PWD/libs/android-support-v4.jar -DgroupId=com.google.android -DartifactId=support-v5 -Dversion=r7 -Dpackaging=jar
```
Where ```$PWD``` is the current directory in _*nix_ terminals. If the current directory isn't the main directory of the project, ```$PWD``` must be replaced with the relative path to this file. And finally, build the project:
```
mvn install --quiet -DskipTests=true -B
```

##What is api.py
This file is the pseudo-json file that a webserver creates for the app. You need to modify the path of the music directory. Your web server have to support Python scripts and have installed [mutagen](https://code.google.com/p/mutagen/).
You will need to modify the lines 13 & 14, because you need to configure this two variables (``` path ``` & ``` webpath ```) acording to the little description near the variable.

##DOWNLOAD APK
With the new _Releases_ system, the builds would be there. This is a good change :D
[Builds](https://github.com/melchor629/Musicote-Melchor629/releases)
Musicote App
====================
[![Build Status](https://travis-ci.org/melchor629/Musicote-Melchor629.png?branch=master)](https://travis-ci.org/melchor629/Musicote-Melchor629)

Una sencilla aplicación para android 2.3, montado con el SDK de android 17 (platform 17)
Esta APP lo único que hace, por ahora, es descargar un archivo JSON y cargar la lista (ArrayList) que lleva dentro
Para editar este projecto se recomienda usar Eclipse, con el plugin de Android instalado
Aqui guardaré todo lo avanzado sobre esta app

An app for android 2.2, writted with the SDK 4.2
This APP only can do, for now, is download a JSON file and load the ArrayList from it...
To edit this project, use Eclipse with the Android ANT installed
Here I will save all my changes of this app

##How to compile
You can compile the project by Eclipse, easy way, or by Maven. In Eclipse you will need to import the project and have installed the android SDK 17. If you use maven, before compile you need to write this command:
```
mvn install:install-file -Dfile=$PWD/libs/android-support-v4.jar -DgroupId=com.google.android -DartifactId=support-v5 -Dversion=r7
-Dpackagin=jar
```
Where ```$PWD``` is the current directory in *nix terminals. If the current directory isn't the main directory of the project, ```$PWD``` must be replaced with the relative path to this file.

##What is api.py
This file is the pseudo-json file that a webserver creates for the app. You need to modify the path of the music directory. Your web server have to support Python scripts and have installed [mutagen](https://code.google.com/p/mutagen/).

##DOWNLOAD APK
Aptoide:
[http://melchor629.store.aptoide.com/cat/Music%20&%20Audio](http://melchor629.store.aptoide.com/cat/Music%20&%20Audio)

Musicote App
====================
[![Build Status](https://travis-ci.org/melchor629/Musicote-Melchor629.png?branch=ActionBarSherlock)](https://travis-ci.org/melchor629/Musicote-Melchor629) [![Coverage Status](https://coveralls.io/repos/melchor629/Musicote-Melchor629/badge.png)](https://coveralls.io/r/melchor629/Musicote-Melchor629)

Una sencilla aplicación para android 2.3, montado con el SDK de android **18** (platform 18)
Descarga el contenido emitido por un servidor web del archivo _"api.py"_, lo procesa y carga en una lista en el que puedes apretar una canción, ver su información básica y reproducir. También incluye Scrobbler para Last.FM, disponible en el menú de ajustes.
Para editar este projecto se recomienda usar Eclipse, con el plugin de Android instalado
Aqui guardaré todo lo avanzado sobre esta app

An app for android 2.3 and higger, writted with the android **SDK 18**
It downloads the content created by the _"api.py"_ and a web server then it process the content and loads it in a list. You can touch a song in the list and see some basic information and listen to it. Also it includes a Last.FM Scrobbler available in the settings menu.
To edit this project, use Eclipse with android's plugin or maven
Here I will save all my changes of this app

##How to compile
####Android Dependencies
What should have installed to build:

 - __[Download][1] latests tools__ and install:
 - __platform-tools__ (Tools > Android SDK Platform-tools)
 - __android-18__ (Android 4.3 (API 18) > SDK Platform)
 - __android-14__ (Android 4.0 (API 14) > SDK Platform)
 - __sysimg-18__ (Android 4.3 (API 18) > ARM EABI v7a System Image)
 - __build-tools-19.0.0__ (Tools > Android SDK Build-tools _(REV 19)_)
 - __extra-android-support__ (Extras > Android Support Library)
 - __extra-android-m2repository__ (Extras > Android Support Repository)

**Trick**: *see .travis.yml to see what commands Travis CI use to build the project.*

####Project Dependencies
 - [loopj/Android-Async-HTTP][2]
 - [chrisbanes/ActionBar-PullToRefresh][3]

You can compile the project by [Eclipse][4], not bad way, or by [Gradle][5], easy, and [Android Studio][6], easiest. In Eclipse you will need to import the project, have installed the android SDK 18 and other tools and download the projects libraries. With Android Studio you only need to import `build.gradle`. And if you use Gradle, you only have to build it: `gradle build`, and don't forget to set `ANDROID_HOME` variable.

##What is api.py
This file is the pseudo-json file that a webserver creates for the app. You need to modify the path of the music directory. Your web server have to support Python scripts and have installed [mutagen][7].
You will need to modify the lines 13 & 14, because you need to configure this two variables (``` path ``` & ``` webpath ```) acording to the little description near the variable.

##DOWNLOAD APK
With the new _Releases_ system, the builds would be there. This is a good change :D
[Builds][8]


  [1]: http://developer.android.com/intl/es/sdk/index.html
  [2]: https://github.com/loopj/android-async-http
  [3]: https://github.com/chrisbanes/ActionBar-PullToRefresh
  [4]: www.eclipse.org
  [5]: www.gradle.org
  [6]: http://developer.android.com/intl/en/sdk/installing/studio.html
  [7]: https://code.google.com/p/mutagen/
  [8]: https://github.com/melchor629/Musicote-Melchor629/releases
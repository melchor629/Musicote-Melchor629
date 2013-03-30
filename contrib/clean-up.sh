#!/bin/sh
cd ../src/com/melchor629/musicote/
perl -wpi -e "s/ /    /g" *.java
perl -wpi -e "s/ +$//g" *.java
cd scrobbler
perl -wpi -e "s/ /    /g" *.java
perl -wpi -e "s/ +$//g" *.java

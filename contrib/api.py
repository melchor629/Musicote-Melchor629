#!/usr/bin/python
#-*- coding: utf-8 -*-

import os
import sys
from mutagen.mp3 import MP3
from mutagen.m4a import M4A
from mutagen.easyid3 import EasyID3
import json

print "Content-type:text/json;charset=utf-8\n"

path = "/path/to/music/folder" """Modify this line, the RELATIVE PATH to music folder"""
webpath = "/WebPath/to/music/folder" """Modify this line too, the path to music web folder""" 
musicote = []
directorios = []
i = 0

for base, dirs, files in os.walk(path):
    directorios.append(base)

for carpeta in directorios:
    dir = os.listdir(carpeta)
    for archivo in dir:
        if "mp3" in archivo:
            i = i+1
            audio = MP3(carpeta+"/"+archivo, ID3=EasyID3)
            try:
                titulo =  audio["title"][0]
            except:
                titulo = ""
            try:
                artista =  audio["artist"][0]
            except:
                artista = ""
            try:
                album =  audio["album"][0]
            except:
                album = ""
            try:
                dur =  audio.info.length
                min = int(dur/60)
                sec = int(dur - min*60)
                if len(str(sec)) == 1:
                    sec = "0"+str(sec)
                duracion = str(min)+":"+str(sec)
            except:
                duracion = ""
            musicote.append({"id": str(i),"titulo": titulo, "album": album, "artista": artista, "duracion": duracion, "archivo": carpeta.replace("/users/melchor9000/music","/musica")+"/"+archivo})

        elif ("mp4" in archivo) | ("m4a" in archivo):
            i = i+1
            audio = M4A(carpeta+"/"+archivo)
            album = audio.tags["\xa9alb"]
            artista = audio.tags["\xa9ART"]
            titulo = audio.tags["\xa9nam"]
            dur =  audio.info.length
            min = int(dur/60)
            sec = int(dur - min*60)
            if len(str(sec)) == 1:
                sec = "0"+str(sec)
            duracion = str(min)+":"+str(sec)
            musicote.append({"id": str(i),"titulo": titulo, "album": album, "artista": artista, "duracion": duracion, "archivo": carpeta.replace(path,webpath)+"/"+archivo})

print json.dumps({"canciones": musicote});

#!/bin/bash

echo "Compilation du menu de la borne d'arcade"
echo "Veuillez patienter"
javac -cp .:/home/pi/git/MG2D *.java

cd projet


#PENSER A REMETTRE COMPILATION JEUX!!!
for i in *
do
    cd "$i"
    echo "Compilation du jeu $i"
    if [ -f pom.xml ]; then
        echo "  -> projet Maven détecté, utilisation de mvn package"
        mvn clean package -q
    else
        echo "  -> compilation Java basique"
        javac -cp .:../..:/home/pi/git/MG2D *.java
    fi
    cd ..
done

cd ..

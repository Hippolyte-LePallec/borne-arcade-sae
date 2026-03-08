#!/bin/bash

setxkbmap borne

cd ..
echo "nettoyage des répertoires"
echo "Veuillez patienter"

./Script/clean.sh
./Script/compilation.sh

echo "Lancement du  Menu"
echo "Veuillez patienter"

java -cp .:/home/pi/git/MG2D Main

./Script/clean.sh

for i in {30..1}
do
    echo Extinction de la borne dans $i secondes
    sleep 1
done

sudo halt

#!/bin/bash
if [ "$USER" = "root" ]
then
	echo "Cannot run as root!"
	exit
fi

sudo usermod -aG docker $USER
curl -s "https://get.sdkman.io" | bash
source "/home/$USER/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.16-amzn
sdk install scala 2.13.16
sdk install sbt 1.9.9

echo "Exit out of wsl and open a new instance to get docker running"

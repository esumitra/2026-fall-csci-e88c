#!/bin/bash
if [ "$USER" != "root" ]
then
	echo "Must be run as root!"
	exit

fi
pkg="curl unzip zip build-essential git"
for i in $pkg
do
	echo "Installing $i..."
	apt-get install $i
done
#Docker repo config
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
echo "Installing docker deps..."
apt-get update
apt-get install -y \
	apt-transport-https \
	ca-certificates \
	curl \
	gnupg \
	lsb-release \
	docker-ce \
	docker-ce-cli \
	containerd.io

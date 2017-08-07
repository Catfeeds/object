#!/usr/bin/env bash
useradd $1
usermod -g wheel $1
mkdir /home/$1/.ssh/
mv  $2 /home/$1/.ssh/authorized_keys
chown -R $1:wheel /home/$1/.ssh
chmod 700 /home/$1/.ssh/
chmod 600 /home/$1/.ssh/authorized_keys

#!/bin/sh

VERS=2.5.0
MC_VERS=1.8

cd $(dirname $0)
ARCH=ThebombzenAPI-v$VERS-mc$MC_VERS.jar
./gradlew build
cp build/libs/thebombzenapi-$VERS.jar $ARCH
mkdir -p META-INF

cat >META-INF/MANIFEST.MF <<EOF
Manifest-Version: 1.0
Main-Class: thebombzen.mods.thebombzenapi.installer.APIInstallerFrame
EOF

zip -u $ARCH META-INF/MANIFEST.MF



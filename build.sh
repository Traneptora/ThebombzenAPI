#!/bin/sh
set -e

VERS=2.6.0
MC_VERS=1.9
MDK=1.9.4-12.17.0.1937
ARCHIVE=ThebombzenAPI-v$VERS-mc$MC_VERS.jar

CURRDIR="$PWD"

cd "$(dirname $0)"

mkdir -p build
cd build

if [ ! -e gradlew ] ; then
	cd ..
	TMP=ThebombzenAPI
	if [ -e ThebombzenAPI ] ; then
		TMP=$(mktemp)
		rm -f $TMP
		mv ThebombzenAPI $TMP
	fi
	mv build ThebombzenAPI
	cd ThebombzenAPI
	wget http://files.minecraftforge.net/maven/net/minecraftforge/forge/$MDK/forge-$MDK-mdk.zip
	unzip forge-$MDK-mdk.zip
	./gradlew setupDecompWorkspace
	./gradlew eclipse
	rm forge-$MDK-mdk.zip
	cd src/main
	rm -rf java resources
	ln -s ../../../resources
	ln -s ../../../src java
	cd ../..
	rm build.gradle
	ln -s ../build.gradle
	cd ..
	mv ThebombzenAPI build
	mv $TMP ThebombzenAPI 2>/dev/null || true
	cd build
fi

./gradlew build

cp build/libs/thebombzenapi-$VERS.jar $ARCHIVE
mkdir -p META-INF

cat >META-INF/MANIFEST.MF <<EOF
Manifest-Version: 1.0
Main-Class: thebombzen.mods.thebombzenapi.installer.APIInstallerFrame
EOF

zip -u $ARCHIVE META-INF/MANIFEST.MF

cp $ARCHIVE "$CURRDIR"



JFREECHART_JARS_DIR = jfreechart-1.0.13/lib/*

pvgraph.jar : build PVGraph.java
	javac -d build -cp "$(JFREECHART_JARS_DIR)" PVGraph.java
	cp -u sun.png build
	cd build; jar cfe ../$@ PVGraph *.class *.png

build:
	mkdir build

clean:
	rm -rf build pvgraph.jar

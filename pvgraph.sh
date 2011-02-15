#!/bin/sh

JFREECHART_JARS_DIR=jfreechart-1.0.13/lib/*

MYSQL_CONNECTOR_JAR=/usr/share/java/mysql-connector-java.jar

java -cp "$JFREECHART_JARS_DIR:$MYSQL_CONNECTOR_JAR:pvgraph.jar" PVGraph

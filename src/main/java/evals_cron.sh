#!/bin/sh
#This is a wrapper around the evals_backend.sh.  It's purpose is to provide the CP_ROOT and CLASSPATH value,
#and to change permission o the evals_backend.sh which is provided in a war file.

export CP_ROOT=/opt/lr6/   #change this to your CP_ROOT
export CLASSPATH
export TOMCAT_VERSION=tomcat-9.0.37
SCRIPT=$CP_ROOT/$TOMCAT_VERSION
SCRIPT=$SCRIPT/webapps/evals/src/java/edu/osu/cws/evals/backend/evals_backend.sh
$SCRIPT

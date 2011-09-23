#!/bin/sh
#This is a wrapper around the evals_backend.sh.  It's purpose is to provide the CP_ROOT and CLASSPATH value,
#and to change permission o the evals_backend.sh which is provided in a war file.

export CP_ROOT=/opt/luminis   #change this to your CP_ROOT
export CLASSPATH
SCRIPT=$CP_ROOT/products/tomcat/tomcat-admin
SCRIPT=$SCRIPT/webapps/pass/WEB-INF/src/edu/osu/cws/pass/backend/evals_backend.sh
$SCRIPT
#!/bin/sh
# Sept. 20, 2011
# Joan Lu joan.lu@oregonstate.edu
# This script is meant to run as a cronjob to update the EvalS database.  It does the following:
# 1. Creates Appraisal records a certain time before the appraisal period starts.
# 2. updates appraisal status as time progress and send out emails. For example, change status from "gaals due" to "goals over due"
# 3. Sends out follow up notification emails.
# The work is done by the java program.  Errors are logged to CWS's graylogs.
# This script is called from another script.  The other script
#   export the CP_ROOT and CLASSPATH variables,
#   execute this script

#   The other script is supposed to be set as a cronjob under the luminis user.


JAVA_HOME=/usr/bin
PORTAL=$CP_ROOT/tomcat-7.0.62
PORTAL_LIB=$PORTAL/lib
PORTAL_LIB_EXT=$PORTAL_LIB/ext
PORTLET_ROOT=$PORTAL/webapps/evals
WEB_INF=$PORTLET_ROOT/WEB-INF
LIB_DIR=$WEB_INF/lib
CLASS_DIR=$WEB_INF/classes

CLASSPATH=$CLASSPATH:$CLASS_DIR
CLASSPATH=$CLASSPATH:$LIB_DIR/aopalliance.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/c3p0-0.9.1.2.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/cglib-2.2.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/json-simple-1.1.1.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/commons-collections-3.1.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/commons-configuration-1.6.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/commons-email-1.3.1.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/commons-lang-2.6.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/commons-logging-1.1.1.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/dbunit-2.4.8.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/dom4j-1.6.1.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/gelfj-1.0.2.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/guice-3.0.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/hibernate3.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/hibernate-jpa-2.0-api-1.0.0.Final.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/javax.inject.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/joda-time-2.2.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/ojdbc8.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/esources_en.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/slf4j-api-1.6.1.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/slf4j-jdk14-1.6.1.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/util-bridges.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/util-java.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/antlr-2.7.6.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/javassist-3.12.0.GA.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/jstl-impl.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/jstl.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/resources_en.jar
CLASSPATH=$CLASSPATH:$LIB_DIR/javax.mail-1.4.4.jar
CLASSPATH=$CLASSPATH:/$LIB_DIR/opencsv-2.3.jar
CLASSPATH=$CLASSPATH:/$PORTAL_LIB_EXT/portal-service.jar
CLASSPATH=$CLASSPATH:/$PORTAL_LIB_EXT/jta.jar

cd $PORTLET_ROOT
$JAVA_HOME/bin/java -classpath $CLASSPATH edu.osu.cws.evals.backend.BackendAction


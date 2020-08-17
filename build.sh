#!/bin/sh
# build_logic.sh - Performs steps to compile and deploy evals
# exports: evals war file
# usage: Build evals for local development. Jenkins should be used to deploy to dev and prod

VM=$VM_USR@$VM_ADDRESS
LIFERAY_SDK=$LIFERAY_DIR/liferay-plugins-sdk-$LIFERAY_SDK_VERSION
EVALS_WAR=evals-$LIFERAY_SDK_VERSION.1.war
COMPILED_WAR=$LIFERAY_DIR/bundles/deploy/$EVALS_WAR

# copy evals files to liferay sdk
rm -rf $LIFERAY_SDK/portlets/evals
cp -r $EVALS $LIFERAY_SDK/portlets

# run ant to compile evals
echo "################################# Compile ##################################################"
cd $LIFERAY_SDK
ant
ANT_RESULT=$?

if [ $ANT_RESULT -eq 0 ] && [ $DEPLOY_TO_VM = true ]
then
  echo "\n################################# Deploy ################################################"
  scp $COMPILED_WAR $VM:$VM_DIR
  ssh $VM "docker cp $VM_DIR/$EVALS_WAR liferay-tomcat:/opt/liferay/deploy; rm $VM_DIR/$EVALS_WAR"
fi

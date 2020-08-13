#!/bin/sh
# build.sh - Sets variables for evals building
# exports: evals war file
# usage: Set user defined variables here to be used by build_logic.sh

# evals requires older versions of ant and java 
# set them here so you don't need to overwite your path
export JAVA_HOME=path/to/java
export ANT_HOME=path/to/ant

export EVALS=path/to/evals
# path to directory containing liferay sdk and bundle directories
export LIFERAY_DIR=path/to/liferay/files
# Version number only (e.g. 6.1.1)
export LIFERAY_SDK_VERSION=version#

# set DEPLOY_TO_VM to true if deploying to a VM
export DEPLOY_TO_VM=false
export VM_DIR=directory/to/copy/warfile
export VM_USR=USER
export VM_ADDRESS=ADDRESS

sh build_logic.sh

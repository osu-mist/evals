export VM_DIR=/home/apidevs # directory on VM to copy compiled bundles to
export VM_USR=apidevs # user on VM for ssh
export VM_ADDRESS=localhost # VM host running liferay docker container
export VM=$VM_USR@$VM_ADDRESS
export EVALS_WAR_DIR=./bundles/deploy # directory "blade deploy" deploys too, probably doesn't need to change
export EVALS_WAR=evals.war # name of deployed evals war file, probably doesn't need to change
export THEME_WAR=osu-theme.war # name of deployed evals theme file, probably doesn't need to change
export CONTAINER=liferay-tomcat # name of container on the vm,  probably doesn't need to change

sh ./deploy.sh $1;

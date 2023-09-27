export EVALS_PORTLET_DIR=/opt/liferay/tomcat-9.0.37/webapps/evals
export DOCKER_JAR=/usr/lib/jvm/zulu8-ca/bin/jar

(
  cd ../.. && \
  blade deploy;

  if [ $? != 0 ]; then
    echo "Error occurred with \"blade deploy\"";
    exit 1;
  fi

  if [ $1 == "all" ] || [ $1 == "theme" ]; then
    echo "Deploying Theme";
    scp $EVALS_WAR_DIR/$THEME_WAR $VM:$VM_DIR && \
    ssh $VM "docker cp $VM_DIR/$THEME_WAR $CONTAINER:/opt/liferay/deploy; rm $VM_DIR/$THEME_WAR"
  fi

  if [ $1 == "all" ] || [ $1 == "evals" ]; then
    echo "Deploying EvalS";
    scp $EVALS_WAR_DIR/$EVALS_WAR $VM:$VM_DIR && \
    ssh $VM "docker cp $VM_DIR/$EVALS_WAR $CONTAINER:/opt/liferay/deploy; docker cp $VM_DIR/$EVALS_WAR $CONTAINER:/opt/evals; docker exec $CONTAINER bash -c \"mkdir -p $EVALS_PORTLET_DIR; cd $EVALS_PORTLET_DIR; /usr/lib/jvm/zulu8-ca/bin/jar -xvf /opt/evals/$EVALS_WAR\"; rm $VM_DIR/$EVALS_WAR"
  fi
)

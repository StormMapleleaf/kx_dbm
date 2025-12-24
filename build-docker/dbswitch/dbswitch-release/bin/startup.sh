APP_MAIN="org.dromara.dbswitch.admin.AdminApplication"


APP_HOME="${BASH_SOURCE-$0}"
APP_HOME="$(dirname "${APP_HOME}")"
APP_HOME="$(cd "${APP_HOME}"; pwd)"
APP_HOME="$(cd "$(dirname ${APP_HOME})"; pwd)"

APP_BIN_PATH=$APP_HOME/bin
APP_LIB_PATH=$APP_HOME/lib
APP_EXT_PATH=$APP_HOME/ext
APP_CONF_PATH=$APP_HOME/conf
export APP_DRIVERS_PATH=$APP_HOME/drivers

APP_PID_FILE="${APP_HOME}/run/${APP_MAIN}.pid"
APP_RUN_LOG="${APP_HOME}/run/run_${APP_MAIN}.log"

[ -d "${APP_HOME}/run" ] || mkdir -p "${APP_HOME}/run"
cd ${APP_HOME}

echo -n `date +'%Y-%m-%d %H:%M:%S'`              >>${APP_RUN_LOG}
echo "---- Start service [${APP_MAIN}] process. ">>${APP_RUN_LOG}

JVMFLAGS="-Dfile.encoding=UTF-8 -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:./gc.log"

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

CLASSPATH=$APP_CONF_PATH
for i in $APP_LIB_PATH/*.jar
do
	CLASSPATH="$i:$CLASSPATH"
done
CLASSPATH="$CLASSPATH:$APP_EXT_PATH/*"

$JAVA -cp $CLASSPATH $JVMFLAGS $APP_MAIN $APP_CONF_PATH

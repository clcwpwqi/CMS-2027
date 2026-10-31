#!/bin/sh
APP_HOME=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$CLASSPATH" ]; then
  echo "gradle-wrapper.jar missing; using system Gradle" >&2
  exec gradle "$@"
fi
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
JAVACMD=java
if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
fi
exec "$JAVACMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

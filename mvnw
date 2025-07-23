#!/bin/sh
# ----------------------------------------------------------------------------
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements. See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership. The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License. You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Maven Start Up Batch script
#
# Required ENV vars:
# ------------------
#   JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
# ------------------
#   M2_HOME - location of maven2's installed home dir
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
#     e.g. to debug Maven itself, use
#       set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE
#       set MAVEN_DEBUG_OPTS=-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
# ----------------------------------------------------------------------------

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
EXECUTABLE=mvn

# Check if the Maven wrapper jar exists
if [ ! -f "$PRGDIR/.mvn/wrapper/maven-wrapper.jar" ]; then
  echo "Could not find .mvn/wrapper/maven-wrapper.jar. Please run the following command to initialize:"
  echo "mvn -N io.takari:maven:wrapper"
  exit 1
fi

# For Cygwin, switch paths to Windows format before running java
if [ "$(uname)" = "CYGWIN" ]; then
  PRGDIR=`cygpath --path --windows "$PRGDIR"`
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

exec "$JAVA_HOME/bin/java" \
  -classpath "$PRGDIR/.mvn/wrapper/maven-wrapper.jar" \
  -Dmaven.home="$M2_HOME" \
  -Dmaven.multiModuleProjectDirectory="$PRGDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"

#!/bin/sh
classpath=$1
shift
exec java -classpath "$classpath" -De.onErrorExit=report org.erights.e.elang.interp.Rune `dirname $0`/limitSub.e "$@"
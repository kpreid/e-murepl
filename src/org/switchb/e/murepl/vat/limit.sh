#!/bin/sh

# Copyright 2011 Kevin Reid, under the terms of the MIT X license
# found at http://www.opensource.org/licenses/mit-license.html ................

# Usage: limit.sh [-l <ulimit option> <ulimit value>]* -- <classpath> <limitSub args>

while [ "X$1" = 'X-l' ]; do
  ulimit "$2" "$3"
  shift; shift; shift
done

if [ \! "X$1" = "X--" ]; then
  echo "$0: Missing -- argument"
  exit 254
fi
shift

classpath=$1
shift
exec java -classpath "$classpath" -De.onErrorExit=report org.erights.e.elang.interp.Rune `dirname $0`/limitSub.e "$@"

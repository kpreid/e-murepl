#!/bin/sh
exec rune -De.onErrorExit=report `dirname $0`/limitSub.e "$@"
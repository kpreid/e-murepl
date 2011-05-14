#!/bin/bash
echo "limit.sh stdout"
echo "limit.sh stderr" >&2
exec rune `dirname $0`/limitSub.e "$@"
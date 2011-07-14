redo-ifchange depclasspath
CLASSPATH="$(cat depclasspath)"
echo Building from $CLASSPATH >&2

javac -cp "$CLASSPATH" $(find . -name '*.java' -print) >&2
redo-ifchange $(find . -name '*.java' -print)

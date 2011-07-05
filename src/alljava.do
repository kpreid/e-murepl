CLASSPATH="$(rune -show | perl -ne 'print scalar <> if /^-cp$/')"

function glob_to_classpath () {
  local IFS=":" jars
  jars=($1)
  CLASSPATH="$CLASSPATH:${jars[*]}"
  IFS="$OLDIFS"
}
glob_to_classpath ../deps/jetty/lib/servlet-api-*.jar

# (pwd; echo $CLASSPATH) >&2

javac -cp "$CLASSPATH" $(find . -name '*.java' -print) >&2
redo-ifchange $(find . -name '*.java' -print)

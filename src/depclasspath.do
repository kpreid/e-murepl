redo-ifchange ../deps/jetty/lib
CLASSPATH="$(devrune -show | perl -ne 'print scalar <> if /^-cp$/')"
function glob_to_classpath () {
  local IFS=":" jars
  jars=($1)
  CLASSPATH="$CLASSPATH:${jars[*]}"
  IFS="$OLDIFS"
}
glob_to_classpath ../deps/jetty/lib/servlet-api-*.jar
echo $CLASSPATH
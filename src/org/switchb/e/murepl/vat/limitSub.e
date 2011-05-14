pragma.syntax("0.9")

def [bootstrapSturdyText] := interp.getArgs()
introducer.onTheAir()
def bootstrapResolver := introducer.sturdyFromURI(bootstrapSturdyText).getRcvr()

/** This object provides access to the sub-vat */
def innerController {
  
}

stderr.println("limitSub about to contact bootstrap")
interp.waitAtTop(
  when (bootstrapResolver <- resolve(innerController)) -> {
    stderr.println("contacted bootstrap resolver")
  } catch p {
    stderr.println("error from contacting bootstrap resolver")
    interp.exitAtTop(p)
  }
)
pragma.syntax("0.9")

def EExpr := <type:org.erights.e.elang.evm.EExpr>

def [bootstrapSturdyText] := interp.getArgs()
introducer.onTheAir()
def bootstrapResolver := introducer.sturdyFromURI(bootstrapSturdyText).getRcvr()

/** This object provides access to the sub-vat */
def innerController {
  
  to seed(expr :EExpr) {
    #stderr.println(`seed $expr`)
    def r := expr.eval(privilegedScope)
    #stderr.println("done seed")
    return r
  }
  
  to orderlyShutdown() {
    interp.continueAtTop()
  }
}

{
  when (bootstrapResolver <- resolve(innerController)) -> {
    #stderr.println("contacted bootstrap resolver")
  } catch p {
    stderr.println("# limitSub: error from contacting bootstrap resolver")
    interp.exitAtTop(p)
  }

  interp.blockAtTop()
}

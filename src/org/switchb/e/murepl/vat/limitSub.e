# Copyright 2011 Kevin Reid, under the terms of the MIT X license
# found at http://www.opensource.org/licenses/mit-license.html ................

pragma.syntax("0.9")

def EExpr := <type:org.erights.e.elang.evm.EExpr>

def [bootstrapSturdyText, myName] := interp.getArgs()

# This process ends when 'shutdown' is resolved
def [shutdown, shutdownResolver] := Ref.promise()

/** This object provides access to the sub-vat */
def innerController {
  
  to __reactToLostClient(problem) {
    if (!Ref.isResolved(shutdown)) {
      stderr.println(`# Isolated vat $myName: Shutting down due to lost client: $problem`)
      innerController.orderlyShutdown()
    }
  }
  
  to seed(expr :EExpr) {
    #stderr.println(`seed $expr`)
    def r := expr.eval(privilegedScope)
    #stderr.println("done seed")
    return r
  }
  
  to orderlyShutdown() {
    shutdownResolver.resolveRace(true)
  }
}

# Establish communication with creator vat.
{
  introducer.onTheAir()
  def bootstrapResolver := introducer.sturdyFromURI(bootstrapSturdyText).getRcvr()

  when (bootstrapResolver <- resolve(innerController)) -> {
    #stderr.println("contacted bootstrap resolver")
  } catch p {
    stderr.println(`# Isolated vat $myName: error from contacting bootstrap resolver:`)
    interp.exitAtTop(p)
  }
}

interp.waitAtTop(shutdown)

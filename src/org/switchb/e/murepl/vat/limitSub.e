# Copyright 2011 Kevin Reid, under the terms of the MIT X license
# found at http://www.opensource.org/licenses/mit-license.html ................

pragma.syntax("0.9")

# stderr.println("limitSub.e started")

def <murepl> := <import:org.switchb.e.murepl.*>

def EExpr := <type:org.erights.e.elang.evm.EExpr>
def makeLogger := <murepl:util.makeLogger>
def runtime := <unsafe:java.lang.makeRuntime>.getRuntime()

def [bootstrapSturdyText, myName] := interp.getArgs()

# This process ends when 'shutdown' is resolved
def [shutdown, shutdownResolver] := Ref.promise()

def rootLogger := makeLogger.makeStandardLogger(stderr, timer, runtime).nest(myName)

/** This object provides access to the sub-vat */
def innerController {
  
  to __reactToLostClient(problem) {
    if (!Ref.isResolved(shutdown)) {
      rootLogger(`Shutting down due to lost client: $problem`)
      innerController.orderlyShutdown()
    }
  }
  
  to seed(expr :EExpr) {
    #stderr.println(`seed $expr`)
    def r := expr.eval(privilegedScope.with("logger", rootLogger))
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
    rootLogger("contacted bootstrap resolver")
  } catch p {
    rootLogger(`error from contacting bootstrap resolver:`)
    interp.exitAtTop(p)
  }
}

rootLogger("Isolated vat ready")
interp.waitAtTop(shutdown)

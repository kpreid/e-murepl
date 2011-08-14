#!/usr/bin/env rune

# Copyright 2011 Kevin Reid, under the terms of the MIT X license
# found at http://www.opensource.org/licenses/mit-license.html ................

pragma.syntax("0.9")

introducer.onTheAir()

def seedVat := <elang:interp.seedVatAuthor>(<unsafe>)

def <murepl> := <import:org.switchb.e.murepl.*>

def makeLogger := <murepl:util.makeLogger>

def runtime := <unsafe:java.lang.makeRuntime>.getRuntime()
def rootLogger := makeLogger.makeStandardLogger(stderr, timer, runtime)

def makeIsolatedVat := <murepl:vat.makeIsolatedVatAuthor>(<unsafe>, interp.getProps(), <unsafe:java.lang.makeRuntime>, introducer, identityMgr, stdout, stderr, seedVat, rootLogger)
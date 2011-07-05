// Copyright 2011 Kevin Reid, under the terms of the MIT X license
// found at http://www.opensource.org/licenses/mit-license.html ...............

package org.switchb.e.murepl.transport.http;

import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.erights.e.elib.deflect.Deflector;
import org.erights.e.elib.base.Callable;
import org.erights.e.elib.util.OneArgFunc;
import org.erights.e.elib.prim.E;
import org.erights.e.elib.prim.Message;
import org.erights.e.elib.ref.Ref;
import org.erights.e.elib.ref.Resolver;
import org.erights.e.elib.vat.Vat;

import org.switchb.e.murepl.RemoteDeflector;

/**
 * Wrapper allowing the implementation of a HttpServlet in E by forwarding
 * messages to a vat.
 */
public class EHttpServlet extends HttpServlet {
  
  private String[] methods = {
    "OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE", "TRACE"
  };
  
  private Vat vat;
  private Object handler;
  private boolean[] implemented = new boolean[methods.length];
  
  /**
   * This constructor closes over the current vat when it is called.
   */
  public EHttpServlet(final Object handler) {
    this.handler = handler;
    vat = Vat.getCurrentVat();
    
    for (int i = 0; i < methods.length; i++) {
      implemented[i] = (Boolean)E.call(handler, "__respondsTo", methods[i], 2);
    }
  }
  
  @Override
  protected void service(final HttpServletRequest req,
                         final HttpServletResponse res)
      throws javax.servlet.ServletException, java.io.IOException {
    boolean thisImplemented = true;
    for (int i = 0; i < methods.length; i++) {
      if (methods[i].equals(req.getMethod())) {
        thisImplemented = implemented[i];
        break;
      }
    }
    System.err.println("EHttpServlet: " + req.getMethod() + " implemented: " + thisImplemented);
    if (thisImplemented) {
      final CountDownLatch latch = new CountDownLatch(1);
      vat.now(new Runnable() { public void run() {
        Ref.whenResolved(E.send(handler,
                                req.getMethod(),
                                req, res),
                         new OneArgFunc() { 
                           public Object run(Object resolution) {
          latch.countDown();
          return null;
        }});
      }});
      System.err.println("EHttpServlet: awaiting resolution");
      try {
        latch.await();
      } catch (InterruptedException e) {}
      System.err.println("EHttpServlet: done");
    } else {
      super.service(req, res);
    }
  }
}
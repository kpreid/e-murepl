// Copyright 2002 Combex, Inc. under the terms of the MIT X license
// found at http://www.opensource.org/licenses/mit-license.html ...............

package org.switchb.e.murepl;

import org.erights.e.develop.exception.ThrowableSugar;
import org.erights.e.develop.trace.Trace;
import org.erights.e.elib.base.Callable;
import org.erights.e.elib.base.Ejection;
import org.erights.e.elib.prim.E;
import org.erights.e.elib.prim.StaticMaker;
import org.erights.e.elib.ref.Ref;
import org.erights.e.elib.serial.JOSSPassByConstruction;
import org.erights.e.elib.serial.Persistent;
import org.erights.e.elib.tables.FlexList;
import org.erights.e.elib.tables.Selfless;
import org.erights.e.elib.vat.Vat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This is like {@link org.erights.e.elib.deflect.Deflector}, except that the
 * deflection may be invoked from another thread, which will enqueue messages
 * on the vat. It is thus useful for wrapping listeners which will be invoked
 * by a thread spawned by a Java library, such as many network protocol
 * libraries.
 *
 * XXX The design of this has not been fully reviewed for things which are
 * remnants of being copied from Deflector.
 *
 * XXX Migrate this into E-on-Java proper.
 *
 * @author Kevin Reid
 * @author Mark S. Miller
 */
public class RemoteDeflector
  implements InvocationHandler, JOSSPassByConstruction, Selfless, Persistent {

    static private final long serialVersionUID = 2723871113337938770L;

    /**
     *
     */
    static private final StaticMaker ThisMaker =
      StaticMaker.make(RemoteDeflector.class);

    /**
     * See the class comment about E-Interfaces
     */
    static private final Class[] EInterfaces = {Callable.class,
      JOSSPassByConstruction.class,
      Selfless.class,
      Persistent.class};

    /**
     * @serial The wrapped/target/deflected object.
     */
    private final Callable myDeflected;

    /**
     * XXX Bug: Since this is transient, it must be restored to the containing
     * Vat on revival; or perhaps it shouldn't be transient?
     */
    private transient final Vat myVat;

    /**
     * Makes a deflector which will wrap and deflect a Callable.
     */
    private RemoteDeflector(Callable target) {
        myDeflected = target;
        myVat = Vat.getCurrentVat();
    }

    /**
     * Deflects target to face by wrapping it in a Deflector, and wrapping that
     * in a deflection (a Proxy).
     * <p/>
     * Taming note: To be used only by {@link org.erights.e.meta.java.lang.InterfaceGuardSugar#coerce
     * InterfaceGuardSugar.coerce/2}, as that's where the we check whether
     * <tt>face</tt> is a rubber-stamping (non-{@link org.erights.e.elib.serial.Marker
     * Marker}) interface.
     */
    static public Proxy deflect(Object target, Class face) {
        target = Ref.resolution(target);

        FlexList faceList =
          FlexList.fromType(Class.class, 1 + EInterfaces.length);
        faceList.push(face);
        faceList.push(Callable.class);
        for (int i = 1, len = EInterfaces.length; i < len; i++) {
            Class intf = EInterfaces[i];
            if (intf.isInstance(target)) {
                faceList.push(intf);
            }
        }
        Class[] faces = (Class[])faceList.getArray(Class.class);
        Callable targ = Ref.toCallable(target);
        InvocationHandler handler = new RemoteDeflector(targ);
        return (Proxy)Proxy.newProxyInstance(Callable.class.getClassLoader(),
                                             faces,
                                             handler);
    }

    /**
     * Uses 'DeflectorMaker(myDeflected)'
     */
    public Object[] getSpreadUncall() {
        Object[] result = {ThisMaker, "run", myDeflected};
        return result;
    }

    /**
     * This is the magic method invoked by the Proxy mechanism.
     * <p/>
     * If the method is either an Object method or a method declared by one of
     * the EInterfaces, then just invoke this method directly on the deflected
     * target object.
     * <p/>
     * Otherwise, we turn it into a callAll() on the deflected target object.
     * <p/>
     * XXX Currently, we only use the method's simple name, but that's fine for
     * all objects defined in E. The only practical place this fails is remote
     * invocation of overloaded Java methods.
     *
     * @param optArgs may be null, so we replace with an empty list.
     */
    public Object invoke(Object proxy, Method method, Object[] optArgs)
      throws Throwable {

        try {
            Object[] args = E.NO_ARGS;
            if (null != optArgs) {
                args = optArgs;
            }
            Class clazz = method.getDeclaringClass();
            boolean isEInterface = (Object.class == clazz);
            if (!isEInterface) {
                for (int i = 0; i < EInterfaces.length; i++) {
                    if (EInterfaces[i] == clazz) {
                        isEInterface = true;
                        break;
                    }
                }
            }
            if (isEInterface) {
                System.err.println("RemoteDeflector not handling " + method.getName() + " right\n");
                return myVat.invoke(myDeflected, method, args);
            }

            String verb = method.getName();
            Class retType = method.getReturnType();
            myVat.qSendAllOnly(myDeflected, false, verb, args);
            return null;

        } catch (Throwable ex) {
            Throwable leaf = ThrowableSugar.leaf(ex);
            if (leaf instanceof Ejection) {
                throw (Ejection)leaf;
            }
            if (Trace.causality.event && Trace.ON) {
                Trace.causality.eventm("During Deflection: ", ex);
            }
            throw ex;
        }
    }
}

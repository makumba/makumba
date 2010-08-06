/*
 * Created on Aug 6, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.context.FacesContext;

/**
 * Tree visiting utilities.
 */
public class VisitUtils {

    /**
     * Performs a full tree visit, skipping iteration.
     * 
     * @param context
     *            the FacesContext for this request
     * @param callback
     *            the callback to invoke during the visit
     * @return the result of visitTree() call
     */
    public static boolean doFullNonIteratingVisit(FacesContext context, VisitCallback callback) {

        assert context != null;
        assert callback != null;

        UIViewRoot root = context.getViewRoot();
        VisitContext vContext = VisitContext.createVisitContext(context);

        startSkipIteration(context);

        try {
            return root.visitTree(vContext, callback);
        } finally {
            endSkipIteration(context);
        }
    }

    /**
     * Tests whether we are skipping iteration.
     */
    public static boolean isSkippingIteration(FacesContext context) {

        Object hint = context.getAttributes().get(SKIP_ITERATION_HINT);
        return Boolean.TRUE.equals(hint);
    }

    public static void startStateSaveRestore(FacesContext context) {
        context.getAttributes().put(EXECUTE_STATE_SAVING_HINT, true);
    }

    public static void endStateSaveRestore(FacesContext context) {
        context.getAttributes().remove(EXECUTE_STATE_SAVING_HINT);
    }

    private static void startSkipIteration(FacesContext context) {

        // This code assumes that we won't have nested visits.
        assert !isSkippingIteration(context);
        context.getAttributes().put(SKIP_ITERATION_HINT, true);
    }

    private static void endSkipIteration(FacesContext context) {

        assert isSkippingIteration(context);
        context.getAttributes().remove(SKIP_ITERATION_HINT);
    }

    private static String SKIP_ITERATION_HINT = "javax.faces.visit.SKIP_ITERATION";

    private static String EXECUTE_STATE_SAVING_HINT = "javax.faces.visit.EXECUTE_STATE_SAVING";
}

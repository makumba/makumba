///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id: FormsHQLTest.java 4982 2010-05-09 21:00:38Z cristian_bogdan $
//  $Name$
/////////////////////////////////////

package org.makumba.test.tags;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.Test;

import org.apache.cactus.Request;
import org.makumba.test.util.MakumbaJspTestCase;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;

/**
 * @author Rudolf Mayer
 * @author Manuel Gay
 * @version $Id: FormsHQLTest.java 4982 2010-05-09 21:00:38Z cristian_bogdan $
 */
public class FormsHQLTest extends MakumbaJspTestCase {

    {
        recording = false;
        jspDir = "forms-hql";
    }

    public static Test suite() {
        return makeSuite(FormsHQLTest.class, "hql");
    }

    public void testTomcat() {
    }

    public void testHibernateMakNewForm() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakNewForm(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }

    public void beginHibernateMakAddForm(Request request) throws Exception {
        WebForm form = getFormInJspWithTestName(false);
        // set the input field "email" to "bartolomeus@rogue.be"
        form.setParameter("email", "bartolomeus@rogue.be");
        // submit the form
        form.submit();
    }

    public void testHibernateMakAddForm() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakAddForm(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }

    public void testHibernateMakEditForm() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endHibernateMakEditForm(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }

    public void testHibernateMakForm() throws ServletException, IOException, SAXException {
        includeJspWithTestName();
    }

    public void endHibernateMakForm(WebResponse response) throws Exception {
        compareToFileWithTestName(response);
    }
}

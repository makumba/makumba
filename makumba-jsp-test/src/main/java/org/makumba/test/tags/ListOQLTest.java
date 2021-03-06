package org.makumba.test.tags;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.makumba.test.util.MakumbaJspTestCase;
import org.makumba.test.util.MakumbaTestSetup;
import org.makumba.test.util.MakumbaWebTestSetup;

import com.meterware.httpunit.WebResponse;

/**
 * Tests the OQL list engine.
 * 
 * @author Johannes Peeters
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: ListOQLTest.java,v 1.1 25.09.2007 15:58:58 Manuel Exp $
 */
public class ListOQLTest extends MakumbaJspTestCase {

    @Override
    protected String getJspDir() {
        return "list-oql";
    }

    @Override
    protected MakumbaTestSetup getSetup() {
        return setup;
    }

    static Suite setup;

    private static final class Suite extends MakumbaWebTestSetup {
        private Suite(Test arg0) {
            super(arg0, "oql");
        }
    }

    public static Test suite() {
        setup = new Suite(new TestSuite(ListOQLTest.class));
        return setup;
    }

    public void testTomcat() {
    }

    public void testMakObjectTag() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakObjectTag(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListTag() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListTag(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListFunctionOrderBy() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListFunctionOrderBy(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListCount() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListCount(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListCountMultiNestedLists() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListCountMultiNestedLists(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    // FIXME this is a bug/behaviour that might need to be fixed (see http://bugs.makumba.org/show_bug.cgi?id=1201)
    public void underDiscussiontestMakListCountClosedList() throws ServletException, IOException {
        // FIXME: jsp name not the same as test name
        pageContext.include("list-oql/testMakListCountClosedList.jsp");
    }

    public void endMakListCountClosedList(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListMaxResults() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListMaxResults(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListNextCount() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListNextCount(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListNextCountById() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListNextCountById(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListNextCountNestedList() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListNextCountNestedList(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListNextCountAfterMultiNestedList() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListNextCountAfterMultiNestedList(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListNextCountAsymmetricNestedList() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListNextCountAsymmetricNestedList(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListNextCountMultiNestedList() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListNextCountMultiNestedList(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListPointerComparison() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListPointerComparison(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListInSetPointers() throws ServletException, IOException {
        // FIXME: jsp name not the same as test name
        pageContext.include("list-oql/testMakListInSetPtrs.jsp");
    }

    public void endMakListInSetPointers(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueChar() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakValueChar(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueDate() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakValueDate(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueInt() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakValueInt(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueDouble() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakValueDouble(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueText() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakValueText(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueSet() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakValueSet(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueFunction() throws Exception {
        includeJspWithTestName();
    }

    public void endMakValueFunction(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueFunctionAndTag() throws Exception {
        includeJspWithTestName();
    }

    public void endMakValueFunctionAndTag(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueChangeFunctions() throws Exception {
        includeJspWithTestName();
    }

    public void endMakValueChangeFunctions(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    /*
     * Commented out by manu on 22-05-2009, since those tests fail with the current comparison method public void
     * testMakValueTS_create() throws ServletException, IOException { // FIXME: this test will fail // a line-by-line
     * comparison can not work for the dynamic values TS_create & TS_ modify
     * pageContext.include("list-oql/testMakValueTS_create.jsp"); } public void endMakValueTS_create(WebResponse
     * response) throws Exception { checkResult(response); } public void testMakValueTS_modify() throws
     * ServletException, IOException { // FIXME: this test will fail // a line-by-line comparison can not work for the
     * dynamic values TS_create & TS_ modify pageContext.include("list-oql/testMakValueTS_modify.jsp"); } public void
     * endMakValueTS_modify(WebResponse response) throws Exception { checkResult(response); }
     */

    public void testMQLFunctions() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMQLFunctions(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakumbaMQLFunctions() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakumbaMQLFunctions(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakPagination() throws ServletException, IOException {
        // FIXME: jsp name not the same as test name
        pageContext.include("list-oql/testMakPaginationTag.jsp");
    }

    public void endMakPagination(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakPaginationGroupBy() throws ServletException, IOException {
        // FIXME: jsp name not the same as test name
        pageContext.include("list-oql/testMakPaginationTagGroupBy.jsp");
    }

    public void endMakPaginationGroupBy(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMDDFunctions() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMDDFunctions(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakValueDistinct() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakValueDistinct(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMakListAggregation() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMakListAggregation(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMDDFunctions2() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMDDFunctions2(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testMDDFunctionsPointers() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endMDDFunctionsPointers(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testSectionSimple() throws ServletException, IOException {
        pageContext.include("list-oql/testMakSectionSimple.jsp");
    }

    public void endSectionSimple(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testSectionList() throws ServletException, IOException {
        // FIXME: jsp name not the same as test name
        pageContext.include("list-oql/testMakSectionList.jsp");
    }

    public void endSectionList(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testSectionListExpr() throws ServletException, IOException {
        // FIXME: jsp name not the same as test name
        pageContext.include("list-oql/testMakSectionListExpr.jsp");
    }

    public void endSectionListExpr(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testNotInSet() throws ServletException, IOException {
        // FIXME: jsp name not the same as test name
        pageContext.include("list-oql/testNotIn.jsp");
    }

    public void endNotInSet(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testParamMultiple() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endParamMultiple(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testParamDifferentTypes() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endParamDifferentTypes(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }

    public void testParamRepeatedAssignement() throws ServletException, IOException {
        includeJspWithTestName();
    }

    public void endParamRepeatedAssignement(WebResponse response) throws Exception {
        compareToFileWithTestName(response, false);
    }
}

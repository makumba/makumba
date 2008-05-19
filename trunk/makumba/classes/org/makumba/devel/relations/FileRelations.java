package org.makumba.devel.relations;

import java.util.Map;
import java.util.Vector;

/**
 * This class holds the relations a file has with JSP, MDD and/or Java files.
 * 
 * @author Manuel Gay
 * @version $Id: FileRelations.java,v 1.1 Apr 22, 2008 10:09:54 AM manu Exp $
 */
public class FileRelations {

    public FileRelations(String relativeFilePath, Map<String, Vector<FileRelations.RelationOrigin>> jspRelations,
            Map<String, Vector<FileRelations.RelationOrigin>> mddRelations,
            Map<String, Vector<FileRelations.RelationOrigin>> javaRelations) {

        this.path = relativeFilePath;
        this.jspRelations = jspRelations;
        this.javaRelations = javaRelations;
        this.mddRelations = mddRelations;
    }

    private String path;

    private Map<String, Vector<FileRelations.RelationOrigin>> jspRelations;

    private Map<String, Vector<FileRelations.RelationOrigin>> mddRelations;

    private Map<String, Vector<FileRelations.RelationOrigin>> javaRelations;

    public Map<String, Vector<FileRelations.RelationOrigin>> getJspRelations() {
        return jspRelations;
    }

    public Map<String, Vector<FileRelations.RelationOrigin>> getMddRelations() {
        return mddRelations;
    }

    public Map<String, Vector<FileRelations.RelationOrigin>> getJavaRelations() {
        return javaRelations;
    }

    public String getPath() {
        return path;
    }

    /**
     * Origin of the relation
     * 
     * @author Manuel Gay
     * @version $Id: FileRelations.java,v 1.1 Apr 23, 2008 2:59:44 PM manu Exp $
     */
    public class RelationOrigin {

        private int startCol;

        private int endCol;

        private int startLine;

        private int endLine;

        private String tagName;

        private String expr;

        private String field;

        private String reason;

        /**
         * Gets the start column of the tag at the origin of a relation
         * 
         * @return the column of the start of the tag, -1 if it doesn't apply
         */
        public int getStartCol() {
            return startCol;
        }

        /**
         * Gets the end column of the tag at the origin of a relation
         * 
         * @return the column of the end of the tag, -1 if it doesn't apply
         */
        public int getEndCol() {
            return endCol;
        }

        /**
         * Gets the start line of the tag at the origin of a relation
         * 
         * @return the line of the start of the tag, -1 if it doesn't apply
         */
        public int getStartLine() {
            return startLine;
        }

        /**
         * Gets the end line of the tag at the origin of a relation
         * 
         * @return the line of the end of the tag, -1 if it doesn't apply
         */
        public int getEndLine() {
            return endLine;
        }

        /**
         * Gets the name of the tag at the origin of a relation
         * 
         * @return the name of the tag, or null if it doesn't apply
         */
        public String getTagName() {
            return tagName;
        }

        /**
         * Gets the expression at the origin of a relation. This applies to relations to an MDD.
         * 
         * @return the name of the expression at the origin of a relation, null otherwise
         */
        public String getExpr() {
            return expr;
        }

        /**
         * The field pointed by a relation to a MDD. This applies to relations to an MDD
         * 
         * @return the field pointed by a relation. If the pointed type is an element of a setComplex (or other field
         *         type contained in a MDD), this will return a result of the kind "setName.fieldName" (e.g.
         *         address.streetno, with address being a setComplex)
         */
        public String getField() {
            return field;
        }

        /**
         * Gets the reason at the origin of a relation, if none of the other elements could be provided. This is the
         * case for Java->MDD relations, where the reason is the query issued in the Java BL.
         * 
         * @return more information if available, null otherwise
         */
        public String getOtherReason() {
            return reason;
        }

        public RelationOrigin(int startCol, int endCol, int startLine, int endLine, String tagName, String expr,
                String field, String reason) {
            this.startCol = startCol;
            this.endCol = endCol;
            this.startLine = startLine;
            this.endLine = endLine;
            this.tagName = tagName;
            this.expr = expr;
            this.field = field;
            this.reason = reason;
        }

        public String toString() {
            String result = "Relation origin:\n";
            result += "  startCol: " + startCol + "\n";
            result += "  endCol: " + endCol + "\n";
            result += "  startLine: " + startLine + "\n";
            result += "  endLine: " + endLine + "\n";
            result += "  tagName: " + tagName + "\n";
            result += "  expr: " + expr + "\n";
            result += "  field: " + field + "\n";
            result += "  reason: " + reason + "\n";
            return result;
        }
    }

    public String toString() {
        String result = "Relations for file " + path + "\n";

        result += "  == JSP relations\n";
        for (String jsp : jspRelations.keySet()) {
            result += jsp + "\n";
            Vector<RelationOrigin> origin = jspRelations.get(jsp);
            for (RelationOrigin relationOrigin : origin) {
                result += relationOrigin.toString();
            }
            result += "\n";
        }

        result += "  == Java relations\n";
        for (String java : javaRelations.keySet()) {
            result += java + "\n";
            Vector<RelationOrigin> origin = javaRelations.get(java);
            for (RelationOrigin relationOrigin : origin) {
                result += relationOrigin.toString();
            }
            result += "\n";
        }

        result += "  == MDD relations\n";
        for (String mdd : mddRelations.keySet()) {
            result += mdd + "\n";
            Vector<RelationOrigin> origin = mddRelations.get(mdd);
            for (RelationOrigin relationOrigin : origin) {
                result += relationOrigin.toString();
            }
            result += "\n";
        }

        result += "\n";

        return result;

    }

    public boolean isEmpty() {
        return getMddRelations().size() == 0 && getJspRelations().size() == 0 && getMddRelations().size() == 0;
    }

}

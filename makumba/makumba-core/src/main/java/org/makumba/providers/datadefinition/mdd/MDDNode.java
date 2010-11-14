package org.makumba.providers.datadefinition.mdd;

import java.net.URL;
import java.util.LinkedHashMap;

import org.makumba.DataDefinition;
import org.makumba.QueryFragmentFunction;
import org.makumba.ValidationRule;
import org.makumba.DataDefinition.MultipleUniqueKeyDefinition;

import antlr.CommonAST;
import antlr.collections.AST;

/**
 * AST node that collects information for building a {@link DataDefinition}
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: MDDNode.java,v 1.1 May 3, 2009 8:01:07 PM manu Exp $
 */
public class MDDNode extends CommonAST {

    private static final long serialVersionUID = -2197404256348086058L;

    final static String createName = "TS_create";

    final static String modifyName = "TS_modify";

    /** name of the data definition **/
    protected String name = "";

    /** name of the pointer to the subfield, for building names dynamically **/
    protected String ptrSubfield = "";

    /** name of the index field **/
    protected String indexName = "";

    /** the title field **/
    protected TitleFieldNode titleField = new TitleFieldNode();

    /** origin of the data definition **/
    protected URL origin;

    /** name of the parent MDD of the subfield **/
    protected String parent;

    /** name of the field in the parent MDD, if this is a ptrOne or setComplex **/
    protected String fieldNameInParent = "";

    /** indicator if this is MDD is a file subfield **/
    protected boolean isFileSubfield = false;

    protected LinkedHashMap<String, FieldNode> fields = new LinkedHashMap<String, FieldNode>();

    protected LinkedHashMap<String, ValidationRule> validationRules = new LinkedHashMap<String, ValidationRule>();

    protected LinkedHashMap<Object, MultipleUniqueKeyDefinition> multiFieldUniqueList = new LinkedHashMap<Object, MultipleUniqueKeyDefinition>();

    protected LinkedHashMap<String, QueryFragmentFunction> functions = new LinkedHashMap<String, QueryFragmentFunction>();

    public MDDNode(String name, URL origin) {
        this.setName(name);
        this.origin = origin;
    }

    /** constructor for the creation of subfields **/
    public MDDNode(MDDNode parent, String subFieldName) {
        this.setName(parent.getName());
        this.ptrSubfield = "->" + subFieldName;
        this.origin = parent.origin;
        this.parent = parent.getName();
        this.fieldNameInParent = subFieldName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name + ptrSubfield;
    }

    protected void setTitleField(TitleFieldNode title) {
        titleField = title;
        titleField.mdd = this;
    }

    public void addField(FieldNode fi) {
        fields.put(fi.name, fi);
    }

    public void removeField(String name) {
        if (fields.get(name) != null) {
            fields.remove(name);
        }
    }

    public void addValidationRule(ValidationRuleNode vn) {
        validationRules.put(vn.getRuleName(), vn);
    }

    public void addMultiUniqueKey(MultipleUniqueKeyDefinition definition) {
        multiFieldUniqueList.put(definition.getFields(), definition);
    }

    public void addFunction(FunctionNode funct, AST parsedFunction) {
        QueryFragmentFunction function = new QueryFragmentFunction(null, funct.name,
                funct.sessionVariableName, funct.queryFragment, funct.parameters, funct.errorMessage);
        funct.function = function;
        functions.put(function.getName(), function);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Type name: " + getName() + "\n");
        sb.append("Type origin: " + origin + "\n");
        if (parent != null) {
            sb.append("Type parent: " + parent + "\n");
        }
        if (titleField != null) {
            sb.append("Title field: " + titleField.getText());
        }
        sb.append("\nFields:" + "\n");
        for (String string : fields.keySet()) {
            sb.append(fields.get(string).toString() + "\n");
        }

        sb.append("\nValidation rules:" + "\n");
        for (String string : validationRules.keySet()) {
            sb.append(validationRules.get(string).toString() + "\n");
        }

        return sb.toString();

    }

}

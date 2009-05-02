package org.makumba.providers.datadefinition.mdd;

import antlr.CommonAST;

public class FieldNode extends CommonAST {
    
    private String name;
    
    private String fieldType;
    
    private String fieldComment;
    
    private int makumbaFieldType;
    
    public FieldNode() { }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setMakumbaFieldType(int makumbaFieldType) {
        this.makumbaFieldType = makumbaFieldType;
    }

    public int getMakumbaFieldType() {
        return makumbaFieldType;
    }
    
    public String toString() {
        return "Field name: " + getName() + "\nField type: " + getFieldType() + "\nField comment: " + getFieldComment();
    }

    public void setFieldComment(String fieldComment) {
        this.fieldComment = fieldComment;
    }

    public String getFieldComment() {
        return fieldComment;
    }

}

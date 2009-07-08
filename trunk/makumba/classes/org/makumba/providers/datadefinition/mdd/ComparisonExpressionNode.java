package org.makumba.providers.datadefinition.mdd;

import java.util.Date;

/**
 * Node that holds data of a comparison expression
 * @author Manuel Gay
 * @version $Id: ComparisonExpressionNode.java,v 1.1 08.07.2009 16:01:49 gaym Exp $
 */
public class ComparisonExpressionNode extends MDDAST {

    public static enum ComparisonType {
        STRING, NUMBER, DATE, INVALID
    };

    private int op;

    private ComparisonType type;

    private String lhs;

    private Date lhs_date;

    private int lhs_type;

    private String rhs;

    private Date rhs_date;

    private int rhs_type;

    public Date getLhs_date() {
        return lhs_date;
    }

    public void setLhs_date(Date lhs_date) {
        this.lhs_date = lhs_date;
    }

    public Date getRhs_date() {
        return rhs_date;
    }

    public void setRhs_date(Date rhs_date) {
        this.rhs_date = rhs_date;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public ComparisonType getComparisonType() {
        return type;
    }

    public void setComparisonType(ComparisonType type) {
        this.type = type;
    }

    public String getLhs() {
        return lhs;
    }

    public void setLhs(String lhs) {
        this.lhs = lhs;
    }

    public int getLhs_type() {
        return lhs_type;
    }

    public void setLhs_type(int lhs_type) {
        this.lhs_type = lhs_type;
    }

    public String getRhs() {
        return rhs;
    }

    public void setRhs(String rhs) {
        this.rhs = rhs;
    }

    public int getRhs_type() {
        return rhs_type;
    }

    public void setRhs_type(int rhs_type) {
        this.rhs_type = rhs_type;
    }

    @Override
    public String toString() {
        return lhs + " " + op + " " + rhs;
    }
}
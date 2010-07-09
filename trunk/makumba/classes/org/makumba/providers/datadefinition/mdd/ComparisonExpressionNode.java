package org.makumba.providers.datadefinition.mdd;

import java.util.Date;

/**
 * Node that holds data of a comparison expression of the kind RHS operator LHS, where RHS/LHS can be either a field
 * name, a date constant or construct, or a function applied to a field
 * 
 * @author Manuel Gay
 * @version $Id: ComparisonExpressionNode.java,v 1.1 08.07.2009 16:01:49 gaym Exp $
 */
public class ComparisonExpressionNode extends MDDAST {

    private static final long serialVersionUID = 4483783192311810176L;

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

    public int getOperatorType() {
        return op;
    }

    public String getOperator() {
        return getOperator(op);
    }

    private String getOperator(int operator) {
        switch (operator) {
            case MDDTokenTypes.EQ:
                return "=";
            case MDDTokenTypes.LT:
                return "<";
            case MDDTokenTypes.GT:
                return ">";
            case MDDTokenTypes.SQL_NE:
                return "<>";
            case MDDTokenTypes.NE:
                return "!=";
            case MDDTokenTypes.LE:
                return "<=";
            case MDDTokenTypes.GE:
                return ">=";
        }
        throw new RuntimeException("invalid value for operator");
    }

    public boolean isEqualityOperator() {
        return op == MDDTokenTypes.EQ || op == MDDTokenTypes.NE || op == MDDTokenTypes.SQL_NE;
    }

    public String getInvertedOperator() {
        switch (op) {
            case MDDTokenTypes.EQ:
                return getOperator(MDDTokenTypes.NE);
            case MDDTokenTypes.SQL_NE:
            case MDDTokenTypes.NE:
                return getOperator(MDDTokenTypes.EQ);
            case MDDTokenTypes.LT:
                return getOperator(MDDTokenTypes.GT);
            case MDDTokenTypes.GT:
                return getOperator(MDDTokenTypes.LT);
            case MDDTokenTypes.LE:
                return getOperator(MDDTokenTypes.GE);
            case MDDTokenTypes.GE:
                return getOperator(MDDTokenTypes.LE);
        }
        throw new RuntimeException("invalid value for operator");
    }

    public void setOperatorType(int op) {
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
        return lhs + " " + getOperator() + " " + rhs;
    }
}
package org.makumba.providers.query.mql;

import antlr.Token;
import antlr.collections.AST;

/** Makumba adaptation of the original class in org.hibernate.hql.ast.tree. 
 * To update, check the differences with the .original file in this repository 
 * */
/**
 * Base node class for use by Hibernate within its AST trees.
 *
 * @author Joshua Davis
 * @author Steve Ebersole
 */
public class Node extends antlr.CommonAST {
    private static final long serialVersionUID = 1L;
    private String filename;
	private int line;
	private int column;
	private int textLength;

	public Node() {
		super();
	}

	public Node(Token tok) {
		super(tok);  // This will call initialize(tok)!
	}

	public void initialize(Token tok) {
		super.initialize(tok);
		filename = tok.getFilename();
		line = tok.getLine();
		column = tok.getColumn();
		String text = tok.getText();
		textLength = (text==null || text.length()==0) ? 0 : text.length();
	}

	public void initialize(AST t) {
		super.initialize( t );
		if ( t instanceof Node ) {
			Node n = (Node)t;
			filename = n.filename;
			line = n.line;
			column = n.column;
			textLength = n.textLength;
		}
	}

	public String getFilename() {
		return filename;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public int getTextLength() {
		return textLength;
	}
	
	public void setLine(int line) {
	    this.line = line;
	}
	
	public void setCol(int col) {
	    this.column = col;
	}
}

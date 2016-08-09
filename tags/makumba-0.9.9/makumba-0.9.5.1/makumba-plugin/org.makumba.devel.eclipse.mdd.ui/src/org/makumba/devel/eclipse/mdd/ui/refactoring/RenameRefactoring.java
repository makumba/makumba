package org.makumba.devel.eclipse.mdd.ui.refactoring;

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

public class RenameRefactoring extends ProcessorBasedRefactoring {

	private String text;

	public RenameRefactoring(RenameProcessor processor) {
		super(processor);
	}

	public String getRenameText() {
		return text;
	}

	public void setRenameText(String text) {
		this.text = text;
	}

}

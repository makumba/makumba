package org.makumba.devel.eclipse.mdd;

import org.eclipse.emf.ecore.EObject;

public class MQLQuerySegment {

	private String segment;
	private EObject reference;

	public String getSegment() {
		return segment;
	}

	public EObject getReference() {
		return reference;
	}

	public MQLQuerySegment(String segment, EObject reference) {
		this.segment = segment;
		this.reference = reference;
	}
}

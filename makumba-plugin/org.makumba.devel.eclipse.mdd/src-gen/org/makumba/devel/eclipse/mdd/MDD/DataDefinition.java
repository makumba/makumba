/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Data Definition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.DataDefinition#getD <em>D</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getDataDefinition()
 * @model
 * @generated
 */
public interface DataDefinition extends EObject
{
  /**
   * Returns the value of the '<em><b>D</b></em>' containment reference list.
   * The list contents are of type {@link org.makumba.devel.eclipse.mdd.MDD.Declaration}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>D</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>D</em>' containment reference list.
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getDataDefinition_D()
   * @model containment="true"
   * @generated
   */
  EList<Declaration> getD();

} // DataDefinition

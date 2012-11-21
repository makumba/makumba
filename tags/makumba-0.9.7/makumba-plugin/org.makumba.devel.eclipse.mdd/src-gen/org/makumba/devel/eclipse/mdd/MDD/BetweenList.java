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
 * A representation of the model object '<em><b>Between List</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.BetweenList#getC <em>C</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getBetweenList()
 * @model
 * @generated
 */
public interface BetweenList extends EObject
{
  /**
   * Returns the value of the '<em><b>C</b></em>' containment reference list.
   * The list contents are of type {@link org.makumba.devel.eclipse.mdd.MDD.Concatenation}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>C</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>C</em>' containment reference list.
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getBetweenList_C()
   * @model containment="true"
   * @generated
   */
  EList<Concatenation> getC();

} // BetweenList

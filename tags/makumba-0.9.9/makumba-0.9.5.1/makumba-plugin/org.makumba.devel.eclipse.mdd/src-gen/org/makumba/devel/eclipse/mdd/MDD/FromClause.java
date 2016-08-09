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
 * A representation of the model object '<em><b>From Clause</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FromClause#getFromRange <em>From Range</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FromClause#getFromJoin <em>From Join</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFromClause()
 * @model
 * @generated
 */
public interface FromClause extends EObject
{
  /**
   * Returns the value of the '<em><b>From Range</b></em>' containment reference list.
   * The list contents are of type {@link org.makumba.devel.eclipse.mdd.MDD.FromRange}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>From Range</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>From Range</em>' containment reference list.
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFromClause_FromRange()
   * @model containment="true"
   * @generated
   */
  EList<FromRange> getFromRange();

  /**
   * Returns the value of the '<em><b>From Join</b></em>' containment reference list.
   * The list contents are of type {@link org.makumba.devel.eclipse.mdd.MDD.FromJoin}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>From Join</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>From Join</em>' containment reference list.
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFromClause_FromJoin()
   * @model containment="true"
   * @generated
   */
  EList<FromJoin> getFromJoin();

} // FromClause

/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Else Clause</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ElseClause#getU <em>U</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getElseClause()
 * @model
 * @generated
 */
public interface ElseClause extends EObject
{
  /**
   * Returns the value of the '<em><b>U</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>U</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>U</em>' containment reference.
   * @see #setU(UnaryExpression)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getElseClause_U()
   * @model containment="true"
   * @generated
   */
  UnaryExpression getU();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ElseClause#getU <em>U</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>U</em>' containment reference.
   * @see #getU()
   * @generated
   */
  void setU(UnaryExpression value);

} // ElseClause

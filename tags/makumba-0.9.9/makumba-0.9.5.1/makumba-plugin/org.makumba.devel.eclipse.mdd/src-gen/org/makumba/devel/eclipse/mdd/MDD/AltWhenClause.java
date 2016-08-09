/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Alt When Clause</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.AltWhenClause#getW <em>W</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.AltWhenClause#getT <em>T</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getAltWhenClause()
 * @model
 * @generated
 */
public interface AltWhenClause extends EObject
{
  /**
   * Returns the value of the '<em><b>W</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>W</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>W</em>' containment reference.
   * @see #setW(UnaryExpression)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getAltWhenClause_W()
   * @model containment="true"
   * @generated
   */
  UnaryExpression getW();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.AltWhenClause#getW <em>W</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>W</em>' containment reference.
   * @see #getW()
   * @generated
   */
  void setW(UnaryExpression value);

  /**
   * Returns the value of the '<em><b>T</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>T</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>T</em>' containment reference.
   * @see #setT(UnaryExpression)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getAltWhenClause_T()
   * @model containment="true"
   * @generated
   */
  UnaryExpression getT();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.AltWhenClause#getT <em>T</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>T</em>' containment reference.
   * @see #getT()
   * @generated
   */
  void setT(UnaryExpression value);

} // AltWhenClause

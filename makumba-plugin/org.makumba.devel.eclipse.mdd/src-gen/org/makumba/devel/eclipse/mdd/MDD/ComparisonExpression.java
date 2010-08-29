/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Comparison Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getLhs <em>Lhs</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getO <em>O</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getRhs <em>Rhs</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonExpression()
 * @model
 * @generated
 */
public interface ComparisonExpression extends EObject
{
  /**
   * Returns the value of the '<em><b>Lhs</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Lhs</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Lhs</em>' containment reference.
   * @see #setLhs(ComparisonPart)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonExpression_Lhs()
   * @model containment="true"
   * @generated
   */
  ComparisonPart getLhs();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getLhs <em>Lhs</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Lhs</em>' containment reference.
   * @see #getLhs()
   * @generated
   */
  void setLhs(ComparisonPart value);

  /**
   * Returns the value of the '<em><b>O</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>O</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>O</em>' attribute.
   * @see #setO(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonExpression_O()
   * @model
   * @generated
   */
  String getO();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getO <em>O</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>O</em>' attribute.
   * @see #getO()
   * @generated
   */
  void setO(String value);

  /**
   * Returns the value of the '<em><b>Rhs</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Rhs</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Rhs</em>' containment reference.
   * @see #setRhs(ComparisonPart)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonExpression_Rhs()
   * @model containment="true"
   * @generated
   */
  ComparisonPart getRhs();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getRhs <em>Rhs</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Rhs</em>' containment reference.
   * @see #getRhs()
   * @generated
   */
  void setRhs(ComparisonPart value);

} // ComparisonExpression

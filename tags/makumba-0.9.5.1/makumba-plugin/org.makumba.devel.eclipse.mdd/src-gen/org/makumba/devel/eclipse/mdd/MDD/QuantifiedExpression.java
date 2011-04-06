/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Quantified Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.QuantifiedExpression#getS <em>S</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getQuantifiedExpression()
 * @model
 * @generated
 */
public interface QuantifiedExpression extends UnaryExpression
{
  /**
   * Returns the value of the '<em><b>S</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>S</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>S</em>' containment reference.
   * @see #setS(UnionRule)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getQuantifiedExpression_S()
   * @model containment="true"
   * @generated
   */
  UnionRule getS();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.QuantifiedExpression#getS <em>S</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>S</em>' containment reference.
   * @see #getS()
   * @generated
   */
  void setS(UnionRule value);

} // QuantifiedExpression

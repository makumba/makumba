/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>New Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.NewExpression#getP <em>P</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.NewExpression#getS <em>S</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getNewExpression()
 * @model
 * @generated
 */
public interface NewExpression extends EObject
{
  /**
   * Returns the value of the '<em><b>P</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>P</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>P</em>' attribute.
   * @see #setP(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getNewExpression_P()
   * @model
   * @generated
   */
  String getP();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.NewExpression#getP <em>P</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>P</em>' attribute.
   * @see #getP()
   * @generated
   */
  void setP(String value);

  /**
   * Returns the value of the '<em><b>S</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>S</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>S</em>' containment reference.
   * @see #setS(SelectedPropertiesList)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getNewExpression_S()
   * @model containment="true"
   * @generated
   */
  SelectedPropertiesList getS();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.NewExpression#getS <em>S</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>S</em>' containment reference.
   * @see #getS()
   * @generated
   */
  void setS(SelectedPropertiesList value);

} // NewExpression

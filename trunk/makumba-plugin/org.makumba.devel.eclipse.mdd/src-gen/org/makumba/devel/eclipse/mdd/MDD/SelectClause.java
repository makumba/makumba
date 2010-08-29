/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Select Clause</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.SelectClause#getS <em>S</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.SelectClause#getN <em>N</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSelectClause()
 * @model
 * @generated
 */
public interface SelectClause extends EObject
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
   * @see #setS(SelectedPropertiesList)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSelectClause_S()
   * @model containment="true"
   * @generated
   */
  SelectedPropertiesList getS();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.SelectClause#getS <em>S</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>S</em>' containment reference.
   * @see #getS()
   * @generated
   */
  void setS(SelectedPropertiesList value);

  /**
   * Returns the value of the '<em><b>N</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>N</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>N</em>' containment reference.
   * @see #setN(NewExpression)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSelectClause_N()
   * @model containment="true"
   * @generated
   */
  NewExpression getN();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.SelectClause#getN <em>N</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>N</em>' containment reference.
   * @see #getN()
   * @generated
   */
  void setN(NewExpression value);

} // SelectClause

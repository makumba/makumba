/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Lower Function</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.LowerFunction#getField <em>Field</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getLowerFunction()
 * @model
 * @generated
 */
public interface LowerFunction extends EObject
{
  /**
   * Returns the value of the '<em><b>Field</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Field</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Field</em>' containment reference.
   * @see #setField(FieldPath)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getLowerFunction_Field()
   * @model containment="true"
   * @generated
   */
  FieldPath getField();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.LowerFunction#getField <em>Field</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Field</em>' containment reference.
   * @see #getField()
   * @generated
   */
  void setField(FieldPath value);

} // LowerFunction

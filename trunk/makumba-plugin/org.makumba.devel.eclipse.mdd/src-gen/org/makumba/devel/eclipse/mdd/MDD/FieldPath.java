/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Field Path</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FieldPath#getField <em>Field</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FieldPath#getSubField <em>Sub Field</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldPath()
 * @model
 * @generated
 */
public interface FieldPath extends EObject
{
  /**
   * Returns the value of the '<em><b>Field</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Field</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Field</em>' reference.
   * @see #setField(FieldDeclaration)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldPath_Field()
   * @model
   * @generated
   */
  FieldDeclaration getField();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FieldPath#getField <em>Field</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Field</em>' reference.
   * @see #getField()
   * @generated
   */
  void setField(FieldDeclaration value);

  /**
   * Returns the value of the '<em><b>Sub Field</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Sub Field</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Sub Field</em>' containment reference.
   * @see #setSubField(FieldPath)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldPath_SubField()
   * @model containment="true"
   * @generated
   */
  FieldPath getSubField();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FieldPath#getSubField <em>Sub Field</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Sub Field</em>' containment reference.
   * @see #getSubField()
   * @generated
   */
  void setSubField(FieldPath value);

} // FieldPath

/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Field Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FieldType#getType <em>Type</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FieldType#getTypeDec <em>Type Dec</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldType()
 * @model
 * @generated
 */
public interface FieldType extends EObject
{
  /**
   * Returns the value of the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Type</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Type</em>' attribute.
   * @see #setType(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldType_Type()
   * @model
   * @generated
   */
  String getType();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FieldType#getType <em>Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Type</em>' attribute.
   * @see #getType()
   * @generated
   */
  void setType(String value);

  /**
   * Returns the value of the '<em><b>Type Dec</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Type Dec</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Type Dec</em>' reference.
   * @see #setTypeDec(TypeDeclaration)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldType_TypeDec()
   * @model
   * @generated
   */
  TypeDeclaration getTypeDec();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FieldType#getTypeDec <em>Type Dec</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Type Dec</em>' reference.
   * @see #getTypeDec()
   * @generated
   */
  void setTypeDec(TypeDeclaration value);

} // FieldType

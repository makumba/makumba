/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Declaration#getFieldComment <em>Field Comment</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getDeclaration()
 * @model
 * @generated
 */
public interface Declaration extends EObject
{
  /**
   * Returns the value of the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Field Comment</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Field Comment</em>' attribute.
   * @see #setFieldComment(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getDeclaration_FieldComment()
   * @model
   * @generated
   */
  String getFieldComment();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.Declaration#getFieldComment <em>Field Comment</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Field Comment</em>' attribute.
   * @see #getFieldComment()
   * @generated
   */
  void setFieldComment(String value);

} // Declaration

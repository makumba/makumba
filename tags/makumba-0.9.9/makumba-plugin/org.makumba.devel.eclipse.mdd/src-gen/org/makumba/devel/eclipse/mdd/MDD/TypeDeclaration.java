/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Type Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration#getName <em>Name</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration#getFieldType <em>Field Type</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getTypeDeclaration()
 * @model
 * @generated
 */
public interface TypeDeclaration extends Declaration
{
  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getTypeDeclaration_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Field Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Field Type</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Field Type</em>' containment reference.
   * @see #setFieldType(FieldType)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getTypeDeclaration_FieldType()
   * @model containment="true"
   * @generated
   */
  FieldType getFieldType();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration#getFieldType <em>Field Type</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Field Type</em>' containment reference.
   * @see #getFieldType()
   * @generated
   */
  void setFieldType(FieldType value);

} // TypeDeclaration

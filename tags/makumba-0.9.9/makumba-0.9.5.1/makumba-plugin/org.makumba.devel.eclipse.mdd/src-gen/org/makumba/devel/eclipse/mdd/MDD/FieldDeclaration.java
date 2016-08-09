/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Field Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getName <em>Name</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getModifiers <em>Modifiers</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getTypedef <em>Typedef</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldDeclaration()
 * @model
 * @generated
 */
public interface FieldDeclaration extends Declaration
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
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldDeclaration_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Modifiers</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Modifiers</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Modifiers</em>' containment reference.
   * @see #setModifiers(Modifiers)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldDeclaration_Modifiers()
   * @model containment="true"
   * @generated
   */
  Modifiers getModifiers();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getModifiers <em>Modifiers</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Modifiers</em>' containment reference.
   * @see #getModifiers()
   * @generated
   */
  void setModifiers(Modifiers value);

  /**
   * Returns the value of the '<em><b>Typedef</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Typedef</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Typedef</em>' containment reference.
   * @see #setTypedef(FieldType)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFieldDeclaration_Typedef()
   * @model containment="true"
   * @generated
   */
  FieldType getTypedef();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getTypedef <em>Typedef</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Typedef</em>' containment reference.
   * @see #getTypedef()
   * @generated
   */
  void setTypedef(FieldType value);

} // FieldDeclaration

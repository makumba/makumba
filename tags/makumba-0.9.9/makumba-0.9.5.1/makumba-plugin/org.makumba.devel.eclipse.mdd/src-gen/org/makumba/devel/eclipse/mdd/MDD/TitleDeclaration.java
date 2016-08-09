/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Title Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration#getField <em>Field</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration#getFunction <em>Function</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getTitleDeclaration()
 * @model
 * @generated
 */
public interface TitleDeclaration extends Declaration
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
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getTitleDeclaration_Field()
   * @model containment="true"
   * @generated
   */
  FieldPath getField();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration#getField <em>Field</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Field</em>' containment reference.
   * @see #getField()
   * @generated
   */
  void setField(FieldPath value);

  /**
   * Returns the value of the '<em><b>Function</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Function</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Function</em>' containment reference.
   * @see #setFunction(FunctionCall)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getTitleDeclaration_Function()
   * @model containment="true"
   * @generated
   */
  FunctionCall getFunction();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration#getFunction <em>Function</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Function</em>' containment reference.
   * @see #getFunction()
   * @generated
   */
  void setFunction(FunctionCall value);

} // TitleDeclaration

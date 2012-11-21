/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Validation Rule Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration#getErrorMessage <em>Error Message</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getValidationRuleDeclaration()
 * @model
 * @generated
 */
public interface ValidationRuleDeclaration extends Declaration
{
  /**
   * Returns the value of the '<em><b>Error Message</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Error Message</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Error Message</em>' containment reference.
   * @see #setErrorMessage(ErrorMessage)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getValidationRuleDeclaration_ErrorMessage()
   * @model containment="true"
   * @generated
   */
  ErrorMessage getErrorMessage();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration#getErrorMessage <em>Error Message</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Error Message</em>' containment reference.
   * @see #getErrorMessage()
   * @generated
   */
  void setErrorMessage(ErrorMessage value);

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
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getValidationRuleDeclaration_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

} // ValidationRuleDeclaration

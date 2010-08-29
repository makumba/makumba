/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Uniqueness Validation Rule Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.UniquenessValidationRuleDeclaration#getArgs <em>Args</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getUniquenessValidationRuleDeclaration()
 * @model
 * @generated
 */
public interface UniquenessValidationRuleDeclaration extends ValidationRuleDeclaration
{
  /**
   * Returns the value of the '<em><b>Args</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Args</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Args</em>' containment reference.
   * @see #setArgs(FunctionArguments)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getUniquenessValidationRuleDeclaration_Args()
   * @model containment="true"
   * @generated
   */
  FunctionArguments getArgs();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.UniquenessValidationRuleDeclaration#getArgs <em>Args</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Args</em>' containment reference.
   * @see #getArgs()
   * @generated
   */
  void setArgs(FunctionArguments value);

} // UniquenessValidationRuleDeclaration

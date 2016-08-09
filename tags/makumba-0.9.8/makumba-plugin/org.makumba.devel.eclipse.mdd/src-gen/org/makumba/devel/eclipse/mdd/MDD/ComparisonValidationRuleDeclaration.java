/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Comparison Validation Rule Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration#getArgs <em>Args</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration#getComparisonExp <em>Comparison Exp</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonValidationRuleDeclaration()
 * @model
 * @generated
 */
public interface ComparisonValidationRuleDeclaration extends ValidationRuleDeclaration
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
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonValidationRuleDeclaration_Args()
   * @model containment="true"
   * @generated
   */
  FunctionArguments getArgs();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration#getArgs <em>Args</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Args</em>' containment reference.
   * @see #getArgs()
   * @generated
   */
  void setArgs(FunctionArguments value);

  /**
   * Returns the value of the '<em><b>Comparison Exp</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Comparison Exp</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Comparison Exp</em>' containment reference.
   * @see #setComparisonExp(ComparisonExpression)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonValidationRuleDeclaration_ComparisonExp()
   * @model containment="true"
   * @generated
   */
  ComparisonExpression getComparisonExp();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration#getComparisonExp <em>Comparison Exp</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Comparison Exp</em>' containment reference.
   * @see #getComparisonExp()
   * @generated
   */
  void setComparisonExp(ComparisonExpression value);

} // ComparisonValidationRuleDeclaration

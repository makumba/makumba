/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Range Validation Rule Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration#getArg <em>Arg</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration#getRange <em>Range</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getRangeValidationRuleDeclaration()
 * @model
 * @generated
 */
public interface RangeValidationRuleDeclaration extends ValidationRuleDeclaration
{
  /**
   * Returns the value of the '<em><b>Arg</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Arg</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Arg</em>' containment reference.
   * @see #setArg(FieldReference)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getRangeValidationRuleDeclaration_Arg()
   * @model containment="true"
   * @generated
   */
  FieldReference getArg();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration#getArg <em>Arg</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Arg</em>' containment reference.
   * @see #getArg()
   * @generated
   */
  void setArg(FieldReference value);

  /**
   * Returns the value of the '<em><b>Range</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Range</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Range</em>' containment reference.
   * @see #setRange(Range)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getRangeValidationRuleDeclaration_Range()
   * @model containment="true"
   * @generated
   */
  Range getRange();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration#getRange <em>Range</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Range</em>' containment reference.
   * @see #getRange()
   * @generated
   */
  void setRange(Range value);

} // RangeValidationRuleDeclaration

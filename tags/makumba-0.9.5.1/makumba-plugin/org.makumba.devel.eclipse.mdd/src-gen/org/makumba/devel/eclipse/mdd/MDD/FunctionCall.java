/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Function Call</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FunctionCall#getFunction <em>Function</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FunctionCall#getF <em>F</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFunctionCall()
 * @model
 * @generated
 */
public interface FunctionCall extends EObject
{
  /**
   * Returns the value of the '<em><b>Function</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Function</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Function</em>' reference.
   * @see #setFunction(FunctionDeclaration)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFunctionCall_Function()
   * @model
   * @generated
   */
  FunctionDeclaration getFunction();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionCall#getFunction <em>Function</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Function</em>' reference.
   * @see #getFunction()
   * @generated
   */
  void setFunction(FunctionDeclaration value);

  /**
   * Returns the value of the '<em><b>F</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>F</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>F</em>' containment reference.
   * @see #setF(FunctionArguments)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFunctionCall_F()
   * @model containment="true"
   * @generated
   */
  FunctionArguments getF();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionCall#getF <em>F</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>F</em>' containment reference.
   * @see #getF()
   * @generated
   */
  void setF(FunctionArguments value);

} // FunctionCall

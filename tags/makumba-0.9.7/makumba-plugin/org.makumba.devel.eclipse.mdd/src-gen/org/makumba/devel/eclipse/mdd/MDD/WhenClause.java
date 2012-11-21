/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>When Clause</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.WhenClause#getWhenExpr <em>When Expr</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.WhenClause#getThenExpr <em>Then Expr</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getWhenClause()
 * @model
 * @generated
 */
public interface WhenClause extends EObject
{
  /**
   * Returns the value of the '<em><b>When Expr</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>When Expr</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>When Expr</em>' containment reference.
   * @see #setWhenExpr(Expression)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getWhenClause_WhenExpr()
   * @model containment="true"
   * @generated
   */
  Expression getWhenExpr();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.WhenClause#getWhenExpr <em>When Expr</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>When Expr</em>' containment reference.
   * @see #getWhenExpr()
   * @generated
   */
  void setWhenExpr(Expression value);

  /**
   * Returns the value of the '<em><b>Then Expr</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Then Expr</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Then Expr</em>' containment reference.
   * @see #setThenExpr(UnaryExpression)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getWhenClause_ThenExpr()
   * @model containment="true"
   * @generated
   */
  UnaryExpression getThenExpr();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.WhenClause#getThenExpr <em>Then Expr</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Then Expr</em>' containment reference.
   * @see #getThenExpr()
   * @generated
   */
  void setThenExpr(UnaryExpression value);

} // WhenClause

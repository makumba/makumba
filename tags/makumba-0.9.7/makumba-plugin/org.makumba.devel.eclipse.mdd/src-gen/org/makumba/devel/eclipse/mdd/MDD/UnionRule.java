/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Union Rule</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.UnionRule#getQ <em>Q</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getUnionRule()
 * @model
 * @generated
 */
public interface UnionRule extends PrimaryExpression
{
  /**
   * Returns the value of the '<em><b>Q</b></em>' containment reference list.
   * The list contents are of type {@link org.makumba.devel.eclipse.mdd.MDD.QueryRule}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Q</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Q</em>' containment reference list.
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getUnionRule_Q()
   * @model containment="true"
   * @generated
   */
  EList<QueryRule> getQ();

} // UnionRule

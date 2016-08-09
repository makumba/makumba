/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Group By Clause</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.GroupByClause#getE <em>E</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.GroupByClause#getH <em>H</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getGroupByClause()
 * @model
 * @generated
 */
public interface GroupByClause extends EObject
{
  /**
   * Returns the value of the '<em><b>E</b></em>' containment reference list.
   * The list contents are of type {@link org.makumba.devel.eclipse.mdd.MDD.Expression}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>E</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>E</em>' containment reference list.
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getGroupByClause_E()
   * @model containment="true"
   * @generated
   */
  EList<Expression> getE();

  /**
   * Returns the value of the '<em><b>H</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>H</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>H</em>' containment reference.
   * @see #setH(HavingClause)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getGroupByClause_H()
   * @model containment="true"
   * @generated
   */
  HavingClause getH();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.GroupByClause#getH <em>H</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>H</em>' containment reference.
   * @see #getH()
   * @generated
   */
  void setH(HavingClause value);

} // GroupByClause

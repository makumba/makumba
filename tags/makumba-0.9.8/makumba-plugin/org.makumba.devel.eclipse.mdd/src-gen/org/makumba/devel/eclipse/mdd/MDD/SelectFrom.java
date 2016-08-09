/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Select From</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getWhere <em>Where</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getGroupBy <em>Group By</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getOrderBy <em>Order By</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getS <em>S</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getFrom <em>From</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSelectFrom()
 * @model
 * @generated
 */
public interface SelectFrom extends QueryRule
{
  /**
   * Returns the value of the '<em><b>Where</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Where</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Where</em>' containment reference.
   * @see #setWhere(WhereClause)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSelectFrom_Where()
   * @model containment="true"
   * @generated
   */
  WhereClause getWhere();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getWhere <em>Where</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Where</em>' containment reference.
   * @see #getWhere()
   * @generated
   */
  void setWhere(WhereClause value);

  /**
   * Returns the value of the '<em><b>Group By</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Group By</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Group By</em>' containment reference.
   * @see #setGroupBy(GroupByClause)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSelectFrom_GroupBy()
   * @model containment="true"
   * @generated
   */
  GroupByClause getGroupBy();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getGroupBy <em>Group By</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Group By</em>' containment reference.
   * @see #getGroupBy()
   * @generated
   */
  void setGroupBy(GroupByClause value);

  /**
   * Returns the value of the '<em><b>Order By</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Order By</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Order By</em>' containment reference.
   * @see #setOrderBy(OrderByClause)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSelectFrom_OrderBy()
   * @model containment="true"
   * @generated
   */
  OrderByClause getOrderBy();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getOrderBy <em>Order By</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Order By</em>' containment reference.
   * @see #getOrderBy()
   * @generated
   */
  void setOrderBy(OrderByClause value);

  /**
   * Returns the value of the '<em><b>S</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>S</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>S</em>' containment reference.
   * @see #setS(SelectClause)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSelectFrom_S()
   * @model containment="true"
   * @generated
   */
  SelectClause getS();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getS <em>S</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>S</em>' containment reference.
   * @see #getS()
   * @generated
   */
  void setS(SelectClause value);

  /**
   * Returns the value of the '<em><b>From</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>From</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>From</em>' containment reference.
   * @see #setFrom(FromClause)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSelectFrom_From()
   * @model containment="true"
   * @generated
   */
  FromClause getFrom();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getFrom <em>From</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>From</em>' containment reference.
   * @see #getFrom()
   * @generated
   */
  void setFrom(FromClause value);

} // SelectFrom

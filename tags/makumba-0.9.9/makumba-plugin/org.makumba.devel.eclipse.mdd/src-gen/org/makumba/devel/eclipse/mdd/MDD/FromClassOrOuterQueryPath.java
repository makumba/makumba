/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>From Class Or Outer Query Path</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getW <em>W</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getPath <em>Path</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getName <em>Name</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getPropertyFetch <em>Property Fetch</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFromClassOrOuterQueryPath()
 * @model
 * @generated
 */
public interface FromClassOrOuterQueryPath extends FromJoin, FromRange
{
  /**
   * Returns the value of the '<em><b>W</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>W</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>W</em>' containment reference.
   * @see #setW(WithClause)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFromClassOrOuterQueryPath_W()
   * @model containment="true"
   * @generated
   */
  WithClause getW();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getW <em>W</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>W</em>' containment reference.
   * @see #getW()
   * @generated
   */
  void setW(WithClause value);

  /**
   * Returns the value of the '<em><b>Path</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Path</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Path</em>' attribute.
   * @see #setPath(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFromClassOrOuterQueryPath_Path()
   * @model
   * @generated
   */
  String getPath();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getPath <em>Path</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Path</em>' attribute.
   * @see #getPath()
   * @generated
   */
  void setPath(String value);

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
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFromClassOrOuterQueryPath_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Property Fetch</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Property Fetch</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Property Fetch</em>' attribute.
   * @see #setPropertyFetch(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getFromClassOrOuterQueryPath_PropertyFetch()
   * @model
   * @generated
   */
  String getPropertyFetch();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getPropertyFetch <em>Property Fetch</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Property Fetch</em>' attribute.
   * @see #getPropertyFetch()
   * @generated
   */
  void setPropertyFetch(String value);

} // FromClassOrOuterQueryPath

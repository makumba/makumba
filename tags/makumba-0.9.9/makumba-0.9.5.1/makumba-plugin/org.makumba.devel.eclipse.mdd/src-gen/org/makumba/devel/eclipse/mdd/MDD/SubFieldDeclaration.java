/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sub Field Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration#getSubFieldOf <em>Sub Field Of</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration#getD <em>D</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSubFieldDeclaration()
 * @model
 * @generated
 */
public interface SubFieldDeclaration extends Declaration
{
  /**
   * Returns the value of the '<em><b>Sub Field Of</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Sub Field Of</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Sub Field Of</em>' reference.
   * @see #setSubFieldOf(FieldDeclaration)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSubFieldDeclaration_SubFieldOf()
   * @model
   * @generated
   */
  FieldDeclaration getSubFieldOf();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration#getSubFieldOf <em>Sub Field Of</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Sub Field Of</em>' reference.
   * @see #getSubFieldOf()
   * @generated
   */
  void setSubFieldOf(FieldDeclaration value);

  /**
   * Returns the value of the '<em><b>D</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>D</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>D</em>' containment reference.
   * @see #setD(Declaration)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getSubFieldDeclaration_D()
   * @model containment="true"
   * @generated
   */
  Declaration getD();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration#getD <em>D</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>D</em>' containment reference.
   * @see #getD()
   * @generated
   */
  void setD(Declaration value);

} // SubFieldDeclaration

/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Pointer Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.PointerType#getRef <em>Ref</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getPointerType()
 * @model
 * @generated
 */
public interface PointerType extends FieldType
{
  /**
   * Returns the value of the '<em><b>Ref</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Ref</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Ref</em>' reference.
   * @see #setRef(DataDefinition)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getPointerType_Ref()
   * @model
   * @generated
   */
  DataDefinition getRef();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.PointerType#getRef <em>Ref</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Ref</em>' reference.
   * @see #getRef()
   * @generated
   */
  void setRef(DataDefinition value);

} // PointerType

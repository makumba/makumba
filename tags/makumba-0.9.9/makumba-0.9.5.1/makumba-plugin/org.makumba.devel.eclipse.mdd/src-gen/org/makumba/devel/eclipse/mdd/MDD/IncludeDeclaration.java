/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Include Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.IncludeDeclaration#getImportedNamespace <em>Imported Namespace</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getIncludeDeclaration()
 * @model
 * @generated
 */
public interface IncludeDeclaration extends Declaration
{
  /**
   * Returns the value of the '<em><b>Imported Namespace</b></em>' reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Imported Namespace</em>' reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Imported Namespace</em>' reference.
   * @see #setImportedNamespace(DataDefinition)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getIncludeDeclaration_ImportedNamespace()
   * @model
   * @generated
   */
  DataDefinition getImportedNamespace();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.IncludeDeclaration#getImportedNamespace <em>Imported Namespace</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Imported Namespace</em>' reference.
   * @see #getImportedNamespace()
   * @generated
   */
  void setImportedNamespace(DataDefinition value);

} // IncludeDeclaration

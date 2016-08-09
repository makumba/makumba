/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Atom</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Atom#getPrime <em>Prime</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Atom#getE <em>E</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Atom#getExp <em>Exp</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getAtom()
 * @model
 * @generated
 */
public interface Atom extends UnaryExpression
{
  /**
   * Returns the value of the '<em><b>Prime</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Prime</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Prime</em>' containment reference.
   * @see #setPrime(PrimaryExpression)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getAtom_Prime()
   * @model containment="true"
   * @generated
   */
  PrimaryExpression getPrime();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.Atom#getPrime <em>Prime</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Prime</em>' containment reference.
   * @see #getPrime()
   * @generated
   */
  void setPrime(PrimaryExpression value);

  /**
   * Returns the value of the '<em><b>E</b></em>' containment reference list.
   * The list contents are of type {@link org.makumba.devel.eclipse.mdd.MDD.ExprList}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>E</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>E</em>' containment reference list.
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getAtom_E()
   * @model containment="true"
   * @generated
   */
  EList<ExprList> getE();

  /**
   * Returns the value of the '<em><b>Exp</b></em>' containment reference list.
   * The list contents are of type {@link org.makumba.devel.eclipse.mdd.MDD.Expression}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Exp</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Exp</em>' containment reference list.
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getAtom_Exp()
   * @model containment="true"
   * @generated
   */
  EList<Expression> getExp();

} // Atom

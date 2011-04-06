/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Concatenation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getA <em>A</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getI <em>I</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getB <em>B</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getC <em>C</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getL <em>L</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getP <em>P</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getConcatenation()
 * @model
 * @generated
 */
public interface Concatenation extends RelationalExpression
{
  /**
   * Returns the value of the '<em><b>A</b></em>' containment reference list.
   * The list contents are of type {@link org.makumba.devel.eclipse.mdd.MDD.AdditiveExpression}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>A</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>A</em>' containment reference list.
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getConcatenation_A()
   * @model containment="true"
   * @generated
   */
  EList<AdditiveExpression> getA();

  /**
   * Returns the value of the '<em><b>I</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>I</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>I</em>' containment reference.
   * @see #setI(CompoundExpr)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getConcatenation_I()
   * @model containment="true"
   * @generated
   */
  CompoundExpr getI();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getI <em>I</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>I</em>' containment reference.
   * @see #getI()
   * @generated
   */
  void setI(CompoundExpr value);

  /**
   * Returns the value of the '<em><b>B</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>B</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>B</em>' containment reference.
   * @see #setB(BetweenList)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getConcatenation_B()
   * @model containment="true"
   * @generated
   */
  BetweenList getB();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getB <em>B</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>B</em>' containment reference.
   * @see #getB()
   * @generated
   */
  void setB(BetweenList value);

  /**
   * Returns the value of the '<em><b>C</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>C</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>C</em>' containment reference.
   * @see #setC(Concatenation)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getConcatenation_C()
   * @model containment="true"
   * @generated
   */
  Concatenation getC();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getC <em>C</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>C</em>' containment reference.
   * @see #getC()
   * @generated
   */
  void setC(Concatenation value);

  /**
   * Returns the value of the '<em><b>L</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>L</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>L</em>' containment reference.
   * @see #setL(LikeEscape)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getConcatenation_L()
   * @model containment="true"
   * @generated
   */
  LikeEscape getL();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getL <em>L</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>L</em>' containment reference.
   * @see #getL()
   * @generated
   */
  void setL(LikeEscape value);

  /**
   * Returns the value of the '<em><b>P</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>P</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>P</em>' attribute.
   * @see #setP(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getConcatenation_P()
   * @model
   * @generated
   */
  String getP();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getP <em>P</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>P</em>' attribute.
   * @see #getP()
   * @generated
   */
  void setP(String value);

} // Concatenation

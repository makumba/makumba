/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Comparison Part</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getField <em>Field</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getN <em>N</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getDf <em>Df</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getU <em>U</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getL <em>L</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getD <em>D</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonPart()
 * @model
 * @generated
 */
public interface ComparisonPart extends EObject
{
  /**
   * Returns the value of the '<em><b>Field</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Field</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Field</em>' containment reference.
   * @see #setField(FieldPath)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonPart_Field()
   * @model containment="true"
   * @generated
   */
  FieldPath getField();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getField <em>Field</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Field</em>' containment reference.
   * @see #getField()
   * @generated
   */
  void setField(FieldPath value);

  /**
   * Returns the value of the '<em><b>N</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>N</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>N</em>' attribute.
   * @see #setN(int)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonPart_N()
   * @model
   * @generated
   */
  int getN();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getN <em>N</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>N</em>' attribute.
   * @see #getN()
   * @generated
   */
  void setN(int value);

  /**
   * Returns the value of the '<em><b>Df</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Df</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Df</em>' attribute.
   * @see #setDf(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonPart_Df()
   * @model
   * @generated
   */
  String getDf();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getDf <em>Df</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Df</em>' attribute.
   * @see #getDf()
   * @generated
   */
  void setDf(String value);

  /**
   * Returns the value of the '<em><b>U</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>U</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>U</em>' containment reference.
   * @see #setU(UpperFunction)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonPart_U()
   * @model containment="true"
   * @generated
   */
  UpperFunction getU();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getU <em>U</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>U</em>' containment reference.
   * @see #getU()
   * @generated
   */
  void setU(UpperFunction value);

  /**
   * Returns the value of the '<em><b>L</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>L</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>L</em>' containment reference.
   * @see #setL(LowerFunction)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonPart_L()
   * @model containment="true"
   * @generated
   */
  LowerFunction getL();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getL <em>L</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>L</em>' containment reference.
   * @see #getL()
   * @generated
   */
  void setL(LowerFunction value);

  /**
   * Returns the value of the '<em><b>D</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>D</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>D</em>' attribute.
   * @see #setD(String)
   * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage#getComparisonPart_D()
   * @model
   * @generated
   */
  String getD();

  /**
   * Sets the value of the '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getD <em>D</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>D</em>' attribute.
   * @see #getD()
   * @generated
   */
  void setD(String value);

} // ComparisonPart

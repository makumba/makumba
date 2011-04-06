/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression;
import org.makumba.devel.eclipse.mdd.MDD.ComparisonPart;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Comparison Expression</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonExpressionImpl#getLhs <em>Lhs</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonExpressionImpl#getO <em>O</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonExpressionImpl#getRhs <em>Rhs</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ComparisonExpressionImpl extends MinimalEObjectImpl.Container implements ComparisonExpression
{
  /**
   * The cached value of the '{@link #getLhs() <em>Lhs</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLhs()
   * @generated
   * @ordered
   */
  protected ComparisonPart lhs;

  /**
   * The default value of the '{@link #getO() <em>O</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getO()
   * @generated
   * @ordered
   */
  protected static final String O_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getO() <em>O</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getO()
   * @generated
   * @ordered
   */
  protected String o = O_EDEFAULT;

  /**
   * The cached value of the '{@link #getRhs() <em>Rhs</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getRhs()
   * @generated
   * @ordered
   */
  protected ComparisonPart rhs;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ComparisonExpressionImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return MDDPackage.Literals.COMPARISON_EXPRESSION;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ComparisonPart getLhs()
  {
    return lhs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetLhs(ComparisonPart newLhs, NotificationChain msgs)
  {
    ComparisonPart oldLhs = lhs;
    lhs = newLhs;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.COMPARISON_EXPRESSION__LHS, oldLhs, newLhs);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setLhs(ComparisonPart newLhs)
  {
    if (newLhs != lhs)
    {
      NotificationChain msgs = null;
      if (lhs != null)
        msgs = ((InternalEObject)lhs).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.COMPARISON_EXPRESSION__LHS, null, msgs);
      if (newLhs != null)
        msgs = ((InternalEObject)newLhs).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.COMPARISON_EXPRESSION__LHS, null, msgs);
      msgs = basicSetLhs(newLhs, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.COMPARISON_EXPRESSION__LHS, newLhs, newLhs));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getO()
  {
    return o;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setO(String newO)
  {
    String oldO = o;
    o = newO;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.COMPARISON_EXPRESSION__O, oldO, o));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ComparisonPart getRhs()
  {
    return rhs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetRhs(ComparisonPart newRhs, NotificationChain msgs)
  {
    ComparisonPart oldRhs = rhs;
    rhs = newRhs;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.COMPARISON_EXPRESSION__RHS, oldRhs, newRhs);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setRhs(ComparisonPart newRhs)
  {
    if (newRhs != rhs)
    {
      NotificationChain msgs = null;
      if (rhs != null)
        msgs = ((InternalEObject)rhs).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.COMPARISON_EXPRESSION__RHS, null, msgs);
      if (newRhs != null)
        msgs = ((InternalEObject)newRhs).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.COMPARISON_EXPRESSION__RHS, null, msgs);
      msgs = basicSetRhs(newRhs, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.COMPARISON_EXPRESSION__RHS, newRhs, newRhs));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case MDDPackage.COMPARISON_EXPRESSION__LHS:
        return basicSetLhs(null, msgs);
      case MDDPackage.COMPARISON_EXPRESSION__RHS:
        return basicSetRhs(null, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case MDDPackage.COMPARISON_EXPRESSION__LHS:
        return getLhs();
      case MDDPackage.COMPARISON_EXPRESSION__O:
        return getO();
      case MDDPackage.COMPARISON_EXPRESSION__RHS:
        return getRhs();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case MDDPackage.COMPARISON_EXPRESSION__LHS:
        setLhs((ComparisonPart)newValue);
        return;
      case MDDPackage.COMPARISON_EXPRESSION__O:
        setO((String)newValue);
        return;
      case MDDPackage.COMPARISON_EXPRESSION__RHS:
        setRhs((ComparisonPart)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case MDDPackage.COMPARISON_EXPRESSION__LHS:
        setLhs((ComparisonPart)null);
        return;
      case MDDPackage.COMPARISON_EXPRESSION__O:
        setO(O_EDEFAULT);
        return;
      case MDDPackage.COMPARISON_EXPRESSION__RHS:
        setRhs((ComparisonPart)null);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case MDDPackage.COMPARISON_EXPRESSION__LHS:
        return lhs != null;
      case MDDPackage.COMPARISON_EXPRESSION__O:
        return O_EDEFAULT == null ? o != null : !O_EDEFAULT.equals(o);
      case MDDPackage.COMPARISON_EXPRESSION__RHS:
        return rhs != null;
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy()) return super.toString();

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (o: ");
    result.append(o);
    result.append(')');
    return result.toString();
  }

} //ComparisonExpressionImpl

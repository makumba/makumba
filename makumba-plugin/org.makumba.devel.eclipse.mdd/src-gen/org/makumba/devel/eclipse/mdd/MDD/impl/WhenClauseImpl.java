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

import org.makumba.devel.eclipse.mdd.MDD.Expression;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.UnaryExpression;
import org.makumba.devel.eclipse.mdd.MDD.WhenClause;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>When Clause</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.WhenClauseImpl#getWhenExpr <em>When Expr</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.WhenClauseImpl#getThenExpr <em>Then Expr</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class WhenClauseImpl extends MinimalEObjectImpl.Container implements WhenClause
{
  /**
   * The cached value of the '{@link #getWhenExpr() <em>When Expr</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getWhenExpr()
   * @generated
   * @ordered
   */
  protected Expression whenExpr;

  /**
   * The cached value of the '{@link #getThenExpr() <em>Then Expr</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getThenExpr()
   * @generated
   * @ordered
   */
  protected UnaryExpression thenExpr;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected WhenClauseImpl()
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
    return MDDPackage.Literals.WHEN_CLAUSE;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Expression getWhenExpr()
  {
    return whenExpr;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetWhenExpr(Expression newWhenExpr, NotificationChain msgs)
  {
    Expression oldWhenExpr = whenExpr;
    whenExpr = newWhenExpr;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.WHEN_CLAUSE__WHEN_EXPR, oldWhenExpr, newWhenExpr);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setWhenExpr(Expression newWhenExpr)
  {
    if (newWhenExpr != whenExpr)
    {
      NotificationChain msgs = null;
      if (whenExpr != null)
        msgs = ((InternalEObject)whenExpr).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.WHEN_CLAUSE__WHEN_EXPR, null, msgs);
      if (newWhenExpr != null)
        msgs = ((InternalEObject)newWhenExpr).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.WHEN_CLAUSE__WHEN_EXPR, null, msgs);
      msgs = basicSetWhenExpr(newWhenExpr, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.WHEN_CLAUSE__WHEN_EXPR, newWhenExpr, newWhenExpr));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public UnaryExpression getThenExpr()
  {
    return thenExpr;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetThenExpr(UnaryExpression newThenExpr, NotificationChain msgs)
  {
    UnaryExpression oldThenExpr = thenExpr;
    thenExpr = newThenExpr;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.WHEN_CLAUSE__THEN_EXPR, oldThenExpr, newThenExpr);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setThenExpr(UnaryExpression newThenExpr)
  {
    if (newThenExpr != thenExpr)
    {
      NotificationChain msgs = null;
      if (thenExpr != null)
        msgs = ((InternalEObject)thenExpr).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.WHEN_CLAUSE__THEN_EXPR, null, msgs);
      if (newThenExpr != null)
        msgs = ((InternalEObject)newThenExpr).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.WHEN_CLAUSE__THEN_EXPR, null, msgs);
      msgs = basicSetThenExpr(newThenExpr, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.WHEN_CLAUSE__THEN_EXPR, newThenExpr, newThenExpr));
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
      case MDDPackage.WHEN_CLAUSE__WHEN_EXPR:
        return basicSetWhenExpr(null, msgs);
      case MDDPackage.WHEN_CLAUSE__THEN_EXPR:
        return basicSetThenExpr(null, msgs);
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
      case MDDPackage.WHEN_CLAUSE__WHEN_EXPR:
        return getWhenExpr();
      case MDDPackage.WHEN_CLAUSE__THEN_EXPR:
        return getThenExpr();
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
      case MDDPackage.WHEN_CLAUSE__WHEN_EXPR:
        setWhenExpr((Expression)newValue);
        return;
      case MDDPackage.WHEN_CLAUSE__THEN_EXPR:
        setThenExpr((UnaryExpression)newValue);
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
      case MDDPackage.WHEN_CLAUSE__WHEN_EXPR:
        setWhenExpr((Expression)null);
        return;
      case MDDPackage.WHEN_CLAUSE__THEN_EXPR:
        setThenExpr((UnaryExpression)null);
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
      case MDDPackage.WHEN_CLAUSE__WHEN_EXPR:
        return whenExpr != null;
      case MDDPackage.WHEN_CLAUSE__THEN_EXPR:
        return thenExpr != null;
    }
    return super.eIsSet(featureID);
  }

} //WhenClauseImpl

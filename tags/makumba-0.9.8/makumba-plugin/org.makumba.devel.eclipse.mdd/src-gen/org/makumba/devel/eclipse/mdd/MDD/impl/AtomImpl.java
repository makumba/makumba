/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.makumba.devel.eclipse.mdd.MDD.Atom;
import org.makumba.devel.eclipse.mdd.MDD.ExprList;
import org.makumba.devel.eclipse.mdd.MDD.Expression;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.PrimaryExpression;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Atom</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.AtomImpl#getPrime <em>Prime</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.AtomImpl#getE <em>E</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.AtomImpl#getExp <em>Exp</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class AtomImpl extends UnaryExpressionImpl implements Atom
{
  /**
   * The cached value of the '{@link #getPrime() <em>Prime</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPrime()
   * @generated
   * @ordered
   */
  protected PrimaryExpression prime;

  /**
   * The cached value of the '{@link #getE() <em>E</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getE()
   * @generated
   * @ordered
   */
  protected EList<ExprList> e;

  /**
   * The cached value of the '{@link #getExp() <em>Exp</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getExp()
   * @generated
   * @ordered
   */
  protected EList<Expression> exp;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected AtomImpl()
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
    return MDDPackage.Literals.ATOM;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PrimaryExpression getPrime()
  {
    return prime;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetPrime(PrimaryExpression newPrime, NotificationChain msgs)
  {
    PrimaryExpression oldPrime = prime;
    prime = newPrime;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.ATOM__PRIME, oldPrime, newPrime);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setPrime(PrimaryExpression newPrime)
  {
    if (newPrime != prime)
    {
      NotificationChain msgs = null;
      if (prime != null)
        msgs = ((InternalEObject)prime).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.ATOM__PRIME, null, msgs);
      if (newPrime != null)
        msgs = ((InternalEObject)newPrime).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.ATOM__PRIME, null, msgs);
      msgs = basicSetPrime(newPrime, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.ATOM__PRIME, newPrime, newPrime));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<ExprList> getE()
  {
    if (e == null)
    {
      e = new EObjectContainmentEList<ExprList>(ExprList.class, this, MDDPackage.ATOM__E);
    }
    return e;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Expression> getExp()
  {
    if (exp == null)
    {
      exp = new EObjectContainmentEList<Expression>(Expression.class, this, MDDPackage.ATOM__EXP);
    }
    return exp;
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
      case MDDPackage.ATOM__PRIME:
        return basicSetPrime(null, msgs);
      case MDDPackage.ATOM__E:
        return ((InternalEList<?>)getE()).basicRemove(otherEnd, msgs);
      case MDDPackage.ATOM__EXP:
        return ((InternalEList<?>)getExp()).basicRemove(otherEnd, msgs);
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
      case MDDPackage.ATOM__PRIME:
        return getPrime();
      case MDDPackage.ATOM__E:
        return getE();
      case MDDPackage.ATOM__EXP:
        return getExp();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case MDDPackage.ATOM__PRIME:
        setPrime((PrimaryExpression)newValue);
        return;
      case MDDPackage.ATOM__E:
        getE().clear();
        getE().addAll((Collection<? extends ExprList>)newValue);
        return;
      case MDDPackage.ATOM__EXP:
        getExp().clear();
        getExp().addAll((Collection<? extends Expression>)newValue);
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
      case MDDPackage.ATOM__PRIME:
        setPrime((PrimaryExpression)null);
        return;
      case MDDPackage.ATOM__E:
        getE().clear();
        return;
      case MDDPackage.ATOM__EXP:
        getExp().clear();
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
      case MDDPackage.ATOM__PRIME:
        return prime != null;
      case MDDPackage.ATOM__E:
        return e != null && !e.isEmpty();
      case MDDPackage.ATOM__EXP:
        return exp != null && !exp.isEmpty();
    }
    return super.eIsSet(featureID);
  }

} //AtomImpl

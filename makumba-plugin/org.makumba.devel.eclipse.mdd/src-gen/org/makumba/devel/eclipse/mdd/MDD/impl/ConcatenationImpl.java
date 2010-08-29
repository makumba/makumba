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

import org.makumba.devel.eclipse.mdd.MDD.AdditiveExpression;
import org.makumba.devel.eclipse.mdd.MDD.BetweenList;
import org.makumba.devel.eclipse.mdd.MDD.CompoundExpr;
import org.makumba.devel.eclipse.mdd.MDD.Concatenation;
import org.makumba.devel.eclipse.mdd.MDD.LikeEscape;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Concatenation</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl#getA <em>A</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl#getI <em>I</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl#getB <em>B</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl#getC <em>C</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl#getL <em>L</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl#getP <em>P</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ConcatenationImpl extends RelationalExpressionImpl implements Concatenation
{
  /**
   * The cached value of the '{@link #getA() <em>A</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getA()
   * @generated
   * @ordered
   */
  protected EList<AdditiveExpression> a;

  /**
   * The cached value of the '{@link #getI() <em>I</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getI()
   * @generated
   * @ordered
   */
  protected CompoundExpr i;

  /**
   * The cached value of the '{@link #getB() <em>B</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getB()
   * @generated
   * @ordered
   */
  protected BetweenList b;

  /**
   * The cached value of the '{@link #getC() <em>C</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getC()
   * @generated
   * @ordered
   */
  protected Concatenation c;

  /**
   * The cached value of the '{@link #getL() <em>L</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getL()
   * @generated
   * @ordered
   */
  protected LikeEscape l;

  /**
   * The default value of the '{@link #getP() <em>P</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getP()
   * @generated
   * @ordered
   */
  protected static final String P_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getP() <em>P</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getP()
   * @generated
   * @ordered
   */
  protected String p = P_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ConcatenationImpl()
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
    return MDDPackage.Literals.CONCATENATION;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<AdditiveExpression> getA()
  {
    if (a == null)
    {
      a = new EObjectContainmentEList<AdditiveExpression>(AdditiveExpression.class, this, MDDPackage.CONCATENATION__A);
    }
    return a;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public CompoundExpr getI()
  {
    return i;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetI(CompoundExpr newI, NotificationChain msgs)
  {
    CompoundExpr oldI = i;
    i = newI;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.CONCATENATION__I, oldI, newI);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setI(CompoundExpr newI)
  {
    if (newI != i)
    {
      NotificationChain msgs = null;
      if (i != null)
        msgs = ((InternalEObject)i).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.CONCATENATION__I, null, msgs);
      if (newI != null)
        msgs = ((InternalEObject)newI).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.CONCATENATION__I, null, msgs);
      msgs = basicSetI(newI, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.CONCATENATION__I, newI, newI));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BetweenList getB()
  {
    return b;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetB(BetweenList newB, NotificationChain msgs)
  {
    BetweenList oldB = b;
    b = newB;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.CONCATENATION__B, oldB, newB);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setB(BetweenList newB)
  {
    if (newB != b)
    {
      NotificationChain msgs = null;
      if (b != null)
        msgs = ((InternalEObject)b).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.CONCATENATION__B, null, msgs);
      if (newB != null)
        msgs = ((InternalEObject)newB).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.CONCATENATION__B, null, msgs);
      msgs = basicSetB(newB, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.CONCATENATION__B, newB, newB));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Concatenation getC()
  {
    return c;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetC(Concatenation newC, NotificationChain msgs)
  {
    Concatenation oldC = c;
    c = newC;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.CONCATENATION__C, oldC, newC);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setC(Concatenation newC)
  {
    if (newC != c)
    {
      NotificationChain msgs = null;
      if (c != null)
        msgs = ((InternalEObject)c).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.CONCATENATION__C, null, msgs);
      if (newC != null)
        msgs = ((InternalEObject)newC).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.CONCATENATION__C, null, msgs);
      msgs = basicSetC(newC, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.CONCATENATION__C, newC, newC));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public LikeEscape getL()
  {
    return l;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetL(LikeEscape newL, NotificationChain msgs)
  {
    LikeEscape oldL = l;
    l = newL;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.CONCATENATION__L, oldL, newL);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setL(LikeEscape newL)
  {
    if (newL != l)
    {
      NotificationChain msgs = null;
      if (l != null)
        msgs = ((InternalEObject)l).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.CONCATENATION__L, null, msgs);
      if (newL != null)
        msgs = ((InternalEObject)newL).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.CONCATENATION__L, null, msgs);
      msgs = basicSetL(newL, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.CONCATENATION__L, newL, newL));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getP()
  {
    return p;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setP(String newP)
  {
    String oldP = p;
    p = newP;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.CONCATENATION__P, oldP, p));
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
      case MDDPackage.CONCATENATION__A:
        return ((InternalEList<?>)getA()).basicRemove(otherEnd, msgs);
      case MDDPackage.CONCATENATION__I:
        return basicSetI(null, msgs);
      case MDDPackage.CONCATENATION__B:
        return basicSetB(null, msgs);
      case MDDPackage.CONCATENATION__C:
        return basicSetC(null, msgs);
      case MDDPackage.CONCATENATION__L:
        return basicSetL(null, msgs);
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
      case MDDPackage.CONCATENATION__A:
        return getA();
      case MDDPackage.CONCATENATION__I:
        return getI();
      case MDDPackage.CONCATENATION__B:
        return getB();
      case MDDPackage.CONCATENATION__C:
        return getC();
      case MDDPackage.CONCATENATION__L:
        return getL();
      case MDDPackage.CONCATENATION__P:
        return getP();
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
      case MDDPackage.CONCATENATION__A:
        getA().clear();
        getA().addAll((Collection<? extends AdditiveExpression>)newValue);
        return;
      case MDDPackage.CONCATENATION__I:
        setI((CompoundExpr)newValue);
        return;
      case MDDPackage.CONCATENATION__B:
        setB((BetweenList)newValue);
        return;
      case MDDPackage.CONCATENATION__C:
        setC((Concatenation)newValue);
        return;
      case MDDPackage.CONCATENATION__L:
        setL((LikeEscape)newValue);
        return;
      case MDDPackage.CONCATENATION__P:
        setP((String)newValue);
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
      case MDDPackage.CONCATENATION__A:
        getA().clear();
        return;
      case MDDPackage.CONCATENATION__I:
        setI((CompoundExpr)null);
        return;
      case MDDPackage.CONCATENATION__B:
        setB((BetweenList)null);
        return;
      case MDDPackage.CONCATENATION__C:
        setC((Concatenation)null);
        return;
      case MDDPackage.CONCATENATION__L:
        setL((LikeEscape)null);
        return;
      case MDDPackage.CONCATENATION__P:
        setP(P_EDEFAULT);
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
      case MDDPackage.CONCATENATION__A:
        return a != null && !a.isEmpty();
      case MDDPackage.CONCATENATION__I:
        return i != null;
      case MDDPackage.CONCATENATION__B:
        return b != null;
      case MDDPackage.CONCATENATION__C:
        return c != null;
      case MDDPackage.CONCATENATION__L:
        return l != null;
      case MDDPackage.CONCATENATION__P:
        return P_EDEFAULT == null ? p != null : !P_EDEFAULT.equals(p);
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
    result.append(" (p: ");
    result.append(p);
    result.append(')');
    return result.toString();
  }

} //ConcatenationImpl

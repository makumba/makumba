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
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.makumba.devel.eclipse.mdd.MDD.Expression;
import org.makumba.devel.eclipse.mdd.MDD.GroupByClause;
import org.makumba.devel.eclipse.mdd.MDD.HavingClause;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Group By Clause</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.GroupByClauseImpl#getE <em>E</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.GroupByClauseImpl#getH <em>H</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GroupByClauseImpl extends MinimalEObjectImpl.Container implements GroupByClause
{
  /**
   * The cached value of the '{@link #getE() <em>E</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getE()
   * @generated
   * @ordered
   */
  protected EList<Expression> e;

  /**
   * The cached value of the '{@link #getH() <em>H</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getH()
   * @generated
   * @ordered
   */
  protected HavingClause h;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected GroupByClauseImpl()
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
    return MDDPackage.Literals.GROUP_BY_CLAUSE;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<Expression> getE()
  {
    if (e == null)
    {
      e = new EObjectContainmentEList<Expression>(Expression.class, this, MDDPackage.GROUP_BY_CLAUSE__E);
    }
    return e;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public HavingClause getH()
  {
    return h;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetH(HavingClause newH, NotificationChain msgs)
  {
    HavingClause oldH = h;
    h = newH;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.GROUP_BY_CLAUSE__H, oldH, newH);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setH(HavingClause newH)
  {
    if (newH != h)
    {
      NotificationChain msgs = null;
      if (h != null)
        msgs = ((InternalEObject)h).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.GROUP_BY_CLAUSE__H, null, msgs);
      if (newH != null)
        msgs = ((InternalEObject)newH).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.GROUP_BY_CLAUSE__H, null, msgs);
      msgs = basicSetH(newH, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.GROUP_BY_CLAUSE__H, newH, newH));
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
      case MDDPackage.GROUP_BY_CLAUSE__E:
        return ((InternalEList<?>)getE()).basicRemove(otherEnd, msgs);
      case MDDPackage.GROUP_BY_CLAUSE__H:
        return basicSetH(null, msgs);
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
      case MDDPackage.GROUP_BY_CLAUSE__E:
        return getE();
      case MDDPackage.GROUP_BY_CLAUSE__H:
        return getH();
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
      case MDDPackage.GROUP_BY_CLAUSE__E:
        getE().clear();
        getE().addAll((Collection<? extends Expression>)newValue);
        return;
      case MDDPackage.GROUP_BY_CLAUSE__H:
        setH((HavingClause)newValue);
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
      case MDDPackage.GROUP_BY_CLAUSE__E:
        getE().clear();
        return;
      case MDDPackage.GROUP_BY_CLAUSE__H:
        setH((HavingClause)null);
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
      case MDDPackage.GROUP_BY_CLAUSE__E:
        return e != null && !e.isEmpty();
      case MDDPackage.GROUP_BY_CLAUSE__H:
        return h != null;
    }
    return super.eIsSet(featureID);
  }

} //GroupByClauseImpl

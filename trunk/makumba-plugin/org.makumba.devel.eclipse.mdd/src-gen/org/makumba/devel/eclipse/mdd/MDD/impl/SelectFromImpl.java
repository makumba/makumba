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

import org.makumba.devel.eclipse.mdd.MDD.FromClause;
import org.makumba.devel.eclipse.mdd.MDD.GroupByClause;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.OrderByClause;
import org.makumba.devel.eclipse.mdd.MDD.SelectClause;
import org.makumba.devel.eclipse.mdd.MDD.SelectFrom;
import org.makumba.devel.eclipse.mdd.MDD.WhereClause;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Select From</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectFromImpl#getWhere <em>Where</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectFromImpl#getGroupBy <em>Group By</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectFromImpl#getOrderBy <em>Order By</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectFromImpl#getS <em>S</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectFromImpl#getFrom <em>From</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SelectFromImpl extends QueryRuleImpl implements SelectFrom
{
  /**
   * The cached value of the '{@link #getWhere() <em>Where</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getWhere()
   * @generated
   * @ordered
   */
  protected WhereClause where;

  /**
   * The cached value of the '{@link #getGroupBy() <em>Group By</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getGroupBy()
   * @generated
   * @ordered
   */
  protected GroupByClause groupBy;

  /**
   * The cached value of the '{@link #getOrderBy() <em>Order By</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getOrderBy()
   * @generated
   * @ordered
   */
  protected OrderByClause orderBy;

  /**
   * The cached value of the '{@link #getS() <em>S</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getS()
   * @generated
   * @ordered
   */
  protected SelectClause s;

  /**
   * The cached value of the '{@link #getFrom() <em>From</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFrom()
   * @generated
   * @ordered
   */
  protected FromClause from;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected SelectFromImpl()
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
    return MDDPackage.Literals.SELECT_FROM;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public WhereClause getWhere()
  {
    return where;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetWhere(WhereClause newWhere, NotificationChain msgs)
  {
    WhereClause oldWhere = where;
    where = newWhere;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__WHERE, oldWhere, newWhere);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setWhere(WhereClause newWhere)
  {
    if (newWhere != where)
    {
      NotificationChain msgs = null;
      if (where != null)
        msgs = ((InternalEObject)where).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__WHERE, null, msgs);
      if (newWhere != null)
        msgs = ((InternalEObject)newWhere).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__WHERE, null, msgs);
      msgs = basicSetWhere(newWhere, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__WHERE, newWhere, newWhere));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GroupByClause getGroupBy()
  {
    return groupBy;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetGroupBy(GroupByClause newGroupBy, NotificationChain msgs)
  {
    GroupByClause oldGroupBy = groupBy;
    groupBy = newGroupBy;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__GROUP_BY, oldGroupBy, newGroupBy);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setGroupBy(GroupByClause newGroupBy)
  {
    if (newGroupBy != groupBy)
    {
      NotificationChain msgs = null;
      if (groupBy != null)
        msgs = ((InternalEObject)groupBy).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__GROUP_BY, null, msgs);
      if (newGroupBy != null)
        msgs = ((InternalEObject)newGroupBy).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__GROUP_BY, null, msgs);
      msgs = basicSetGroupBy(newGroupBy, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__GROUP_BY, newGroupBy, newGroupBy));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OrderByClause getOrderBy()
  {
    return orderBy;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetOrderBy(OrderByClause newOrderBy, NotificationChain msgs)
  {
    OrderByClause oldOrderBy = orderBy;
    orderBy = newOrderBy;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__ORDER_BY, oldOrderBy, newOrderBy);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setOrderBy(OrderByClause newOrderBy)
  {
    if (newOrderBy != orderBy)
    {
      NotificationChain msgs = null;
      if (orderBy != null)
        msgs = ((InternalEObject)orderBy).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__ORDER_BY, null, msgs);
      if (newOrderBy != null)
        msgs = ((InternalEObject)newOrderBy).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__ORDER_BY, null, msgs);
      msgs = basicSetOrderBy(newOrderBy, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__ORDER_BY, newOrderBy, newOrderBy));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SelectClause getS()
  {
    return s;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetS(SelectClause newS, NotificationChain msgs)
  {
    SelectClause oldS = s;
    s = newS;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__S, oldS, newS);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setS(SelectClause newS)
  {
    if (newS != s)
    {
      NotificationChain msgs = null;
      if (s != null)
        msgs = ((InternalEObject)s).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__S, null, msgs);
      if (newS != null)
        msgs = ((InternalEObject)newS).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__S, null, msgs);
      msgs = basicSetS(newS, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__S, newS, newS));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FromClause getFrom()
  {
    return from;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetFrom(FromClause newFrom, NotificationChain msgs)
  {
    FromClause oldFrom = from;
    from = newFrom;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__FROM, oldFrom, newFrom);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setFrom(FromClause newFrom)
  {
    if (newFrom != from)
    {
      NotificationChain msgs = null;
      if (from != null)
        msgs = ((InternalEObject)from).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__FROM, null, msgs);
      if (newFrom != null)
        msgs = ((InternalEObject)newFrom).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SELECT_FROM__FROM, null, msgs);
      msgs = basicSetFrom(newFrom, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.SELECT_FROM__FROM, newFrom, newFrom));
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
      case MDDPackage.SELECT_FROM__WHERE:
        return basicSetWhere(null, msgs);
      case MDDPackage.SELECT_FROM__GROUP_BY:
        return basicSetGroupBy(null, msgs);
      case MDDPackage.SELECT_FROM__ORDER_BY:
        return basicSetOrderBy(null, msgs);
      case MDDPackage.SELECT_FROM__S:
        return basicSetS(null, msgs);
      case MDDPackage.SELECT_FROM__FROM:
        return basicSetFrom(null, msgs);
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
      case MDDPackage.SELECT_FROM__WHERE:
        return getWhere();
      case MDDPackage.SELECT_FROM__GROUP_BY:
        return getGroupBy();
      case MDDPackage.SELECT_FROM__ORDER_BY:
        return getOrderBy();
      case MDDPackage.SELECT_FROM__S:
        return getS();
      case MDDPackage.SELECT_FROM__FROM:
        return getFrom();
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
      case MDDPackage.SELECT_FROM__WHERE:
        setWhere((WhereClause)newValue);
        return;
      case MDDPackage.SELECT_FROM__GROUP_BY:
        setGroupBy((GroupByClause)newValue);
        return;
      case MDDPackage.SELECT_FROM__ORDER_BY:
        setOrderBy((OrderByClause)newValue);
        return;
      case MDDPackage.SELECT_FROM__S:
        setS((SelectClause)newValue);
        return;
      case MDDPackage.SELECT_FROM__FROM:
        setFrom((FromClause)newValue);
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
      case MDDPackage.SELECT_FROM__WHERE:
        setWhere((WhereClause)null);
        return;
      case MDDPackage.SELECT_FROM__GROUP_BY:
        setGroupBy((GroupByClause)null);
        return;
      case MDDPackage.SELECT_FROM__ORDER_BY:
        setOrderBy((OrderByClause)null);
        return;
      case MDDPackage.SELECT_FROM__S:
        setS((SelectClause)null);
        return;
      case MDDPackage.SELECT_FROM__FROM:
        setFrom((FromClause)null);
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
      case MDDPackage.SELECT_FROM__WHERE:
        return where != null;
      case MDDPackage.SELECT_FROM__GROUP_BY:
        return groupBy != null;
      case MDDPackage.SELECT_FROM__ORDER_BY:
        return orderBy != null;
      case MDDPackage.SELECT_FROM__S:
        return s != null;
      case MDDPackage.SELECT_FROM__FROM:
        return from != null;
    }
    return super.eIsSet(featureID);
  }

} //SelectFromImpl

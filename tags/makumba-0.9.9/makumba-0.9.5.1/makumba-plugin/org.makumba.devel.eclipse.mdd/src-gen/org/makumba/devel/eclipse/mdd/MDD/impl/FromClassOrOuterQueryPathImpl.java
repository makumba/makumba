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

import org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath;
import org.makumba.devel.eclipse.mdd.MDD.FromRange;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.WithClause;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>From Class Or Outer Query Path</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl#getAlias <em>Alias</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl#getP <em>P</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl#getW <em>W</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl#getPath <em>Path</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl#getPropertyFetch <em>Property Fetch</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FromClassOrOuterQueryPathImpl extends FromJoinImpl implements FromClassOrOuterQueryPath
{
  /**
   * The default value of the '{@link #getAlias() <em>Alias</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getAlias()
   * @generated
   * @ordered
   */
  protected static final String ALIAS_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getAlias() <em>Alias</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getAlias()
   * @generated
   * @ordered
   */
  protected String alias = ALIAS_EDEFAULT;

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
   * The cached value of the '{@link #getW() <em>W</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getW()
   * @generated
   * @ordered
   */
  protected WithClause w;

  /**
   * The default value of the '{@link #getPath() <em>Path</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPath()
   * @generated
   * @ordered
   */
  protected static final String PATH_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getPath() <em>Path</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPath()
   * @generated
   * @ordered
   */
  protected String path = PATH_EDEFAULT;

  /**
   * The default value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected static final String NAME_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected String name = NAME_EDEFAULT;

  /**
   * The default value of the '{@link #getPropertyFetch() <em>Property Fetch</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPropertyFetch()
   * @generated
   * @ordered
   */
  protected static final String PROPERTY_FETCH_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getPropertyFetch() <em>Property Fetch</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPropertyFetch()
   * @generated
   * @ordered
   */
  protected String propertyFetch = PROPERTY_FETCH_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected FromClassOrOuterQueryPathImpl()
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
    return MDDPackage.Literals.FROM_CLASS_OR_OUTER_QUERY_PATH;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getAlias()
  {
    return alias;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setAlias(String newAlias)
  {
    String oldAlias = alias;
    alias = newAlias;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__ALIAS, oldAlias, alias));
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
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__P, oldP, p));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public WithClause getW()
  {
    return w;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetW(WithClause newW, NotificationChain msgs)
  {
    WithClause oldW = w;
    w = newW;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__W, oldW, newW);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setW(WithClause newW)
  {
    if (newW != w)
    {
      NotificationChain msgs = null;
      if (w != null)
        msgs = ((InternalEObject)w).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__W, null, msgs);
      if (newW != null)
        msgs = ((InternalEObject)newW).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__W, null, msgs);
      msgs = basicSetW(newW, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__W, newW, newW));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getPath()
  {
    return path;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setPath(String newPath)
  {
    String oldPath = path;
    path = newPath;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PATH, oldPath, path));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getName()
  {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setName(String newName)
  {
    String oldName = name;
    name = newName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getPropertyFetch()
  {
    return propertyFetch;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setPropertyFetch(String newPropertyFetch)
  {
    String oldPropertyFetch = propertyFetch;
    propertyFetch = newPropertyFetch;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PROPERTY_FETCH, oldPropertyFetch, propertyFetch));
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
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__W:
        return basicSetW(null, msgs);
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
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__ALIAS:
        return getAlias();
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__P:
        return getP();
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__W:
        return getW();
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PATH:
        return getPath();
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__NAME:
        return getName();
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PROPERTY_FETCH:
        return getPropertyFetch();
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
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__ALIAS:
        setAlias((String)newValue);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__P:
        setP((String)newValue);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__W:
        setW((WithClause)newValue);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PATH:
        setPath((String)newValue);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__NAME:
        setName((String)newValue);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PROPERTY_FETCH:
        setPropertyFetch((String)newValue);
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
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__ALIAS:
        setAlias(ALIAS_EDEFAULT);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__P:
        setP(P_EDEFAULT);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__W:
        setW((WithClause)null);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PATH:
        setPath(PATH_EDEFAULT);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__NAME:
        setName(NAME_EDEFAULT);
        return;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PROPERTY_FETCH:
        setPropertyFetch(PROPERTY_FETCH_EDEFAULT);
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
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__ALIAS:
        return ALIAS_EDEFAULT == null ? alias != null : !ALIAS_EDEFAULT.equals(alias);
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__P:
        return P_EDEFAULT == null ? p != null : !P_EDEFAULT.equals(p);
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__W:
        return w != null;
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PATH:
        return PATH_EDEFAULT == null ? path != null : !PATH_EDEFAULT.equals(path);
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__NAME:
        return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__PROPERTY_FETCH:
        return PROPERTY_FETCH_EDEFAULT == null ? propertyFetch != null : !PROPERTY_FETCH_EDEFAULT.equals(propertyFetch);
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass)
  {
    if (baseClass == FromRange.class)
    {
      switch (derivedFeatureID)
      {
        case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__ALIAS: return MDDPackage.FROM_RANGE__ALIAS;
        case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__P: return MDDPackage.FROM_RANGE__P;
        default: return -1;
      }
    }
    return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass)
  {
    if (baseClass == FromRange.class)
    {
      switch (baseFeatureID)
      {
        case MDDPackage.FROM_RANGE__ALIAS: return MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__ALIAS;
        case MDDPackage.FROM_RANGE__P: return MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH__P;
        default: return -1;
      }
    }
    return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
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
    result.append(" (alias: ");
    result.append(alias);
    result.append(", p: ");
    result.append(p);
    result.append(", path: ");
    result.append(path);
    result.append(", name: ");
    result.append(name);
    result.append(", propertyFetch: ");
    result.append(propertyFetch);
    result.append(')');
    return result.toString();
  }

} //FromClassOrOuterQueryPathImpl

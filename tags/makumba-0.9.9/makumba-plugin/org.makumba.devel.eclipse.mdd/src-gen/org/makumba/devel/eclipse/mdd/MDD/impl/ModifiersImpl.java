/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.Modifiers;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Modifiers</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ModifiersImpl#isUnique <em>Unique</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ModifiersImpl#isFixed <em>Fixed</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ModifiersImpl#isNotNull <em>Not Null</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ModifiersImpl#isNotEmpty <em>Not Empty</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ModifiersImpl extends MinimalEObjectImpl.Container implements Modifiers
{
  /**
   * The default value of the '{@link #isUnique() <em>Unique</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isUnique()
   * @generated
   * @ordered
   */
  protected static final boolean UNIQUE_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isUnique() <em>Unique</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isUnique()
   * @generated
   * @ordered
   */
  protected boolean unique = UNIQUE_EDEFAULT;

  /**
   * The default value of the '{@link #isFixed() <em>Fixed</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isFixed()
   * @generated
   * @ordered
   */
  protected static final boolean FIXED_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isFixed() <em>Fixed</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isFixed()
   * @generated
   * @ordered
   */
  protected boolean fixed = FIXED_EDEFAULT;

  /**
   * The default value of the '{@link #isNotNull() <em>Not Null</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isNotNull()
   * @generated
   * @ordered
   */
  protected static final boolean NOT_NULL_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isNotNull() <em>Not Null</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isNotNull()
   * @generated
   * @ordered
   */
  protected boolean notNull = NOT_NULL_EDEFAULT;

  /**
   * The default value of the '{@link #isNotEmpty() <em>Not Empty</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isNotEmpty()
   * @generated
   * @ordered
   */
  protected static final boolean NOT_EMPTY_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isNotEmpty() <em>Not Empty</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isNotEmpty()
   * @generated
   * @ordered
   */
  protected boolean notEmpty = NOT_EMPTY_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ModifiersImpl()
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
    return MDDPackage.Literals.MODIFIERS;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isUnique()
  {
    return unique;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setUnique(boolean newUnique)
  {
    boolean oldUnique = unique;
    unique = newUnique;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.MODIFIERS__UNIQUE, oldUnique, unique));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isFixed()
  {
    return fixed;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setFixed(boolean newFixed)
  {
    boolean oldFixed = fixed;
    fixed = newFixed;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.MODIFIERS__FIXED, oldFixed, fixed));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isNotNull()
  {
    return notNull;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setNotNull(boolean newNotNull)
  {
    boolean oldNotNull = notNull;
    notNull = newNotNull;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.MODIFIERS__NOT_NULL, oldNotNull, notNull));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isNotEmpty()
  {
    return notEmpty;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setNotEmpty(boolean newNotEmpty)
  {
    boolean oldNotEmpty = notEmpty;
    notEmpty = newNotEmpty;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.MODIFIERS__NOT_EMPTY, oldNotEmpty, notEmpty));
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
      case MDDPackage.MODIFIERS__UNIQUE:
        return isUnique();
      case MDDPackage.MODIFIERS__FIXED:
        return isFixed();
      case MDDPackage.MODIFIERS__NOT_NULL:
        return isNotNull();
      case MDDPackage.MODIFIERS__NOT_EMPTY:
        return isNotEmpty();
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
      case MDDPackage.MODIFIERS__UNIQUE:
        setUnique((Boolean)newValue);
        return;
      case MDDPackage.MODIFIERS__FIXED:
        setFixed((Boolean)newValue);
        return;
      case MDDPackage.MODIFIERS__NOT_NULL:
        setNotNull((Boolean)newValue);
        return;
      case MDDPackage.MODIFIERS__NOT_EMPTY:
        setNotEmpty((Boolean)newValue);
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
      case MDDPackage.MODIFIERS__UNIQUE:
        setUnique(UNIQUE_EDEFAULT);
        return;
      case MDDPackage.MODIFIERS__FIXED:
        setFixed(FIXED_EDEFAULT);
        return;
      case MDDPackage.MODIFIERS__NOT_NULL:
        setNotNull(NOT_NULL_EDEFAULT);
        return;
      case MDDPackage.MODIFIERS__NOT_EMPTY:
        setNotEmpty(NOT_EMPTY_EDEFAULT);
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
      case MDDPackage.MODIFIERS__UNIQUE:
        return unique != UNIQUE_EDEFAULT;
      case MDDPackage.MODIFIERS__FIXED:
        return fixed != FIXED_EDEFAULT;
      case MDDPackage.MODIFIERS__NOT_NULL:
        return notNull != NOT_NULL_EDEFAULT;
      case MDDPackage.MODIFIERS__NOT_EMPTY:
        return notEmpty != NOT_EMPTY_EDEFAULT;
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
    result.append(" (unique: ");
    result.append(unique);
    result.append(", fixed: ");
    result.append(fixed);
    result.append(", notNull: ");
    result.append(notNull);
    result.append(", notEmpty: ");
    result.append(notEmpty);
    result.append(')');
    return result.toString();
  }

} //ModifiersImpl

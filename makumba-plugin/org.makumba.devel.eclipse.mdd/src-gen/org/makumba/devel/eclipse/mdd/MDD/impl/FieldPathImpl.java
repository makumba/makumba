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

import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.FieldPath;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Field Path</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldPathImpl#getField <em>Field</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldPathImpl#getSubField <em>Sub Field</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FieldPathImpl extends MinimalEObjectImpl.Container implements FieldPath
{
  /**
   * The cached value of the '{@link #getField() <em>Field</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getField()
   * @generated
   * @ordered
   */
  protected FieldDeclaration field;

  /**
   * The cached value of the '{@link #getSubField() <em>Sub Field</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSubField()
   * @generated
   * @ordered
   */
  protected FieldPath subField;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected FieldPathImpl()
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
    return MDDPackage.Literals.FIELD_PATH;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldDeclaration getField()
  {
    if (field != null && field.eIsProxy())
    {
      InternalEObject oldField = (InternalEObject)field;
      field = (FieldDeclaration)eResolveProxy(oldField);
      if (field != oldField)
      {
        if (eNotificationRequired())
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, MDDPackage.FIELD_PATH__FIELD, oldField, field));
      }
    }
    return field;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldDeclaration basicGetField()
  {
    return field;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setField(FieldDeclaration newField)
  {
    FieldDeclaration oldField = field;
    field = newField;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.FIELD_PATH__FIELD, oldField, field));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldPath getSubField()
  {
    return subField;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetSubField(FieldPath newSubField, NotificationChain msgs)
  {
    FieldPath oldSubField = subField;
    subField = newSubField;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.FIELD_PATH__SUB_FIELD, oldSubField, newSubField);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSubField(FieldPath newSubField)
  {
    if (newSubField != subField)
    {
      NotificationChain msgs = null;
      if (subField != null)
        msgs = ((InternalEObject)subField).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.FIELD_PATH__SUB_FIELD, null, msgs);
      if (newSubField != null)
        msgs = ((InternalEObject)newSubField).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.FIELD_PATH__SUB_FIELD, null, msgs);
      msgs = basicSetSubField(newSubField, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.FIELD_PATH__SUB_FIELD, newSubField, newSubField));
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
      case MDDPackage.FIELD_PATH__SUB_FIELD:
        return basicSetSubField(null, msgs);
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
      case MDDPackage.FIELD_PATH__FIELD:
        if (resolve) return getField();
        return basicGetField();
      case MDDPackage.FIELD_PATH__SUB_FIELD:
        return getSubField();
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
      case MDDPackage.FIELD_PATH__FIELD:
        setField((FieldDeclaration)newValue);
        return;
      case MDDPackage.FIELD_PATH__SUB_FIELD:
        setSubField((FieldPath)newValue);
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
      case MDDPackage.FIELD_PATH__FIELD:
        setField((FieldDeclaration)null);
        return;
      case MDDPackage.FIELD_PATH__SUB_FIELD:
        setSubField((FieldPath)null);
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
      case MDDPackage.FIELD_PATH__FIELD:
        return field != null;
      case MDDPackage.FIELD_PATH__SUB_FIELD:
        return subField != null;
    }
    return super.eIsSet(featureID);
  }

} //FieldPathImpl

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

import org.makumba.devel.eclipse.mdd.MDD.FieldType;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Type Declaration</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.TypeDeclarationImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.TypeDeclarationImpl#getFieldType <em>Field Type</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TypeDeclarationImpl extends DeclarationImpl implements TypeDeclaration
{
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
   * The cached value of the '{@link #getFieldType() <em>Field Type</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFieldType()
   * @generated
   * @ordered
   */
  protected FieldType fieldType;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected TypeDeclarationImpl()
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
    return MDDPackage.Literals.TYPE_DECLARATION;
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
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.TYPE_DECLARATION__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldType getFieldType()
  {
    return fieldType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetFieldType(FieldType newFieldType, NotificationChain msgs)
  {
    FieldType oldFieldType = fieldType;
    fieldType = newFieldType;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.TYPE_DECLARATION__FIELD_TYPE, oldFieldType, newFieldType);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setFieldType(FieldType newFieldType)
  {
    if (newFieldType != fieldType)
    {
      NotificationChain msgs = null;
      if (fieldType != null)
        msgs = ((InternalEObject)fieldType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.TYPE_DECLARATION__FIELD_TYPE, null, msgs);
      if (newFieldType != null)
        msgs = ((InternalEObject)newFieldType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.TYPE_DECLARATION__FIELD_TYPE, null, msgs);
      msgs = basicSetFieldType(newFieldType, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.TYPE_DECLARATION__FIELD_TYPE, newFieldType, newFieldType));
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
      case MDDPackage.TYPE_DECLARATION__FIELD_TYPE:
        return basicSetFieldType(null, msgs);
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
      case MDDPackage.TYPE_DECLARATION__NAME:
        return getName();
      case MDDPackage.TYPE_DECLARATION__FIELD_TYPE:
        return getFieldType();
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
      case MDDPackage.TYPE_DECLARATION__NAME:
        setName((String)newValue);
        return;
      case MDDPackage.TYPE_DECLARATION__FIELD_TYPE:
        setFieldType((FieldType)newValue);
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
      case MDDPackage.TYPE_DECLARATION__NAME:
        setName(NAME_EDEFAULT);
        return;
      case MDDPackage.TYPE_DECLARATION__FIELD_TYPE:
        setFieldType((FieldType)null);
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
      case MDDPackage.TYPE_DECLARATION__NAME:
        return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
      case MDDPackage.TYPE_DECLARATION__FIELD_TYPE:
        return fieldType != null;
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
    result.append(" (name: ");
    result.append(name);
    result.append(')');
    return result.toString();
  }

} //TypeDeclarationImpl

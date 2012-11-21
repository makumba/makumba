/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Native Validation Rule Declaration</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.NativeValidationRuleDeclarationImpl#getField <em>Field</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.NativeValidationRuleDeclarationImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.NativeValidationRuleDeclarationImpl#getMessage <em>Message</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class NativeValidationRuleDeclarationImpl extends DeclarationImpl implements NativeValidationRuleDeclaration
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
   * The default value of the '{@link #getType() <em>Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getType()
   * @generated
   * @ordered
   */
  protected static final String TYPE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getType()
   * @generated
   * @ordered
   */
  protected String type = TYPE_EDEFAULT;

  /**
   * The default value of the '{@link #getMessage() <em>Message</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMessage()
   * @generated
   * @ordered
   */
  protected static final String MESSAGE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getMessage() <em>Message</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMessage()
   * @generated
   * @ordered
   */
  protected String message = MESSAGE_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected NativeValidationRuleDeclarationImpl()
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
    return MDDPackage.Literals.NATIVE_VALIDATION_RULE_DECLARATION;
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
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__FIELD, oldField, field));
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
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__FIELD, oldField, field));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getType()
  {
    return type;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setType(String newType)
  {
    String oldType = type;
    type = newType;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__TYPE, oldType, type));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getMessage()
  {
    return message;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setMessage(String newMessage)
  {
    String oldMessage = message;
    message = newMessage;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__MESSAGE, oldMessage, message));
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
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__FIELD:
        if (resolve) return getField();
        return basicGetField();
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__TYPE:
        return getType();
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__MESSAGE:
        return getMessage();
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
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__FIELD:
        setField((FieldDeclaration)newValue);
        return;
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__TYPE:
        setType((String)newValue);
        return;
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__MESSAGE:
        setMessage((String)newValue);
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
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__FIELD:
        setField((FieldDeclaration)null);
        return;
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__TYPE:
        setType(TYPE_EDEFAULT);
        return;
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__MESSAGE:
        setMessage(MESSAGE_EDEFAULT);
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
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__FIELD:
        return field != null;
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__TYPE:
        return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION__MESSAGE:
        return MESSAGE_EDEFAULT == null ? message != null : !MESSAGE_EDEFAULT.equals(message);
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
    result.append(" (type: ");
    result.append(type);
    result.append(", message: ");
    result.append(message);
    result.append(')');
    return result.toString();
  }

} //NativeValidationRuleDeclarationImpl

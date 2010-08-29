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

import org.makumba.devel.eclipse.mdd.MDD.ErrorMessage;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Validation Rule Declaration</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ValidationRuleDeclarationImpl#getErrorMessage <em>Error Message</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ValidationRuleDeclarationImpl#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ValidationRuleDeclarationImpl extends DeclarationImpl implements ValidationRuleDeclaration
{
  /**
   * The cached value of the '{@link #getErrorMessage() <em>Error Message</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getErrorMessage()
   * @generated
   * @ordered
   */
  protected ErrorMessage errorMessage;

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
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ValidationRuleDeclarationImpl()
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
    return MDDPackage.Literals.VALIDATION_RULE_DECLARATION;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ErrorMessage getErrorMessage()
  {
    return errorMessage;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetErrorMessage(ErrorMessage newErrorMessage, NotificationChain msgs)
  {
    ErrorMessage oldErrorMessage = errorMessage;
    errorMessage = newErrorMessage;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.VALIDATION_RULE_DECLARATION__ERROR_MESSAGE, oldErrorMessage, newErrorMessage);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setErrorMessage(ErrorMessage newErrorMessage)
  {
    if (newErrorMessage != errorMessage)
    {
      NotificationChain msgs = null;
      if (errorMessage != null)
        msgs = ((InternalEObject)errorMessage).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.VALIDATION_RULE_DECLARATION__ERROR_MESSAGE, null, msgs);
      if (newErrorMessage != null)
        msgs = ((InternalEObject)newErrorMessage).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.VALIDATION_RULE_DECLARATION__ERROR_MESSAGE, null, msgs);
      msgs = basicSetErrorMessage(newErrorMessage, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.VALIDATION_RULE_DECLARATION__ERROR_MESSAGE, newErrorMessage, newErrorMessage));
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
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.VALIDATION_RULE_DECLARATION__NAME, oldName, name));
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
      case MDDPackage.VALIDATION_RULE_DECLARATION__ERROR_MESSAGE:
        return basicSetErrorMessage(null, msgs);
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
      case MDDPackage.VALIDATION_RULE_DECLARATION__ERROR_MESSAGE:
        return getErrorMessage();
      case MDDPackage.VALIDATION_RULE_DECLARATION__NAME:
        return getName();
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
      case MDDPackage.VALIDATION_RULE_DECLARATION__ERROR_MESSAGE:
        setErrorMessage((ErrorMessage)newValue);
        return;
      case MDDPackage.VALIDATION_RULE_DECLARATION__NAME:
        setName((String)newValue);
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
      case MDDPackage.VALIDATION_RULE_DECLARATION__ERROR_MESSAGE:
        setErrorMessage((ErrorMessage)null);
        return;
      case MDDPackage.VALIDATION_RULE_DECLARATION__NAME:
        setName(NAME_EDEFAULT);
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
      case MDDPackage.VALIDATION_RULE_DECLARATION__ERROR_MESSAGE:
        return errorMessage != null;
      case MDDPackage.VALIDATION_RULE_DECLARATION__NAME:
        return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
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

} //ValidationRuleDeclarationImpl

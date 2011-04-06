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

import org.makumba.devel.eclipse.mdd.MDD.FieldReference;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Regex Validation Rule Declaration</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.RegexValidationRuleDeclarationImpl#getArg <em>Arg</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.RegexValidationRuleDeclarationImpl#getExp <em>Exp</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RegexValidationRuleDeclarationImpl extends ValidationRuleDeclarationImpl implements RegexValidationRuleDeclaration
{
  /**
   * The cached value of the '{@link #getArg() <em>Arg</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getArg()
   * @generated
   * @ordered
   */
  protected FieldReference arg;

  /**
   * The default value of the '{@link #getExp() <em>Exp</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getExp()
   * @generated
   * @ordered
   */
  protected static final String EXP_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getExp() <em>Exp</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getExp()
   * @generated
   * @ordered
   */
  protected String exp = EXP_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected RegexValidationRuleDeclarationImpl()
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
    return MDDPackage.Literals.REGEX_VALIDATION_RULE_DECLARATION;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldReference getArg()
  {
    return arg;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetArg(FieldReference newArg, NotificationChain msgs)
  {
    FieldReference oldArg = arg;
    arg = newArg;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__ARG, oldArg, newArg);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setArg(FieldReference newArg)
  {
    if (newArg != arg)
    {
      NotificationChain msgs = null;
      if (arg != null)
        msgs = ((InternalEObject)arg).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__ARG, null, msgs);
      if (newArg != null)
        msgs = ((InternalEObject)newArg).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__ARG, null, msgs);
      msgs = basicSetArg(newArg, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__ARG, newArg, newArg));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getExp()
  {
    return exp;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setExp(String newExp)
  {
    String oldExp = exp;
    exp = newExp;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__EXP, oldExp, exp));
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
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__ARG:
        return basicSetArg(null, msgs);
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
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__ARG:
        return getArg();
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__EXP:
        return getExp();
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
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__ARG:
        setArg((FieldReference)newValue);
        return;
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__EXP:
        setExp((String)newValue);
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
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__ARG:
        setArg((FieldReference)null);
        return;
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__EXP:
        setExp(EXP_EDEFAULT);
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
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__ARG:
        return arg != null;
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION__EXP:
        return EXP_EDEFAULT == null ? exp != null : !EXP_EDEFAULT.equals(exp);
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
    result.append(" (exp: ");
    result.append(exp);
    result.append(')');
    return result.toString();
  }

} //RegexValidationRuleDeclarationImpl

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

import org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression;
import org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.FunctionArguments;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Comparison Validation Rule Declaration</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonValidationRuleDeclarationImpl#getArgs <em>Args</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonValidationRuleDeclarationImpl#getComparisonExp <em>Comparison Exp</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ComparisonValidationRuleDeclarationImpl extends ValidationRuleDeclarationImpl implements ComparisonValidationRuleDeclaration
{
  /**
   * The cached value of the '{@link #getArgs() <em>Args</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getArgs()
   * @generated
   * @ordered
   */
  protected FunctionArguments args;

  /**
   * The cached value of the '{@link #getComparisonExp() <em>Comparison Exp</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getComparisonExp()
   * @generated
   * @ordered
   */
  protected ComparisonExpression comparisonExp;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ComparisonValidationRuleDeclarationImpl()
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
    return MDDPackage.Literals.COMPARISON_VALIDATION_RULE_DECLARATION;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FunctionArguments getArgs()
  {
    return args;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetArgs(FunctionArguments newArgs, NotificationChain msgs)
  {
    FunctionArguments oldArgs = args;
    args = newArgs;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__ARGS, oldArgs, newArgs);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setArgs(FunctionArguments newArgs)
  {
    if (newArgs != args)
    {
      NotificationChain msgs = null;
      if (args != null)
        msgs = ((InternalEObject)args).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__ARGS, null, msgs);
      if (newArgs != null)
        msgs = ((InternalEObject)newArgs).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__ARGS, null, msgs);
      msgs = basicSetArgs(newArgs, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__ARGS, newArgs, newArgs));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ComparisonExpression getComparisonExp()
  {
    return comparisonExp;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetComparisonExp(ComparisonExpression newComparisonExp, NotificationChain msgs)
  {
    ComparisonExpression oldComparisonExp = comparisonExp;
    comparisonExp = newComparisonExp;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP, oldComparisonExp, newComparisonExp);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setComparisonExp(ComparisonExpression newComparisonExp)
  {
    if (newComparisonExp != comparisonExp)
    {
      NotificationChain msgs = null;
      if (comparisonExp != null)
        msgs = ((InternalEObject)comparisonExp).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP, null, msgs);
      if (newComparisonExp != null)
        msgs = ((InternalEObject)newComparisonExp).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP, null, msgs);
      msgs = basicSetComparisonExp(newComparisonExp, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP, newComparisonExp, newComparisonExp));
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
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__ARGS:
        return basicSetArgs(null, msgs);
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP:
        return basicSetComparisonExp(null, msgs);
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
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__ARGS:
        return getArgs();
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP:
        return getComparisonExp();
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
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__ARGS:
        setArgs((FunctionArguments)newValue);
        return;
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP:
        setComparisonExp((ComparisonExpression)newValue);
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
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__ARGS:
        setArgs((FunctionArguments)null);
        return;
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP:
        setComparisonExp((ComparisonExpression)null);
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
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__ARGS:
        return args != null;
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP:
        return comparisonExp != null;
    }
    return super.eIsSet(featureID);
  }

} //ComparisonValidationRuleDeclarationImpl

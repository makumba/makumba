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

import org.makumba.devel.eclipse.mdd.MDD.Declaration;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;
import org.makumba.devel.eclipse.mdd.MDD.MDDPackage;
import org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sub Field Declaration</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.SubFieldDeclarationImpl#getSubFieldOf <em>Sub Field Of</em>}</li>
 *   <li>{@link org.makumba.devel.eclipse.mdd.MDD.impl.SubFieldDeclarationImpl#getD <em>D</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SubFieldDeclarationImpl extends DeclarationImpl implements SubFieldDeclaration
{
  /**
   * The cached value of the '{@link #getSubFieldOf() <em>Sub Field Of</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSubFieldOf()
   * @generated
   * @ordered
   */
  protected FieldDeclaration subFieldOf;

  /**
   * The cached value of the '{@link #getD() <em>D</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getD()
   * @generated
   * @ordered
   */
  protected Declaration d;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected SubFieldDeclarationImpl()
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
    return MDDPackage.Literals.SUB_FIELD_DECLARATION;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldDeclaration getSubFieldOf()
  {
    if (subFieldOf != null && subFieldOf.eIsProxy())
    {
      InternalEObject oldSubFieldOf = (InternalEObject)subFieldOf;
      subFieldOf = (FieldDeclaration)eResolveProxy(oldSubFieldOf);
      if (subFieldOf != oldSubFieldOf)
      {
        if (eNotificationRequired())
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, MDDPackage.SUB_FIELD_DECLARATION__SUB_FIELD_OF, oldSubFieldOf, subFieldOf));
      }
    }
    return subFieldOf;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldDeclaration basicGetSubFieldOf()
  {
    return subFieldOf;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSubFieldOf(FieldDeclaration newSubFieldOf)
  {
    FieldDeclaration oldSubFieldOf = subFieldOf;
    subFieldOf = newSubFieldOf;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.SUB_FIELD_DECLARATION__SUB_FIELD_OF, oldSubFieldOf, subFieldOf));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Declaration getD()
  {
    return d;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetD(Declaration newD, NotificationChain msgs)
  {
    Declaration oldD = d;
    d = newD;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MDDPackage.SUB_FIELD_DECLARATION__D, oldD, newD);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setD(Declaration newD)
  {
    if (newD != d)
    {
      NotificationChain msgs = null;
      if (d != null)
        msgs = ((InternalEObject)d).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SUB_FIELD_DECLARATION__D, null, msgs);
      if (newD != null)
        msgs = ((InternalEObject)newD).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MDDPackage.SUB_FIELD_DECLARATION__D, null, msgs);
      msgs = basicSetD(newD, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, MDDPackage.SUB_FIELD_DECLARATION__D, newD, newD));
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
      case MDDPackage.SUB_FIELD_DECLARATION__D:
        return basicSetD(null, msgs);
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
      case MDDPackage.SUB_FIELD_DECLARATION__SUB_FIELD_OF:
        if (resolve) return getSubFieldOf();
        return basicGetSubFieldOf();
      case MDDPackage.SUB_FIELD_DECLARATION__D:
        return getD();
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
      case MDDPackage.SUB_FIELD_DECLARATION__SUB_FIELD_OF:
        setSubFieldOf((FieldDeclaration)newValue);
        return;
      case MDDPackage.SUB_FIELD_DECLARATION__D:
        setD((Declaration)newValue);
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
      case MDDPackage.SUB_FIELD_DECLARATION__SUB_FIELD_OF:
        setSubFieldOf((FieldDeclaration)null);
        return;
      case MDDPackage.SUB_FIELD_DECLARATION__D:
        setD((Declaration)null);
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
      case MDDPackage.SUB_FIELD_DECLARATION__SUB_FIELD_OF:
        return subFieldOf != null;
      case MDDPackage.SUB_FIELD_DECLARATION__D:
        return d != null;
    }
    return super.eIsSet(featureID);
  }

} //SubFieldDeclarationImpl

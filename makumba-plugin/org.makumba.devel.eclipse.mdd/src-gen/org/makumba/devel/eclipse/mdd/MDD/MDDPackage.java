/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDFactory
 * @model kind="package"
 * @generated
 */
public interface MDDPackage extends EPackage
{
  /**
   * The package name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNAME = "MDD";

  /**
   * The package namespace URI.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_URI = "http://www.makumba.org/devel/eclipse/mdd/MDD";

  /**
   * The package namespace name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_PREFIX = "MDD";

  /**
   * The singleton instance of the package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  MDDPackage eINSTANCE = org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl.init();

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.DataDefinitionImpl <em>Data Definition</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.DataDefinitionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getDataDefinition()
   * @generated
   */
  int DATA_DEFINITION = 0;

  /**
   * The feature id for the '<em><b>D</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int DATA_DEFINITION__D = 0;

  /**
   * The number of structural features of the '<em>Data Definition</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int DATA_DEFINITION_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.DeclarationImpl <em>Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.DeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getDeclaration()
   * @generated
   */
  int DECLARATION = 1;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int DECLARATION__FIELD_COMMENT = 0;

  /**
   * The number of structural features of the '<em>Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int DECLARATION_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldDeclarationImpl <em>Field Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FieldDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFieldDeclaration()
   * @generated
   */
  int FIELD_DECLARATION = 2;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_DECLARATION__FIELD_COMMENT = DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_DECLARATION__NAME = DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Modifiers</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_DECLARATION__MODIFIERS = DECLARATION_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Typedef</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_DECLARATION__TYPEDEF = DECLARATION_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Field Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_DECLARATION_FEATURE_COUNT = DECLARATION_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ModifiersImpl <em>Modifiers</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ModifiersImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getModifiers()
   * @generated
   */
  int MODIFIERS = 3;

  /**
   * The feature id for the '<em><b>Unique</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODIFIERS__UNIQUE = 0;

  /**
   * The feature id for the '<em><b>Fixed</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODIFIERS__FIXED = 1;

  /**
   * The feature id for the '<em><b>Not Null</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODIFIERS__NOT_NULL = 2;

  /**
   * The feature id for the '<em><b>Not Empty</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODIFIERS__NOT_EMPTY = 3;

  /**
   * The number of structural features of the '<em>Modifiers</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODIFIERS_FEATURE_COUNT = 4;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldTypeImpl <em>Field Type</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FieldTypeImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFieldType()
   * @generated
   */
  int FIELD_TYPE = 4;

  /**
   * The feature id for the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_TYPE__TYPE = 0;

  /**
   * The feature id for the '<em><b>Type Dec</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_TYPE__TYPE_DEC = 1;

  /**
   * The number of structural features of the '<em>Field Type</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_TYPE_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.IntEnumImpl <em>Int Enum</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.IntEnumImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getIntEnum()
   * @generated
   */
  int INT_ENUM = 5;

  /**
   * The feature id for the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INT_ENUM__TYPE = FIELD_TYPE__TYPE;

  /**
   * The feature id for the '<em><b>Type Dec</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INT_ENUM__TYPE_DEC = FIELD_TYPE__TYPE_DEC;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INT_ENUM__NAME = FIELD_TYPE_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Values</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INT_ENUM__VALUES = FIELD_TYPE_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Int Enum</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INT_ENUM_FEATURE_COUNT = FIELD_TYPE_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.CharEnumImpl <em>Char Enum</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.CharEnumImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getCharEnum()
   * @generated
   */
  int CHAR_ENUM = 6;

  /**
   * The feature id for the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_ENUM__TYPE = FIELD_TYPE__TYPE;

  /**
   * The feature id for the '<em><b>Type Dec</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_ENUM__TYPE_DEC = FIELD_TYPE__TYPE_DEC;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_ENUM__NAME = FIELD_TYPE_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Values</b></em>' attribute list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_ENUM__VALUES = FIELD_TYPE_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Char Enum</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_ENUM_FEATURE_COUNT = FIELD_TYPE_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.EnumValueImpl <em>Enum Value</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.EnumValueImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getEnumValue()
   * @generated
   */
  int ENUM_VALUE = 7;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ENUM_VALUE__NAME = 0;

  /**
   * The feature id for the '<em><b>Value</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ENUM_VALUE__VALUE = 1;

  /**
   * The feature id for the '<em><b>Decpricated</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ENUM_VALUE__DECPRICATED = 2;

  /**
   * The number of structural features of the '<em>Enum Value</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ENUM_VALUE_FEATURE_COUNT = 3;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.CharTypeImpl <em>Char Type</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.CharTypeImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getCharType()
   * @generated
   */
  int CHAR_TYPE = 8;

  /**
   * The feature id for the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_TYPE__TYPE = FIELD_TYPE__TYPE;

  /**
   * The feature id for the '<em><b>Type Dec</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_TYPE__TYPE_DEC = FIELD_TYPE__TYPE_DEC;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_TYPE__NAME = FIELD_TYPE_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Length</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_TYPE__LENGTH = FIELD_TYPE_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Char Type</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CHAR_TYPE_FEATURE_COUNT = FIELD_TYPE_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.PointerTypeImpl <em>Pointer Type</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.PointerTypeImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getPointerType()
   * @generated
   */
  int POINTER_TYPE = 9;

  /**
   * The feature id for the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int POINTER_TYPE__TYPE = FIELD_TYPE__TYPE;

  /**
   * The feature id for the '<em><b>Type Dec</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int POINTER_TYPE__TYPE_DEC = FIELD_TYPE__TYPE_DEC;

  /**
   * The feature id for the '<em><b>Ref</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int POINTER_TYPE__REF = FIELD_TYPE_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Pointer Type</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int POINTER_TYPE_FEATURE_COUNT = FIELD_TYPE_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SetTypeImpl <em>Set Type</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.SetTypeImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSetType()
   * @generated
   */
  int SET_TYPE = 10;

  /**
   * The feature id for the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SET_TYPE__TYPE = FIELD_TYPE__TYPE;

  /**
   * The feature id for the '<em><b>Type Dec</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SET_TYPE__TYPE_DEC = FIELD_TYPE__TYPE_DEC;

  /**
   * The feature id for the '<em><b>Ref</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SET_TYPE__REF = FIELD_TYPE_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Set Type</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SET_TYPE_FEATURE_COUNT = FIELD_TYPE_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SubFieldDeclarationImpl <em>Sub Field Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.SubFieldDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSubFieldDeclaration()
   * @generated
   */
  int SUB_FIELD_DECLARATION = 11;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SUB_FIELD_DECLARATION__FIELD_COMMENT = DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Sub Field Of</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SUB_FIELD_DECLARATION__SUB_FIELD_OF = DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>D</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SUB_FIELD_DECLARATION__D = DECLARATION_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Sub Field Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SUB_FIELD_DECLARATION_FEATURE_COUNT = DECLARATION_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.TitleDeclarationImpl <em>Title Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.TitleDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getTitleDeclaration()
   * @generated
   */
  int TITLE_DECLARATION = 12;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TITLE_DECLARATION__FIELD_COMMENT = DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Field</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TITLE_DECLARATION__FIELD = DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Function</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TITLE_DECLARATION__FUNCTION = DECLARATION_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Title Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TITLE_DECLARATION_FEATURE_COUNT = DECLARATION_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.IncludeDeclarationImpl <em>Include Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.IncludeDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getIncludeDeclaration()
   * @generated
   */
  int INCLUDE_DECLARATION = 13;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INCLUDE_DECLARATION__FIELD_COMMENT = DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Imported Namespace</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INCLUDE_DECLARATION__IMPORTED_NAMESPACE = DECLARATION_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Include Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INCLUDE_DECLARATION_FEATURE_COUNT = DECLARATION_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.TypeDeclarationImpl <em>Type Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.TypeDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getTypeDeclaration()
   * @generated
   */
  int TYPE_DECLARATION = 14;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_DECLARATION__FIELD_COMMENT = DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_DECLARATION__NAME = DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Field Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_DECLARATION__FIELD_TYPE = DECLARATION_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Type Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_DECLARATION_FEATURE_COUNT = DECLARATION_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ValidationRuleDeclarationImpl <em>Validation Rule Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ValidationRuleDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getValidationRuleDeclaration()
   * @generated
   */
  int VALIDATION_RULE_DECLARATION = 15;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VALIDATION_RULE_DECLARATION__FIELD_COMMENT = DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Error Message</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VALIDATION_RULE_DECLARATION__ERROR_MESSAGE = DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VALIDATION_RULE_DECLARATION__NAME = DECLARATION_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Validation Rule Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VALIDATION_RULE_DECLARATION_FEATURE_COUNT = DECLARATION_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonValidationRuleDeclarationImpl <em>Comparison Validation Rule Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonValidationRuleDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getComparisonValidationRuleDeclaration()
   * @generated
   */
  int COMPARISON_VALIDATION_RULE_DECLARATION = 16;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_VALIDATION_RULE_DECLARATION__FIELD_COMMENT = VALIDATION_RULE_DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Error Message</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_VALIDATION_RULE_DECLARATION__ERROR_MESSAGE = VALIDATION_RULE_DECLARATION__ERROR_MESSAGE;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_VALIDATION_RULE_DECLARATION__NAME = VALIDATION_RULE_DECLARATION__NAME;

  /**
   * The feature id for the '<em><b>Args</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_VALIDATION_RULE_DECLARATION__ARGS = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Comparison Exp</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Comparison Validation Rule Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_VALIDATION_RULE_DECLARATION_FEATURE_COUNT = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonExpressionImpl <em>Comparison Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getComparisonExpression()
   * @generated
   */
  int COMPARISON_EXPRESSION = 17;

  /**
   * The feature id for the '<em><b>Lhs</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_EXPRESSION__LHS = 0;

  /**
   * The feature id for the '<em><b>O</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_EXPRESSION__O = 1;

  /**
   * The feature id for the '<em><b>Rhs</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_EXPRESSION__RHS = 2;

  /**
   * The number of structural features of the '<em>Comparison Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_EXPRESSION_FEATURE_COUNT = 3;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonPartImpl <em>Comparison Part</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonPartImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getComparisonPart()
   * @generated
   */
  int COMPARISON_PART = 18;

  /**
   * The feature id for the '<em><b>Field</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_PART__FIELD = 0;

  /**
   * The feature id for the '<em><b>N</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_PART__N = 1;

  /**
   * The feature id for the '<em><b>Df</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_PART__DF = 2;

  /**
   * The feature id for the '<em><b>U</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_PART__U = 3;

  /**
   * The feature id for the '<em><b>L</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_PART__L = 4;

  /**
   * The feature id for the '<em><b>D</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_PART__D = 5;

  /**
   * The number of structural features of the '<em>Comparison Part</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPARISON_PART_FEATURE_COUNT = 6;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.UpperFunctionImpl <em>Upper Function</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.UpperFunctionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getUpperFunction()
   * @generated
   */
  int UPPER_FUNCTION = 19;

  /**
   * The feature id for the '<em><b>Field</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UPPER_FUNCTION__FIELD = 0;

  /**
   * The number of structural features of the '<em>Upper Function</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UPPER_FUNCTION_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.LowerFunctionImpl <em>Lower Function</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.LowerFunctionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getLowerFunction()
   * @generated
   */
  int LOWER_FUNCTION = 20;

  /**
   * The feature id for the '<em><b>Field</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LOWER_FUNCTION__FIELD = 0;

  /**
   * The number of structural features of the '<em>Lower Function</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LOWER_FUNCTION_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.RangeValidationRuleDeclarationImpl <em>Range Validation Rule Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.RangeValidationRuleDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getRangeValidationRuleDeclaration()
   * @generated
   */
  int RANGE_VALIDATION_RULE_DECLARATION = 21;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RANGE_VALIDATION_RULE_DECLARATION__FIELD_COMMENT = VALIDATION_RULE_DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Error Message</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RANGE_VALIDATION_RULE_DECLARATION__ERROR_MESSAGE = VALIDATION_RULE_DECLARATION__ERROR_MESSAGE;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RANGE_VALIDATION_RULE_DECLARATION__NAME = VALIDATION_RULE_DECLARATION__NAME;

  /**
   * The feature id for the '<em><b>Arg</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RANGE_VALIDATION_RULE_DECLARATION__ARG = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Range</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RANGE_VALIDATION_RULE_DECLARATION__RANGE = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Range Validation Rule Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RANGE_VALIDATION_RULE_DECLARATION_FEATURE_COUNT = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.RegexValidationRuleDeclarationImpl <em>Regex Validation Rule Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.RegexValidationRuleDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getRegexValidationRuleDeclaration()
   * @generated
   */
  int REGEX_VALIDATION_RULE_DECLARATION = 22;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int REGEX_VALIDATION_RULE_DECLARATION__FIELD_COMMENT = VALIDATION_RULE_DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Error Message</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int REGEX_VALIDATION_RULE_DECLARATION__ERROR_MESSAGE = VALIDATION_RULE_DECLARATION__ERROR_MESSAGE;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int REGEX_VALIDATION_RULE_DECLARATION__NAME = VALIDATION_RULE_DECLARATION__NAME;

  /**
   * The feature id for the '<em><b>Arg</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int REGEX_VALIDATION_RULE_DECLARATION__ARG = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Exp</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int REGEX_VALIDATION_RULE_DECLARATION__EXP = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Regex Validation Rule Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int REGEX_VALIDATION_RULE_DECLARATION_FEATURE_COUNT = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.RangeImpl <em>Range</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.RangeImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getRange()
   * @generated
   */
  int RANGE = 23;

  /**
   * The feature id for the '<em><b>F</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RANGE__F = 0;

  /**
   * The feature id for the '<em><b>T</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RANGE__T = 1;

  /**
   * The number of structural features of the '<em>Range</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RANGE_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.UniquenessValidationRuleDeclarationImpl <em>Uniqueness Validation Rule Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.UniquenessValidationRuleDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getUniquenessValidationRuleDeclaration()
   * @generated
   */
  int UNIQUENESS_VALIDATION_RULE_DECLARATION = 24;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UNIQUENESS_VALIDATION_RULE_DECLARATION__FIELD_COMMENT = VALIDATION_RULE_DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Error Message</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UNIQUENESS_VALIDATION_RULE_DECLARATION__ERROR_MESSAGE = VALIDATION_RULE_DECLARATION__ERROR_MESSAGE;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UNIQUENESS_VALIDATION_RULE_DECLARATION__NAME = VALIDATION_RULE_DECLARATION__NAME;

  /**
   * The feature id for the '<em><b>Args</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UNIQUENESS_VALIDATION_RULE_DECLARATION__ARGS = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Uniqueness Validation Rule Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UNIQUENESS_VALIDATION_RULE_DECLARATION_FEATURE_COUNT = VALIDATION_RULE_DECLARATION_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ErrorMessageImpl <em>Error Message</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ErrorMessageImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getErrorMessage()
   * @generated
   */
  int ERROR_MESSAGE = 25;

  /**
   * The feature id for the '<em><b>Message</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ERROR_MESSAGE__MESSAGE = 0;

  /**
   * The number of structural features of the '<em>Error Message</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ERROR_MESSAGE_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.NativeValidationRuleDeclarationImpl <em>Native Validation Rule Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.NativeValidationRuleDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getNativeValidationRuleDeclaration()
   * @generated
   */
  int NATIVE_VALIDATION_RULE_DECLARATION = 26;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_VALIDATION_RULE_DECLARATION__FIELD_COMMENT = DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>Field</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_VALIDATION_RULE_DECLARATION__FIELD = DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_VALIDATION_RULE_DECLARATION__TYPE = DECLARATION_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Message</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_VALIDATION_RULE_DECLARATION__MESSAGE = DECLARATION_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Native Validation Rule Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_VALIDATION_RULE_DECLARATION_FEATURE_COUNT = DECLARATION_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionDeclarationImpl <em>Function Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionDeclaration()
   * @generated
   */
  int FUNCTION_DECLARATION = 27;

  /**
   * The feature id for the '<em><b>Field Comment</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_DECLARATION__FIELD_COMMENT = DECLARATION__FIELD_COMMENT;

  /**
   * The feature id for the '<em><b>B</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_DECLARATION__B = DECLARATION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_DECLARATION__NAME = DECLARATION_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Arg</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_DECLARATION__ARG = DECLARATION_FEATURE_COUNT + 2;

  /**
   * The feature id for the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_DECLARATION__BODY = DECLARATION_FEATURE_COUNT + 3;

  /**
   * The feature id for the '<em><b>M</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_DECLARATION__M = DECLARATION_FEATURE_COUNT + 4;

  /**
   * The number of structural features of the '<em>Function Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_DECLARATION_FEATURE_COUNT = DECLARATION_FEATURE_COUNT + 5;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentDeclarationImpl <em>Function Argument Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionArgumentDeclaration()
   * @generated
   */
  int FUNCTION_ARGUMENT_DECLARATION = 28;

  /**
   * The feature id for the '<em><b>F</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_ARGUMENT_DECLARATION__F = 0;

  /**
   * The number of structural features of the '<em>Function Argument Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_ARGUMENT_DECLARATION_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentBodyImpl <em>Function Argument Body</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentBodyImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionArgumentBody()
   * @generated
   */
  int FUNCTION_ARGUMENT_BODY = 29;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_ARGUMENT_BODY__NAME = 0;

  /**
   * The number of structural features of the '<em>Function Argument Body</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_ARGUMENT_BODY_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionCallImpl <em>Function Call</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionCallImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionCall()
   * @generated
   */
  int FUNCTION_CALL = 30;

  /**
   * The feature id for the '<em><b>Function</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_CALL__FUNCTION = 0;

  /**
   * The feature id for the '<em><b>F</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_CALL__F = 1;

  /**
   * The number of structural features of the '<em>Function Call</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_CALL_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldPathImpl <em>Field Path</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FieldPathImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFieldPath()
   * @generated
   */
  int FIELD_PATH = 31;

  /**
   * The feature id for the '<em><b>Field</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_PATH__FIELD = 0;

  /**
   * The feature id for the '<em><b>Sub Field</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_PATH__SUB_FIELD = 1;

  /**
   * The number of structural features of the '<em>Field Path</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_PATH_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldReferenceImpl <em>Field Reference</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FieldReferenceImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFieldReference()
   * @generated
   */
  int FIELD_REFERENCE = 32;

  /**
   * The feature id for the '<em><b>Field</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_REFERENCE__FIELD = 0;

  /**
   * The number of structural features of the '<em>Field Reference</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FIELD_REFERENCE_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentsImpl <em>Function Arguments</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentsImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionArguments()
   * @generated
   */
  int FUNCTION_ARGUMENTS = 33;

  /**
   * The feature id for the '<em><b>Args</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_ARGUMENTS__ARGS = 0;

  /**
   * The number of structural features of the '<em>Function Arguments</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_ARGUMENTS_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionBodyImpl <em>Function Body</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionBodyImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionBody()
   * @generated
   */
  int FUNCTION_BODY = 34;

  /**
   * The feature id for the '<em><b>S</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_BODY__S = 0;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_BODY__E = 1;

  /**
   * The number of structural features of the '<em>Function Body</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_BODY_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.StatementImpl <em>Statement</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.StatementImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getStatement()
   * @generated
   */
  int STATEMENT = 35;

  /**
   * The number of structural features of the '<em>Statement</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STATEMENT_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.PrimaryExpressionImpl <em>Primary Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.PrimaryExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getPrimaryExpression()
   * @generated
   */
  int PRIMARY_EXPRESSION = 72;

  /**
   * The number of structural features of the '<em>Primary Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PRIMARY_EXPRESSION_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.UnionRuleImpl <em>Union Rule</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.UnionRuleImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getUnionRule()
   * @generated
   */
  int UNION_RULE = 36;

  /**
   * The feature id for the '<em><b>Q</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UNION_RULE__Q = PRIMARY_EXPRESSION_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Union Rule</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UNION_RULE_FEATURE_COUNT = PRIMARY_EXPRESSION_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.QueryRuleImpl <em>Query Rule</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.QueryRuleImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getQueryRule()
   * @generated
   */
  int QUERY_RULE = 37;

  /**
   * The number of structural features of the '<em>Query Rule</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int QUERY_RULE_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectFromImpl <em>Select From</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.SelectFromImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSelectFrom()
   * @generated
   */
  int SELECT_FROM = 38;

  /**
   * The feature id for the '<em><b>Where</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECT_FROM__WHERE = QUERY_RULE_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Group By</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECT_FROM__GROUP_BY = QUERY_RULE_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Order By</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECT_FROM__ORDER_BY = QUERY_RULE_FEATURE_COUNT + 2;

  /**
   * The feature id for the '<em><b>S</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECT_FROM__S = QUERY_RULE_FEATURE_COUNT + 3;

  /**
   * The feature id for the '<em><b>From</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECT_FROM__FROM = QUERY_RULE_FEATURE_COUNT + 4;

  /**
   * The number of structural features of the '<em>Select From</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECT_FROM_FEATURE_COUNT = QUERY_RULE_FEATURE_COUNT + 5;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectClauseImpl <em>Select Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.SelectClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSelectClause()
   * @generated
   */
  int SELECT_CLAUSE = 39;

  /**
   * The feature id for the '<em><b>S</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECT_CLAUSE__S = 0;

  /**
   * The feature id for the '<em><b>N</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECT_CLAUSE__N = 1;

  /**
   * The number of structural features of the '<em>Select Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECT_CLAUSE_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.NewExpressionImpl <em>New Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.NewExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getNewExpression()
   * @generated
   */
  int NEW_EXPRESSION = 40;

  /**
   * The feature id for the '<em><b>P</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NEW_EXPRESSION__P = 0;

  /**
   * The feature id for the '<em><b>S</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NEW_EXPRESSION__S = 1;

  /**
   * The number of structural features of the '<em>New Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NEW_EXPRESSION_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClauseImpl <em>From Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FromClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFromClause()
   * @generated
   */
  int FROM_CLAUSE = 41;

  /**
   * The feature id for the '<em><b>From Range</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLAUSE__FROM_RANGE = 0;

  /**
   * The feature id for the '<em><b>From Join</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLAUSE__FROM_JOIN = 1;

  /**
   * The number of structural features of the '<em>From Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLAUSE_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromJoinImpl <em>From Join</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FromJoinImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFromJoin()
   * @generated
   */
  int FROM_JOIN = 42;

  /**
   * The number of structural features of the '<em>From Join</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_JOIN_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.WithClauseImpl <em>With Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.WithClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getWithClause()
   * @generated
   */
  int WITH_CLAUSE = 43;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int WITH_CLAUSE__E = 0;

  /**
   * The number of structural features of the '<em>With Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int WITH_CLAUSE_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromRangeImpl <em>From Range</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FromRangeImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFromRange()
   * @generated
   */
  int FROM_RANGE = 44;

  /**
   * The feature id for the '<em><b>Alias</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_RANGE__ALIAS = 0;

  /**
   * The feature id for the '<em><b>P</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_RANGE__P = 1;

  /**
   * The number of structural features of the '<em>From Range</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_RANGE_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl <em>From Class Or Outer Query Path</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFromClassOrOuterQueryPath()
   * @generated
   */
  int FROM_CLASS_OR_OUTER_QUERY_PATH = 45;

  /**
   * The feature id for the '<em><b>Alias</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLASS_OR_OUTER_QUERY_PATH__ALIAS = FROM_JOIN_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>P</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLASS_OR_OUTER_QUERY_PATH__P = FROM_JOIN_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>W</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLASS_OR_OUTER_QUERY_PATH__W = FROM_JOIN_FEATURE_COUNT + 2;

  /**
   * The feature id for the '<em><b>Path</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLASS_OR_OUTER_QUERY_PATH__PATH = FROM_JOIN_FEATURE_COUNT + 3;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLASS_OR_OUTER_QUERY_PATH__NAME = FROM_JOIN_FEATURE_COUNT + 4;

  /**
   * The feature id for the '<em><b>Property Fetch</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLASS_OR_OUTER_QUERY_PATH__PROPERTY_FETCH = FROM_JOIN_FEATURE_COUNT + 5;

  /**
   * The number of structural features of the '<em>From Class Or Outer Query Path</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FROM_CLASS_OR_OUTER_QUERY_PATH_FEATURE_COUNT = FROM_JOIN_FEATURE_COUNT + 6;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.InCollectionElementsDeclarationImpl <em>In Collection Elements Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.InCollectionElementsDeclarationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getInCollectionElementsDeclaration()
   * @generated
   */
  int IN_COLLECTION_ELEMENTS_DECLARATION = 46;

  /**
   * The feature id for the '<em><b>Alias</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int IN_COLLECTION_ELEMENTS_DECLARATION__ALIAS = FROM_RANGE__ALIAS;

  /**
   * The feature id for the '<em><b>P</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int IN_COLLECTION_ELEMENTS_DECLARATION__P = FROM_RANGE__P;

  /**
   * The number of structural features of the '<em>In Collection Elements Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int IN_COLLECTION_ELEMENTS_DECLARATION_FEATURE_COUNT = FROM_RANGE_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.GroupByClauseImpl <em>Group By Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.GroupByClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getGroupByClause()
   * @generated
   */
  int GROUP_BY_CLAUSE = 47;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int GROUP_BY_CLAUSE__E = 0;

  /**
   * The feature id for the '<em><b>H</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int GROUP_BY_CLAUSE__H = 1;

  /**
   * The number of structural features of the '<em>Group By Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int GROUP_BY_CLAUSE_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.OrderByClauseImpl <em>Order By Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.OrderByClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getOrderByClause()
   * @generated
   */
  int ORDER_BY_CLAUSE = 48;

  /**
   * The feature id for the '<em><b>O</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ORDER_BY_CLAUSE__O = 0;

  /**
   * The number of structural features of the '<em>Order By Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ORDER_BY_CLAUSE_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.OrderElementImpl <em>Order Element</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.OrderElementImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getOrderElement()
   * @generated
   */
  int ORDER_ELEMENT = 49;

  /**
   * The number of structural features of the '<em>Order Element</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ORDER_ELEMENT_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.HavingClauseImpl <em>Having Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.HavingClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getHavingClause()
   * @generated
   */
  int HAVING_CLAUSE = 50;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int HAVING_CLAUSE__E = 0;

  /**
   * The number of structural features of the '<em>Having Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int HAVING_CLAUSE_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.WhereClauseImpl <em>Where Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.WhereClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getWhereClause()
   * @generated
   */
  int WHERE_CLAUSE = 51;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int WHERE_CLAUSE__E = 0;

  /**
   * The number of structural features of the '<em>Where Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int WHERE_CLAUSE_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectedPropertiesListImpl <em>Selected Properties List</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.SelectedPropertiesListImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSelectedPropertiesList()
   * @generated
   */
  int SELECTED_PROPERTIES_LIST = 52;

  /**
   * The feature id for the '<em><b>A</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECTED_PROPERTIES_LIST__A = 0;

  /**
   * The number of structural features of the '<em>Selected Properties List</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int SELECTED_PROPERTIES_LIST_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AliasedExpressionImpl <em>Aliased Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.AliasedExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAliasedExpression()
   * @generated
   */
  int ALIASED_EXPRESSION = 53;

  /**
   * The number of structural features of the '<em>Aliased Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ALIASED_EXPRESSION_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ExpressionImpl <em>Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getExpression()
   * @generated
   */
  int EXPRESSION = 54;

  /**
   * The feature id for the '<em><b>Id</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPRESSION__ID = ORDER_ELEMENT_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>V</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPRESSION__V = ORDER_ELEMENT_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPRESSION_FEATURE_COUNT = ORDER_ELEMENT_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.LogicalOrExpressionImpl <em>Logical Or Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.LogicalOrExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getLogicalOrExpression()
   * @generated
   */
  int LOGICAL_OR_EXPRESSION = 55;

  /**
   * The feature id for the '<em><b>Id</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LOGICAL_OR_EXPRESSION__ID = EXPRESSION__ID;

  /**
   * The feature id for the '<em><b>V</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LOGICAL_OR_EXPRESSION__V = EXPRESSION__V;

  /**
   * The feature id for the '<em><b>L</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LOGICAL_OR_EXPRESSION__L = EXPRESSION_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Logical Or Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LOGICAL_OR_EXPRESSION_FEATURE_COUNT = EXPRESSION_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.LogicalAndExpressionImpl <em>Logical And Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.LogicalAndExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getLogicalAndExpression()
   * @generated
   */
  int LOGICAL_AND_EXPRESSION = 56;

  /**
   * The feature id for the '<em><b>N</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LOGICAL_AND_EXPRESSION__N = 0;

  /**
   * The number of structural features of the '<em>Logical And Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LOGICAL_AND_EXPRESSION_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.NegatedExpressionImpl <em>Negated Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.NegatedExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getNegatedExpression()
   * @generated
   */
  int NEGATED_EXPRESSION = 57;

  /**
   * The feature id for the '<em><b>N</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NEGATED_EXPRESSION__N = 0;

  /**
   * The number of structural features of the '<em>Negated Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NEGATED_EXPRESSION_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.EqualityExpressionImpl <em>Equality Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.EqualityExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getEqualityExpression()
   * @generated
   */
  int EQUALITY_EXPRESSION = 58;

  /**
   * The feature id for the '<em><b>N</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EQUALITY_EXPRESSION__N = NEGATED_EXPRESSION__N;

  /**
   * The feature id for the '<em><b>R</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EQUALITY_EXPRESSION__R = NEGATED_EXPRESSION_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Equality Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EQUALITY_EXPRESSION_FEATURE_COUNT = NEGATED_EXPRESSION_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.RelationalExpressionImpl <em>Relational Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.RelationalExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getRelationalExpression()
   * @generated
   */
  int RELATIONAL_EXPRESSION = 59;

  /**
   * The number of structural features of the '<em>Relational Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RELATIONAL_EXPRESSION_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.LikeEscapeImpl <em>Like Escape</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.LikeEscapeImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getLikeEscape()
   * @generated
   */
  int LIKE_ESCAPE = 60;

  /**
   * The feature id for the '<em><b>C</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LIKE_ESCAPE__C = 0;

  /**
   * The number of structural features of the '<em>Like Escape</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LIKE_ESCAPE_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.BetweenListImpl <em>Between List</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.BetweenListImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getBetweenList()
   * @generated
   */
  int BETWEEN_LIST = 61;

  /**
   * The feature id for the '<em><b>C</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int BETWEEN_LIST__C = 0;

  /**
   * The number of structural features of the '<em>Between List</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int BETWEEN_LIST_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl <em>Concatenation</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getConcatenation()
   * @generated
   */
  int CONCATENATION = 62;

  /**
   * The feature id for the '<em><b>A</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONCATENATION__A = RELATIONAL_EXPRESSION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>I</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONCATENATION__I = RELATIONAL_EXPRESSION_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>B</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONCATENATION__B = RELATIONAL_EXPRESSION_FEATURE_COUNT + 2;

  /**
   * The feature id for the '<em><b>C</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONCATENATION__C = RELATIONAL_EXPRESSION_FEATURE_COUNT + 3;

  /**
   * The feature id for the '<em><b>L</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONCATENATION__L = RELATIONAL_EXPRESSION_FEATURE_COUNT + 4;

  /**
   * The feature id for the '<em><b>P</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONCATENATION__P = RELATIONAL_EXPRESSION_FEATURE_COUNT + 5;

  /**
   * The number of structural features of the '<em>Concatenation</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONCATENATION_FEATURE_COUNT = RELATIONAL_EXPRESSION_FEATURE_COUNT + 6;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AdditiveExpressionImpl <em>Additive Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.AdditiveExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAdditiveExpression()
   * @generated
   */
  int ADDITIVE_EXPRESSION = 63;

  /**
   * The feature id for the '<em><b>A</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ADDITIVE_EXPRESSION__A = CONCATENATION__A;

  /**
   * The feature id for the '<em><b>I</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ADDITIVE_EXPRESSION__I = CONCATENATION__I;

  /**
   * The feature id for the '<em><b>B</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ADDITIVE_EXPRESSION__B = CONCATENATION__B;

  /**
   * The feature id for the '<em><b>C</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ADDITIVE_EXPRESSION__C = CONCATENATION__C;

  /**
   * The feature id for the '<em><b>L</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ADDITIVE_EXPRESSION__L = CONCATENATION__L;

  /**
   * The feature id for the '<em><b>P</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ADDITIVE_EXPRESSION__P = CONCATENATION__P;

  /**
   * The feature id for the '<em><b>M</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ADDITIVE_EXPRESSION__M = CONCATENATION_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Additive Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ADDITIVE_EXPRESSION_FEATURE_COUNT = CONCATENATION_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.MultiplyExpressionImpl <em>Multiply Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MultiplyExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getMultiplyExpression()
   * @generated
   */
  int MULTIPLY_EXPRESSION = 64;

  /**
   * The feature id for the '<em><b>U</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MULTIPLY_EXPRESSION__U = 0;

  /**
   * The number of structural features of the '<em>Multiply Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MULTIPLY_EXPRESSION_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.UnaryExpressionImpl <em>Unary Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.UnaryExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getUnaryExpression()
   * @generated
   */
  int UNARY_EXPRESSION = 65;

  /**
   * The feature id for the '<em><b>U</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UNARY_EXPRESSION__U = 0;

  /**
   * The number of structural features of the '<em>Unary Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int UNARY_EXPRESSION_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.CaseExpressionImpl <em>Case Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.CaseExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getCaseExpression()
   * @generated
   */
  int CASE_EXPRESSION = 66;

  /**
   * The feature id for the '<em><b>U</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CASE_EXPRESSION__U = UNARY_EXPRESSION__U;

  /**
   * The feature id for the '<em><b>W</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CASE_EXPRESSION__W = UNARY_EXPRESSION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CASE_EXPRESSION__E = UNARY_EXPRESSION_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>A</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CASE_EXPRESSION__A = UNARY_EXPRESSION_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Case Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CASE_EXPRESSION_FEATURE_COUNT = UNARY_EXPRESSION_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.WhenClauseImpl <em>When Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.WhenClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getWhenClause()
   * @generated
   */
  int WHEN_CLAUSE = 67;

  /**
   * The feature id for the '<em><b>When Expr</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int WHEN_CLAUSE__WHEN_EXPR = 0;

  /**
   * The feature id for the '<em><b>Then Expr</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int WHEN_CLAUSE__THEN_EXPR = 1;

  /**
   * The number of structural features of the '<em>When Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int WHEN_CLAUSE_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AltWhenClauseImpl <em>Alt When Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.AltWhenClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAltWhenClause()
   * @generated
   */
  int ALT_WHEN_CLAUSE = 68;

  /**
   * The feature id for the '<em><b>W</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ALT_WHEN_CLAUSE__W = 0;

  /**
   * The feature id for the '<em><b>T</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ALT_WHEN_CLAUSE__T = 1;

  /**
   * The number of structural features of the '<em>Alt When Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ALT_WHEN_CLAUSE_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ElseClauseImpl <em>Else Clause</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ElseClauseImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getElseClause()
   * @generated
   */
  int ELSE_CLAUSE = 69;

  /**
   * The feature id for the '<em><b>U</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ELSE_CLAUSE__U = 0;

  /**
   * The number of structural features of the '<em>Else Clause</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ELSE_CLAUSE_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.QuantifiedExpressionImpl <em>Quantified Expression</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.QuantifiedExpressionImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getQuantifiedExpression()
   * @generated
   */
  int QUANTIFIED_EXPRESSION = 70;

  /**
   * The feature id for the '<em><b>U</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int QUANTIFIED_EXPRESSION__U = UNARY_EXPRESSION__U;

  /**
   * The feature id for the '<em><b>S</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int QUANTIFIED_EXPRESSION__S = UNARY_EXPRESSION_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Quantified Expression</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int QUANTIFIED_EXPRESSION_FEATURE_COUNT = UNARY_EXPRESSION_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AtomImpl <em>Atom</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.AtomImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAtom()
   * @generated
   */
  int ATOM = 71;

  /**
   * The feature id for the '<em><b>U</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ATOM__U = UNARY_EXPRESSION__U;

  /**
   * The feature id for the '<em><b>Prime</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ATOM__PRIME = UNARY_EXPRESSION_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ATOM__E = UNARY_EXPRESSION_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Exp</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ATOM__EXP = UNARY_EXPRESSION_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Atom</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ATOM_FEATURE_COUNT = UNARY_EXPRESSION_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ExpressionOrVectorImpl <em>Expression Or Vector</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ExpressionOrVectorImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getExpressionOrVector()
   * @generated
   */
  int EXPRESSION_OR_VECTOR = 73;

  /**
   * The number of structural features of the '<em>Expression Or Vector</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPRESSION_OR_VECTOR_FEATURE_COUNT = PRIMARY_EXPRESSION_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.VectorExprImpl <em>Vector Expr</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.VectorExprImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getVectorExpr()
   * @generated
   */
  int VECTOR_EXPR = 74;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VECTOR_EXPR__E = 0;

  /**
   * The number of structural features of the '<em>Vector Expr</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VECTOR_EXPR_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.IdentPrimaryImpl <em>Ident Primary</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.IdentPrimaryImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getIdentPrimary()
   * @generated
   */
  int IDENT_PRIMARY = 75;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int IDENT_PRIMARY__E = PRIMARY_EXPRESSION_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Ident Primary</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int IDENT_PRIMARY_FEATURE_COUNT = PRIMARY_EXPRESSION_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AggregateImpl <em>Aggregate</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.AggregateImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAggregate()
   * @generated
   */
  int AGGREGATE = 76;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int AGGREGATE__E = IDENT_PRIMARY__E;

  /**
   * The feature id for the '<em><b>A</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int AGGREGATE__A = IDENT_PRIMARY_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Aggregate</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int AGGREGATE_FEATURE_COUNT = IDENT_PRIMARY_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.CompoundExprImpl <em>Compound Expr</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.CompoundExprImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getCompoundExpr()
   * @generated
   */
  int COMPOUND_EXPR = 77;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPOUND_EXPR__E = 0;

  /**
   * The feature id for the '<em><b>S</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPOUND_EXPR__S = 1;

  /**
   * The number of structural features of the '<em>Compound Expr</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMPOUND_EXPR_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ExprListImpl <em>Expr List</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.ExprListImpl
   * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getExprList()
   * @generated
   */
  int EXPR_LIST = 78;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_LIST__E = 0;

  /**
   * The feature id for the '<em><b>Id</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_LIST__ID = 1;

  /**
   * The number of structural features of the '<em>Expr List</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_LIST_FEATURE_COUNT = 2;


  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.DataDefinition <em>Data Definition</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Data Definition</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.DataDefinition
   * @generated
   */
  EClass getDataDefinition();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.DataDefinition#getD <em>D</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>D</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.DataDefinition#getD()
   * @see #getDataDefinition()
   * @generated
   */
  EReference getDataDefinition_D();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.Declaration <em>Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Declaration
   * @generated
   */
  EClass getDeclaration();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.Declaration#getFieldComment <em>Field Comment</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Field Comment</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Declaration#getFieldComment()
   * @see #getDeclaration()
   * @generated
   */
  EAttribute getDeclaration_FieldComment();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration <em>Field Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Field Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration
   * @generated
   */
  EClass getFieldDeclaration();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getName()
   * @see #getFieldDeclaration()
   * @generated
   */
  EAttribute getFieldDeclaration_Name();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getModifiers <em>Modifiers</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Modifiers</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getModifiers()
   * @see #getFieldDeclaration()
   * @generated
   */
  EReference getFieldDeclaration_Modifiers();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getTypedef <em>Typedef</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Typedef</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration#getTypedef()
   * @see #getFieldDeclaration()
   * @generated
   */
  EReference getFieldDeclaration_Typedef();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.Modifiers <em>Modifiers</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Modifiers</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Modifiers
   * @generated
   */
  EClass getModifiers();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.Modifiers#isUnique <em>Unique</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Unique</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Modifiers#isUnique()
   * @see #getModifiers()
   * @generated
   */
  EAttribute getModifiers_Unique();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.Modifiers#isFixed <em>Fixed</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Fixed</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Modifiers#isFixed()
   * @see #getModifiers()
   * @generated
   */
  EAttribute getModifiers_Fixed();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.Modifiers#isNotNull <em>Not Null</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Not Null</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Modifiers#isNotNull()
   * @see #getModifiers()
   * @generated
   */
  EAttribute getModifiers_NotNull();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.Modifiers#isNotEmpty <em>Not Empty</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Not Empty</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Modifiers#isNotEmpty()
   * @see #getModifiers()
   * @generated
   */
  EAttribute getModifiers_NotEmpty();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FieldType <em>Field Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Field Type</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldType
   * @generated
   */
  EClass getFieldType();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FieldType#getType <em>Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Type</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldType#getType()
   * @see #getFieldType()
   * @generated
   */
  EAttribute getFieldType_Type();

  /**
   * Returns the meta object for the reference '{@link org.makumba.devel.eclipse.mdd.MDD.FieldType#getTypeDec <em>Type Dec</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Type Dec</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldType#getTypeDec()
   * @see #getFieldType()
   * @generated
   */
  EReference getFieldType_TypeDec();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.IntEnum <em>Int Enum</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Int Enum</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.IntEnum
   * @generated
   */
  EClass getIntEnum();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.IntEnum#getValues <em>Values</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Values</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.IntEnum#getValues()
   * @see #getIntEnum()
   * @generated
   */
  EReference getIntEnum_Values();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.CharEnum <em>Char Enum</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Char Enum</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CharEnum
   * @generated
   */
  EClass getCharEnum();

  /**
   * Returns the meta object for the attribute list '{@link org.makumba.devel.eclipse.mdd.MDD.CharEnum#getValues <em>Values</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute list '<em>Values</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CharEnum#getValues()
   * @see #getCharEnum()
   * @generated
   */
  EAttribute getCharEnum_Values();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.EnumValue <em>Enum Value</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Enum Value</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.EnumValue
   * @generated
   */
  EClass getEnumValue();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.EnumValue#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.EnumValue#getName()
   * @see #getEnumValue()
   * @generated
   */
  EAttribute getEnumValue_Name();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.EnumValue#getValue <em>Value</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Value</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.EnumValue#getValue()
   * @see #getEnumValue()
   * @generated
   */
  EAttribute getEnumValue_Value();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.EnumValue#isDecpricated <em>Decpricated</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Decpricated</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.EnumValue#isDecpricated()
   * @see #getEnumValue()
   * @generated
   */
  EAttribute getEnumValue_Decpricated();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.CharType <em>Char Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Char Type</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CharType
   * @generated
   */
  EClass getCharType();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.CharType#getLength <em>Length</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Length</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CharType#getLength()
   * @see #getCharType()
   * @generated
   */
  EAttribute getCharType_Length();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.PointerType <em>Pointer Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Pointer Type</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.PointerType
   * @generated
   */
  EClass getPointerType();

  /**
   * Returns the meta object for the reference '{@link org.makumba.devel.eclipse.mdd.MDD.PointerType#getRef <em>Ref</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Ref</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.PointerType#getRef()
   * @see #getPointerType()
   * @generated
   */
  EReference getPointerType_Ref();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.SetType <em>Set Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Set Type</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SetType
   * @generated
   */
  EClass getSetType();

  /**
   * Returns the meta object for the reference '{@link org.makumba.devel.eclipse.mdd.MDD.SetType#getRef <em>Ref</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Ref</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SetType#getRef()
   * @see #getSetType()
   * @generated
   */
  EReference getSetType_Ref();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration <em>Sub Field Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Sub Field Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration
   * @generated
   */
  EClass getSubFieldDeclaration();

  /**
   * Returns the meta object for the reference '{@link org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration#getSubFieldOf <em>Sub Field Of</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Sub Field Of</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration#getSubFieldOf()
   * @see #getSubFieldDeclaration()
   * @generated
   */
  EReference getSubFieldDeclaration_SubFieldOf();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration#getD <em>D</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>D</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration#getD()
   * @see #getSubFieldDeclaration()
   * @generated
   */
  EReference getSubFieldDeclaration_D();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration <em>Title Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Title Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration
   * @generated
   */
  EClass getTitleDeclaration();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration#getField <em>Field</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Field</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration#getField()
   * @see #getTitleDeclaration()
   * @generated
   */
  EReference getTitleDeclaration_Field();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration#getFunction <em>Function</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Function</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration#getFunction()
   * @see #getTitleDeclaration()
   * @generated
   */
  EReference getTitleDeclaration_Function();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.IncludeDeclaration <em>Include Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Include Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.IncludeDeclaration
   * @generated
   */
  EClass getIncludeDeclaration();

  /**
   * Returns the meta object for the reference '{@link org.makumba.devel.eclipse.mdd.MDD.IncludeDeclaration#getImportedNamespace <em>Imported Namespace</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Imported Namespace</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.IncludeDeclaration#getImportedNamespace()
   * @see #getIncludeDeclaration()
   * @generated
   */
  EReference getIncludeDeclaration_ImportedNamespace();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration <em>Type Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Type Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration
   * @generated
   */
  EClass getTypeDeclaration();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration#getName()
   * @see #getTypeDeclaration()
   * @generated
   */
  EAttribute getTypeDeclaration_Name();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration#getFieldType <em>Field Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Field Type</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration#getFieldType()
   * @see #getTypeDeclaration()
   * @generated
   */
  EReference getTypeDeclaration_FieldType();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration <em>Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Validation Rule Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration
   * @generated
   */
  EClass getValidationRuleDeclaration();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration#getErrorMessage <em>Error Message</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Error Message</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration#getErrorMessage()
   * @see #getValidationRuleDeclaration()
   * @generated
   */
  EReference getValidationRuleDeclaration_ErrorMessage();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration#getName()
   * @see #getValidationRuleDeclaration()
   * @generated
   */
  EAttribute getValidationRuleDeclaration_Name();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration <em>Comparison Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Comparison Validation Rule Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration
   * @generated
   */
  EClass getComparisonValidationRuleDeclaration();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration#getArgs <em>Args</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Args</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration#getArgs()
   * @see #getComparisonValidationRuleDeclaration()
   * @generated
   */
  EReference getComparisonValidationRuleDeclaration_Args();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration#getComparisonExp <em>Comparison Exp</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Comparison Exp</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration#getComparisonExp()
   * @see #getComparisonValidationRuleDeclaration()
   * @generated
   */
  EReference getComparisonValidationRuleDeclaration_ComparisonExp();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression <em>Comparison Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Comparison Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression
   * @generated
   */
  EClass getComparisonExpression();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getLhs <em>Lhs</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Lhs</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getLhs()
   * @see #getComparisonExpression()
   * @generated
   */
  EReference getComparisonExpression_Lhs();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getO <em>O</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>O</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getO()
   * @see #getComparisonExpression()
   * @generated
   */
  EAttribute getComparisonExpression_O();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getRhs <em>Rhs</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Rhs</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression#getRhs()
   * @see #getComparisonExpression()
   * @generated
   */
  EReference getComparisonExpression_Rhs();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart <em>Comparison Part</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Comparison Part</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonPart
   * @generated
   */
  EClass getComparisonPart();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getField <em>Field</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Field</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getField()
   * @see #getComparisonPart()
   * @generated
   */
  EReference getComparisonPart_Field();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getN <em>N</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>N</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getN()
   * @see #getComparisonPart()
   * @generated
   */
  EAttribute getComparisonPart_N();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getDf <em>Df</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Df</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getDf()
   * @see #getComparisonPart()
   * @generated
   */
  EAttribute getComparisonPart_Df();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getU <em>U</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>U</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getU()
   * @see #getComparisonPart()
   * @generated
   */
  EReference getComparisonPart_U();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getL <em>L</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>L</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getL()
   * @see #getComparisonPart()
   * @generated
   */
  EReference getComparisonPart_L();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getD <em>D</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>D</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonPart#getD()
   * @see #getComparisonPart()
   * @generated
   */
  EAttribute getComparisonPart_D();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.UpperFunction <em>Upper Function</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Upper Function</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.UpperFunction
   * @generated
   */
  EClass getUpperFunction();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.UpperFunction#getField <em>Field</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Field</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.UpperFunction#getField()
   * @see #getUpperFunction()
   * @generated
   */
  EReference getUpperFunction_Field();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.LowerFunction <em>Lower Function</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Lower Function</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.LowerFunction
   * @generated
   */
  EClass getLowerFunction();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.LowerFunction#getField <em>Field</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Field</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.LowerFunction#getField()
   * @see #getLowerFunction()
   * @generated
   */
  EReference getLowerFunction_Field();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration <em>Range Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Range Validation Rule Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration
   * @generated
   */
  EClass getRangeValidationRuleDeclaration();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration#getArg <em>Arg</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Arg</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration#getArg()
   * @see #getRangeValidationRuleDeclaration()
   * @generated
   */
  EReference getRangeValidationRuleDeclaration_Arg();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration#getRange <em>Range</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Range</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration#getRange()
   * @see #getRangeValidationRuleDeclaration()
   * @generated
   */
  EReference getRangeValidationRuleDeclaration_Range();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration <em>Regex Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Regex Validation Rule Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration
   * @generated
   */
  EClass getRegexValidationRuleDeclaration();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration#getArg <em>Arg</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Arg</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration#getArg()
   * @see #getRegexValidationRuleDeclaration()
   * @generated
   */
  EReference getRegexValidationRuleDeclaration_Arg();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration#getExp <em>Exp</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Exp</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration#getExp()
   * @see #getRegexValidationRuleDeclaration()
   * @generated
   */
  EAttribute getRegexValidationRuleDeclaration_Exp();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.Range <em>Range</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Range</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Range
   * @generated
   */
  EClass getRange();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.Range#getF <em>F</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>F</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Range#getF()
   * @see #getRange()
   * @generated
   */
  EAttribute getRange_F();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.Range#getT <em>T</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>T</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Range#getT()
   * @see #getRange()
   * @generated
   */
  EAttribute getRange_T();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.UniquenessValidationRuleDeclaration <em>Uniqueness Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Uniqueness Validation Rule Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.UniquenessValidationRuleDeclaration
   * @generated
   */
  EClass getUniquenessValidationRuleDeclaration();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.UniquenessValidationRuleDeclaration#getArgs <em>Args</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Args</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.UniquenessValidationRuleDeclaration#getArgs()
   * @see #getUniquenessValidationRuleDeclaration()
   * @generated
   */
  EReference getUniquenessValidationRuleDeclaration_Args();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.ErrorMessage <em>Error Message</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Error Message</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ErrorMessage
   * @generated
   */
  EClass getErrorMessage();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.ErrorMessage#getMessage <em>Message</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Message</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ErrorMessage#getMessage()
   * @see #getErrorMessage()
   * @generated
   */
  EAttribute getErrorMessage_Message();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration <em>Native Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Native Validation Rule Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration
   * @generated
   */
  EClass getNativeValidationRuleDeclaration();

  /**
   * Returns the meta object for the reference '{@link org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration#getField <em>Field</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Field</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration#getField()
   * @see #getNativeValidationRuleDeclaration()
   * @generated
   */
  EReference getNativeValidationRuleDeclaration_Field();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration#getType <em>Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Type</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration#getType()
   * @see #getNativeValidationRuleDeclaration()
   * @generated
   */
  EAttribute getNativeValidationRuleDeclaration_Type();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration#getMessage <em>Message</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Message</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration#getMessage()
   * @see #getNativeValidationRuleDeclaration()
   * @generated
   */
  EAttribute getNativeValidationRuleDeclaration_Message();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration <em>Function Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Function Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration
   * @generated
   */
  EClass getFunctionDeclaration();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getB <em>B</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>B</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getB()
   * @see #getFunctionDeclaration()
   * @generated
   */
  EAttribute getFunctionDeclaration_B();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getName()
   * @see #getFunctionDeclaration()
   * @generated
   */
  EAttribute getFunctionDeclaration_Name();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getArg <em>Arg</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Arg</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getArg()
   * @see #getFunctionDeclaration()
   * @generated
   */
  EReference getFunctionDeclaration_Arg();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getBody <em>Body</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Body</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getBody()
   * @see #getFunctionDeclaration()
   * @generated
   */
  EReference getFunctionDeclaration_Body();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getM <em>M</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>M</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration#getM()
   * @see #getFunctionDeclaration()
   * @generated
   */
  EReference getFunctionDeclaration_M();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentDeclaration <em>Function Argument Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Function Argument Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentDeclaration
   * @generated
   */
  EClass getFunctionArgumentDeclaration();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentDeclaration#getF <em>F</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>F</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentDeclaration#getF()
   * @see #getFunctionArgumentDeclaration()
   * @generated
   */
  EReference getFunctionArgumentDeclaration_F();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentBody <em>Function Argument Body</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Function Argument Body</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentBody
   * @generated
   */
  EClass getFunctionArgumentBody();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentBody#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentBody#getName()
   * @see #getFunctionArgumentBody()
   * @generated
   */
  EAttribute getFunctionArgumentBody_Name();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionCall <em>Function Call</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Function Call</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionCall
   * @generated
   */
  EClass getFunctionCall();

  /**
   * Returns the meta object for the reference '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionCall#getFunction <em>Function</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Function</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionCall#getFunction()
   * @see #getFunctionCall()
   * @generated
   */
  EReference getFunctionCall_Function();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionCall#getF <em>F</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>F</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionCall#getF()
   * @see #getFunctionCall()
   * @generated
   */
  EReference getFunctionCall_F();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FieldPath <em>Field Path</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Field Path</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldPath
   * @generated
   */
  EClass getFieldPath();

  /**
   * Returns the meta object for the reference '{@link org.makumba.devel.eclipse.mdd.MDD.FieldPath#getField <em>Field</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Field</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldPath#getField()
   * @see #getFieldPath()
   * @generated
   */
  EReference getFieldPath_Field();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FieldPath#getSubField <em>Sub Field</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Sub Field</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldPath#getSubField()
   * @see #getFieldPath()
   * @generated
   */
  EReference getFieldPath_SubField();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FieldReference <em>Field Reference</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Field Reference</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldReference
   * @generated
   */
  EClass getFieldReference();

  /**
   * Returns the meta object for the reference '{@link org.makumba.devel.eclipse.mdd.MDD.FieldReference#getField <em>Field</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Field</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldReference#getField()
   * @see #getFieldReference()
   * @generated
   */
  EReference getFieldReference_Field();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionArguments <em>Function Arguments</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Function Arguments</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionArguments
   * @generated
   */
  EClass getFunctionArguments();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionArguments#getArgs <em>Args</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Args</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionArguments#getArgs()
   * @see #getFunctionArguments()
   * @generated
   */
  EReference getFunctionArguments_Args();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionBody <em>Function Body</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Function Body</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionBody
   * @generated
   */
  EClass getFunctionBody();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionBody#getS <em>S</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>S</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionBody#getS()
   * @see #getFunctionBody()
   * @generated
   */
  EReference getFunctionBody_S();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionBody#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionBody#getE()
   * @see #getFunctionBody()
   * @generated
   */
  EReference getFunctionBody_E();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.Statement <em>Statement</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Statement</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Statement
   * @generated
   */
  EClass getStatement();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.UnionRule <em>Union Rule</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Union Rule</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.UnionRule
   * @generated
   */
  EClass getUnionRule();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.UnionRule#getQ <em>Q</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Q</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.UnionRule#getQ()
   * @see #getUnionRule()
   * @generated
   */
  EReference getUnionRule_Q();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.QueryRule <em>Query Rule</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Query Rule</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.QueryRule
   * @generated
   */
  EClass getQueryRule();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom <em>Select From</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Select From</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectFrom
   * @generated
   */
  EClass getSelectFrom();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getWhere <em>Where</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Where</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getWhere()
   * @see #getSelectFrom()
   * @generated
   */
  EReference getSelectFrom_Where();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getGroupBy <em>Group By</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Group By</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getGroupBy()
   * @see #getSelectFrom()
   * @generated
   */
  EReference getSelectFrom_GroupBy();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getOrderBy <em>Order By</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Order By</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getOrderBy()
   * @see #getSelectFrom()
   * @generated
   */
  EReference getSelectFrom_OrderBy();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getS <em>S</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>S</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getS()
   * @see #getSelectFrom()
   * @generated
   */
  EReference getSelectFrom_S();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getFrom <em>From</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>From</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectFrom#getFrom()
   * @see #getSelectFrom()
   * @generated
   */
  EReference getSelectFrom_From();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.SelectClause <em>Select Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Select Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectClause
   * @generated
   */
  EClass getSelectClause();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.SelectClause#getS <em>S</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>S</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectClause#getS()
   * @see #getSelectClause()
   * @generated
   */
  EReference getSelectClause_S();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.SelectClause#getN <em>N</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>N</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectClause#getN()
   * @see #getSelectClause()
   * @generated
   */
  EReference getSelectClause_N();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.NewExpression <em>New Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>New Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.NewExpression
   * @generated
   */
  EClass getNewExpression();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.NewExpression#getP <em>P</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>P</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.NewExpression#getP()
   * @see #getNewExpression()
   * @generated
   */
  EAttribute getNewExpression_P();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.NewExpression#getS <em>S</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>S</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.NewExpression#getS()
   * @see #getNewExpression()
   * @generated
   */
  EReference getNewExpression_S();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FromClause <em>From Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>From Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClause
   * @generated
   */
  EClass getFromClause();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.FromClause#getFromRange <em>From Range</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>From Range</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClause#getFromRange()
   * @see #getFromClause()
   * @generated
   */
  EReference getFromClause_FromRange();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.FromClause#getFromJoin <em>From Join</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>From Join</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClause#getFromJoin()
   * @see #getFromClause()
   * @generated
   */
  EReference getFromClause_FromJoin();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FromJoin <em>From Join</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>From Join</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromJoin
   * @generated
   */
  EClass getFromJoin();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.WithClause <em>With Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>With Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.WithClause
   * @generated
   */
  EClass getWithClause();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.WithClause#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.WithClause#getE()
   * @see #getWithClause()
   * @generated
   */
  EReference getWithClause_E();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FromRange <em>From Range</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>From Range</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromRange
   * @generated
   */
  EClass getFromRange();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FromRange#getAlias <em>Alias</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Alias</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromRange#getAlias()
   * @see #getFromRange()
   * @generated
   */
  EAttribute getFromRange_Alias();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FromRange#getP <em>P</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>P</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromRange#getP()
   * @see #getFromRange()
   * @generated
   */
  EAttribute getFromRange_P();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath <em>From Class Or Outer Query Path</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>From Class Or Outer Query Path</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath
   * @generated
   */
  EClass getFromClassOrOuterQueryPath();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getW <em>W</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>W</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getW()
   * @see #getFromClassOrOuterQueryPath()
   * @generated
   */
  EReference getFromClassOrOuterQueryPath_W();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getPath <em>Path</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Path</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getPath()
   * @see #getFromClassOrOuterQueryPath()
   * @generated
   */
  EAttribute getFromClassOrOuterQueryPath_Path();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getName()
   * @see #getFromClassOrOuterQueryPath()
   * @generated
   */
  EAttribute getFromClassOrOuterQueryPath_Name();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getPropertyFetch <em>Property Fetch</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Property Fetch</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath#getPropertyFetch()
   * @see #getFromClassOrOuterQueryPath()
   * @generated
   */
  EAttribute getFromClassOrOuterQueryPath_PropertyFetch();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.InCollectionElementsDeclaration <em>In Collection Elements Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>In Collection Elements Declaration</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.InCollectionElementsDeclaration
   * @generated
   */
  EClass getInCollectionElementsDeclaration();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.GroupByClause <em>Group By Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Group By Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.GroupByClause
   * @generated
   */
  EClass getGroupByClause();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.GroupByClause#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.GroupByClause#getE()
   * @see #getGroupByClause()
   * @generated
   */
  EReference getGroupByClause_E();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.GroupByClause#getH <em>H</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>H</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.GroupByClause#getH()
   * @see #getGroupByClause()
   * @generated
   */
  EReference getGroupByClause_H();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.OrderByClause <em>Order By Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Order By Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.OrderByClause
   * @generated
   */
  EClass getOrderByClause();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.OrderByClause#getO <em>O</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>O</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.OrderByClause#getO()
   * @see #getOrderByClause()
   * @generated
   */
  EReference getOrderByClause_O();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.OrderElement <em>Order Element</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Order Element</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.OrderElement
   * @generated
   */
  EClass getOrderElement();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.HavingClause <em>Having Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Having Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.HavingClause
   * @generated
   */
  EClass getHavingClause();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.HavingClause#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.HavingClause#getE()
   * @see #getHavingClause()
   * @generated
   */
  EReference getHavingClause_E();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.WhereClause <em>Where Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Where Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.WhereClause
   * @generated
   */
  EClass getWhereClause();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.WhereClause#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.WhereClause#getE()
   * @see #getWhereClause()
   * @generated
   */
  EReference getWhereClause_E();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.SelectedPropertiesList <em>Selected Properties List</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Selected Properties List</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectedPropertiesList
   * @generated
   */
  EClass getSelectedPropertiesList();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.SelectedPropertiesList#getA <em>A</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>A</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectedPropertiesList#getA()
   * @see #getSelectedPropertiesList()
   * @generated
   */
  EReference getSelectedPropertiesList_A();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.AliasedExpression <em>Aliased Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Aliased Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.AliasedExpression
   * @generated
   */
  EClass getAliasedExpression();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.Expression <em>Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Expression
   * @generated
   */
  EClass getExpression();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.Expression#getId <em>Id</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Id</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Expression#getId()
   * @see #getExpression()
   * @generated
   */
  EAttribute getExpression_Id();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.Expression#getV <em>V</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>V</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Expression#getV()
   * @see #getExpression()
   * @generated
   */
  EReference getExpression_V();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.LogicalOrExpression <em>Logical Or Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Logical Or Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.LogicalOrExpression
   * @generated
   */
  EClass getLogicalOrExpression();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.LogicalOrExpression#getL <em>L</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>L</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.LogicalOrExpression#getL()
   * @see #getLogicalOrExpression()
   * @generated
   */
  EReference getLogicalOrExpression_L();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.LogicalAndExpression <em>Logical And Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Logical And Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.LogicalAndExpression
   * @generated
   */
  EClass getLogicalAndExpression();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.LogicalAndExpression#getN <em>N</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>N</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.LogicalAndExpression#getN()
   * @see #getLogicalAndExpression()
   * @generated
   */
  EReference getLogicalAndExpression_N();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.NegatedExpression <em>Negated Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Negated Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.NegatedExpression
   * @generated
   */
  EClass getNegatedExpression();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.NegatedExpression#getN <em>N</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>N</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.NegatedExpression#getN()
   * @see #getNegatedExpression()
   * @generated
   */
  EReference getNegatedExpression_N();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.EqualityExpression <em>Equality Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Equality Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.EqualityExpression
   * @generated
   */
  EClass getEqualityExpression();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.EqualityExpression#getR <em>R</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>R</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.EqualityExpression#getR()
   * @see #getEqualityExpression()
   * @generated
   */
  EReference getEqualityExpression_R();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.RelationalExpression <em>Relational Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Relational Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.RelationalExpression
   * @generated
   */
  EClass getRelationalExpression();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.LikeEscape <em>Like Escape</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Like Escape</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.LikeEscape
   * @generated
   */
  EClass getLikeEscape();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.LikeEscape#getC <em>C</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>C</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.LikeEscape#getC()
   * @see #getLikeEscape()
   * @generated
   */
  EReference getLikeEscape_C();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.BetweenList <em>Between List</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Between List</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.BetweenList
   * @generated
   */
  EClass getBetweenList();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.BetweenList#getC <em>C</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>C</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.BetweenList#getC()
   * @see #getBetweenList()
   * @generated
   */
  EReference getBetweenList_C();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation <em>Concatenation</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Concatenation</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Concatenation
   * @generated
   */
  EClass getConcatenation();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getA <em>A</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>A</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Concatenation#getA()
   * @see #getConcatenation()
   * @generated
   */
  EReference getConcatenation_A();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getI <em>I</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>I</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Concatenation#getI()
   * @see #getConcatenation()
   * @generated
   */
  EReference getConcatenation_I();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getB <em>B</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>B</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Concatenation#getB()
   * @see #getConcatenation()
   * @generated
   */
  EReference getConcatenation_B();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getC <em>C</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>C</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Concatenation#getC()
   * @see #getConcatenation()
   * @generated
   */
  EReference getConcatenation_C();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getL <em>L</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>L</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Concatenation#getL()
   * @see #getConcatenation()
   * @generated
   */
  EReference getConcatenation_L();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation#getP <em>P</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>P</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Concatenation#getP()
   * @see #getConcatenation()
   * @generated
   */
  EAttribute getConcatenation_P();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.AdditiveExpression <em>Additive Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Additive Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.AdditiveExpression
   * @generated
   */
  EClass getAdditiveExpression();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.AdditiveExpression#getM <em>M</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>M</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.AdditiveExpression#getM()
   * @see #getAdditiveExpression()
   * @generated
   */
  EReference getAdditiveExpression_M();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.MultiplyExpression <em>Multiply Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Multiply Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.MultiplyExpression
   * @generated
   */
  EClass getMultiplyExpression();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.MultiplyExpression#getU <em>U</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>U</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.MultiplyExpression#getU()
   * @see #getMultiplyExpression()
   * @generated
   */
  EReference getMultiplyExpression_U();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.UnaryExpression <em>Unary Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Unary Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.UnaryExpression
   * @generated
   */
  EClass getUnaryExpression();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.UnaryExpression#getU <em>U</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>U</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.UnaryExpression#getU()
   * @see #getUnaryExpression()
   * @generated
   */
  EReference getUnaryExpression_U();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.CaseExpression <em>Case Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Case Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CaseExpression
   * @generated
   */
  EClass getCaseExpression();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.CaseExpression#getW <em>W</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>W</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CaseExpression#getW()
   * @see #getCaseExpression()
   * @generated
   */
  EReference getCaseExpression_W();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.CaseExpression#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CaseExpression#getE()
   * @see #getCaseExpression()
   * @generated
   */
  EReference getCaseExpression_E();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.CaseExpression#getA <em>A</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>A</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CaseExpression#getA()
   * @see #getCaseExpression()
   * @generated
   */
  EReference getCaseExpression_A();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.WhenClause <em>When Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>When Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.WhenClause
   * @generated
   */
  EClass getWhenClause();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.WhenClause#getWhenExpr <em>When Expr</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>When Expr</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.WhenClause#getWhenExpr()
   * @see #getWhenClause()
   * @generated
   */
  EReference getWhenClause_WhenExpr();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.WhenClause#getThenExpr <em>Then Expr</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Then Expr</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.WhenClause#getThenExpr()
   * @see #getWhenClause()
   * @generated
   */
  EReference getWhenClause_ThenExpr();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.AltWhenClause <em>Alt When Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Alt When Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.AltWhenClause
   * @generated
   */
  EClass getAltWhenClause();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.AltWhenClause#getW <em>W</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>W</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.AltWhenClause#getW()
   * @see #getAltWhenClause()
   * @generated
   */
  EReference getAltWhenClause_W();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.AltWhenClause#getT <em>T</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>T</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.AltWhenClause#getT()
   * @see #getAltWhenClause()
   * @generated
   */
  EReference getAltWhenClause_T();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.ElseClause <em>Else Clause</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Else Clause</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ElseClause
   * @generated
   */
  EClass getElseClause();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.ElseClause#getU <em>U</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>U</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ElseClause#getU()
   * @see #getElseClause()
   * @generated
   */
  EReference getElseClause_U();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.QuantifiedExpression <em>Quantified Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Quantified Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.QuantifiedExpression
   * @generated
   */
  EClass getQuantifiedExpression();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.QuantifiedExpression#getS <em>S</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>S</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.QuantifiedExpression#getS()
   * @see #getQuantifiedExpression()
   * @generated
   */
  EReference getQuantifiedExpression_S();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.Atom <em>Atom</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Atom</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Atom
   * @generated
   */
  EClass getAtom();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.Atom#getPrime <em>Prime</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Prime</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Atom#getPrime()
   * @see #getAtom()
   * @generated
   */
  EReference getAtom_Prime();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.Atom#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Atom#getE()
   * @see #getAtom()
   * @generated
   */
  EReference getAtom_E();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.Atom#getExp <em>Exp</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Exp</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Atom#getExp()
   * @see #getAtom()
   * @generated
   */
  EReference getAtom_Exp();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.PrimaryExpression <em>Primary Expression</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Primary Expression</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.PrimaryExpression
   * @generated
   */
  EClass getPrimaryExpression();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.ExpressionOrVector <em>Expression Or Vector</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expression Or Vector</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ExpressionOrVector
   * @generated
   */
  EClass getExpressionOrVector();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.VectorExpr <em>Vector Expr</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Vector Expr</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.VectorExpr
   * @generated
   */
  EClass getVectorExpr();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.VectorExpr#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.VectorExpr#getE()
   * @see #getVectorExpr()
   * @generated
   */
  EReference getVectorExpr_E();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.IdentPrimary <em>Ident Primary</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Ident Primary</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.IdentPrimary
   * @generated
   */
  EClass getIdentPrimary();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.IdentPrimary#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.IdentPrimary#getE()
   * @see #getIdentPrimary()
   * @generated
   */
  EReference getIdentPrimary_E();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.Aggregate <em>Aggregate</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Aggregate</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Aggregate
   * @generated
   */
  EClass getAggregate();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.Aggregate#getA <em>A</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>A</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.Aggregate#getA()
   * @see #getAggregate()
   * @generated
   */
  EReference getAggregate_A();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.CompoundExpr <em>Compound Expr</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Compound Expr</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CompoundExpr
   * @generated
   */
  EClass getCompoundExpr();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.CompoundExpr#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CompoundExpr#getE()
   * @see #getCompoundExpr()
   * @generated
   */
  EReference getCompoundExpr_E();

  /**
   * Returns the meta object for the containment reference '{@link org.makumba.devel.eclipse.mdd.MDD.CompoundExpr#getS <em>S</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>S</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.CompoundExpr#getS()
   * @see #getCompoundExpr()
   * @generated
   */
  EReference getCompoundExpr_S();

  /**
   * Returns the meta object for class '{@link org.makumba.devel.eclipse.mdd.MDD.ExprList <em>Expr List</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr List</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ExprList
   * @generated
   */
  EClass getExprList();

  /**
   * Returns the meta object for the containment reference list '{@link org.makumba.devel.eclipse.mdd.MDD.ExprList#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>E</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ExprList#getE()
   * @see #getExprList()
   * @generated
   */
  EReference getExprList_E();

  /**
   * Returns the meta object for the attribute '{@link org.makumba.devel.eclipse.mdd.MDD.ExprList#getId <em>Id</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Id</em>'.
   * @see org.makumba.devel.eclipse.mdd.MDD.ExprList#getId()
   * @see #getExprList()
   * @generated
   */
  EAttribute getExprList_Id();

  /**
   * Returns the factory that creates the instances of the model.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the factory that creates the instances of the model.
   * @generated
   */
  MDDFactory getMDDFactory();

  /**
   * <!-- begin-user-doc -->
   * Defines literals for the meta objects that represent
   * <ul>
   *   <li>each class,</li>
   *   <li>each feature of each class,</li>
   *   <li>each enum,</li>
   *   <li>and each data type</li>
   * </ul>
   * <!-- end-user-doc -->
   * @generated
   */
  interface Literals
  {
    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.DataDefinitionImpl <em>Data Definition</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.DataDefinitionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getDataDefinition()
     * @generated
     */
    EClass DATA_DEFINITION = eINSTANCE.getDataDefinition();

    /**
     * The meta object literal for the '<em><b>D</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference DATA_DEFINITION__D = eINSTANCE.getDataDefinition_D();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.DeclarationImpl <em>Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.DeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getDeclaration()
     * @generated
     */
    EClass DECLARATION = eINSTANCE.getDeclaration();

    /**
     * The meta object literal for the '<em><b>Field Comment</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute DECLARATION__FIELD_COMMENT = eINSTANCE.getDeclaration_FieldComment();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldDeclarationImpl <em>Field Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FieldDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFieldDeclaration()
     * @generated
     */
    EClass FIELD_DECLARATION = eINSTANCE.getFieldDeclaration();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FIELD_DECLARATION__NAME = eINSTANCE.getFieldDeclaration_Name();

    /**
     * The meta object literal for the '<em><b>Modifiers</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FIELD_DECLARATION__MODIFIERS = eINSTANCE.getFieldDeclaration_Modifiers();

    /**
     * The meta object literal for the '<em><b>Typedef</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FIELD_DECLARATION__TYPEDEF = eINSTANCE.getFieldDeclaration_Typedef();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ModifiersImpl <em>Modifiers</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ModifiersImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getModifiers()
     * @generated
     */
    EClass MODIFIERS = eINSTANCE.getModifiers();

    /**
     * The meta object literal for the '<em><b>Unique</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute MODIFIERS__UNIQUE = eINSTANCE.getModifiers_Unique();

    /**
     * The meta object literal for the '<em><b>Fixed</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute MODIFIERS__FIXED = eINSTANCE.getModifiers_Fixed();

    /**
     * The meta object literal for the '<em><b>Not Null</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute MODIFIERS__NOT_NULL = eINSTANCE.getModifiers_NotNull();

    /**
     * The meta object literal for the '<em><b>Not Empty</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute MODIFIERS__NOT_EMPTY = eINSTANCE.getModifiers_NotEmpty();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldTypeImpl <em>Field Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FieldTypeImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFieldType()
     * @generated
     */
    EClass FIELD_TYPE = eINSTANCE.getFieldType();

    /**
     * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FIELD_TYPE__TYPE = eINSTANCE.getFieldType_Type();

    /**
     * The meta object literal for the '<em><b>Type Dec</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FIELD_TYPE__TYPE_DEC = eINSTANCE.getFieldType_TypeDec();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.IntEnumImpl <em>Int Enum</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.IntEnumImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getIntEnum()
     * @generated
     */
    EClass INT_ENUM = eINSTANCE.getIntEnum();

    /**
     * The meta object literal for the '<em><b>Values</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference INT_ENUM__VALUES = eINSTANCE.getIntEnum_Values();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.CharEnumImpl <em>Char Enum</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.CharEnumImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getCharEnum()
     * @generated
     */
    EClass CHAR_ENUM = eINSTANCE.getCharEnum();

    /**
     * The meta object literal for the '<em><b>Values</b></em>' attribute list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute CHAR_ENUM__VALUES = eINSTANCE.getCharEnum_Values();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.EnumValueImpl <em>Enum Value</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.EnumValueImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getEnumValue()
     * @generated
     */
    EClass ENUM_VALUE = eINSTANCE.getEnumValue();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute ENUM_VALUE__NAME = eINSTANCE.getEnumValue_Name();

    /**
     * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute ENUM_VALUE__VALUE = eINSTANCE.getEnumValue_Value();

    /**
     * The meta object literal for the '<em><b>Decpricated</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute ENUM_VALUE__DECPRICATED = eINSTANCE.getEnumValue_Decpricated();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.CharTypeImpl <em>Char Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.CharTypeImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getCharType()
     * @generated
     */
    EClass CHAR_TYPE = eINSTANCE.getCharType();

    /**
     * The meta object literal for the '<em><b>Length</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute CHAR_TYPE__LENGTH = eINSTANCE.getCharType_Length();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.PointerTypeImpl <em>Pointer Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.PointerTypeImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getPointerType()
     * @generated
     */
    EClass POINTER_TYPE = eINSTANCE.getPointerType();

    /**
     * The meta object literal for the '<em><b>Ref</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference POINTER_TYPE__REF = eINSTANCE.getPointerType_Ref();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SetTypeImpl <em>Set Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.SetTypeImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSetType()
     * @generated
     */
    EClass SET_TYPE = eINSTANCE.getSetType();

    /**
     * The meta object literal for the '<em><b>Ref</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SET_TYPE__REF = eINSTANCE.getSetType_Ref();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SubFieldDeclarationImpl <em>Sub Field Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.SubFieldDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSubFieldDeclaration()
     * @generated
     */
    EClass SUB_FIELD_DECLARATION = eINSTANCE.getSubFieldDeclaration();

    /**
     * The meta object literal for the '<em><b>Sub Field Of</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SUB_FIELD_DECLARATION__SUB_FIELD_OF = eINSTANCE.getSubFieldDeclaration_SubFieldOf();

    /**
     * The meta object literal for the '<em><b>D</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SUB_FIELD_DECLARATION__D = eINSTANCE.getSubFieldDeclaration_D();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.TitleDeclarationImpl <em>Title Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.TitleDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getTitleDeclaration()
     * @generated
     */
    EClass TITLE_DECLARATION = eINSTANCE.getTitleDeclaration();

    /**
     * The meta object literal for the '<em><b>Field</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference TITLE_DECLARATION__FIELD = eINSTANCE.getTitleDeclaration_Field();

    /**
     * The meta object literal for the '<em><b>Function</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference TITLE_DECLARATION__FUNCTION = eINSTANCE.getTitleDeclaration_Function();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.IncludeDeclarationImpl <em>Include Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.IncludeDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getIncludeDeclaration()
     * @generated
     */
    EClass INCLUDE_DECLARATION = eINSTANCE.getIncludeDeclaration();

    /**
     * The meta object literal for the '<em><b>Imported Namespace</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference INCLUDE_DECLARATION__IMPORTED_NAMESPACE = eINSTANCE.getIncludeDeclaration_ImportedNamespace();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.TypeDeclarationImpl <em>Type Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.TypeDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getTypeDeclaration()
     * @generated
     */
    EClass TYPE_DECLARATION = eINSTANCE.getTypeDeclaration();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute TYPE_DECLARATION__NAME = eINSTANCE.getTypeDeclaration_Name();

    /**
     * The meta object literal for the '<em><b>Field Type</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference TYPE_DECLARATION__FIELD_TYPE = eINSTANCE.getTypeDeclaration_FieldType();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ValidationRuleDeclarationImpl <em>Validation Rule Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ValidationRuleDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getValidationRuleDeclaration()
     * @generated
     */
    EClass VALIDATION_RULE_DECLARATION = eINSTANCE.getValidationRuleDeclaration();

    /**
     * The meta object literal for the '<em><b>Error Message</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference VALIDATION_RULE_DECLARATION__ERROR_MESSAGE = eINSTANCE.getValidationRuleDeclaration_ErrorMessage();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute VALIDATION_RULE_DECLARATION__NAME = eINSTANCE.getValidationRuleDeclaration_Name();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonValidationRuleDeclarationImpl <em>Comparison Validation Rule Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonValidationRuleDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getComparisonValidationRuleDeclaration()
     * @generated
     */
    EClass COMPARISON_VALIDATION_RULE_DECLARATION = eINSTANCE.getComparisonValidationRuleDeclaration();

    /**
     * The meta object literal for the '<em><b>Args</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMPARISON_VALIDATION_RULE_DECLARATION__ARGS = eINSTANCE.getComparisonValidationRuleDeclaration_Args();

    /**
     * The meta object literal for the '<em><b>Comparison Exp</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMPARISON_VALIDATION_RULE_DECLARATION__COMPARISON_EXP = eINSTANCE.getComparisonValidationRuleDeclaration_ComparisonExp();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonExpressionImpl <em>Comparison Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getComparisonExpression()
     * @generated
     */
    EClass COMPARISON_EXPRESSION = eINSTANCE.getComparisonExpression();

    /**
     * The meta object literal for the '<em><b>Lhs</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMPARISON_EXPRESSION__LHS = eINSTANCE.getComparisonExpression_Lhs();

    /**
     * The meta object literal for the '<em><b>O</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute COMPARISON_EXPRESSION__O = eINSTANCE.getComparisonExpression_O();

    /**
     * The meta object literal for the '<em><b>Rhs</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMPARISON_EXPRESSION__RHS = eINSTANCE.getComparisonExpression_Rhs();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonPartImpl <em>Comparison Part</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ComparisonPartImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getComparisonPart()
     * @generated
     */
    EClass COMPARISON_PART = eINSTANCE.getComparisonPart();

    /**
     * The meta object literal for the '<em><b>Field</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMPARISON_PART__FIELD = eINSTANCE.getComparisonPart_Field();

    /**
     * The meta object literal for the '<em><b>N</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute COMPARISON_PART__N = eINSTANCE.getComparisonPart_N();

    /**
     * The meta object literal for the '<em><b>Df</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute COMPARISON_PART__DF = eINSTANCE.getComparisonPart_Df();

    /**
     * The meta object literal for the '<em><b>U</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMPARISON_PART__U = eINSTANCE.getComparisonPart_U();

    /**
     * The meta object literal for the '<em><b>L</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMPARISON_PART__L = eINSTANCE.getComparisonPart_L();

    /**
     * The meta object literal for the '<em><b>D</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute COMPARISON_PART__D = eINSTANCE.getComparisonPart_D();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.UpperFunctionImpl <em>Upper Function</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.UpperFunctionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getUpperFunction()
     * @generated
     */
    EClass UPPER_FUNCTION = eINSTANCE.getUpperFunction();

    /**
     * The meta object literal for the '<em><b>Field</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference UPPER_FUNCTION__FIELD = eINSTANCE.getUpperFunction_Field();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.LowerFunctionImpl <em>Lower Function</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.LowerFunctionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getLowerFunction()
     * @generated
     */
    EClass LOWER_FUNCTION = eINSTANCE.getLowerFunction();

    /**
     * The meta object literal for the '<em><b>Field</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference LOWER_FUNCTION__FIELD = eINSTANCE.getLowerFunction_Field();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.RangeValidationRuleDeclarationImpl <em>Range Validation Rule Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.RangeValidationRuleDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getRangeValidationRuleDeclaration()
     * @generated
     */
    EClass RANGE_VALIDATION_RULE_DECLARATION = eINSTANCE.getRangeValidationRuleDeclaration();

    /**
     * The meta object literal for the '<em><b>Arg</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference RANGE_VALIDATION_RULE_DECLARATION__ARG = eINSTANCE.getRangeValidationRuleDeclaration_Arg();

    /**
     * The meta object literal for the '<em><b>Range</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference RANGE_VALIDATION_RULE_DECLARATION__RANGE = eINSTANCE.getRangeValidationRuleDeclaration_Range();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.RegexValidationRuleDeclarationImpl <em>Regex Validation Rule Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.RegexValidationRuleDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getRegexValidationRuleDeclaration()
     * @generated
     */
    EClass REGEX_VALIDATION_RULE_DECLARATION = eINSTANCE.getRegexValidationRuleDeclaration();

    /**
     * The meta object literal for the '<em><b>Arg</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference REGEX_VALIDATION_RULE_DECLARATION__ARG = eINSTANCE.getRegexValidationRuleDeclaration_Arg();

    /**
     * The meta object literal for the '<em><b>Exp</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute REGEX_VALIDATION_RULE_DECLARATION__EXP = eINSTANCE.getRegexValidationRuleDeclaration_Exp();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.RangeImpl <em>Range</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.RangeImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getRange()
     * @generated
     */
    EClass RANGE = eINSTANCE.getRange();

    /**
     * The meta object literal for the '<em><b>F</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute RANGE__F = eINSTANCE.getRange_F();

    /**
     * The meta object literal for the '<em><b>T</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute RANGE__T = eINSTANCE.getRange_T();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.UniquenessValidationRuleDeclarationImpl <em>Uniqueness Validation Rule Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.UniquenessValidationRuleDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getUniquenessValidationRuleDeclaration()
     * @generated
     */
    EClass UNIQUENESS_VALIDATION_RULE_DECLARATION = eINSTANCE.getUniquenessValidationRuleDeclaration();

    /**
     * The meta object literal for the '<em><b>Args</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference UNIQUENESS_VALIDATION_RULE_DECLARATION__ARGS = eINSTANCE.getUniquenessValidationRuleDeclaration_Args();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ErrorMessageImpl <em>Error Message</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ErrorMessageImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getErrorMessage()
     * @generated
     */
    EClass ERROR_MESSAGE = eINSTANCE.getErrorMessage();

    /**
     * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute ERROR_MESSAGE__MESSAGE = eINSTANCE.getErrorMessage_Message();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.NativeValidationRuleDeclarationImpl <em>Native Validation Rule Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.NativeValidationRuleDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getNativeValidationRuleDeclaration()
     * @generated
     */
    EClass NATIVE_VALIDATION_RULE_DECLARATION = eINSTANCE.getNativeValidationRuleDeclaration();

    /**
     * The meta object literal for the '<em><b>Field</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference NATIVE_VALIDATION_RULE_DECLARATION__FIELD = eINSTANCE.getNativeValidationRuleDeclaration_Field();

    /**
     * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute NATIVE_VALIDATION_RULE_DECLARATION__TYPE = eINSTANCE.getNativeValidationRuleDeclaration_Type();

    /**
     * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute NATIVE_VALIDATION_RULE_DECLARATION__MESSAGE = eINSTANCE.getNativeValidationRuleDeclaration_Message();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionDeclarationImpl <em>Function Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionDeclaration()
     * @generated
     */
    EClass FUNCTION_DECLARATION = eINSTANCE.getFunctionDeclaration();

    /**
     * The meta object literal for the '<em><b>B</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FUNCTION_DECLARATION__B = eINSTANCE.getFunctionDeclaration_B();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FUNCTION_DECLARATION__NAME = eINSTANCE.getFunctionDeclaration_Name();

    /**
     * The meta object literal for the '<em><b>Arg</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_DECLARATION__ARG = eINSTANCE.getFunctionDeclaration_Arg();

    /**
     * The meta object literal for the '<em><b>Body</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_DECLARATION__BODY = eINSTANCE.getFunctionDeclaration_Body();

    /**
     * The meta object literal for the '<em><b>M</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_DECLARATION__M = eINSTANCE.getFunctionDeclaration_M();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentDeclarationImpl <em>Function Argument Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionArgumentDeclaration()
     * @generated
     */
    EClass FUNCTION_ARGUMENT_DECLARATION = eINSTANCE.getFunctionArgumentDeclaration();

    /**
     * The meta object literal for the '<em><b>F</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_ARGUMENT_DECLARATION__F = eINSTANCE.getFunctionArgumentDeclaration_F();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentBodyImpl <em>Function Argument Body</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentBodyImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionArgumentBody()
     * @generated
     */
    EClass FUNCTION_ARGUMENT_BODY = eINSTANCE.getFunctionArgumentBody();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FUNCTION_ARGUMENT_BODY__NAME = eINSTANCE.getFunctionArgumentBody_Name();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionCallImpl <em>Function Call</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionCallImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionCall()
     * @generated
     */
    EClass FUNCTION_CALL = eINSTANCE.getFunctionCall();

    /**
     * The meta object literal for the '<em><b>Function</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_CALL__FUNCTION = eINSTANCE.getFunctionCall_Function();

    /**
     * The meta object literal for the '<em><b>F</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_CALL__F = eINSTANCE.getFunctionCall_F();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldPathImpl <em>Field Path</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FieldPathImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFieldPath()
     * @generated
     */
    EClass FIELD_PATH = eINSTANCE.getFieldPath();

    /**
     * The meta object literal for the '<em><b>Field</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FIELD_PATH__FIELD = eINSTANCE.getFieldPath_Field();

    /**
     * The meta object literal for the '<em><b>Sub Field</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FIELD_PATH__SUB_FIELD = eINSTANCE.getFieldPath_SubField();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FieldReferenceImpl <em>Field Reference</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FieldReferenceImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFieldReference()
     * @generated
     */
    EClass FIELD_REFERENCE = eINSTANCE.getFieldReference();

    /**
     * The meta object literal for the '<em><b>Field</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FIELD_REFERENCE__FIELD = eINSTANCE.getFieldReference_Field();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentsImpl <em>Function Arguments</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionArgumentsImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionArguments()
     * @generated
     */
    EClass FUNCTION_ARGUMENTS = eINSTANCE.getFunctionArguments();

    /**
     * The meta object literal for the '<em><b>Args</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_ARGUMENTS__ARGS = eINSTANCE.getFunctionArguments_Args();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FunctionBodyImpl <em>Function Body</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FunctionBodyImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFunctionBody()
     * @generated
     */
    EClass FUNCTION_BODY = eINSTANCE.getFunctionBody();

    /**
     * The meta object literal for the '<em><b>S</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_BODY__S = eINSTANCE.getFunctionBody_S();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_BODY__E = eINSTANCE.getFunctionBody_E();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.StatementImpl <em>Statement</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.StatementImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getStatement()
     * @generated
     */
    EClass STATEMENT = eINSTANCE.getStatement();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.UnionRuleImpl <em>Union Rule</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.UnionRuleImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getUnionRule()
     * @generated
     */
    EClass UNION_RULE = eINSTANCE.getUnionRule();

    /**
     * The meta object literal for the '<em><b>Q</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference UNION_RULE__Q = eINSTANCE.getUnionRule_Q();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.QueryRuleImpl <em>Query Rule</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.QueryRuleImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getQueryRule()
     * @generated
     */
    EClass QUERY_RULE = eINSTANCE.getQueryRule();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectFromImpl <em>Select From</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.SelectFromImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSelectFrom()
     * @generated
     */
    EClass SELECT_FROM = eINSTANCE.getSelectFrom();

    /**
     * The meta object literal for the '<em><b>Where</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SELECT_FROM__WHERE = eINSTANCE.getSelectFrom_Where();

    /**
     * The meta object literal for the '<em><b>Group By</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SELECT_FROM__GROUP_BY = eINSTANCE.getSelectFrom_GroupBy();

    /**
     * The meta object literal for the '<em><b>Order By</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SELECT_FROM__ORDER_BY = eINSTANCE.getSelectFrom_OrderBy();

    /**
     * The meta object literal for the '<em><b>S</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SELECT_FROM__S = eINSTANCE.getSelectFrom_S();

    /**
     * The meta object literal for the '<em><b>From</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SELECT_FROM__FROM = eINSTANCE.getSelectFrom_From();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectClauseImpl <em>Select Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.SelectClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSelectClause()
     * @generated
     */
    EClass SELECT_CLAUSE = eINSTANCE.getSelectClause();

    /**
     * The meta object literal for the '<em><b>S</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SELECT_CLAUSE__S = eINSTANCE.getSelectClause_S();

    /**
     * The meta object literal for the '<em><b>N</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SELECT_CLAUSE__N = eINSTANCE.getSelectClause_N();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.NewExpressionImpl <em>New Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.NewExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getNewExpression()
     * @generated
     */
    EClass NEW_EXPRESSION = eINSTANCE.getNewExpression();

    /**
     * The meta object literal for the '<em><b>P</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute NEW_EXPRESSION__P = eINSTANCE.getNewExpression_P();

    /**
     * The meta object literal for the '<em><b>S</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference NEW_EXPRESSION__S = eINSTANCE.getNewExpression_S();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClauseImpl <em>From Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FromClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFromClause()
     * @generated
     */
    EClass FROM_CLAUSE = eINSTANCE.getFromClause();

    /**
     * The meta object literal for the '<em><b>From Range</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FROM_CLAUSE__FROM_RANGE = eINSTANCE.getFromClause_FromRange();

    /**
     * The meta object literal for the '<em><b>From Join</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FROM_CLAUSE__FROM_JOIN = eINSTANCE.getFromClause_FromJoin();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromJoinImpl <em>From Join</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FromJoinImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFromJoin()
     * @generated
     */
    EClass FROM_JOIN = eINSTANCE.getFromJoin();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.WithClauseImpl <em>With Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.WithClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getWithClause()
     * @generated
     */
    EClass WITH_CLAUSE = eINSTANCE.getWithClause();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference WITH_CLAUSE__E = eINSTANCE.getWithClause_E();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromRangeImpl <em>From Range</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FromRangeImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFromRange()
     * @generated
     */
    EClass FROM_RANGE = eINSTANCE.getFromRange();

    /**
     * The meta object literal for the '<em><b>Alias</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FROM_RANGE__ALIAS = eINSTANCE.getFromRange_Alias();

    /**
     * The meta object literal for the '<em><b>P</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FROM_RANGE__P = eINSTANCE.getFromRange_P();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl <em>From Class Or Outer Query Path</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.FromClassOrOuterQueryPathImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getFromClassOrOuterQueryPath()
     * @generated
     */
    EClass FROM_CLASS_OR_OUTER_QUERY_PATH = eINSTANCE.getFromClassOrOuterQueryPath();

    /**
     * The meta object literal for the '<em><b>W</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FROM_CLASS_OR_OUTER_QUERY_PATH__W = eINSTANCE.getFromClassOrOuterQueryPath_W();

    /**
     * The meta object literal for the '<em><b>Path</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FROM_CLASS_OR_OUTER_QUERY_PATH__PATH = eINSTANCE.getFromClassOrOuterQueryPath_Path();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FROM_CLASS_OR_OUTER_QUERY_PATH__NAME = eINSTANCE.getFromClassOrOuterQueryPath_Name();

    /**
     * The meta object literal for the '<em><b>Property Fetch</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FROM_CLASS_OR_OUTER_QUERY_PATH__PROPERTY_FETCH = eINSTANCE.getFromClassOrOuterQueryPath_PropertyFetch();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.InCollectionElementsDeclarationImpl <em>In Collection Elements Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.InCollectionElementsDeclarationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getInCollectionElementsDeclaration()
     * @generated
     */
    EClass IN_COLLECTION_ELEMENTS_DECLARATION = eINSTANCE.getInCollectionElementsDeclaration();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.GroupByClauseImpl <em>Group By Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.GroupByClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getGroupByClause()
     * @generated
     */
    EClass GROUP_BY_CLAUSE = eINSTANCE.getGroupByClause();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference GROUP_BY_CLAUSE__E = eINSTANCE.getGroupByClause_E();

    /**
     * The meta object literal for the '<em><b>H</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference GROUP_BY_CLAUSE__H = eINSTANCE.getGroupByClause_H();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.OrderByClauseImpl <em>Order By Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.OrderByClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getOrderByClause()
     * @generated
     */
    EClass ORDER_BY_CLAUSE = eINSTANCE.getOrderByClause();

    /**
     * The meta object literal for the '<em><b>O</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference ORDER_BY_CLAUSE__O = eINSTANCE.getOrderByClause_O();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.OrderElementImpl <em>Order Element</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.OrderElementImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getOrderElement()
     * @generated
     */
    EClass ORDER_ELEMENT = eINSTANCE.getOrderElement();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.HavingClauseImpl <em>Having Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.HavingClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getHavingClause()
     * @generated
     */
    EClass HAVING_CLAUSE = eINSTANCE.getHavingClause();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference HAVING_CLAUSE__E = eINSTANCE.getHavingClause_E();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.WhereClauseImpl <em>Where Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.WhereClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getWhereClause()
     * @generated
     */
    EClass WHERE_CLAUSE = eINSTANCE.getWhereClause();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference WHERE_CLAUSE__E = eINSTANCE.getWhereClause_E();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.SelectedPropertiesListImpl <em>Selected Properties List</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.SelectedPropertiesListImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getSelectedPropertiesList()
     * @generated
     */
    EClass SELECTED_PROPERTIES_LIST = eINSTANCE.getSelectedPropertiesList();

    /**
     * The meta object literal for the '<em><b>A</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference SELECTED_PROPERTIES_LIST__A = eINSTANCE.getSelectedPropertiesList_A();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AliasedExpressionImpl <em>Aliased Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.AliasedExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAliasedExpression()
     * @generated
     */
    EClass ALIASED_EXPRESSION = eINSTANCE.getAliasedExpression();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ExpressionImpl <em>Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getExpression()
     * @generated
     */
    EClass EXPRESSION = eINSTANCE.getExpression();

    /**
     * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPRESSION__ID = eINSTANCE.getExpression_Id();

    /**
     * The meta object literal for the '<em><b>V</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPRESSION__V = eINSTANCE.getExpression_V();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.LogicalOrExpressionImpl <em>Logical Or Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.LogicalOrExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getLogicalOrExpression()
     * @generated
     */
    EClass LOGICAL_OR_EXPRESSION = eINSTANCE.getLogicalOrExpression();

    /**
     * The meta object literal for the '<em><b>L</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference LOGICAL_OR_EXPRESSION__L = eINSTANCE.getLogicalOrExpression_L();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.LogicalAndExpressionImpl <em>Logical And Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.LogicalAndExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getLogicalAndExpression()
     * @generated
     */
    EClass LOGICAL_AND_EXPRESSION = eINSTANCE.getLogicalAndExpression();

    /**
     * The meta object literal for the '<em><b>N</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference LOGICAL_AND_EXPRESSION__N = eINSTANCE.getLogicalAndExpression_N();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.NegatedExpressionImpl <em>Negated Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.NegatedExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getNegatedExpression()
     * @generated
     */
    EClass NEGATED_EXPRESSION = eINSTANCE.getNegatedExpression();

    /**
     * The meta object literal for the '<em><b>N</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference NEGATED_EXPRESSION__N = eINSTANCE.getNegatedExpression_N();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.EqualityExpressionImpl <em>Equality Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.EqualityExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getEqualityExpression()
     * @generated
     */
    EClass EQUALITY_EXPRESSION = eINSTANCE.getEqualityExpression();

    /**
     * The meta object literal for the '<em><b>R</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EQUALITY_EXPRESSION__R = eINSTANCE.getEqualityExpression_R();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.RelationalExpressionImpl <em>Relational Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.RelationalExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getRelationalExpression()
     * @generated
     */
    EClass RELATIONAL_EXPRESSION = eINSTANCE.getRelationalExpression();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.LikeEscapeImpl <em>Like Escape</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.LikeEscapeImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getLikeEscape()
     * @generated
     */
    EClass LIKE_ESCAPE = eINSTANCE.getLikeEscape();

    /**
     * The meta object literal for the '<em><b>C</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference LIKE_ESCAPE__C = eINSTANCE.getLikeEscape_C();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.BetweenListImpl <em>Between List</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.BetweenListImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getBetweenList()
     * @generated
     */
    EClass BETWEEN_LIST = eINSTANCE.getBetweenList();

    /**
     * The meta object literal for the '<em><b>C</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference BETWEEN_LIST__C = eINSTANCE.getBetweenList_C();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl <em>Concatenation</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ConcatenationImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getConcatenation()
     * @generated
     */
    EClass CONCATENATION = eINSTANCE.getConcatenation();

    /**
     * The meta object literal for the '<em><b>A</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CONCATENATION__A = eINSTANCE.getConcatenation_A();

    /**
     * The meta object literal for the '<em><b>I</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CONCATENATION__I = eINSTANCE.getConcatenation_I();

    /**
     * The meta object literal for the '<em><b>B</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CONCATENATION__B = eINSTANCE.getConcatenation_B();

    /**
     * The meta object literal for the '<em><b>C</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CONCATENATION__C = eINSTANCE.getConcatenation_C();

    /**
     * The meta object literal for the '<em><b>L</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CONCATENATION__L = eINSTANCE.getConcatenation_L();

    /**
     * The meta object literal for the '<em><b>P</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute CONCATENATION__P = eINSTANCE.getConcatenation_P();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AdditiveExpressionImpl <em>Additive Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.AdditiveExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAdditiveExpression()
     * @generated
     */
    EClass ADDITIVE_EXPRESSION = eINSTANCE.getAdditiveExpression();

    /**
     * The meta object literal for the '<em><b>M</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference ADDITIVE_EXPRESSION__M = eINSTANCE.getAdditiveExpression_M();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.MultiplyExpressionImpl <em>Multiply Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MultiplyExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getMultiplyExpression()
     * @generated
     */
    EClass MULTIPLY_EXPRESSION = eINSTANCE.getMultiplyExpression();

    /**
     * The meta object literal for the '<em><b>U</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference MULTIPLY_EXPRESSION__U = eINSTANCE.getMultiplyExpression_U();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.UnaryExpressionImpl <em>Unary Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.UnaryExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getUnaryExpression()
     * @generated
     */
    EClass UNARY_EXPRESSION = eINSTANCE.getUnaryExpression();

    /**
     * The meta object literal for the '<em><b>U</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference UNARY_EXPRESSION__U = eINSTANCE.getUnaryExpression_U();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.CaseExpressionImpl <em>Case Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.CaseExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getCaseExpression()
     * @generated
     */
    EClass CASE_EXPRESSION = eINSTANCE.getCaseExpression();

    /**
     * The meta object literal for the '<em><b>W</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CASE_EXPRESSION__W = eINSTANCE.getCaseExpression_W();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CASE_EXPRESSION__E = eINSTANCE.getCaseExpression_E();

    /**
     * The meta object literal for the '<em><b>A</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CASE_EXPRESSION__A = eINSTANCE.getCaseExpression_A();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.WhenClauseImpl <em>When Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.WhenClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getWhenClause()
     * @generated
     */
    EClass WHEN_CLAUSE = eINSTANCE.getWhenClause();

    /**
     * The meta object literal for the '<em><b>When Expr</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference WHEN_CLAUSE__WHEN_EXPR = eINSTANCE.getWhenClause_WhenExpr();

    /**
     * The meta object literal for the '<em><b>Then Expr</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference WHEN_CLAUSE__THEN_EXPR = eINSTANCE.getWhenClause_ThenExpr();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AltWhenClauseImpl <em>Alt When Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.AltWhenClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAltWhenClause()
     * @generated
     */
    EClass ALT_WHEN_CLAUSE = eINSTANCE.getAltWhenClause();

    /**
     * The meta object literal for the '<em><b>W</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference ALT_WHEN_CLAUSE__W = eINSTANCE.getAltWhenClause_W();

    /**
     * The meta object literal for the '<em><b>T</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference ALT_WHEN_CLAUSE__T = eINSTANCE.getAltWhenClause_T();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ElseClauseImpl <em>Else Clause</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ElseClauseImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getElseClause()
     * @generated
     */
    EClass ELSE_CLAUSE = eINSTANCE.getElseClause();

    /**
     * The meta object literal for the '<em><b>U</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference ELSE_CLAUSE__U = eINSTANCE.getElseClause_U();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.QuantifiedExpressionImpl <em>Quantified Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.QuantifiedExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getQuantifiedExpression()
     * @generated
     */
    EClass QUANTIFIED_EXPRESSION = eINSTANCE.getQuantifiedExpression();

    /**
     * The meta object literal for the '<em><b>S</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference QUANTIFIED_EXPRESSION__S = eINSTANCE.getQuantifiedExpression_S();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AtomImpl <em>Atom</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.AtomImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAtom()
     * @generated
     */
    EClass ATOM = eINSTANCE.getAtom();

    /**
     * The meta object literal for the '<em><b>Prime</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference ATOM__PRIME = eINSTANCE.getAtom_Prime();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference ATOM__E = eINSTANCE.getAtom_E();

    /**
     * The meta object literal for the '<em><b>Exp</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference ATOM__EXP = eINSTANCE.getAtom_Exp();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.PrimaryExpressionImpl <em>Primary Expression</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.PrimaryExpressionImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getPrimaryExpression()
     * @generated
     */
    EClass PRIMARY_EXPRESSION = eINSTANCE.getPrimaryExpression();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ExpressionOrVectorImpl <em>Expression Or Vector</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ExpressionOrVectorImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getExpressionOrVector()
     * @generated
     */
    EClass EXPRESSION_OR_VECTOR = eINSTANCE.getExpressionOrVector();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.VectorExprImpl <em>Vector Expr</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.VectorExprImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getVectorExpr()
     * @generated
     */
    EClass VECTOR_EXPR = eINSTANCE.getVectorExpr();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference VECTOR_EXPR__E = eINSTANCE.getVectorExpr_E();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.IdentPrimaryImpl <em>Ident Primary</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.IdentPrimaryImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getIdentPrimary()
     * @generated
     */
    EClass IDENT_PRIMARY = eINSTANCE.getIdentPrimary();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference IDENT_PRIMARY__E = eINSTANCE.getIdentPrimary_E();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.AggregateImpl <em>Aggregate</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.AggregateImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getAggregate()
     * @generated
     */
    EClass AGGREGATE = eINSTANCE.getAggregate();

    /**
     * The meta object literal for the '<em><b>A</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference AGGREGATE__A = eINSTANCE.getAggregate_A();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.CompoundExprImpl <em>Compound Expr</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.CompoundExprImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getCompoundExpr()
     * @generated
     */
    EClass COMPOUND_EXPR = eINSTANCE.getCompoundExpr();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMPOUND_EXPR__E = eINSTANCE.getCompoundExpr_E();

    /**
     * The meta object literal for the '<em><b>S</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMPOUND_EXPR__S = eINSTANCE.getCompoundExpr_S();

    /**
     * The meta object literal for the '{@link org.makumba.devel.eclipse.mdd.MDD.impl.ExprListImpl <em>Expr List</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.ExprListImpl
     * @see org.makumba.devel.eclipse.mdd.MDD.impl.MDDPackageImpl#getExprList()
     * @generated
     */
    EClass EXPR_LIST = eINSTANCE.getExprList();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_LIST__E = eINSTANCE.getExprList_E();

    /**
     * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_LIST__ID = eINSTANCE.getExprList_Id();

  }

} //MDDPackage

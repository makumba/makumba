/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage
 * @generated
 */
public interface MDDFactory extends EFactory
{
  /**
   * The singleton instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  MDDFactory eINSTANCE = org.makumba.devel.eclipse.mdd.MDD.impl.MDDFactoryImpl.init();

  /**
   * Returns a new object of class '<em>Data Definition</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Data Definition</em>'.
   * @generated
   */
  DataDefinition createDataDefinition();

  /**
   * Returns a new object of class '<em>Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Declaration</em>'.
   * @generated
   */
  Declaration createDeclaration();

  /**
   * Returns a new object of class '<em>Field Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Field Declaration</em>'.
   * @generated
   */
  FieldDeclaration createFieldDeclaration();

  /**
   * Returns a new object of class '<em>Modifiers</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Modifiers</em>'.
   * @generated
   */
  Modifiers createModifiers();

  /**
   * Returns a new object of class '<em>Field Type</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Field Type</em>'.
   * @generated
   */
  FieldType createFieldType();

  /**
   * Returns a new object of class '<em>Int Enum</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Int Enum</em>'.
   * @generated
   */
  IntEnum createIntEnum();

  /**
   * Returns a new object of class '<em>Char Enum</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Char Enum</em>'.
   * @generated
   */
  CharEnum createCharEnum();

  /**
   * Returns a new object of class '<em>Enum Value</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Enum Value</em>'.
   * @generated
   */
  EnumValue createEnumValue();

  /**
   * Returns a new object of class '<em>Char Type</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Char Type</em>'.
   * @generated
   */
  CharType createCharType();

  /**
   * Returns a new object of class '<em>Pointer Type</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Pointer Type</em>'.
   * @generated
   */
  PointerType createPointerType();

  /**
   * Returns a new object of class '<em>Set Type</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Set Type</em>'.
   * @generated
   */
  SetType createSetType();

  /**
   * Returns a new object of class '<em>Sub Field Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Sub Field Declaration</em>'.
   * @generated
   */
  SubFieldDeclaration createSubFieldDeclaration();

  /**
   * Returns a new object of class '<em>Title Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Title Declaration</em>'.
   * @generated
   */
  TitleDeclaration createTitleDeclaration();

  /**
   * Returns a new object of class '<em>Include Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Include Declaration</em>'.
   * @generated
   */
  IncludeDeclaration createIncludeDeclaration();

  /**
   * Returns a new object of class '<em>Type Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Type Declaration</em>'.
   * @generated
   */
  TypeDeclaration createTypeDeclaration();

  /**
   * Returns a new object of class '<em>Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Validation Rule Declaration</em>'.
   * @generated
   */
  ValidationRuleDeclaration createValidationRuleDeclaration();

  /**
   * Returns a new object of class '<em>Comparison Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Comparison Validation Rule Declaration</em>'.
   * @generated
   */
  ComparisonValidationRuleDeclaration createComparisonValidationRuleDeclaration();

  /**
   * Returns a new object of class '<em>Comparison Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Comparison Expression</em>'.
   * @generated
   */
  ComparisonExpression createComparisonExpression();

  /**
   * Returns a new object of class '<em>Comparison Part</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Comparison Part</em>'.
   * @generated
   */
  ComparisonPart createComparisonPart();

  /**
   * Returns a new object of class '<em>Upper Function</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Upper Function</em>'.
   * @generated
   */
  UpperFunction createUpperFunction();

  /**
   * Returns a new object of class '<em>Lower Function</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Lower Function</em>'.
   * @generated
   */
  LowerFunction createLowerFunction();

  /**
   * Returns a new object of class '<em>Range Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Range Validation Rule Declaration</em>'.
   * @generated
   */
  RangeValidationRuleDeclaration createRangeValidationRuleDeclaration();

  /**
   * Returns a new object of class '<em>Regex Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Regex Validation Rule Declaration</em>'.
   * @generated
   */
  RegexValidationRuleDeclaration createRegexValidationRuleDeclaration();

  /**
   * Returns a new object of class '<em>Range</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Range</em>'.
   * @generated
   */
  Range createRange();

  /**
   * Returns a new object of class '<em>Uniqueness Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Uniqueness Validation Rule Declaration</em>'.
   * @generated
   */
  UniquenessValidationRuleDeclaration createUniquenessValidationRuleDeclaration();

  /**
   * Returns a new object of class '<em>Error Message</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Error Message</em>'.
   * @generated
   */
  ErrorMessage createErrorMessage();

  /**
   * Returns a new object of class '<em>Native Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Native Validation Rule Declaration</em>'.
   * @generated
   */
  NativeValidationRuleDeclaration createNativeValidationRuleDeclaration();

  /**
   * Returns a new object of class '<em>Function Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Function Declaration</em>'.
   * @generated
   */
  FunctionDeclaration createFunctionDeclaration();

  /**
   * Returns a new object of class '<em>Function Argument Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Function Argument Declaration</em>'.
   * @generated
   */
  FunctionArgumentDeclaration createFunctionArgumentDeclaration();

  /**
   * Returns a new object of class '<em>Function Argument Body</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Function Argument Body</em>'.
   * @generated
   */
  FunctionArgumentBody createFunctionArgumentBody();

  /**
   * Returns a new object of class '<em>Function Call</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Function Call</em>'.
   * @generated
   */
  FunctionCall createFunctionCall();

  /**
   * Returns a new object of class '<em>Field Path</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Field Path</em>'.
   * @generated
   */
  FieldPath createFieldPath();

  /**
   * Returns a new object of class '<em>Field Reference</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Field Reference</em>'.
   * @generated
   */
  FieldReference createFieldReference();

  /**
   * Returns a new object of class '<em>Function Arguments</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Function Arguments</em>'.
   * @generated
   */
  FunctionArguments createFunctionArguments();

  /**
   * Returns a new object of class '<em>Function Body</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Function Body</em>'.
   * @generated
   */
  FunctionBody createFunctionBody();

  /**
   * Returns a new object of class '<em>Statement</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Statement</em>'.
   * @generated
   */
  Statement createStatement();

  /**
   * Returns a new object of class '<em>Union Rule</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Union Rule</em>'.
   * @generated
   */
  UnionRule createUnionRule();

  /**
   * Returns a new object of class '<em>Query Rule</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Query Rule</em>'.
   * @generated
   */
  QueryRule createQueryRule();

  /**
   * Returns a new object of class '<em>Select From</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Select From</em>'.
   * @generated
   */
  SelectFrom createSelectFrom();

  /**
   * Returns a new object of class '<em>Select Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Select Clause</em>'.
   * @generated
   */
  SelectClause createSelectClause();

  /**
   * Returns a new object of class '<em>New Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>New Expression</em>'.
   * @generated
   */
  NewExpression createNewExpression();

  /**
   * Returns a new object of class '<em>From Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>From Clause</em>'.
   * @generated
   */
  FromClause createFromClause();

  /**
   * Returns a new object of class '<em>From Join</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>From Join</em>'.
   * @generated
   */
  FromJoin createFromJoin();

  /**
   * Returns a new object of class '<em>With Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>With Clause</em>'.
   * @generated
   */
  WithClause createWithClause();

  /**
   * Returns a new object of class '<em>From Range</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>From Range</em>'.
   * @generated
   */
  FromRange createFromRange();

  /**
   * Returns a new object of class '<em>From Class Or Outer Query Path</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>From Class Or Outer Query Path</em>'.
   * @generated
   */
  FromClassOrOuterQueryPath createFromClassOrOuterQueryPath();

  /**
   * Returns a new object of class '<em>In Collection Elements Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>In Collection Elements Declaration</em>'.
   * @generated
   */
  InCollectionElementsDeclaration createInCollectionElementsDeclaration();

  /**
   * Returns a new object of class '<em>Group By Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Group By Clause</em>'.
   * @generated
   */
  GroupByClause createGroupByClause();

  /**
   * Returns a new object of class '<em>Order By Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Order By Clause</em>'.
   * @generated
   */
  OrderByClause createOrderByClause();

  /**
   * Returns a new object of class '<em>Order Element</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Order Element</em>'.
   * @generated
   */
  OrderElement createOrderElement();

  /**
   * Returns a new object of class '<em>Having Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Having Clause</em>'.
   * @generated
   */
  HavingClause createHavingClause();

  /**
   * Returns a new object of class '<em>Where Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Where Clause</em>'.
   * @generated
   */
  WhereClause createWhereClause();

  /**
   * Returns a new object of class '<em>Selected Properties List</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Selected Properties List</em>'.
   * @generated
   */
  SelectedPropertiesList createSelectedPropertiesList();

  /**
   * Returns a new object of class '<em>Aliased Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Aliased Expression</em>'.
   * @generated
   */
  AliasedExpression createAliasedExpression();

  /**
   * Returns a new object of class '<em>Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expression</em>'.
   * @generated
   */
  Expression createExpression();

  /**
   * Returns a new object of class '<em>Logical Or Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Logical Or Expression</em>'.
   * @generated
   */
  LogicalOrExpression createLogicalOrExpression();

  /**
   * Returns a new object of class '<em>Logical And Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Logical And Expression</em>'.
   * @generated
   */
  LogicalAndExpression createLogicalAndExpression();

  /**
   * Returns a new object of class '<em>Negated Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Negated Expression</em>'.
   * @generated
   */
  NegatedExpression createNegatedExpression();

  /**
   * Returns a new object of class '<em>Equality Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Equality Expression</em>'.
   * @generated
   */
  EqualityExpression createEqualityExpression();

  /**
   * Returns a new object of class '<em>Relational Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Relational Expression</em>'.
   * @generated
   */
  RelationalExpression createRelationalExpression();

  /**
   * Returns a new object of class '<em>Like Escape</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Like Escape</em>'.
   * @generated
   */
  LikeEscape createLikeEscape();

  /**
   * Returns a new object of class '<em>Between List</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Between List</em>'.
   * @generated
   */
  BetweenList createBetweenList();

  /**
   * Returns a new object of class '<em>Concatenation</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Concatenation</em>'.
   * @generated
   */
  Concatenation createConcatenation();

  /**
   * Returns a new object of class '<em>Additive Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Additive Expression</em>'.
   * @generated
   */
  AdditiveExpression createAdditiveExpression();

  /**
   * Returns a new object of class '<em>Multiply Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Multiply Expression</em>'.
   * @generated
   */
  MultiplyExpression createMultiplyExpression();

  /**
   * Returns a new object of class '<em>Unary Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Unary Expression</em>'.
   * @generated
   */
  UnaryExpression createUnaryExpression();

  /**
   * Returns a new object of class '<em>Case Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Case Expression</em>'.
   * @generated
   */
  CaseExpression createCaseExpression();

  /**
   * Returns a new object of class '<em>When Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>When Clause</em>'.
   * @generated
   */
  WhenClause createWhenClause();

  /**
   * Returns a new object of class '<em>Alt When Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Alt When Clause</em>'.
   * @generated
   */
  AltWhenClause createAltWhenClause();

  /**
   * Returns a new object of class '<em>Else Clause</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Else Clause</em>'.
   * @generated
   */
  ElseClause createElseClause();

  /**
   * Returns a new object of class '<em>Quantified Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Quantified Expression</em>'.
   * @generated
   */
  QuantifiedExpression createQuantifiedExpression();

  /**
   * Returns a new object of class '<em>Atom</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Atom</em>'.
   * @generated
   */
  Atom createAtom();

  /**
   * Returns a new object of class '<em>Primary Expression</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Primary Expression</em>'.
   * @generated
   */
  PrimaryExpression createPrimaryExpression();

  /**
   * Returns a new object of class '<em>Expression Or Vector</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expression Or Vector</em>'.
   * @generated
   */
  ExpressionOrVector createExpressionOrVector();

  /**
   * Returns a new object of class '<em>Vector Expr</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Vector Expr</em>'.
   * @generated
   */
  VectorExpr createVectorExpr();

  /**
   * Returns a new object of class '<em>Ident Primary</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Ident Primary</em>'.
   * @generated
   */
  IdentPrimary createIdentPrimary();

  /**
   * Returns a new object of class '<em>Aggregate</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Aggregate</em>'.
   * @generated
   */
  Aggregate createAggregate();

  /**
   * Returns a new object of class '<em>Compound Expr</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Compound Expr</em>'.
   * @generated
   */
  CompoundExpr createCompoundExpr();

  /**
   * Returns a new object of class '<em>Expr List</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr List</em>'.
   * @generated
   */
  ExprList createExprList();

  /**
   * Returns the package supported by this factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the package supported by this factory.
   * @generated
   */
  MDDPackage getMDDPackage();

} //MDDFactory

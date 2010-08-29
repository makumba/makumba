/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.makumba.devel.eclipse.mdd.MDD.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class MDDFactoryImpl extends EFactoryImpl implements MDDFactory
{
  /**
   * Creates the default factory implementation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static MDDFactory init()
  {
    try
    {
      MDDFactory theMDDFactory = (MDDFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.makumba.org/devel/plugin/eclipse/mdd/MDD"); 
      if (theMDDFactory != null)
      {
        return theMDDFactory;
      }
    }
    catch (Exception exception)
    {
      EcorePlugin.INSTANCE.log(exception);
    }
    return new MDDFactoryImpl();
  }

  /**
   * Creates an instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MDDFactoryImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EObject create(EClass eClass)
  {
    switch (eClass.getClassifierID())
    {
      case MDDPackage.DATA_DEFINITION: return createDataDefinition();
      case MDDPackage.DECLARATION: return createDeclaration();
      case MDDPackage.FIELD_DECLARATION: return createFieldDeclaration();
      case MDDPackage.MODIFIERS: return createModifiers();
      case MDDPackage.FIELD_TYPE: return createFieldType();
      case MDDPackage.INT_ENUM: return createIntEnum();
      case MDDPackage.CHAR_ENUM: return createCharEnum();
      case MDDPackage.ENUM_VALUE: return createEnumValue();
      case MDDPackage.CHAR_TYPE: return createCharType();
      case MDDPackage.POINTER_TYPE: return createPointerType();
      case MDDPackage.SET_TYPE: return createSetType();
      case MDDPackage.SUB_FIELD_DECLARATION: return createSubFieldDeclaration();
      case MDDPackage.TITLE_DECLARATION: return createTitleDeclaration();
      case MDDPackage.INCLUDE_DECLARATION: return createIncludeDeclaration();
      case MDDPackage.TYPE_DECLARATION: return createTypeDeclaration();
      case MDDPackage.VALIDATION_RULE_DECLARATION: return createValidationRuleDeclaration();
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION: return createComparisonValidationRuleDeclaration();
      case MDDPackage.COMPARISON_EXPRESSION: return createComparisonExpression();
      case MDDPackage.COMPARISON_PART: return createComparisonPart();
      case MDDPackage.UPPER_FUNCTION: return createUpperFunction();
      case MDDPackage.LOWER_FUNCTION: return createLowerFunction();
      case MDDPackage.RANGE_VALIDATION_RULE_DECLARATION: return createRangeValidationRuleDeclaration();
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION: return createRegexValidationRuleDeclaration();
      case MDDPackage.RANGE: return createRange();
      case MDDPackage.UNIQUENESS_VALIDATION_RULE_DECLARATION: return createUniquenessValidationRuleDeclaration();
      case MDDPackage.ERROR_MESSAGE: return createErrorMessage();
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION: return createNativeValidationRuleDeclaration();
      case MDDPackage.FUNCTION_DECLARATION: return createFunctionDeclaration();
      case MDDPackage.FUNCTION_ARGUMENT_DECLARATION: return createFunctionArgumentDeclaration();
      case MDDPackage.FUNCTION_ARGUMENT_BODY: return createFunctionArgumentBody();
      case MDDPackage.FUNCTION_CALL: return createFunctionCall();
      case MDDPackage.FIELD_PATH: return createFieldPath();
      case MDDPackage.FIELD_REFERENCE: return createFieldReference();
      case MDDPackage.FUNCTION_ARGUMENTS: return createFunctionArguments();
      case MDDPackage.FUNCTION_BODY: return createFunctionBody();
      case MDDPackage.STATEMENT: return createStatement();
      case MDDPackage.UNION_RULE: return createUnionRule();
      case MDDPackage.QUERY_RULE: return createQueryRule();
      case MDDPackage.SELECT_FROM: return createSelectFrom();
      case MDDPackage.SELECT_CLAUSE: return createSelectClause();
      case MDDPackage.NEW_EXPRESSION: return createNewExpression();
      case MDDPackage.FROM_CLAUSE: return createFromClause();
      case MDDPackage.FROM_JOIN: return createFromJoin();
      case MDDPackage.WITH_CLAUSE: return createWithClause();
      case MDDPackage.FROM_RANGE: return createFromRange();
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH: return createFromClassOrOuterQueryPath();
      case MDDPackage.IN_COLLECTION_ELEMENTS_DECLARATION: return createInCollectionElementsDeclaration();
      case MDDPackage.GROUP_BY_CLAUSE: return createGroupByClause();
      case MDDPackage.ORDER_BY_CLAUSE: return createOrderByClause();
      case MDDPackage.ORDER_ELEMENT: return createOrderElement();
      case MDDPackage.HAVING_CLAUSE: return createHavingClause();
      case MDDPackage.WHERE_CLAUSE: return createWhereClause();
      case MDDPackage.SELECTED_PROPERTIES_LIST: return createSelectedPropertiesList();
      case MDDPackage.ALIASED_EXPRESSION: return createAliasedExpression();
      case MDDPackage.EXPRESSION: return createExpression();
      case MDDPackage.LOGICAL_OR_EXPRESSION: return createLogicalOrExpression();
      case MDDPackage.LOGICAL_AND_EXPRESSION: return createLogicalAndExpression();
      case MDDPackage.NEGATED_EXPRESSION: return createNegatedExpression();
      case MDDPackage.EQUALITY_EXPRESSION: return createEqualityExpression();
      case MDDPackage.RELATIONAL_EXPRESSION: return createRelationalExpression();
      case MDDPackage.LIKE_ESCAPE: return createLikeEscape();
      case MDDPackage.BETWEEN_LIST: return createBetweenList();
      case MDDPackage.CONCATENATION: return createConcatenation();
      case MDDPackage.ADDITIVE_EXPRESSION: return createAdditiveExpression();
      case MDDPackage.MULTIPLY_EXPRESSION: return createMultiplyExpression();
      case MDDPackage.UNARY_EXPRESSION: return createUnaryExpression();
      case MDDPackage.CASE_EXPRESSION: return createCaseExpression();
      case MDDPackage.WHEN_CLAUSE: return createWhenClause();
      case MDDPackage.ALT_WHEN_CLAUSE: return createAltWhenClause();
      case MDDPackage.ELSE_CLAUSE: return createElseClause();
      case MDDPackage.QUANTIFIED_EXPRESSION: return createQuantifiedExpression();
      case MDDPackage.ATOM: return createAtom();
      case MDDPackage.PRIMARY_EXPRESSION: return createPrimaryExpression();
      case MDDPackage.EXPRESSION_OR_VECTOR: return createExpressionOrVector();
      case MDDPackage.VECTOR_EXPR: return createVectorExpr();
      case MDDPackage.IDENT_PRIMARY: return createIdentPrimary();
      case MDDPackage.AGGREGATE: return createAggregate();
      case MDDPackage.COMPOUND_EXPR: return createCompoundExpr();
      case MDDPackage.EXPR_LIST: return createExprList();
      default:
        throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public DataDefinition createDataDefinition()
  {
    DataDefinitionImpl dataDefinition = new DataDefinitionImpl();
    return dataDefinition;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Declaration createDeclaration()
  {
    DeclarationImpl declaration = new DeclarationImpl();
    return declaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldDeclaration createFieldDeclaration()
  {
    FieldDeclarationImpl fieldDeclaration = new FieldDeclarationImpl();
    return fieldDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Modifiers createModifiers()
  {
    ModifiersImpl modifiers = new ModifiersImpl();
    return modifiers;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldType createFieldType()
  {
    FieldTypeImpl fieldType = new FieldTypeImpl();
    return fieldType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IntEnum createIntEnum()
  {
    IntEnumImpl intEnum = new IntEnumImpl();
    return intEnum;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public CharEnum createCharEnum()
  {
    CharEnumImpl charEnum = new CharEnumImpl();
    return charEnum;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EnumValue createEnumValue()
  {
    EnumValueImpl enumValue = new EnumValueImpl();
    return enumValue;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public CharType createCharType()
  {
    CharTypeImpl charType = new CharTypeImpl();
    return charType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PointerType createPointerType()
  {
    PointerTypeImpl pointerType = new PointerTypeImpl();
    return pointerType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SetType createSetType()
  {
    SetTypeImpl setType = new SetTypeImpl();
    return setType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SubFieldDeclaration createSubFieldDeclaration()
  {
    SubFieldDeclarationImpl subFieldDeclaration = new SubFieldDeclarationImpl();
    return subFieldDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TitleDeclaration createTitleDeclaration()
  {
    TitleDeclarationImpl titleDeclaration = new TitleDeclarationImpl();
    return titleDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IncludeDeclaration createIncludeDeclaration()
  {
    IncludeDeclarationImpl includeDeclaration = new IncludeDeclarationImpl();
    return includeDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TypeDeclaration createTypeDeclaration()
  {
    TypeDeclarationImpl typeDeclaration = new TypeDeclarationImpl();
    return typeDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ValidationRuleDeclaration createValidationRuleDeclaration()
  {
    ValidationRuleDeclarationImpl validationRuleDeclaration = new ValidationRuleDeclarationImpl();
    return validationRuleDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ComparisonValidationRuleDeclaration createComparisonValidationRuleDeclaration()
  {
    ComparisonValidationRuleDeclarationImpl comparisonValidationRuleDeclaration = new ComparisonValidationRuleDeclarationImpl();
    return comparisonValidationRuleDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ComparisonExpression createComparisonExpression()
  {
    ComparisonExpressionImpl comparisonExpression = new ComparisonExpressionImpl();
    return comparisonExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ComparisonPart createComparisonPart()
  {
    ComparisonPartImpl comparisonPart = new ComparisonPartImpl();
    return comparisonPart;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public UpperFunction createUpperFunction()
  {
    UpperFunctionImpl upperFunction = new UpperFunctionImpl();
    return upperFunction;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public LowerFunction createLowerFunction()
  {
    LowerFunctionImpl lowerFunction = new LowerFunctionImpl();
    return lowerFunction;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public RangeValidationRuleDeclaration createRangeValidationRuleDeclaration()
  {
    RangeValidationRuleDeclarationImpl rangeValidationRuleDeclaration = new RangeValidationRuleDeclarationImpl();
    return rangeValidationRuleDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public RegexValidationRuleDeclaration createRegexValidationRuleDeclaration()
  {
    RegexValidationRuleDeclarationImpl regexValidationRuleDeclaration = new RegexValidationRuleDeclarationImpl();
    return regexValidationRuleDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Range createRange()
  {
    RangeImpl range = new RangeImpl();
    return range;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public UniquenessValidationRuleDeclaration createUniquenessValidationRuleDeclaration()
  {
    UniquenessValidationRuleDeclarationImpl uniquenessValidationRuleDeclaration = new UniquenessValidationRuleDeclarationImpl();
    return uniquenessValidationRuleDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ErrorMessage createErrorMessage()
  {
    ErrorMessageImpl errorMessage = new ErrorMessageImpl();
    return errorMessage;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NativeValidationRuleDeclaration createNativeValidationRuleDeclaration()
  {
    NativeValidationRuleDeclarationImpl nativeValidationRuleDeclaration = new NativeValidationRuleDeclarationImpl();
    return nativeValidationRuleDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FunctionDeclaration createFunctionDeclaration()
  {
    FunctionDeclarationImpl functionDeclaration = new FunctionDeclarationImpl();
    return functionDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FunctionArgumentDeclaration createFunctionArgumentDeclaration()
  {
    FunctionArgumentDeclarationImpl functionArgumentDeclaration = new FunctionArgumentDeclarationImpl();
    return functionArgumentDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FunctionArgumentBody createFunctionArgumentBody()
  {
    FunctionArgumentBodyImpl functionArgumentBody = new FunctionArgumentBodyImpl();
    return functionArgumentBody;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FunctionCall createFunctionCall()
  {
    FunctionCallImpl functionCall = new FunctionCallImpl();
    return functionCall;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldPath createFieldPath()
  {
    FieldPathImpl fieldPath = new FieldPathImpl();
    return fieldPath;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FieldReference createFieldReference()
  {
    FieldReferenceImpl fieldReference = new FieldReferenceImpl();
    return fieldReference;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FunctionArguments createFunctionArguments()
  {
    FunctionArgumentsImpl functionArguments = new FunctionArgumentsImpl();
    return functionArguments;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FunctionBody createFunctionBody()
  {
    FunctionBodyImpl functionBody = new FunctionBodyImpl();
    return functionBody;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Statement createStatement()
  {
    StatementImpl statement = new StatementImpl();
    return statement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public UnionRule createUnionRule()
  {
    UnionRuleImpl unionRule = new UnionRuleImpl();
    return unionRule;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public QueryRule createQueryRule()
  {
    QueryRuleImpl queryRule = new QueryRuleImpl();
    return queryRule;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SelectFrom createSelectFrom()
  {
    SelectFromImpl selectFrom = new SelectFromImpl();
    return selectFrom;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SelectClause createSelectClause()
  {
    SelectClauseImpl selectClause = new SelectClauseImpl();
    return selectClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NewExpression createNewExpression()
  {
    NewExpressionImpl newExpression = new NewExpressionImpl();
    return newExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FromClause createFromClause()
  {
    FromClauseImpl fromClause = new FromClauseImpl();
    return fromClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FromJoin createFromJoin()
  {
    FromJoinImpl fromJoin = new FromJoinImpl();
    return fromJoin;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public WithClause createWithClause()
  {
    WithClauseImpl withClause = new WithClauseImpl();
    return withClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FromRange createFromRange()
  {
    FromRangeImpl fromRange = new FromRangeImpl();
    return fromRange;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FromClassOrOuterQueryPath createFromClassOrOuterQueryPath()
  {
    FromClassOrOuterQueryPathImpl fromClassOrOuterQueryPath = new FromClassOrOuterQueryPathImpl();
    return fromClassOrOuterQueryPath;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public InCollectionElementsDeclaration createInCollectionElementsDeclaration()
  {
    InCollectionElementsDeclarationImpl inCollectionElementsDeclaration = new InCollectionElementsDeclarationImpl();
    return inCollectionElementsDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GroupByClause createGroupByClause()
  {
    GroupByClauseImpl groupByClause = new GroupByClauseImpl();
    return groupByClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OrderByClause createOrderByClause()
  {
    OrderByClauseImpl orderByClause = new OrderByClauseImpl();
    return orderByClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OrderElement createOrderElement()
  {
    OrderElementImpl orderElement = new OrderElementImpl();
    return orderElement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public HavingClause createHavingClause()
  {
    HavingClauseImpl havingClause = new HavingClauseImpl();
    return havingClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public WhereClause createWhereClause()
  {
    WhereClauseImpl whereClause = new WhereClauseImpl();
    return whereClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SelectedPropertiesList createSelectedPropertiesList()
  {
    SelectedPropertiesListImpl selectedPropertiesList = new SelectedPropertiesListImpl();
    return selectedPropertiesList;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AliasedExpression createAliasedExpression()
  {
    AliasedExpressionImpl aliasedExpression = new AliasedExpressionImpl();
    return aliasedExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Expression createExpression()
  {
    ExpressionImpl expression = new ExpressionImpl();
    return expression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public LogicalOrExpression createLogicalOrExpression()
  {
    LogicalOrExpressionImpl logicalOrExpression = new LogicalOrExpressionImpl();
    return logicalOrExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public LogicalAndExpression createLogicalAndExpression()
  {
    LogicalAndExpressionImpl logicalAndExpression = new LogicalAndExpressionImpl();
    return logicalAndExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NegatedExpression createNegatedExpression()
  {
    NegatedExpressionImpl negatedExpression = new NegatedExpressionImpl();
    return negatedExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EqualityExpression createEqualityExpression()
  {
    EqualityExpressionImpl equalityExpression = new EqualityExpressionImpl();
    return equalityExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public RelationalExpression createRelationalExpression()
  {
    RelationalExpressionImpl relationalExpression = new RelationalExpressionImpl();
    return relationalExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public LikeEscape createLikeEscape()
  {
    LikeEscapeImpl likeEscape = new LikeEscapeImpl();
    return likeEscape;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BetweenList createBetweenList()
  {
    BetweenListImpl betweenList = new BetweenListImpl();
    return betweenList;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Concatenation createConcatenation()
  {
    ConcatenationImpl concatenation = new ConcatenationImpl();
    return concatenation;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AdditiveExpression createAdditiveExpression()
  {
    AdditiveExpressionImpl additiveExpression = new AdditiveExpressionImpl();
    return additiveExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MultiplyExpression createMultiplyExpression()
  {
    MultiplyExpressionImpl multiplyExpression = new MultiplyExpressionImpl();
    return multiplyExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public UnaryExpression createUnaryExpression()
  {
    UnaryExpressionImpl unaryExpression = new UnaryExpressionImpl();
    return unaryExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public CaseExpression createCaseExpression()
  {
    CaseExpressionImpl caseExpression = new CaseExpressionImpl();
    return caseExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public WhenClause createWhenClause()
  {
    WhenClauseImpl whenClause = new WhenClauseImpl();
    return whenClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AltWhenClause createAltWhenClause()
  {
    AltWhenClauseImpl altWhenClause = new AltWhenClauseImpl();
    return altWhenClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ElseClause createElseClause()
  {
    ElseClauseImpl elseClause = new ElseClauseImpl();
    return elseClause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public QuantifiedExpression createQuantifiedExpression()
  {
    QuantifiedExpressionImpl quantifiedExpression = new QuantifiedExpressionImpl();
    return quantifiedExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Atom createAtom()
  {
    AtomImpl atom = new AtomImpl();
    return atom;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PrimaryExpression createPrimaryExpression()
  {
    PrimaryExpressionImpl primaryExpression = new PrimaryExpressionImpl();
    return primaryExpression;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExpressionOrVector createExpressionOrVector()
  {
    ExpressionOrVectorImpl expressionOrVector = new ExpressionOrVectorImpl();
    return expressionOrVector;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public VectorExpr createVectorExpr()
  {
    VectorExprImpl vectorExpr = new VectorExprImpl();
    return vectorExpr;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IdentPrimary createIdentPrimary()
  {
    IdentPrimaryImpl identPrimary = new IdentPrimaryImpl();
    return identPrimary;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Aggregate createAggregate()
  {
    AggregateImpl aggregate = new AggregateImpl();
    return aggregate;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public CompoundExpr createCompoundExpr()
  {
    CompoundExprImpl compoundExpr = new CompoundExprImpl();
    return compoundExpr;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprList createExprList()
  {
    ExprListImpl exprList = new ExprListImpl();
    return exprList;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MDDPackage getMDDPackage()
  {
    return (MDDPackage)getEPackage();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @deprecated
   * @generated
   */
  @Deprecated
  public static MDDPackage getPackage()
  {
    return MDDPackage.eINSTANCE;
  }

} //MDDFactoryImpl

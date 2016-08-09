/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD.util;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.makumba.devel.eclipse.mdd.MDD.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage
 * @generated
 */
public class MDDSwitch<T>
{
  /**
   * The cached model package
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected static MDDPackage modelPackage;

  /**
   * Creates an instance of the switch.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MDDSwitch()
  {
    if (modelPackage == null)
    {
      modelPackage = MDDPackage.eINSTANCE;
    }
  }

  /**
   * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the first non-null result returned by a <code>caseXXX</code> call.
   * @generated
   */
  public T doSwitch(EObject theEObject)
  {
    return doSwitch(theEObject.eClass(), theEObject);
  }

  /**
   * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the first non-null result returned by a <code>caseXXX</code> call.
   * @generated
   */
  protected T doSwitch(EClass theEClass, EObject theEObject)
  {
    if (theEClass.eContainer() == modelPackage)
    {
      return doSwitch(theEClass.getClassifierID(), theEObject);
    }
    else
    {
      List<EClass> eSuperTypes = theEClass.getESuperTypes();
      return
        eSuperTypes.isEmpty() ?
          defaultCase(theEObject) :
          doSwitch(eSuperTypes.get(0), theEObject);
    }
  }

  /**
   * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the first non-null result returned by a <code>caseXXX</code> call.
   * @generated
   */
  protected T doSwitch(int classifierID, EObject theEObject)
  {
    switch (classifierID)
    {
      case MDDPackage.DATA_DEFINITION:
      {
        DataDefinition dataDefinition = (DataDefinition)theEObject;
        T result = caseDataDefinition(dataDefinition);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.DECLARATION:
      {
        Declaration declaration = (Declaration)theEObject;
        T result = caseDeclaration(declaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FIELD_DECLARATION:
      {
        FieldDeclaration fieldDeclaration = (FieldDeclaration)theEObject;
        T result = caseFieldDeclaration(fieldDeclaration);
        if (result == null) result = caseDeclaration(fieldDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.MODIFIERS:
      {
        Modifiers modifiers = (Modifiers)theEObject;
        T result = caseModifiers(modifiers);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FIELD_TYPE:
      {
        FieldType fieldType = (FieldType)theEObject;
        T result = caseFieldType(fieldType);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.INT_ENUM:
      {
        IntEnum intEnum = (IntEnum)theEObject;
        T result = caseIntEnum(intEnum);
        if (result == null) result = caseFieldType(intEnum);
        if (result == null) result = caseFunctionArgumentBody(intEnum);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.CHAR_ENUM:
      {
        CharEnum charEnum = (CharEnum)theEObject;
        T result = caseCharEnum(charEnum);
        if (result == null) result = caseFieldType(charEnum);
        if (result == null) result = caseFunctionArgumentBody(charEnum);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.ENUM_VALUE:
      {
        EnumValue enumValue = (EnumValue)theEObject;
        T result = caseEnumValue(enumValue);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.CHAR_TYPE:
      {
        CharType charType = (CharType)theEObject;
        T result = caseCharType(charType);
        if (result == null) result = caseFieldType(charType);
        if (result == null) result = caseFunctionArgumentBody(charType);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.POINTER_TYPE:
      {
        PointerType pointerType = (PointerType)theEObject;
        T result = casePointerType(pointerType);
        if (result == null) result = caseFieldType(pointerType);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.SET_TYPE:
      {
        SetType setType = (SetType)theEObject;
        T result = caseSetType(setType);
        if (result == null) result = caseFieldType(setType);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.SUB_FIELD_DECLARATION:
      {
        SubFieldDeclaration subFieldDeclaration = (SubFieldDeclaration)theEObject;
        T result = caseSubFieldDeclaration(subFieldDeclaration);
        if (result == null) result = caseDeclaration(subFieldDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.TITLE_DECLARATION:
      {
        TitleDeclaration titleDeclaration = (TitleDeclaration)theEObject;
        T result = caseTitleDeclaration(titleDeclaration);
        if (result == null) result = caseDeclaration(titleDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.INCLUDE_DECLARATION:
      {
        IncludeDeclaration includeDeclaration = (IncludeDeclaration)theEObject;
        T result = caseIncludeDeclaration(includeDeclaration);
        if (result == null) result = caseDeclaration(includeDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.TYPE_DECLARATION:
      {
        TypeDeclaration typeDeclaration = (TypeDeclaration)theEObject;
        T result = caseTypeDeclaration(typeDeclaration);
        if (result == null) result = caseDeclaration(typeDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.VALIDATION_RULE_DECLARATION:
      {
        ValidationRuleDeclaration validationRuleDeclaration = (ValidationRuleDeclaration)theEObject;
        T result = caseValidationRuleDeclaration(validationRuleDeclaration);
        if (result == null) result = caseDeclaration(validationRuleDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.COMPARISON_VALIDATION_RULE_DECLARATION:
      {
        ComparisonValidationRuleDeclaration comparisonValidationRuleDeclaration = (ComparisonValidationRuleDeclaration)theEObject;
        T result = caseComparisonValidationRuleDeclaration(comparisonValidationRuleDeclaration);
        if (result == null) result = caseValidationRuleDeclaration(comparisonValidationRuleDeclaration);
        if (result == null) result = caseDeclaration(comparisonValidationRuleDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.COMPARISON_EXPRESSION:
      {
        ComparisonExpression comparisonExpression = (ComparisonExpression)theEObject;
        T result = caseComparisonExpression(comparisonExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.COMPARISON_PART:
      {
        ComparisonPart comparisonPart = (ComparisonPart)theEObject;
        T result = caseComparisonPart(comparisonPart);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.UPPER_FUNCTION:
      {
        UpperFunction upperFunction = (UpperFunction)theEObject;
        T result = caseUpperFunction(upperFunction);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.LOWER_FUNCTION:
      {
        LowerFunction lowerFunction = (LowerFunction)theEObject;
        T result = caseLowerFunction(lowerFunction);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.RANGE_VALIDATION_RULE_DECLARATION:
      {
        RangeValidationRuleDeclaration rangeValidationRuleDeclaration = (RangeValidationRuleDeclaration)theEObject;
        T result = caseRangeValidationRuleDeclaration(rangeValidationRuleDeclaration);
        if (result == null) result = caseValidationRuleDeclaration(rangeValidationRuleDeclaration);
        if (result == null) result = caseDeclaration(rangeValidationRuleDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.REGEX_VALIDATION_RULE_DECLARATION:
      {
        RegexValidationRuleDeclaration regexValidationRuleDeclaration = (RegexValidationRuleDeclaration)theEObject;
        T result = caseRegexValidationRuleDeclaration(regexValidationRuleDeclaration);
        if (result == null) result = caseValidationRuleDeclaration(regexValidationRuleDeclaration);
        if (result == null) result = caseDeclaration(regexValidationRuleDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.RANGE:
      {
        Range range = (Range)theEObject;
        T result = caseRange(range);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.UNIQUENESS_VALIDATION_RULE_DECLARATION:
      {
        UniquenessValidationRuleDeclaration uniquenessValidationRuleDeclaration = (UniquenessValidationRuleDeclaration)theEObject;
        T result = caseUniquenessValidationRuleDeclaration(uniquenessValidationRuleDeclaration);
        if (result == null) result = caseValidationRuleDeclaration(uniquenessValidationRuleDeclaration);
        if (result == null) result = caseDeclaration(uniquenessValidationRuleDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.ERROR_MESSAGE:
      {
        ErrorMessage errorMessage = (ErrorMessage)theEObject;
        T result = caseErrorMessage(errorMessage);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.NATIVE_VALIDATION_RULE_DECLARATION:
      {
        NativeValidationRuleDeclaration nativeValidationRuleDeclaration = (NativeValidationRuleDeclaration)theEObject;
        T result = caseNativeValidationRuleDeclaration(nativeValidationRuleDeclaration);
        if (result == null) result = caseDeclaration(nativeValidationRuleDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FUNCTION_DECLARATION:
      {
        FunctionDeclaration functionDeclaration = (FunctionDeclaration)theEObject;
        T result = caseFunctionDeclaration(functionDeclaration);
        if (result == null) result = caseDeclaration(functionDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FUNCTION_ARGUMENT_DECLARATION:
      {
        FunctionArgumentDeclaration functionArgumentDeclaration = (FunctionArgumentDeclaration)theEObject;
        T result = caseFunctionArgumentDeclaration(functionArgumentDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FUNCTION_ARGUMENT_BODY:
      {
        FunctionArgumentBody functionArgumentBody = (FunctionArgumentBody)theEObject;
        T result = caseFunctionArgumentBody(functionArgumentBody);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FUNCTION_CALL:
      {
        FunctionCall functionCall = (FunctionCall)theEObject;
        T result = caseFunctionCall(functionCall);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FIELD_PATH:
      {
        FieldPath fieldPath = (FieldPath)theEObject;
        T result = caseFieldPath(fieldPath);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FIELD_REFERENCE:
      {
        FieldReference fieldReference = (FieldReference)theEObject;
        T result = caseFieldReference(fieldReference);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FUNCTION_ARGUMENTS:
      {
        FunctionArguments functionArguments = (FunctionArguments)theEObject;
        T result = caseFunctionArguments(functionArguments);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FUNCTION_BODY:
      {
        FunctionBody functionBody = (FunctionBody)theEObject;
        T result = caseFunctionBody(functionBody);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.STATEMENT:
      {
        Statement statement = (Statement)theEObject;
        T result = caseStatement(statement);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.UNION_RULE:
      {
        UnionRule unionRule = (UnionRule)theEObject;
        T result = caseUnionRule(unionRule);
        if (result == null) result = casePrimaryExpression(unionRule);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.QUERY_RULE:
      {
        QueryRule queryRule = (QueryRule)theEObject;
        T result = caseQueryRule(queryRule);
        if (result == null) result = caseStatement(queryRule);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.SELECT_FROM:
      {
        SelectFrom selectFrom = (SelectFrom)theEObject;
        T result = caseSelectFrom(selectFrom);
        if (result == null) result = caseQueryRule(selectFrom);
        if (result == null) result = caseStatement(selectFrom);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.SELECT_CLAUSE:
      {
        SelectClause selectClause = (SelectClause)theEObject;
        T result = caseSelectClause(selectClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.NEW_EXPRESSION:
      {
        NewExpression newExpression = (NewExpression)theEObject;
        T result = caseNewExpression(newExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FROM_CLAUSE:
      {
        FromClause fromClause = (FromClause)theEObject;
        T result = caseFromClause(fromClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FROM_JOIN:
      {
        FromJoin fromJoin = (FromJoin)theEObject;
        T result = caseFromJoin(fromJoin);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.WITH_CLAUSE:
      {
        WithClause withClause = (WithClause)theEObject;
        T result = caseWithClause(withClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FROM_RANGE:
      {
        FromRange fromRange = (FromRange)theEObject;
        T result = caseFromRange(fromRange);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.FROM_CLASS_OR_OUTER_QUERY_PATH:
      {
        FromClassOrOuterQueryPath fromClassOrOuterQueryPath = (FromClassOrOuterQueryPath)theEObject;
        T result = caseFromClassOrOuterQueryPath(fromClassOrOuterQueryPath);
        if (result == null) result = caseFromJoin(fromClassOrOuterQueryPath);
        if (result == null) result = caseFromRange(fromClassOrOuterQueryPath);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.IN_COLLECTION_ELEMENTS_DECLARATION:
      {
        InCollectionElementsDeclaration inCollectionElementsDeclaration = (InCollectionElementsDeclaration)theEObject;
        T result = caseInCollectionElementsDeclaration(inCollectionElementsDeclaration);
        if (result == null) result = caseFromRange(inCollectionElementsDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.GROUP_BY_CLAUSE:
      {
        GroupByClause groupByClause = (GroupByClause)theEObject;
        T result = caseGroupByClause(groupByClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.ORDER_BY_CLAUSE:
      {
        OrderByClause orderByClause = (OrderByClause)theEObject;
        T result = caseOrderByClause(orderByClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.ORDER_ELEMENT:
      {
        OrderElement orderElement = (OrderElement)theEObject;
        T result = caseOrderElement(orderElement);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.HAVING_CLAUSE:
      {
        HavingClause havingClause = (HavingClause)theEObject;
        T result = caseHavingClause(havingClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.WHERE_CLAUSE:
      {
        WhereClause whereClause = (WhereClause)theEObject;
        T result = caseWhereClause(whereClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.SELECTED_PROPERTIES_LIST:
      {
        SelectedPropertiesList selectedPropertiesList = (SelectedPropertiesList)theEObject;
        T result = caseSelectedPropertiesList(selectedPropertiesList);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.ALIASED_EXPRESSION:
      {
        AliasedExpression aliasedExpression = (AliasedExpression)theEObject;
        T result = caseAliasedExpression(aliasedExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.EXPRESSION:
      {
        Expression expression = (Expression)theEObject;
        T result = caseExpression(expression);
        if (result == null) result = caseOrderElement(expression);
        if (result == null) result = caseAliasedExpression(expression);
        if (result == null) result = caseExpressionOrVector(expression);
        if (result == null) result = casePrimaryExpression(expression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.LOGICAL_OR_EXPRESSION:
      {
        LogicalOrExpression logicalOrExpression = (LogicalOrExpression)theEObject;
        T result = caseLogicalOrExpression(logicalOrExpression);
        if (result == null) result = caseExpression(logicalOrExpression);
        if (result == null) result = caseOrderElement(logicalOrExpression);
        if (result == null) result = caseAliasedExpression(logicalOrExpression);
        if (result == null) result = caseExpressionOrVector(logicalOrExpression);
        if (result == null) result = casePrimaryExpression(logicalOrExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.LOGICAL_AND_EXPRESSION:
      {
        LogicalAndExpression logicalAndExpression = (LogicalAndExpression)theEObject;
        T result = caseLogicalAndExpression(logicalAndExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.NEGATED_EXPRESSION:
      {
        NegatedExpression negatedExpression = (NegatedExpression)theEObject;
        T result = caseNegatedExpression(negatedExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.EQUALITY_EXPRESSION:
      {
        EqualityExpression equalityExpression = (EqualityExpression)theEObject;
        T result = caseEqualityExpression(equalityExpression);
        if (result == null) result = caseNegatedExpression(equalityExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.RELATIONAL_EXPRESSION:
      {
        RelationalExpression relationalExpression = (RelationalExpression)theEObject;
        T result = caseRelationalExpression(relationalExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.LIKE_ESCAPE:
      {
        LikeEscape likeEscape = (LikeEscape)theEObject;
        T result = caseLikeEscape(likeEscape);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.BETWEEN_LIST:
      {
        BetweenList betweenList = (BetweenList)theEObject;
        T result = caseBetweenList(betweenList);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.CONCATENATION:
      {
        Concatenation concatenation = (Concatenation)theEObject;
        T result = caseConcatenation(concatenation);
        if (result == null) result = caseRelationalExpression(concatenation);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.ADDITIVE_EXPRESSION:
      {
        AdditiveExpression additiveExpression = (AdditiveExpression)theEObject;
        T result = caseAdditiveExpression(additiveExpression);
        if (result == null) result = caseConcatenation(additiveExpression);
        if (result == null) result = caseRelationalExpression(additiveExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.MULTIPLY_EXPRESSION:
      {
        MultiplyExpression multiplyExpression = (MultiplyExpression)theEObject;
        T result = caseMultiplyExpression(multiplyExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.UNARY_EXPRESSION:
      {
        UnaryExpression unaryExpression = (UnaryExpression)theEObject;
        T result = caseUnaryExpression(unaryExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.CASE_EXPRESSION:
      {
        CaseExpression caseExpression = (CaseExpression)theEObject;
        T result = caseCaseExpression(caseExpression);
        if (result == null) result = caseUnaryExpression(caseExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.WHEN_CLAUSE:
      {
        WhenClause whenClause = (WhenClause)theEObject;
        T result = caseWhenClause(whenClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.ALT_WHEN_CLAUSE:
      {
        AltWhenClause altWhenClause = (AltWhenClause)theEObject;
        T result = caseAltWhenClause(altWhenClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.ELSE_CLAUSE:
      {
        ElseClause elseClause = (ElseClause)theEObject;
        T result = caseElseClause(elseClause);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.QUANTIFIED_EXPRESSION:
      {
        QuantifiedExpression quantifiedExpression = (QuantifiedExpression)theEObject;
        T result = caseQuantifiedExpression(quantifiedExpression);
        if (result == null) result = caseUnaryExpression(quantifiedExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.ATOM:
      {
        Atom atom = (Atom)theEObject;
        T result = caseAtom(atom);
        if (result == null) result = caseUnaryExpression(atom);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.PRIMARY_EXPRESSION:
      {
        PrimaryExpression primaryExpression = (PrimaryExpression)theEObject;
        T result = casePrimaryExpression(primaryExpression);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.EXPRESSION_OR_VECTOR:
      {
        ExpressionOrVector expressionOrVector = (ExpressionOrVector)theEObject;
        T result = caseExpressionOrVector(expressionOrVector);
        if (result == null) result = casePrimaryExpression(expressionOrVector);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.VECTOR_EXPR:
      {
        VectorExpr vectorExpr = (VectorExpr)theEObject;
        T result = caseVectorExpr(vectorExpr);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.IDENT_PRIMARY:
      {
        IdentPrimary identPrimary = (IdentPrimary)theEObject;
        T result = caseIdentPrimary(identPrimary);
        if (result == null) result = casePrimaryExpression(identPrimary);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.AGGREGATE:
      {
        Aggregate aggregate = (Aggregate)theEObject;
        T result = caseAggregate(aggregate);
        if (result == null) result = caseIdentPrimary(aggregate);
        if (result == null) result = casePrimaryExpression(aggregate);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.COMPOUND_EXPR:
      {
        CompoundExpr compoundExpr = (CompoundExpr)theEObject;
        T result = caseCompoundExpr(compoundExpr);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case MDDPackage.EXPR_LIST:
      {
        ExprList exprList = (ExprList)theEObject;
        T result = caseExprList(exprList);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      default: return defaultCase(theEObject);
    }
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Data Definition</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Data Definition</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseDataDefinition(DataDefinition object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseDeclaration(Declaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Field Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Field Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFieldDeclaration(FieldDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Modifiers</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Modifiers</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseModifiers(Modifiers object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Field Type</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Field Type</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFieldType(FieldType object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Int Enum</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Int Enum</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseIntEnum(IntEnum object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Char Enum</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Char Enum</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseCharEnum(CharEnum object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Enum Value</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Enum Value</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseEnumValue(EnumValue object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Char Type</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Char Type</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseCharType(CharType object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Pointer Type</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Pointer Type</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T casePointerType(PointerType object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Set Type</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Set Type</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseSetType(SetType object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Sub Field Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Sub Field Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseSubFieldDeclaration(SubFieldDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Title Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Title Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseTitleDeclaration(TitleDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Include Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Include Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseIncludeDeclaration(IncludeDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Type Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Type Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseTypeDeclaration(TypeDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Validation Rule Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseValidationRuleDeclaration(ValidationRuleDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Comparison Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Comparison Validation Rule Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseComparisonValidationRuleDeclaration(ComparisonValidationRuleDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Comparison Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Comparison Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseComparisonExpression(ComparisonExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Comparison Part</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Comparison Part</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseComparisonPart(ComparisonPart object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Upper Function</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Upper Function</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseUpperFunction(UpperFunction object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Lower Function</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Lower Function</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseLowerFunction(LowerFunction object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Range Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Range Validation Rule Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseRangeValidationRuleDeclaration(RangeValidationRuleDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Regex Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Regex Validation Rule Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseRegexValidationRuleDeclaration(RegexValidationRuleDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Range</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Range</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseRange(Range object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Uniqueness Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Uniqueness Validation Rule Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseUniquenessValidationRuleDeclaration(UniquenessValidationRuleDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Error Message</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Error Message</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseErrorMessage(ErrorMessage object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Native Validation Rule Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Native Validation Rule Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseNativeValidationRuleDeclaration(NativeValidationRuleDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Function Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Function Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFunctionDeclaration(FunctionDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Function Argument Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Function Argument Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFunctionArgumentDeclaration(FunctionArgumentDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Function Argument Body</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Function Argument Body</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFunctionArgumentBody(FunctionArgumentBody object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Function Call</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Function Call</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFunctionCall(FunctionCall object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Field Path</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Field Path</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFieldPath(FieldPath object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Field Reference</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Field Reference</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFieldReference(FieldReference object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Function Arguments</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Function Arguments</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFunctionArguments(FunctionArguments object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Function Body</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Function Body</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFunctionBody(FunctionBody object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Statement</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Statement</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStatement(Statement object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Union Rule</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Union Rule</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseUnionRule(UnionRule object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Query Rule</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Query Rule</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseQueryRule(QueryRule object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Select From</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Select From</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseSelectFrom(SelectFrom object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Select Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Select Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseSelectClause(SelectClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>New Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>New Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseNewExpression(NewExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>From Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>From Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFromClause(FromClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>From Join</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>From Join</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFromJoin(FromJoin object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>With Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>With Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseWithClause(WithClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>From Range</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>From Range</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFromRange(FromRange object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>From Class Or Outer Query Path</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>From Class Or Outer Query Path</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFromClassOrOuterQueryPath(FromClassOrOuterQueryPath object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>In Collection Elements Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>In Collection Elements Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseInCollectionElementsDeclaration(InCollectionElementsDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Group By Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Group By Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseGroupByClause(GroupByClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Order By Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Order By Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOrderByClause(OrderByClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Order Element</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Order Element</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOrderElement(OrderElement object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Having Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Having Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseHavingClause(HavingClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Where Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Where Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseWhereClause(WhereClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Selected Properties List</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Selected Properties List</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseSelectedPropertiesList(SelectedPropertiesList object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Aliased Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Aliased Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseAliasedExpression(AliasedExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExpression(Expression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Logical Or Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Logical Or Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseLogicalOrExpression(LogicalOrExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Logical And Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Logical And Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseLogicalAndExpression(LogicalAndExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Negated Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Negated Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseNegatedExpression(NegatedExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Equality Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Equality Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseEqualityExpression(EqualityExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Relational Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Relational Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseRelationalExpression(RelationalExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Like Escape</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Like Escape</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseLikeEscape(LikeEscape object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Between List</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Between List</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseBetweenList(BetweenList object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Concatenation</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Concatenation</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseConcatenation(Concatenation object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Additive Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Additive Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseAdditiveExpression(AdditiveExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Multiply Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Multiply Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseMultiplyExpression(MultiplyExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Unary Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Unary Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseUnaryExpression(UnaryExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Case Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Case Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseCaseExpression(CaseExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>When Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>When Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseWhenClause(WhenClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Alt When Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Alt When Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseAltWhenClause(AltWhenClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Else Clause</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Else Clause</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseElseClause(ElseClause object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Quantified Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Quantified Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseQuantifiedExpression(QuantifiedExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Atom</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Atom</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseAtom(Atom object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Primary Expression</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Primary Expression</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T casePrimaryExpression(PrimaryExpression object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expression Or Vector</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expression Or Vector</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExpressionOrVector(ExpressionOrVector object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Vector Expr</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Vector Expr</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseVectorExpr(VectorExpr object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Ident Primary</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Ident Primary</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseIdentPrimary(IdentPrimary object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Aggregate</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Aggregate</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseAggregate(Aggregate object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Compound Expr</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Compound Expr</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseCompoundExpr(CompoundExpr object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr List</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr List</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprList(ExprList object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch, but this is the last case anyway.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject)
   * @generated
   */
  public T defaultCase(EObject object)
  {
    return null;
  }

} //MDDSwitch

/**
 * <copyright>
 * </copyright>
 *
 */
package org.makumba.devel.eclipse.mdd.MDD.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.makumba.devel.eclipse.mdd.MDD.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.makumba.devel.eclipse.mdd.MDD.MDDPackage
 * @generated
 */
public class MDDAdapterFactory extends AdapterFactoryImpl
{
  /**
   * The cached model package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected static MDDPackage modelPackage;

  /**
   * Creates an instance of the adapter factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MDDAdapterFactory()
  {
    if (modelPackage == null)
    {
      modelPackage = MDDPackage.eINSTANCE;
    }
  }

  /**
   * Returns whether this factory is applicable for the type of the object.
   * <!-- begin-user-doc -->
   * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
   * <!-- end-user-doc -->
   * @return whether this factory is applicable for the type of the object.
   * @generated
   */
  @Override
  public boolean isFactoryForType(Object object)
  {
    if (object == modelPackage)
    {
      return true;
    }
    if (object instanceof EObject)
    {
      return ((EObject)object).eClass().getEPackage() == modelPackage;
    }
    return false;
  }

  /**
   * The switch that delegates to the <code>createXXX</code> methods.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected MDDSwitch<Adapter> modelSwitch =
    new MDDSwitch<Adapter>()
    {
      @Override
      public Adapter caseDataDefinition(DataDefinition object)
      {
        return createDataDefinitionAdapter();
      }
      @Override
      public Adapter caseDeclaration(Declaration object)
      {
        return createDeclarationAdapter();
      }
      @Override
      public Adapter caseFieldDeclaration(FieldDeclaration object)
      {
        return createFieldDeclarationAdapter();
      }
      @Override
      public Adapter caseModifiers(Modifiers object)
      {
        return createModifiersAdapter();
      }
      @Override
      public Adapter caseFieldType(FieldType object)
      {
        return createFieldTypeAdapter();
      }
      @Override
      public Adapter caseIntEnum(IntEnum object)
      {
        return createIntEnumAdapter();
      }
      @Override
      public Adapter caseCharEnum(CharEnum object)
      {
        return createCharEnumAdapter();
      }
      @Override
      public Adapter caseEnumValue(EnumValue object)
      {
        return createEnumValueAdapter();
      }
      @Override
      public Adapter caseCharType(CharType object)
      {
        return createCharTypeAdapter();
      }
      @Override
      public Adapter casePointerType(PointerType object)
      {
        return createPointerTypeAdapter();
      }
      @Override
      public Adapter caseSetType(SetType object)
      {
        return createSetTypeAdapter();
      }
      @Override
      public Adapter caseSubFieldDeclaration(SubFieldDeclaration object)
      {
        return createSubFieldDeclarationAdapter();
      }
      @Override
      public Adapter caseTitleDeclaration(TitleDeclaration object)
      {
        return createTitleDeclarationAdapter();
      }
      @Override
      public Adapter caseIncludeDeclaration(IncludeDeclaration object)
      {
        return createIncludeDeclarationAdapter();
      }
      @Override
      public Adapter caseTypeDeclaration(TypeDeclaration object)
      {
        return createTypeDeclarationAdapter();
      }
      @Override
      public Adapter caseValidationRuleDeclaration(ValidationRuleDeclaration object)
      {
        return createValidationRuleDeclarationAdapter();
      }
      @Override
      public Adapter caseComparisonValidationRuleDeclaration(ComparisonValidationRuleDeclaration object)
      {
        return createComparisonValidationRuleDeclarationAdapter();
      }
      @Override
      public Adapter caseComparisonExpression(ComparisonExpression object)
      {
        return createComparisonExpressionAdapter();
      }
      @Override
      public Adapter caseComparisonPart(ComparisonPart object)
      {
        return createComparisonPartAdapter();
      }
      @Override
      public Adapter caseUpperFunction(UpperFunction object)
      {
        return createUpperFunctionAdapter();
      }
      @Override
      public Adapter caseLowerFunction(LowerFunction object)
      {
        return createLowerFunctionAdapter();
      }
      @Override
      public Adapter caseRangeValidationRuleDeclaration(RangeValidationRuleDeclaration object)
      {
        return createRangeValidationRuleDeclarationAdapter();
      }
      @Override
      public Adapter caseRegexValidationRuleDeclaration(RegexValidationRuleDeclaration object)
      {
        return createRegexValidationRuleDeclarationAdapter();
      }
      @Override
      public Adapter caseRange(Range object)
      {
        return createRangeAdapter();
      }
      @Override
      public Adapter caseUniquenessValidationRuleDeclaration(UniquenessValidationRuleDeclaration object)
      {
        return createUniquenessValidationRuleDeclarationAdapter();
      }
      @Override
      public Adapter caseErrorMessage(ErrorMessage object)
      {
        return createErrorMessageAdapter();
      }
      @Override
      public Adapter caseNativeValidationRuleDeclaration(NativeValidationRuleDeclaration object)
      {
        return createNativeValidationRuleDeclarationAdapter();
      }
      @Override
      public Adapter caseFunctionDeclaration(FunctionDeclaration object)
      {
        return createFunctionDeclarationAdapter();
      }
      @Override
      public Adapter caseFunctionArgumentDeclaration(FunctionArgumentDeclaration object)
      {
        return createFunctionArgumentDeclarationAdapter();
      }
      @Override
      public Adapter caseFunctionArgumentBody(FunctionArgumentBody object)
      {
        return createFunctionArgumentBodyAdapter();
      }
      @Override
      public Adapter caseFunctionCall(FunctionCall object)
      {
        return createFunctionCallAdapter();
      }
      @Override
      public Adapter caseFieldPath(FieldPath object)
      {
        return createFieldPathAdapter();
      }
      @Override
      public Adapter caseFieldReference(FieldReference object)
      {
        return createFieldReferenceAdapter();
      }
      @Override
      public Adapter caseFunctionArguments(FunctionArguments object)
      {
        return createFunctionArgumentsAdapter();
      }
      @Override
      public Adapter caseFunctionBody(FunctionBody object)
      {
        return createFunctionBodyAdapter();
      }
      @Override
      public Adapter caseStatement(Statement object)
      {
        return createStatementAdapter();
      }
      @Override
      public Adapter caseUnionRule(UnionRule object)
      {
        return createUnionRuleAdapter();
      }
      @Override
      public Adapter caseQueryRule(QueryRule object)
      {
        return createQueryRuleAdapter();
      }
      @Override
      public Adapter caseSelectFrom(SelectFrom object)
      {
        return createSelectFromAdapter();
      }
      @Override
      public Adapter caseSelectClause(SelectClause object)
      {
        return createSelectClauseAdapter();
      }
      @Override
      public Adapter caseNewExpression(NewExpression object)
      {
        return createNewExpressionAdapter();
      }
      @Override
      public Adapter caseFromClause(FromClause object)
      {
        return createFromClauseAdapter();
      }
      @Override
      public Adapter caseFromJoin(FromJoin object)
      {
        return createFromJoinAdapter();
      }
      @Override
      public Adapter caseWithClause(WithClause object)
      {
        return createWithClauseAdapter();
      }
      @Override
      public Adapter caseFromRange(FromRange object)
      {
        return createFromRangeAdapter();
      }
      @Override
      public Adapter caseFromClassOrOuterQueryPath(FromClassOrOuterQueryPath object)
      {
        return createFromClassOrOuterQueryPathAdapter();
      }
      @Override
      public Adapter caseInCollectionElementsDeclaration(InCollectionElementsDeclaration object)
      {
        return createInCollectionElementsDeclarationAdapter();
      }
      @Override
      public Adapter caseGroupByClause(GroupByClause object)
      {
        return createGroupByClauseAdapter();
      }
      @Override
      public Adapter caseOrderByClause(OrderByClause object)
      {
        return createOrderByClauseAdapter();
      }
      @Override
      public Adapter caseOrderElement(OrderElement object)
      {
        return createOrderElementAdapter();
      }
      @Override
      public Adapter caseHavingClause(HavingClause object)
      {
        return createHavingClauseAdapter();
      }
      @Override
      public Adapter caseWhereClause(WhereClause object)
      {
        return createWhereClauseAdapter();
      }
      @Override
      public Adapter caseSelectedPropertiesList(SelectedPropertiesList object)
      {
        return createSelectedPropertiesListAdapter();
      }
      @Override
      public Adapter caseAliasedExpression(AliasedExpression object)
      {
        return createAliasedExpressionAdapter();
      }
      @Override
      public Adapter caseExpression(Expression object)
      {
        return createExpressionAdapter();
      }
      @Override
      public Adapter caseLogicalOrExpression(LogicalOrExpression object)
      {
        return createLogicalOrExpressionAdapter();
      }
      @Override
      public Adapter caseLogicalAndExpression(LogicalAndExpression object)
      {
        return createLogicalAndExpressionAdapter();
      }
      @Override
      public Adapter caseNegatedExpression(NegatedExpression object)
      {
        return createNegatedExpressionAdapter();
      }
      @Override
      public Adapter caseEqualityExpression(EqualityExpression object)
      {
        return createEqualityExpressionAdapter();
      }
      @Override
      public Adapter caseRelationalExpression(RelationalExpression object)
      {
        return createRelationalExpressionAdapter();
      }
      @Override
      public Adapter caseLikeEscape(LikeEscape object)
      {
        return createLikeEscapeAdapter();
      }
      @Override
      public Adapter caseBetweenList(BetweenList object)
      {
        return createBetweenListAdapter();
      }
      @Override
      public Adapter caseConcatenation(Concatenation object)
      {
        return createConcatenationAdapter();
      }
      @Override
      public Adapter caseAdditiveExpression(AdditiveExpression object)
      {
        return createAdditiveExpressionAdapter();
      }
      @Override
      public Adapter caseMultiplyExpression(MultiplyExpression object)
      {
        return createMultiplyExpressionAdapter();
      }
      @Override
      public Adapter caseUnaryExpression(UnaryExpression object)
      {
        return createUnaryExpressionAdapter();
      }
      @Override
      public Adapter caseCaseExpression(CaseExpression object)
      {
        return createCaseExpressionAdapter();
      }
      @Override
      public Adapter caseWhenClause(WhenClause object)
      {
        return createWhenClauseAdapter();
      }
      @Override
      public Adapter caseAltWhenClause(AltWhenClause object)
      {
        return createAltWhenClauseAdapter();
      }
      @Override
      public Adapter caseElseClause(ElseClause object)
      {
        return createElseClauseAdapter();
      }
      @Override
      public Adapter caseQuantifiedExpression(QuantifiedExpression object)
      {
        return createQuantifiedExpressionAdapter();
      }
      @Override
      public Adapter caseAtom(Atom object)
      {
        return createAtomAdapter();
      }
      @Override
      public Adapter casePrimaryExpression(PrimaryExpression object)
      {
        return createPrimaryExpressionAdapter();
      }
      @Override
      public Adapter caseExpressionOrVector(ExpressionOrVector object)
      {
        return createExpressionOrVectorAdapter();
      }
      @Override
      public Adapter caseVectorExpr(VectorExpr object)
      {
        return createVectorExprAdapter();
      }
      @Override
      public Adapter caseIdentPrimary(IdentPrimary object)
      {
        return createIdentPrimaryAdapter();
      }
      @Override
      public Adapter caseAggregate(Aggregate object)
      {
        return createAggregateAdapter();
      }
      @Override
      public Adapter caseCompoundExpr(CompoundExpr object)
      {
        return createCompoundExprAdapter();
      }
      @Override
      public Adapter caseExprList(ExprList object)
      {
        return createExprListAdapter();
      }
      @Override
      public Adapter defaultCase(EObject object)
      {
        return createEObjectAdapter();
      }
    };

  /**
   * Creates an adapter for the <code>target</code>.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param target the object to adapt.
   * @return the adapter for the <code>target</code>.
   * @generated
   */
  @Override
  public Adapter createAdapter(Notifier target)
  {
    return modelSwitch.doSwitch((EObject)target);
  }


  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.DataDefinition <em>Data Definition</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.DataDefinition
   * @generated
   */
  public Adapter createDataDefinitionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.Declaration <em>Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.Declaration
   * @generated
   */
  public Adapter createDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration <em>Field Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration
   * @generated
   */
  public Adapter createFieldDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.Modifiers <em>Modifiers</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.Modifiers
   * @generated
   */
  public Adapter createModifiersAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FieldType <em>Field Type</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldType
   * @generated
   */
  public Adapter createFieldTypeAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.IntEnum <em>Int Enum</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.IntEnum
   * @generated
   */
  public Adapter createIntEnumAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.CharEnum <em>Char Enum</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.CharEnum
   * @generated
   */
  public Adapter createCharEnumAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.EnumValue <em>Enum Value</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.EnumValue
   * @generated
   */
  public Adapter createEnumValueAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.CharType <em>Char Type</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.CharType
   * @generated
   */
  public Adapter createCharTypeAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.PointerType <em>Pointer Type</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.PointerType
   * @generated
   */
  public Adapter createPointerTypeAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.SetType <em>Set Type</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.SetType
   * @generated
   */
  public Adapter createSetTypeAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration <em>Sub Field Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.SubFieldDeclaration
   * @generated
   */
  public Adapter createSubFieldDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration <em>Title Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.TitleDeclaration
   * @generated
   */
  public Adapter createTitleDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.IncludeDeclaration <em>Include Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.IncludeDeclaration
   * @generated
   */
  public Adapter createIncludeDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration <em>Type Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.TypeDeclaration
   * @generated
   */
  public Adapter createTypeDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration <em>Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.ValidationRuleDeclaration
   * @generated
   */
  public Adapter createValidationRuleDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration <em>Comparison Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonValidationRuleDeclaration
   * @generated
   */
  public Adapter createComparisonValidationRuleDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression <em>Comparison Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonExpression
   * @generated
   */
  public Adapter createComparisonExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.ComparisonPart <em>Comparison Part</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.ComparisonPart
   * @generated
   */
  public Adapter createComparisonPartAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.UpperFunction <em>Upper Function</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.UpperFunction
   * @generated
   */
  public Adapter createUpperFunctionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.LowerFunction <em>Lower Function</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.LowerFunction
   * @generated
   */
  public Adapter createLowerFunctionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration <em>Range Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.RangeValidationRuleDeclaration
   * @generated
   */
  public Adapter createRangeValidationRuleDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration <em>Regex Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.RegexValidationRuleDeclaration
   * @generated
   */
  public Adapter createRegexValidationRuleDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.Range <em>Range</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.Range
   * @generated
   */
  public Adapter createRangeAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.UniquenessValidationRuleDeclaration <em>Uniqueness Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.UniquenessValidationRuleDeclaration
   * @generated
   */
  public Adapter createUniquenessValidationRuleDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.ErrorMessage <em>Error Message</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.ErrorMessage
   * @generated
   */
  public Adapter createErrorMessageAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration <em>Native Validation Rule Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.NativeValidationRuleDeclaration
   * @generated
   */
  public Adapter createNativeValidationRuleDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration <em>Function Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionDeclaration
   * @generated
   */
  public Adapter createFunctionDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentDeclaration <em>Function Argument Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentDeclaration
   * @generated
   */
  public Adapter createFunctionArgumentDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentBody <em>Function Argument Body</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionArgumentBody
   * @generated
   */
  public Adapter createFunctionArgumentBodyAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionCall <em>Function Call</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionCall
   * @generated
   */
  public Adapter createFunctionCallAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FieldPath <em>Field Path</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldPath
   * @generated
   */
  public Adapter createFieldPathAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FieldReference <em>Field Reference</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FieldReference
   * @generated
   */
  public Adapter createFieldReferenceAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionArguments <em>Function Arguments</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionArguments
   * @generated
   */
  public Adapter createFunctionArgumentsAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FunctionBody <em>Function Body</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FunctionBody
   * @generated
   */
  public Adapter createFunctionBodyAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.Statement <em>Statement</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.Statement
   * @generated
   */
  public Adapter createStatementAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.UnionRule <em>Union Rule</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.UnionRule
   * @generated
   */
  public Adapter createUnionRuleAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.QueryRule <em>Query Rule</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.QueryRule
   * @generated
   */
  public Adapter createQueryRuleAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.SelectFrom <em>Select From</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectFrom
   * @generated
   */
  public Adapter createSelectFromAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.SelectClause <em>Select Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectClause
   * @generated
   */
  public Adapter createSelectClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.NewExpression <em>New Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.NewExpression
   * @generated
   */
  public Adapter createNewExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FromClause <em>From Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClause
   * @generated
   */
  public Adapter createFromClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FromJoin <em>From Join</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromJoin
   * @generated
   */
  public Adapter createFromJoinAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.WithClause <em>With Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.WithClause
   * @generated
   */
  public Adapter createWithClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FromRange <em>From Range</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromRange
   * @generated
   */
  public Adapter createFromRangeAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath <em>From Class Or Outer Query Path</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.FromClassOrOuterQueryPath
   * @generated
   */
  public Adapter createFromClassOrOuterQueryPathAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.InCollectionElementsDeclaration <em>In Collection Elements Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.InCollectionElementsDeclaration
   * @generated
   */
  public Adapter createInCollectionElementsDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.GroupByClause <em>Group By Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.GroupByClause
   * @generated
   */
  public Adapter createGroupByClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.OrderByClause <em>Order By Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.OrderByClause
   * @generated
   */
  public Adapter createOrderByClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.OrderElement <em>Order Element</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.OrderElement
   * @generated
   */
  public Adapter createOrderElementAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.HavingClause <em>Having Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.HavingClause
   * @generated
   */
  public Adapter createHavingClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.WhereClause <em>Where Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.WhereClause
   * @generated
   */
  public Adapter createWhereClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.SelectedPropertiesList <em>Selected Properties List</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.SelectedPropertiesList
   * @generated
   */
  public Adapter createSelectedPropertiesListAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.AliasedExpression <em>Aliased Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.AliasedExpression
   * @generated
   */
  public Adapter createAliasedExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.Expression <em>Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.Expression
   * @generated
   */
  public Adapter createExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.LogicalOrExpression <em>Logical Or Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.LogicalOrExpression
   * @generated
   */
  public Adapter createLogicalOrExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.LogicalAndExpression <em>Logical And Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.LogicalAndExpression
   * @generated
   */
  public Adapter createLogicalAndExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.NegatedExpression <em>Negated Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.NegatedExpression
   * @generated
   */
  public Adapter createNegatedExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.EqualityExpression <em>Equality Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.EqualityExpression
   * @generated
   */
  public Adapter createEqualityExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.RelationalExpression <em>Relational Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.RelationalExpression
   * @generated
   */
  public Adapter createRelationalExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.LikeEscape <em>Like Escape</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.LikeEscape
   * @generated
   */
  public Adapter createLikeEscapeAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.BetweenList <em>Between List</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.BetweenList
   * @generated
   */
  public Adapter createBetweenListAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.Concatenation <em>Concatenation</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.Concatenation
   * @generated
   */
  public Adapter createConcatenationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.AdditiveExpression <em>Additive Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.AdditiveExpression
   * @generated
   */
  public Adapter createAdditiveExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.MultiplyExpression <em>Multiply Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.MultiplyExpression
   * @generated
   */
  public Adapter createMultiplyExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.UnaryExpression <em>Unary Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.UnaryExpression
   * @generated
   */
  public Adapter createUnaryExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.CaseExpression <em>Case Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.CaseExpression
   * @generated
   */
  public Adapter createCaseExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.WhenClause <em>When Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.WhenClause
   * @generated
   */
  public Adapter createWhenClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.AltWhenClause <em>Alt When Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.AltWhenClause
   * @generated
   */
  public Adapter createAltWhenClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.ElseClause <em>Else Clause</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.ElseClause
   * @generated
   */
  public Adapter createElseClauseAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.QuantifiedExpression <em>Quantified Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.QuantifiedExpression
   * @generated
   */
  public Adapter createQuantifiedExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.Atom <em>Atom</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.Atom
   * @generated
   */
  public Adapter createAtomAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.PrimaryExpression <em>Primary Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.PrimaryExpression
   * @generated
   */
  public Adapter createPrimaryExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.ExpressionOrVector <em>Expression Or Vector</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.ExpressionOrVector
   * @generated
   */
  public Adapter createExpressionOrVectorAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.VectorExpr <em>Vector Expr</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.VectorExpr
   * @generated
   */
  public Adapter createVectorExprAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.IdentPrimary <em>Ident Primary</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.IdentPrimary
   * @generated
   */
  public Adapter createIdentPrimaryAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.Aggregate <em>Aggregate</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.Aggregate
   * @generated
   */
  public Adapter createAggregateAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.CompoundExpr <em>Compound Expr</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.CompoundExpr
   * @generated
   */
  public Adapter createCompoundExprAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.makumba.devel.eclipse.mdd.MDD.ExprList <em>Expr List</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.makumba.devel.eclipse.mdd.MDD.ExprList
   * @generated
   */
  public Adapter createExprListAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for the default case.
   * <!-- begin-user-doc -->
   * This default implementation returns null.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @generated
   */
  public Adapter createEObjectAdapter()
  {
    return null;
  }

} //MDDAdapterFactory

/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.BLangCompilerConstants;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BReadonlyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangMappingBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangInputClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangListMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMappingMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.ResolvedTypeBuilder;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.BBYTE_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.BBYTE_MIN_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED16_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED16_MIN_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED32_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED32_MIN_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED8_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.SIGNED8_MIN_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.UNSIGNED16_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.UNSIGNED32_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.UNSIGNED8_MAX_VALUE;

/**
 * This class consists of utility methods which operate on types.
 * These utility methods allows you to check the compatibility of two types,
 * i.e. check whether two types are equal, check whether one type is assignable to another type etc.
 *
 * @since 0.94
 */
public class Types {

    private static final CompilerContext.Key<Types> TYPES_KEY =
            new CompilerContext.Key<>();
    private final ResolvedTypeBuilder typeBuilder;

    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private BLangDiagnosticLog dlog;
    private Names names;
    private int finiteTypeCount = 0;
    private BUnionType expandedXMLBuiltinSubtypes;
    private final BLangAnonymousModelHelper anonymousModelHelper;
    private int recordCount = 0;
    private SymbolEnv env;

    public static Types getInstance(CompilerContext context) {
        Types types = context.get(TYPES_KEY);
        if (types == null) {
            types = new Types(context);
        }

        return types;
    }

    public Types(CompilerContext context) {
        context.put(TYPES_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.names = Names.getInstance(context);
        this.expandedXMLBuiltinSubtypes = BUnionType.create(null,
                                                            symTable.xmlElementType, symTable.xmlCommentType,
                                                            symTable.xmlPIType, symTable.xmlTextType);
        this.typeBuilder = new ResolvedTypeBuilder();
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
    }

    public List<BType> checkTypes(BLangExpression node,
                                  List<BType> actualTypes,
                                  List<BType> expTypes) {
        List<BType> resTypes = new ArrayList<>();
        for (int i = 0; i < actualTypes.size(); i++) {
            resTypes.add(checkType(node, actualTypes.get(i), expTypes.size() > i ? expTypes.get(i) : symTable.noType));
        }
        return resTypes;
    }

    public BType checkType(BLangExpression node,
                           BType actualType,
                           BType expType) {
        return checkType(node, actualType, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES);
    }

    public BType checkType(BLangExpression expr,
                           BType actualType,
                           BType expType,
                           DiagnosticCode diagCode) {
        expr.type = checkType(expr.pos, actualType, expType, diagCode);
        if (expr.type.tag == TypeTags.SEMANTIC_ERROR) {
            return expr.type;
        }

        // Set an implicit cast expression, if applicable
        setImplicitCastExpr(expr, actualType, expType);

        return expr.type;
    }

    public BType checkType(Location pos,
                           BType actualType,
                           BType expType,
                           DiagnosticCode diagCode) {
        if (expType.tag == TypeTags.SEMANTIC_ERROR) {
            return expType;
        } else if (expType.tag == TypeTags.NONE) {
            return actualType;
        } else if (actualType.tag == TypeTags.SEMANTIC_ERROR) {
            return actualType;
        } else if (isAssignable(actualType, expType)) {
            return actualType;
        }

        // e.g. incompatible types: expected 'int', found 'string'
        dlog.error(pos, diagCode, expType, actualType);
        return symTable.semanticError;
    }

    public boolean isJSONContext(BType type) {
        if (type.tag == TypeTags.UNION) {
            return ((BUnionType) type).getMemberTypes().stream().anyMatch(memType -> memType.tag == TypeTags.JSON);
        }
        return type.tag == TypeTags.JSON;
    }

    public boolean isJSONUnionType(BUnionType type) {
        if (type.name != null && (type.name.getValue().equals(Names.JSON.getValue()))) {
            return true;
        }
        return isSameType(type, symTable.jsonType);
    }

    public boolean isLax(BType type) {
        Set<BType> visited = new HashSet<>();
        int result = isLaxType(type, visited);
        if (result == 1) {
            return true;
        }
        return false;
    }

    // TODO : clean
    public int isLaxType(BType type, Set<BType> visited) {
        if (!visited.add(type)) {
            return -1;
        }
        switch (type.tag) {
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.XML_ELEMENT:
                return 1;
            case TypeTags.MAP:
                return isLaxType(((BMapType) type).constraint, visited);
            case TypeTags.UNION:
                if (isSameType(type, symTable.jsonType)) {
                    visited.add(type);
                    return 1;
                }
                boolean atleastOneLaxType = false;
                for (BType member : ((BUnionType) type).getMemberTypes()) {
                    int result = isLaxType(member, visited);
                    if (result == -1) {
                        continue;
                    }
                    if (result == 0) {
                        return 0;
                    }
                    atleastOneLaxType = true;
                }
                return atleastOneLaxType ? 1 : 0;
        }
        return 0;
    }

    public boolean isLaxType(BType type, Map<BType, Boolean> visited) {
        if (visited.containsKey(type)) {
            return visited.get(type);
        }
        switch (type.tag) {
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.XML_ELEMENT:
                visited.put(type, true);
                return true;
            case TypeTags.MAP:
                boolean result = isLaxType(((BMapType) type).constraint, visited);
                visited.put(type, result);
                return result;
            case TypeTags.UNION:
                // TODO: remove
                if (type == symTable.jsonType || isSameType(type, symTable.jsonType)) {
                    visited.put(type, true);
                    return true;
                }
                for (BType member : ((BUnionType) type).getMemberTypes()) {
                    if (!isLaxType(member, visited)) {
                        visited.put(type, false);
                        return false;
                    }
                }
                visited.put(type, true);
                return true;
            }
        visited.put(type, false);
        return false;
    }

    public boolean isSameType(BType source, BType target) {
        return isSameType(source, target, new HashSet<>());
    }

    public boolean isPureType(BType type) {
        IsPureTypeUniqueVisitor visitor = new IsPureTypeUniqueVisitor();
        return visitor.visit(type);
    }

    public boolean isAnydata(BType type) {
        IsAnydataUniqueVisitor visitor = new IsAnydataUniqueVisitor();
        return visitor.visit(type);
    }

    private boolean isSameType(BType source, BType target, Set<TypePair> unresolvedTypes) {
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(source, target);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        BTypeVisitor<BType, Boolean> sameTypeVisitor = new BSameTypeVisitor(unresolvedTypes);
        return target.accept(sameTypeVisitor, source);
    }

    public boolean isValueType(BType type) {
        switch (type.tag) {
            case TypeTags.BOOLEAN:
            case TypeTags.BYTE:
            case TypeTags.DECIMAL:
            case TypeTags.FLOAT:
            case TypeTags.INT:
            case TypeTags.STRING:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.CHAR_STRING:
                return true;
            default:
                return false;
        }
    }

    boolean isBasicNumericType(BType type) {

        return type.tag < TypeTags.STRING || TypeTags.isIntegerTypeTag(type.tag);
    }

    boolean finiteTypeContainsNumericTypeValues(BFiniteType finiteType) {
        return finiteType.getValueSpace().stream().anyMatch(valueExpr -> isBasicNumericType(valueExpr.type));
    }

    public boolean containsErrorType(BType type) {
        if (type.tag == TypeTags.UNION) {
            return ((BUnionType) type).getMemberTypes().stream()
                    .anyMatch(this::containsErrorType);
        }

        if (type.tag == TypeTags.READONLY) {
            return true;
        }

        return type.tag == TypeTags.ERROR;
    }

    public boolean isSubTypeOfList(BType type) {
        if (type.tag != TypeTags.UNION) {
            return isSubTypeOfBaseType(type, TypeTags.ARRAY) || isSubTypeOfBaseType(type, TypeTags.TUPLE);
        }

        return ((BUnionType) type).getMemberTypes().stream().allMatch(this::isSubTypeOfList);
    }

    public BType resolvePatternTypeFromMatchExpr(BLangListBindingPattern listBindingPattern,
                                                 BLangVarBindingPatternMatchPattern varBindingPatternMatchPattern,
                                                 SymbolEnv env) {
        BTupleType listBindingPatternType = (BTupleType) listBindingPattern.type;
        if (varBindingPatternMatchPattern.matchExpr == null) {
            return listBindingPatternType;
        }
        BType matchExprType = varBindingPatternMatchPattern.matchExpr.type;
        BType intersectionType = getTypeIntersection(matchExprType, listBindingPatternType, env);
        if (intersectionType != symTable.semanticError) {
            return intersectionType;
        }
        if (matchExprType.tag == TypeTags.ANYDATA) {
            Collections.fill(listBindingPatternType.tupleTypes, symTable.anydataType);
            if (listBindingPatternType.restType != null) {
                listBindingPatternType.restType = symTable.anydataType;
            }
            return listBindingPatternType;
        }
        return symTable.noType;
    }

    public BType resolvePatternTypeFromMatchExpr(BLangListMatchPattern listMatchPattern,
                                                 BTupleType listMatchPatternType, SymbolEnv env) {
        if (listMatchPattern.matchExpr == null) {
            return listMatchPatternType;
        }
        BType matchExprType = listMatchPattern.matchExpr.type;
        BType intersectionType = getTypeIntersection(matchExprType, listMatchPatternType, env);
        if (intersectionType != symTable.semanticError) {
            return intersectionType;
        }
        if (matchExprType.tag == TypeTags.ANYDATA) {
            Collections.fill(listMatchPatternType.tupleTypes, symTable.anydataType);
            if (listMatchPatternType.restType != null) {
                listMatchPatternType.restType = symTable.anydataType;
            }
            return listMatchPatternType;
        }
        return symTable.noType;
    }

    public BType resolvePatternTypeFromMatchExpr(BLangErrorMatchPattern errorMatchPattern, BLangExpression matchExpr) {
        if (matchExpr == null) {
            return errorMatchPattern.type;
        }

        BType matchExprType = matchExpr.type;
        BType patternType = errorMatchPattern.type;
        if (isAssignable(matchExprType, patternType)) {
            return matchExprType;
        }
        if (isAssignable(patternType, matchExprType)) {
            return patternType;
        }
        return symTable.noType;
    }

    public BType resolvePatternTypeFromMatchExpr(BLangConstPattern constPattern, BLangExpression constPatternExpr) {
        if (constPattern.matchExpr == null) {
            if (constPatternExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                return ((BLangSimpleVarRef) constPatternExpr).symbol.type;
            } else {
                return constPatternExpr.type;
            }
        }

        BType matchExprType = constPattern.matchExpr.type;
        BType constMatchPatternExprType = constPatternExpr.type;

        if (constPatternExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef constVarRef = (BLangSimpleVarRef) constPatternExpr;
            if (constVarRef.symbol == null) {
                return symTable.noType;
            }
            BType constVarRefSymbolType = constVarRef.symbol.type;
            if (isAssignable(constVarRefSymbolType, matchExprType)) {
                return constVarRefSymbolType;
            }
            return symTable.noType;
        }
        // After the above check, according to spec all other const-patterns should be literals.
        BLangLiteral constPatternLiteral = (BLangLiteral) constPatternExpr;
        if (containsAnyType(constMatchPatternExprType)) {
            return matchExprType;
        } else if (containsAnyType(matchExprType)) {
            return constMatchPatternExprType;
        }
        // This should handle specially
        if (matchExprType.tag == TypeTags.BYTE && constMatchPatternExprType.tag == TypeTags.INT) {
            return matchExprType;
        }
        if (isAssignable(constMatchPatternExprType, matchExprType)) {
            return constMatchPatternExprType;
        }
        if (matchExprType.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) matchExprType).getMemberTypes()) {
                if (memberType.tag == TypeTags.FINITE) {
                    if (isAssignableToFiniteType(memberType, constPatternLiteral)) {
                        return memberType;
                    }
                } else {
                    if (isAssignable(constMatchPatternExprType, matchExprType)) {
                        return constMatchPatternExprType;
                    }
                }
            }
        } else if (matchExprType.tag == TypeTags.FINITE) {
            if (isAssignableToFiniteType(matchExprType, constPatternLiteral)) {
                return matchExprType;
            }
        }
        return symTable.noType;
    }

    BType resolvePatternTypeFromMatchExpr(BLangMappingMatchPattern mappingMatchPattern, BType patternType,
                                                 SymbolEnv env) {
        if (mappingMatchPattern.matchExpr == null) {
            return patternType;
        }
        BType intersectionType = getTypeIntersection(mappingMatchPattern.matchExpr.type, patternType, env);
        if (intersectionType == symTable.semanticError) {
            return symTable.noType;
        }
        return intersectionType;
    }

    public BType resolvePatternTypeFromMatchExpr(BLangMappingBindingPattern mappingBindingPattern,
                                                 BLangVarBindingPatternMatchPattern varBindingPatternMatchPattern,
                                                 SymbolEnv env) {
        BRecordType mappingBindingPatternType = (BRecordType) mappingBindingPattern.type;
        if (varBindingPatternMatchPattern.matchExpr == null) {
            return mappingBindingPatternType;
        }
        BType intersectionType = getTypeIntersection(varBindingPatternMatchPattern.matchExpr.type,
                mappingBindingPatternType, env);
        if (intersectionType == symTable.semanticError) {
            return symTable.noType;
        }
        return intersectionType;
    }

    private boolean containsAnyType(BType type) {
        if (type.tag != TypeTags.UNION) {
            return type.tag == TypeTags.ANY;
        }

        for (BType memberTypes : ((BUnionType) type).getMemberTypes()) {
            if (memberTypes.tag == TypeTags.ANY) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAnyDataType(BType type) {
        if (type.tag != TypeTags.UNION) {
            return type.tag == TypeTags.ANYDATA;
        }

        for (BType memberTypes : ((BUnionType) type).getMemberTypes()) {
            if (memberTypes.tag == TypeTags.ANYDATA) {
                return true;
            }
        }
        return false;
    }

    BType mergeTypes(BType typeFirst, BType typeSecond) {
        if (containsAnyType(typeFirst) && !containsErrorType(typeSecond)) {
            return typeSecond;
        }
        if (containsAnyType(typeSecond) && !containsErrorType(typeFirst)) {
            return typeFirst;
        }
        if (containsAnyDataType(typeFirst) && !containsErrorType(typeSecond)) {
            return typeSecond;
        }
        if (containsAnyDataType(typeSecond) && !containsErrorType(typeFirst)) {
            return typeFirst;
        }
        if (isSameBasicType(typeFirst, typeSecond)) {
            return typeFirst;
        }
        return BUnionType.create(null, typeFirst, typeSecond);
    }

    public boolean isSubTypeOfMapping(BType type) {
        if (type.tag != TypeTags.UNION) {
            return isSubTypeOfBaseType(type, TypeTags.MAP) || isSubTypeOfBaseType(type, TypeTags.RECORD);
        }

        return ((BUnionType) type).getMemberTypes().stream().allMatch(this::isSubTypeOfMapping);
    }

    public boolean isSubTypeOfBaseType(BType type, int baseTypeTag) {
        if (type.tag != TypeTags.UNION) {
            return type.tag == baseTypeTag;
        }
        // TODO: Recheck this
        if (TypeTags.isXMLTypeTag(baseTypeTag)) {
            return true;
        }
        return ((BUnionType) type).getMemberTypes().stream().allMatch(memType -> memType.tag == baseTypeTag);
    }

    /**
     * Checks whether source type is assignable to the target type.
     * <p>
     * Source type is assignable to the target type if,
     * 1) the target type is any and the source type is not a value type.
     * 2) there exists an implicit cast symbol from source to target.
     * 3) both types are JSON and the target constraint is no type.
     * 4) both types are array type and both array types are assignable.
     * 5) both types are MAP and the target constraint is any type or constraints are structurally equivalent.
     *
     * @param source type.
     * @param target type.
     * @return true if source type is assignable to the target type.
     */
    public boolean isAssignable(BType source, BType target) {
        return isAssignable(source, target, new HashSet<>());
    }

    boolean isStampingAllowed(BType source, BType target) {
        return (isAssignable(source, target) || isAssignable(target, source) ||
                checkTypeEquivalencyForStamping(source, target) || checkTypeEquivalencyForStamping(target, source));
    }

    private boolean checkTypeEquivalencyForStamping(BType source, BType target) {
        if (target.tag == TypeTags.RECORD) {
            if (source.tag == TypeTags.RECORD) {
                TypePair pair = new TypePair(source, target);
                Set<TypePair> unresolvedTypes = new HashSet<>();
                unresolvedTypes.add(pair);
                return checkRecordEquivalencyForStamping((BRecordType) source, (BRecordType) target, unresolvedTypes);
            } else if (source.tag == TypeTags.MAP) {
                int mapConstraintTypeTag = ((BMapType) source).constraint.tag;
                if ((!(mapConstraintTypeTag == TypeTags.ANY || mapConstraintTypeTag == TypeTags.ANYDATA)) &&
                        ((BRecordType) target).sealed) {
                    for (BField field : ((BStructureType) target).getFields().values()) {
                        if (field.getType().tag != mapConstraintTypeTag) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } else if (target.tag == TypeTags.JSON) {
            return source.tag == TypeTags.JSON || source.tag == TypeTags.RECORD || source.tag == TypeTags.MAP;
        } else if (target.tag == TypeTags.MAP) {
            if (source.tag == TypeTags.MAP) {
                return isStampingAllowed(((BMapType) source).getConstraint(), ((BMapType) target).getConstraint());
            } else if (source.tag == TypeTags.UNION) {
                return checkUnionEquivalencyForStamping(source, target);
            }
        } else if (target.tag == TypeTags.ARRAY) {
            if (source.tag == TypeTags.JSON) {
                return true;
            } else if (source.tag == TypeTags.TUPLE) {
                BType arrayElementType = ((BArrayType) target).eType;
                for (BType tupleMemberType : ((BTupleType) source).getTupleTypes()) {
                    if (!isStampingAllowed(tupleMemberType, arrayElementType)) {
                        return false;
                    }
                }
                return true;
            } else if (source.tag == TypeTags.ARRAY) {
                return checkTypeEquivalencyForStamping(((BArrayType) source).eType, ((BArrayType) target).eType);
            }
        } else if (target.tag == TypeTags.UNION) {
            return checkUnionEquivalencyForStamping(source, target);
        } else if (target.tag == TypeTags.TUPLE && source.tag == TypeTags.TUPLE) {
            return checkTupleEquivalencyForStamping(source, target);
        }

        return false;
    }

    private boolean checkRecordEquivalencyForStamping(BRecordType rhsType, BRecordType lhsType,
                                                      Set<TypePair> unresolvedTypes) {
        // Both records should be public or private.
        // Get the XOR of both flags(masks)
        // If both are public, then public bit should be 0;
        // If both are private, then public bit should be 0;
        // The public bit is on means, one is public, and the other one is private.
        if (Symbols.isFlagOn(lhsType.tsymbol.flags ^ rhsType.tsymbol.flags, Flags.PUBLIC)) {
            return false;
        }

        // If both records are private, they should be in the same package.
        if (Symbols.isPrivate(lhsType.tsymbol) && rhsType.tsymbol.pkgID != lhsType.tsymbol.pkgID) {
            return false;
        }

        // RHS type should have at least all the fields as well attached functions of LHS type.
        if (lhsType.fields.size() > rhsType.fields.size()) {
            return false;
        }

        // If only one is a closed record, the records aren't equivalent
        if (lhsType.sealed && !rhsType.sealed) {
            return false;
        }

        return checkFieldEquivalencyForStamping(lhsType, rhsType, unresolvedTypes);
    }

    private boolean checkFieldEquivalencyForStamping(BStructureType lhsType, BStructureType rhsType,
                                                     Set<TypePair> unresolvedTypes) {
        for (BField lhsField : lhsType.fields.values()) {
            BField rhsField = rhsType.fields.get(lhsField.name.value);

            if (rhsField == null || !isStampingAllowed(rhsField.type, lhsField.type)) {
                return false;
            }
        }

        for (BField rhsField : rhsType.fields.values()) {
            BField lhsField = lhsType.fields.get(rhsField.name.value);

            if (lhsField == null && !isStampingAllowed(rhsField.type, ((BRecordType) lhsType).restFieldType)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkUnionEquivalencyForStamping(BType source, BType target) {
        Set<BType> sourceTypes = new LinkedHashSet<>();
        Set<BType> targetTypes = new LinkedHashSet<>();

        if (source.tag == TypeTags.UNION) {
            BUnionType sourceUnionType = (BUnionType) source;
            sourceTypes.addAll(sourceUnionType.getMemberTypes());
        } else {
            sourceTypes.add(source);
        }

        if (target.tag == TypeTags.UNION) {
            BUnionType targetUnionType = (BUnionType) target;
            targetTypes.addAll(targetUnionType.getMemberTypes());
        } else {
            targetTypes.add(target);
        }

        boolean notAssignable = sourceTypes
                .stream()
                .map(s -> targetTypes
                        .stream()
                        .anyMatch(t -> isStampingAllowed(s, t)))
                .anyMatch(assignable -> !assignable);

        return !notAssignable;
    }

    private boolean checkTupleEquivalencyForStamping(BType source, BType target) {
        if (source.tag != TypeTags.TUPLE || target.tag != TypeTags.TUPLE) {
            return false;
        }

        BTupleType lhsTupleType = (BTupleType) target;
        BTupleType rhsTupleType = (BTupleType) source;

        if (lhsTupleType.tupleTypes.size() != rhsTupleType.tupleTypes.size()) {
            return false;
        }

        for (int i = 0; i < lhsTupleType.tupleTypes.size(); i++) {
            if (!isStampingAllowed(rhsTupleType.tupleTypes.get(i), lhsTupleType.tupleTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isAssignable(BType source, BType target, Set<TypePair> unresolvedTypes) {

        if (isSameType(source, target)) {
            return true;
        }

        int sourceTag = source.tag;
        int targetTag = target.tag;

        if (!Symbols.isFlagOn(source.flags, Flags.PARAMETERIZED) &&
                !isInherentlyImmutableType(target) && Symbols.isFlagOn(target.flags, Flags.READONLY) &&
                !isInherentlyImmutableType(source) && isMutable(source)) {
            return false;
        }

        if (sourceTag == TypeTags.INTERSECTION) {
            return isAssignable(((BIntersectionType) source).effectiveType,
                                targetTag != TypeTags.INTERSECTION ? target :
                                        ((BIntersectionType) target).effectiveType, unresolvedTypes);
        }

        if (targetTag == TypeTags.INTERSECTION) {
            return isAssignable(source, ((BIntersectionType) target).effectiveType, unresolvedTypes);
        }

        if (sourceTag == TypeTags.PARAMETERIZED_TYPE) {
            return isParameterizedTypeAssignable(source, target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.BYTE && targetTag == TypeTags.INT) {
            return true;
        }

        if (TypeTags.isXMLTypeTag(sourceTag) && (TypeTags.isXMLTypeTag(targetTag) || targetTag == TypeTags.STRING)) {
            return isXMLTypeAssignable(source, target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.CHAR_STRING && targetTag == TypeTags.STRING) {
            return true;
        }

        if (sourceTag == TypeTags.CHAR_STRING && targetTag == TypeTags.XML_TEXT) {
            return true;
        }

        if (sourceTag == TypeTags.XML_TEXT && targetTag == TypeTags.CHAR_STRING) {
            return true;
        }

        if (sourceTag == TypeTags.ERROR && targetTag == TypeTags.ERROR) {
            return isErrorTypeAssignable((BErrorType) source, (BErrorType) target, unresolvedTypes);
        } else if (sourceTag == TypeTags.ERROR && targetTag == TypeTags.ANY) {
            return false;
        }

        if (sourceTag == TypeTags.NIL && (isNullable(target) || targetTag == TypeTags.JSON)) {
            return true;
        }

        // TODO: Remove the isValueType() check
        if (targetTag == TypeTags.ANY && !containsErrorType(source) && !isValueType(source)) {
            return true;
        }

        if (targetTag == TypeTags.ANYDATA && !containsErrorType(source)) {
            if (isAnydata(source)) {
                return true;
            }
        }

        if (targetTag == TypeTags.READONLY) {
            if ((isInherentlyImmutableType(source) || Symbols.isFlagOn(source.flags, Flags.READONLY))) {
                return true;
            }
            if (isAssignable(source, symTable.anyAndReadonlyOrError, unresolvedTypes)) {
                return true;
            }
        }

        if (sourceTag == TypeTags.READONLY && isAssignable(symTable.anyAndReadonlyOrError, target, unresolvedTypes)) {
            return true;
        }

        if (targetTag == TypeTags.MAP && sourceTag == TypeTags.RECORD) {
            BRecordType recordType = (BRecordType) source;
            return isAssignableRecordType(recordType, target, unresolvedTypes);
        }

        if (targetTag == TypeTags.RECORD && sourceTag == TypeTags.MAP) {
            return isAssignableMapType((BMapType) source, (BRecordType) target);
        }

        if (targetTag == TypeTags.TYPEDESC && sourceTag == TypeTags.TYPEDESC) {
            return isAssignable(((BTypedescType) source).constraint, (((BTypedescType) target).constraint),
                                unresolvedTypes);
        }

        if (targetTag == TypeTags.TABLE && sourceTag == TypeTags.TABLE) {
            return isAssignableTableType((BTableType) source, (BTableType) target, unresolvedTypes);
        }

        if (targetTag == TypeTags.STREAM && sourceTag == TypeTags.STREAM) {
            return isAssignableStreamType((BStreamType) source, (BStreamType) target, unresolvedTypes);
        }

        if (isBuiltInTypeWidenPossible(source, target) == TypeTestResult.TRUE) {
            return true;
        }

        if (sourceTag == TypeTags.FINITE) {
            return isFiniteTypeAssignable((BFiniteType) source, target, unresolvedTypes);
        }

        if ((targetTag == TypeTags.UNION || sourceTag == TypeTags.UNION) &&
                isAssignableToUnionType(source, target, unresolvedTypes)) {
            return true;
        }

        if (targetTag == TypeTags.JSON) {
            if (sourceTag == TypeTags.JSON) {
                return true;
            }

            if (sourceTag == TypeTags.ARRAY) {
                return isArrayTypesAssignable((BArrayType) source, target, unresolvedTypes);
            }

            if (sourceTag == TypeTags.MAP) {
                return isAssignable(((BMapType) source).constraint, target, unresolvedTypes);
            }

            if (sourceTag == TypeTags.RECORD) {
                return isAssignableRecordType((BRecordType) source, target, unresolvedTypes);
            }

        }

        if (targetTag == TypeTags.FUTURE && sourceTag == TypeTags.FUTURE) {
            if (((BFutureType) target).constraint.tag == TypeTags.NONE) {
                return true;
            }
            return isAssignable(((BFutureType) source).constraint, ((BFutureType) target).constraint, unresolvedTypes);
        }

        if (targetTag == TypeTags.MAP && sourceTag == TypeTags.MAP) {
            // Here source condition is added for prevent assigning map union constrained
            // to map any constrained.
            if (((BMapType) target).constraint.tag == TypeTags.ANY &&
                    ((BMapType) source).constraint.tag != TypeTags.UNION) {
                return true;
            }

            return isAssignable(((BMapType) source).constraint, ((BMapType) target).constraint, unresolvedTypes);
        }

        if ((sourceTag == TypeTags.OBJECT || sourceTag == TypeTags.RECORD)
                && (targetTag == TypeTags.OBJECT || targetTag == TypeTags.RECORD)) {
            return checkStructEquivalency(source, target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.TUPLE && targetTag == TypeTags.ARRAY) {
            return isTupleTypeAssignableToArrayType((BTupleType) source, (BArrayType) target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.ARRAY && targetTag == TypeTags.TUPLE) {
            return isArrayTypeAssignableToTupleType((BArrayType) source, (BTupleType) target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.TUPLE || targetTag == TypeTags.TUPLE) {
            return isTupleTypeAssignable(source, target, unresolvedTypes);
        }

        if (sourceTag == TypeTags.INVOKABLE && targetTag == TypeTags.INVOKABLE) {
            return isFunctionTypeAssignable((BInvokableType) source, (BInvokableType) target, new HashSet<>());
        }

        return sourceTag == TypeTags.ARRAY && targetTag == TypeTags.ARRAY &&
                isArrayTypesAssignable((BArrayType) source, target, unresolvedTypes);
    }

    private boolean isMutable(BType type) {
        if (Symbols.isFlagOn(type.flags, Flags.READONLY)) {
            return false;
        }

        if (type.tag != TypeTags.UNION) {
            return true;
        }

        BUnionType unionType = (BUnionType) type;
        for (BType memberType : unionType.getMemberTypes()) {
            if (!Symbols.isFlagOn(memberType.flags, Flags.READONLY)) {
                return true;
            }
        }

        unionType.flags |= Flags.READONLY;
        unionType.tsymbol.flags |= Flags.READONLY;
        return false;
    }

    private boolean isParameterizedTypeAssignable(BType source, BType target, Set<TypePair> unresolvedTypes) {
        BType resolvedSourceType = typeBuilder.build(source);

        if (target.tag != TypeTags.PARAMETERIZED_TYPE) {
            return isAssignable(resolvedSourceType, target, unresolvedTypes);
        }

        if (((BParameterizedType) source).paramIndex != ((BParameterizedType) target).paramIndex) {
            return false;
        }

        return isAssignable(resolvedSourceType, typeBuilder.build(target), unresolvedTypes);
    }

    private boolean isAssignableRecordType(BRecordType recordType, BType type, Set<TypePair> unresolvedTypes) {
        TypePair pair = new TypePair(recordType, type);
        if (!unresolvedTypes.add(pair)) {
            return true;
        }

        BType targetType;
        switch (type.tag) {
            case TypeTags.MAP:
                targetType = ((BMapType) type).constraint;
                break;
            case TypeTags.JSON:
                targetType = type;
                break;
            default:
                throw new IllegalArgumentException("Incompatible target type: " + type.toString());
        }
        return recordFieldsAssignableToType(recordType, targetType, unresolvedTypes);
    }

    private boolean isAssignableStreamType(BStreamType sourceStreamType, BStreamType targetStreamType,
                                           Set<TypePair> unresolvedTypes) {
        return isAssignable(sourceStreamType.constraint, targetStreamType.constraint, unresolvedTypes)
                && isAssignable(sourceStreamType.error, targetStreamType.error, unresolvedTypes);
    }

    private boolean recordFieldsAssignableToType(BRecordType recordType, BType targetType,
                                                 Set<TypePair> unresolvedTypes) {
        for (BField field : recordType.fields.values()) {
            if (!isAssignable(field.type, targetType, unresolvedTypes)) {
                return false;
            }
        }

        if (!recordType.sealed) {
            return isAssignable(recordType.restFieldType, targetType, unresolvedTypes);
        }

        return true;
    }

    private boolean isAssignableTableType(BTableType sourceTableType, BTableType targetTableType,
                                          Set<TypePair> unresolvedTypes) {
        if (!isAssignable(sourceTableType.constraint, targetTableType.constraint, unresolvedTypes)) {
            return false;
        }

        if (targetTableType.keyTypeConstraint == null && targetTableType.fieldNameList == null) {
            return true;
        }

        if (targetTableType.keyTypeConstraint != null) {
            if (sourceTableType.keyTypeConstraint != null &&
                    (isAssignable(sourceTableType.keyTypeConstraint, targetTableType.keyTypeConstraint,
                            unresolvedTypes))) {
                return true;
            }

            if (sourceTableType.fieldNameList == null) {
                return false;
            }

            List<BType> fieldTypes = new ArrayList<>();
            sourceTableType.fieldNameList.forEach(field -> fieldTypes
                    .add(getTableConstraintField(sourceTableType.constraint, field).type));

            if (fieldTypes.size() == 1) {
                return isAssignable(fieldTypes.get(0), targetTableType.keyTypeConstraint, unresolvedTypes);
            }

            BTupleType tupleType = new BTupleType(fieldTypes);
            return isAssignable(tupleType, targetTableType.keyTypeConstraint, unresolvedTypes);
        }

        return targetTableType.fieldNameList.equals(sourceTableType.fieldNameList);
    }


    BField getTableConstraintField(BType constraintType, String fieldName) {

        switch (constraintType.tag) {
            case TypeTags.RECORD:
                Map<String, BField> fieldList = ((BRecordType) constraintType).getFields();
                return fieldList.get(fieldName);
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) constraintType;
                Set<BType> memTypes = unionType.getMemberTypes();
                List<BField> fields = memTypes.stream().map(type -> getTableConstraintField(type, fieldName))
                        .filter(Objects::nonNull).collect(Collectors.toList());

                if (fields.size() != memTypes.size()) {
                    return null;
                }

                if (fields.stream().allMatch(field -> isAssignable(field.type, fields.get(0).type) &&
                        isAssignable(fields.get(0).type, field.type))) {
                    return fields.get(0);
                }
                break;
            case TypeTags.INTERSECTION:
                return getTableConstraintField(((BIntersectionType) constraintType).effectiveType, fieldName);
        }

        return null;
    }

    private boolean isAssignableMapType(BMapType sourceMapType, BRecordType targetRecType) {
        if (targetRecType.sealed) {
            return false;
        }

        for (BField field : targetRecType.fields.values()) {
            if (!Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL)) {
                return false;
            }

            if (hasIncompatibleReadOnlyFlags(field.symbol.flags, sourceMapType.flags)) {
                return false;
            }

            if (!isAssignable(sourceMapType.constraint, field.type)) {
                return false;
            }
        }

        return isAssignable(sourceMapType.constraint, targetRecType.restFieldType);
    }

    private boolean hasIncompatibleReadOnlyFlags(long targetFlags, long sourceFlags) {
        return Symbols.isFlagOn(targetFlags, Flags.READONLY) && !Symbols.isFlagOn(sourceFlags, Flags.READONLY);
    }

    private boolean isErrorTypeAssignable(BErrorType source, BErrorType target, Set<TypePair> unresolvedTypes) {
        if (target == symTable.errorType) {
            return true;
        }
        TypePair pair = new TypePair(source, target);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);
        return isAssignable(source.detailType, target.detailType, unresolvedTypes)
                && target.typeIdSet.isAssignableFrom(source.typeIdSet);
    }

    // TODO: Recheck this to support finite types
    private boolean isXMLTypeAssignable(BType sourceType, BType targetType, Set<TypePair> unresolvedTypes) {
        int sourceTag = sourceType.tag;
        int targetTag = targetType.tag;

        if (targetTag == TypeTags.XML) {
            BXMLType target = (BXMLType) targetType;
            if (target.constraint != null) {
                if (TypeTags.isXMLNonSequenceType(sourceTag)) {
                    return isAssignable(sourceType, target.constraint, unresolvedTypes);
                }
                BXMLType source = (BXMLType) sourceType;
                if (source.constraint.tag == TypeTags.NEVER) {
                    if (sourceTag == targetTag) {
                        return true;
                    }
                    return isAssignable(source, target.constraint, unresolvedTypes);
                }
                return isAssignable(source.constraint, target.constraint, unresolvedTypes);
            }
            return true;
        }
        if (sourceTag == TypeTags.XML) {
            BXMLType source = (BXMLType) sourceType;
            if (targetTag == TypeTags.XML_TEXT) {
                if (source.constraint != null) {
                    return source.constraint.tag == TypeTags.NEVER;
                }
                return false;
            }
            if (targetTag == TypeTags.STRING) {
                if (source.constraint.tag == TypeTags.NEVER) {
                    return true;
                }
                return isAssignable(source.constraint, targetType, unresolvedTypes);
            }
        } else if (sourceTag == TypeTags.XML_TEXT && targetTag == TypeTags.STRING) {
            return true;
        }
        return sourceTag == targetTag;
    }

    public boolean isXMLExprCastableToString(BType source, BType target) {
        if (target.tag == TypeTags.STRING && isXMLSourceCastableToString(source)) {
            return true;
        }
        if (target.tag == TypeTags.UNION || target.tag == TypeTags.FINITE) {
            return isAssignable(target, symTable.stringType) && isXMLSourceCastableToString(source);
        }
        return false;
    }

    public boolean isXMLSourceCastableToString(BType source) {
        int exprTag = source.tag;
        if (exprTag == TypeTags.XML_TEXT) {
            return true;
        }
        if (exprTag == TypeTags.XML) {
            BXMLType conversionExpressionType = (BXMLType) source;
            // Revisit and check xml<xml<constraint>>> on chained iteration.
            while (conversionExpressionType.constraint.tag == TypeTags.XML) {
                conversionExpressionType = (BXMLType) conversionExpressionType.constraint;
            }
            return conversionExpressionType.constraint.tag == TypeTags.NEVER ||
                    conversionExpressionType.constraint.tag == TypeTags.XML_TEXT;
        }
        if (exprTag == TypeTags.UNION) {
            for (BType member : ((BUnionType) source).getMemberTypes()) {
                if (!TypeTags.isXMLTypeTag(member.tag) && !(member.tag == TypeTags.STRING)) {
                    return false;
                }
            }
            return isAssignable(source, symTable.stringType);
        }
        return false;
    }

    private boolean isTupleTypeAssignable(BType source, BType target, Set<TypePair> unresolvedTypes) {
        if (source.tag != TypeTags.TUPLE || target.tag != TypeTags.TUPLE) {
            return false;
        }

        BTupleType lhsTupleType = (BTupleType) target;
        BTupleType rhsTupleType = (BTupleType) source;

        if (lhsTupleType.restType == null && rhsTupleType.restType != null) {
            return false;
        }

        if (lhsTupleType.restType == null && lhsTupleType.tupleTypes.size() != rhsTupleType.tupleTypes.size()) {
            return false;
        }

        if (lhsTupleType.restType != null && rhsTupleType.restType != null) {
            if (!isAssignable(rhsTupleType.restType, lhsTupleType.restType, unresolvedTypes)) {
                return false;
            }
        }

        if (lhsTupleType.tupleTypes.size() > rhsTupleType.tupleTypes.size()) {
            return false;
        }

        for (int i = 0; i < rhsTupleType.tupleTypes.size(); i++) {
            BType lhsType = (lhsTupleType.tupleTypes.size() > i)
                    ? lhsTupleType.tupleTypes.get(i) : lhsTupleType.restType;
            if (!isAssignable(rhsTupleType.tupleTypes.get(i), lhsType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTupleTypeAssignableToArrayType(BTupleType source, BArrayType target,
                                                     Set<TypePair> unresolvedTypes) {
        if (target.state != BArrayState.OPEN
                && (source.restType != null || source.tupleTypes.size() != target.size)) {
            return false;
        }

        List<BType> sourceTypes = new ArrayList<>(source.tupleTypes);
        if (source.restType != null) {
            sourceTypes.add(source.restType);
        }
        return sourceTypes.stream()
                .allMatch(tupleElemType -> isAssignable(tupleElemType, target.eType, unresolvedTypes));
    }

    private boolean isArrayTypeAssignableToTupleType(BArrayType source, BTupleType target,
                                                     Set<TypePair> unresolvedTypes) {
        if (!target.tupleTypes.isEmpty()) {
            if (source.state == BArrayState.OPEN) {
                // [int, int, int...] = int[] || [int, int] = int[]
                return false;
            }

            if (target.restType != null && target.tupleTypes.size() > source.size) {
                // [int, int, int...] = int[1]
                return false;
            }

            if (target.restType == null && target.tupleTypes.size() != source.size) {
                // [int, int] = int[1], [int, int] = int[3]
                return false;
            }

        }

        List<BType> targetTypes = new ArrayList<>(target.tupleTypes);
        if (target.restType != null) {
            targetTypes.add(target.restType);
        }
        return targetTypes.stream()
                .allMatch(tupleElemType -> isAssignable(source.eType, tupleElemType, unresolvedTypes));
    }

    private boolean isArrayTypesAssignable(BArrayType source, BType target, Set<TypePair> unresolvedTypes) {
        BType sourceElementType = source.getElementType();
        if (target.tag == TypeTags.ARRAY) {
            BArrayType targetArrayType = (BArrayType) target;
            BType targetElementType = targetArrayType.getElementType();
            if (targetArrayType.state == BArrayState.OPEN) {
                return isAssignable(sourceElementType, targetElementType, unresolvedTypes);
            }

            if (targetArrayType.size != source.size) {
                return false;
            }

            return isAssignable(sourceElementType, targetElementType, unresolvedTypes);
        } else if (target.tag == TypeTags.JSON) {
            return isAssignable(sourceElementType, target, unresolvedTypes);
        } else if (target.tag == TypeTags.ANYDATA) {
            return isAssignable(sourceElementType, target, unresolvedTypes);
        }
        return false;
    }

    private boolean isFunctionTypeAssignable(BInvokableType source, BInvokableType target,
                                             Set<TypePair> unresolvedTypes) {
        if (hasIncompatibleIsolatedFlags(source, target) || hasIncompatibleTransactionalFlags(source, target)) {
            return false;
        }

        // For invokable types with typeParam parameters, we have to check whether the source param types are
        // covariant with the target param types.
        if (containsTypeParams(target)) {
            // TODO: 7/4/19 See if the below code can be generalized to avoid code duplication
            if (source.paramTypes.size() != target.paramTypes.size()) {
                return false;
            }

            for (int i = 0; i < source.paramTypes.size(); i++) {
                BType sourceParam = source.paramTypes.get(i);
                BType targetParam = target.paramTypes.get(i);
                boolean isTypeParam = TypeParamAnalyzer.isTypeParam(targetParam);

                if (isTypeParam) {
                    if (!isAssignable(sourceParam, targetParam)) {
                        return false;
                    }
                } else {
                    if (!isAssignable(targetParam, sourceParam)) {
                        return false;
                    }
                }
            }

            if (source.retType == null && target.retType == null) {
                return true;
            } else if (source.retType == null || target.retType == null) {
                return false;
            }

            // Source return type should be covariant with target return type
            return isAssignable(source.retType, target.retType, unresolvedTypes);
        }

        // Source param types should be contravariant with target param types. Hence s and t switched when checking
        // assignability.
        return checkFunctionTypeEquality(source, target, unresolvedTypes, (s, t, ut) -> isAssignable(t, s, ut));
    }

    public boolean isInherentlyImmutableType(BType type) {
        if (isValueType(type)) {
            return true;
        }

        switch (type.tag) {
            case TypeTags.XML_TEXT:
            case TypeTags.FINITE: // Assuming a finite type will only have members from simple basic types.
            case TypeTags.READONLY:
            case TypeTags.NIL:
            case TypeTags.ERROR:
            case TypeTags.INVOKABLE:
            case TypeTags.TYPEDESC:
            case TypeTags.HANDLE:
                return true;
            case TypeTags.XML:
                return ((BXMLType) type).constraint.tag == TypeTags.NEVER;
        }
        return false;
    }

    boolean isSelectivelyImmutableType(BType type) {
        return isSelectivelyImmutableType(type, new HashSet<>(), false);
    }

    boolean isSelectivelyImmutableType(BType type, boolean forceCheck) {
        return isSelectivelyImmutableType(type, new HashSet<>(), forceCheck);
    }

    public boolean isSelectivelyImmutableType(BType type, Set<BType> unresolvedTypes) {
        return isSelectivelyImmutableType(type, unresolvedTypes, false);
    }

    private boolean isSelectivelyImmutableType(BType type, Set<BType> unresolvedTypes, boolean forceCheck) {
        return isSelectivelyImmutableType(type, false, unresolvedTypes, forceCheck);
    }

    private boolean isSelectivelyImmutableType(BType type, boolean disallowReadOnlyObjects, Set<BType> unresolvedTypes,
                                               boolean forceCheck) {
        if (isInherentlyImmutableType(type) || !(type instanceof SelectivelyImmutableReferenceType)) {
            // Always immutable.
            return false;
        }

        if (!unresolvedTypes.add(type)) {
            return true;
        }

        if (!forceCheck && ((SelectivelyImmutableReferenceType) type).getImmutableType() != null) {
            return true;
        }

        switch (type.tag) {
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.XML_COMMENT:
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_PI:
                return true;
            case TypeTags.ARRAY:
                BType elementType = ((BArrayType) type).eType;
                return isInherentlyImmutableType(elementType) ||
                        isSelectivelyImmutableType(elementType, unresolvedTypes, forceCheck);
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) type;
                for (BType tupMemType : tupleType.tupleTypes) {
                    if (!isInherentlyImmutableType(tupMemType) &&
                            !isSelectivelyImmutableType(tupMemType, unresolvedTypes, forceCheck)) {
                        return false;
                    }
                }

                BType tupRestType = tupleType.restType;
                if (tupRestType == null) {
                    return true;
                }

                return isInherentlyImmutableType(tupRestType) ||
                        isSelectivelyImmutableType(tupRestType, unresolvedTypes, forceCheck);
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) type;
                for (BField field : recordType.fields.values()) {
                    BType fieldType = field.type;
                    if (!isInherentlyImmutableType(fieldType) &&
                            !isSelectivelyImmutableType(fieldType, unresolvedTypes, forceCheck)) {
                        return false;
                    }
                }

                BType recordRestType = recordType.restFieldType;
                if (recordRestType == null || recordRestType == symTable.noType) {
                    return true;
                }

                return isInherentlyImmutableType(recordRestType) ||
                        isSelectivelyImmutableType(recordRestType, unresolvedTypes, forceCheck);
            case TypeTags.MAP:
                BType constraintType = ((BMapType) type).constraint;
                return isInherentlyImmutableType(constraintType) ||
                        isSelectivelyImmutableType(constraintType, unresolvedTypes, forceCheck);
            case TypeTags.OBJECT:
                BObjectType objectType = (BObjectType) type;

                for (BField field : objectType.fields.values()) {
                    BType fieldType = field.type;
                    if (!isInherentlyImmutableType(fieldType) &&
                            !isSelectivelyImmutableType(fieldType, unresolvedTypes, forceCheck)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.TABLE:
                BType tableConstraintType = ((BTableType) type).constraint;
                return isInherentlyImmutableType(tableConstraintType) ||
                        isSelectivelyImmutableType(tableConstraintType, unresolvedTypes, forceCheck);
            case TypeTags.UNION:
                boolean readonlyIntersectionExists = false;
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    if (isInherentlyImmutableType(memberType) ||
                            isSelectivelyImmutableType(memberType, unresolvedTypes, forceCheck)) {
                        readonlyIntersectionExists = true;
                    }
                }
                return readonlyIntersectionExists;
            case TypeTags.INTERSECTION:
                return isSelectivelyImmutableType(((BIntersectionType) type).effectiveType, unresolvedTypes,
                                                  forceCheck);
        }
        return false;
    }

    private boolean containsTypeParams(BInvokableType type) {
        boolean hasParameterizedTypes = type.paramTypes.stream()
                .anyMatch(t -> {
                    if (t.tag == TypeTags.FUNCTION_POINTER) {
                        return containsTypeParams((BInvokableType) t);
                    }
                    return TypeParamAnalyzer.isTypeParam(t);
                });

        if (hasParameterizedTypes) {
            return hasParameterizedTypes;
        }

        if (type.retType.tag == TypeTags.FUNCTION_POINTER) {
            return containsTypeParams((BInvokableType) type.retType);
        }

        return TypeParamAnalyzer.isTypeParam(type.retType);
    }

    private boolean isSameFunctionType(BInvokableType source, BInvokableType target, Set<TypePair> unresolvedTypes) {
        return checkFunctionTypeEquality(source, target, unresolvedTypes, this::isSameType);
    }

    private boolean checkFunctionTypeEquality(BInvokableType source, BInvokableType target,
                                              Set<TypePair> unresolvedTypes, TypeEqualityPredicate equality) {
        if (hasIncompatibleIsolatedFlags(source, target) || hasIncompatibleTransactionalFlags(source, target)) {
            return false;
        }

        if (source.paramTypes.size() != target.paramTypes.size()) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.size(); i++) {
            if (!equality.test(source.paramTypes.get(i), target.paramTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }

        if ((source.restType != null && target.restType == null) ||
                target.restType != null && source.restType == null) {
            return false;
        } else if (source.restType != null && !equality.test(source.restType, target.restType, unresolvedTypes)) {
            return false;
        }

        if (source.retType == null && target.retType == null) {
            return true;
        } else if (source.retType == null || target.retType == null) {
            return false;
        }

        // Source return type should be covariant with target return type
        return isAssignable(source.retType, target.retType, unresolvedTypes);
    }

    private boolean hasIncompatibleIsolatedFlags(BInvokableType source, BInvokableType target) {
        return Symbols.isFlagOn(target.flags, Flags.ISOLATED) && !Symbols.isFlagOn(source.flags, Flags.ISOLATED);
    }

    private boolean hasIncompatibleTransactionalFlags(BInvokableType source, BInvokableType target) {
        return Symbols.isFlagOn(source.flags, Flags.TRANSACTIONAL) &&
                !Symbols.isFlagOn(target.flags, Flags.TRANSACTIONAL);
    }

    public boolean isSameArrayType(BType source, BType target, Set<TypePair> unresolvedTypes) {
        if (target.tag != TypeTags.ARRAY || source.tag != TypeTags.ARRAY) {
            return false;
        }

        BArrayType lhsArrayType = (BArrayType) target;
        BArrayType rhsArrayType = (BArrayType) source;
        boolean hasSameTypeElements = isSameType(lhsArrayType.eType, rhsArrayType.eType, unresolvedTypes);
        if (lhsArrayType.state == BArrayState.OPEN) {
            return (rhsArrayType.state == BArrayState.OPEN) && hasSameTypeElements;
        }

        return checkSealedArraySizeEquality(rhsArrayType, lhsArrayType) && hasSameTypeElements;
    }

    public boolean isSameStreamType(BType source, BType target, Set<TypePair> unresolvedTypes) {
        if (target.tag != TypeTags.STREAM || source.tag != TypeTags.STREAM) {
            return false;
        }
        BStreamType lhsStreamType = (BStreamType) target;
        BStreamType rhsStreamType = (BStreamType) source;
        return isSameType(lhsStreamType.constraint, rhsStreamType.constraint, unresolvedTypes)
                && isSameType(lhsStreamType.error, rhsStreamType.error, unresolvedTypes);
    }

    public boolean checkSealedArraySizeEquality(BArrayType rhsArrayType, BArrayType lhsArrayType) {
        return lhsArrayType.size == rhsArrayType.size;
    }

    public boolean checkStructEquivalency(BType rhsType, BType lhsType) {
        return checkStructEquivalency(rhsType, lhsType, new HashSet<>());
    }

    private boolean checkStructEquivalency(BType rhsType, BType lhsType, Set<TypePair> unresolvedTypes) {
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(rhsType, lhsType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        if (rhsType.tag == TypeTags.OBJECT && lhsType.tag == TypeTags.OBJECT) {
            return checkObjectEquivalency((BObjectType) rhsType, (BObjectType) lhsType, unresolvedTypes);
        }

        if (rhsType.tag == TypeTags.RECORD && lhsType.tag == TypeTags.RECORD) {
            return checkRecordEquivalency((BRecordType) rhsType, (BRecordType) lhsType, unresolvedTypes);
        }

        return false;
    }

    public boolean checkObjectEquivalency(BObjectType rhsType, BObjectType lhsType, Set<TypePair> unresolvedTypes) {
        if (Symbols.isFlagOn(lhsType.flags, Flags.ISOLATED) && !Symbols.isFlagOn(rhsType.flags, Flags.ISOLATED)) {
            return false;
        }

        BObjectTypeSymbol lhsStructSymbol = (BObjectTypeSymbol) lhsType.tsymbol;
        BObjectTypeSymbol rhsStructSymbol = (BObjectTypeSymbol) rhsType.tsymbol;
        List<BAttachedFunction> lhsFuncs = lhsStructSymbol.attachedFuncs;
        List<BAttachedFunction> rhsFuncs = ((BObjectTypeSymbol) rhsType.tsymbol).attachedFuncs;
        int lhsAttachedFuncCount = getObjectFuncCount(lhsStructSymbol);
        int rhsAttachedFuncCount = getObjectFuncCount(rhsStructSymbol);

        // If LHS is a service obj, then RHS must be a service object in order to assignable
        boolean isLhsAService = Symbols.isService(lhsStructSymbol);
        if (isLhsAService && !Symbols.isService(rhsStructSymbol)) {
            return false;
        }

        // RHS type should have at least all the fields as well attached functions of LHS type.
        if (lhsType.fields.size() > rhsType.fields.size() || lhsAttachedFuncCount > rhsAttachedFuncCount) {
            return false;
        }

        // The LHS type cannot have any private members. 
        for (BField bField : lhsType.fields.values()) {
            if (Symbols.isPrivate(bField.symbol)) {
                return false;
            }
        }

        for (BAttachedFunction func : lhsFuncs) {
            if (Symbols.isPrivate(func.symbol)) {
                return false;
            }
        }

        for (BField lhsField : lhsType.fields.values()) {
            BField rhsField = rhsType.fields.get(lhsField.name.value);
            if (rhsField == null ||
                    !isInSameVisibilityRegion(lhsField.symbol, rhsField.symbol) ||
                    !isAssignable(rhsField.type, lhsField.type, unresolvedTypes)) {
                return false;
            }
        }

        for (BAttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsStructSymbol.initializerFunc) {
                continue;
            }

            // Service resource methods are not considered as part of service objects type.
            if (isLhsAService && Symbols.isResource(lhsFunc.symbol)) {
                continue;
            }

            BAttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null || !isInSameVisibilityRegion(lhsFunc.symbol, rhsFunc.symbol)) {
                return false;
            }
            if (Symbols.isRemote(lhsFunc.symbol) != Symbols.isRemote(rhsFunc.symbol)) {
                return false;
            }
        }

        return lhsType.typeIdSet.isAssignableFrom(rhsType.typeIdSet);
    }

    private int getObjectFuncCount(BObjectTypeSymbol sym) {
        // If an explicit initializer is available, it could mean,
        // 1) User explicitly defined an initializer
        // 2) The object type is coming from an already compiled source, hence the initializer is already set.
        //    If it's coming from a compiled binary, the attached functions list of the symbol would already contain
        //    the initializer in it.
        if (sym.initializerFunc != null && sym.attachedFuncs.contains(sym.initializerFunc)) {
            return sym.attachedFuncs.size() - 1;
        }
        return sym.attachedFuncs.size();
    }

    public boolean checkRecordEquivalency(BRecordType rhsType, BRecordType lhsType, Set<TypePair> unresolvedTypes) {
        // If the LHS record is closed and the RHS record is open, the records aren't equivalent
        if (lhsType.sealed && !rhsType.sealed) {
            return false;
        }

        // If both are open records, the rest field type of the RHS record should be assignable to the rest field
        // type of the LHS type.
        if (!rhsType.sealed && !isAssignable(rhsType.restFieldType, lhsType.restFieldType, unresolvedTypes)) {
            return false;
        }

        return checkFieldEquivalency(lhsType, rhsType, unresolvedTypes);
    }

    public void setForeachTypedBindingPatternType(BLangForeach foreachNode) {
        BType collectionType = foreachNode.collection.type;
        BType varType;
        switch (collectionType.tag) {
            case TypeTags.STRING:
                varType = symTable.stringType;
                break;
            case TypeTags.ARRAY:
                BArrayType arrayType = (BArrayType) collectionType;
                varType = arrayType.eType;
                break;
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) collectionType;
                LinkedHashSet<BType> tupleTypes = new LinkedHashSet<>(tupleType.tupleTypes);
                if (tupleType.restType != null) {
                    tupleTypes.add(tupleType.restType);
                }
                varType = tupleTypes.size() == 1 ?
                        tupleTypes.iterator().next() : BUnionType.create(null, tupleTypes);
                break;
            case TypeTags.MAP:
                BMapType bMapType = (BMapType) collectionType;
                varType = bMapType.constraint;

                break;
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) collectionType;
                varType = inferRecordFieldType(recordType);
                break;
            case TypeTags.XML:
                BType constraint = ((BXMLType) collectionType).constraint;
                while (constraint.tag == TypeTags.XML) {
                    collectionType = constraint;
                    constraint = ((BXMLType) collectionType).constraint;
                }
                switch (constraint.tag) {
                    case TypeTags.XML_ELEMENT:
                        varType = symTable.xmlElementType;
                        break;
                    case TypeTags.XML_COMMENT:
                        varType = symTable.xmlCommentType;
                        break;
                    case TypeTags.XML_TEXT:
                        varType = symTable.xmlTextType;
                        break;
                    case TypeTags.XML_PI:
                        varType = symTable.xmlPIType;
                        break;
                    default:
                        Set<BType> collectionTypes = getEffectiveMemberTypes((BUnionType) constraint);
                        Set<BType> builtinXMLConstraintTypes = getEffectiveMemberTypes
                                ((BUnionType) ((BXMLType) symTable.xmlType).constraint);
                        if (collectionTypes.size() == 4 && builtinXMLConstraintTypes.equals(collectionTypes)) {
                            varType = symTable.xmlType;
                        } else {
                            LinkedHashSet<BType> collectionTypesInSymTable = new LinkedHashSet<>();
                            for (BType subType : collectionTypes) {
                                switch (subType.tag) {
                                    case TypeTags.XML_ELEMENT:
                                        collectionTypesInSymTable.add(symTable.xmlElementType);
                                        break;
                                    case TypeTags.XML_COMMENT:
                                        collectionTypesInSymTable.add(symTable.xmlCommentType);
                                        break;
                                    case TypeTags.XML_TEXT:
                                        collectionTypesInSymTable.add(symTable.xmlTextType);
                                        break;
                                    case TypeTags.XML_PI:
                                        collectionTypesInSymTable.add(symTable.xmlPIType);
                                        break;
                                }

                            }
                            varType = BUnionType.create(null, collectionTypesInSymTable);
                        }
                }
                break;
            case TypeTags.XML_TEXT:
                varType = symTable.xmlTextType;
                break;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) collectionType;
                varType = tableType.constraint;
                break;
            case TypeTags.STREAM:
                BStreamType streamType = (BStreamType) collectionType;
                if (streamType.constraint.tag == TypeTags.NONE) {
                    varType = symTable.anydataType;
                    break;
                }
                varType = streamType.constraint;
                if (streamType.error.tag != TypeTags.NEVER) {
                    BType actualType = BUnionType.create(null, varType, streamType.error);
                    dlog.error(foreachNode.collection.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                            varType, actualType);
                }
                break;
            case TypeTags.OBJECT:
                // check for iterable objects
                BUnionType nextMethodReturnType = getVarTypeFromIterableObject((BObjectType) collectionType);
                if (nextMethodReturnType != null) {
                    foreachNode.resultType = getRecordType(nextMethodReturnType);
                    BType valueType = (foreachNode.resultType != null)
                            ? ((BRecordType) foreachNode.resultType).fields.get("value").type : null;
                    BType errorType = getErrorType(nextMethodReturnType);
                    if (errorType != null) {
                        BType actualType = BUnionType.create(null, valueType, errorType);
                        dlog.error(foreachNode.collection.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                                valueType, actualType);
                    }
                    foreachNode.nillableResultType = nextMethodReturnType;
                    foreachNode.varType = valueType;
                    return;
                }
                dlog.error(foreachNode.collection.pos, DiagnosticErrorCode.INCOMPATIBLE_ITERATOR_FUNCTION_SIGNATURE);
                // fallthrough
            case TypeTags.SEMANTIC_ERROR:
                foreachNode.varType = symTable.semanticError;
                foreachNode.resultType = symTable.semanticError;
                foreachNode.nillableResultType = symTable.semanticError;
                return;
            default:
                foreachNode.varType = symTable.semanticError;
                foreachNode.resultType = symTable.semanticError;
                foreachNode.nillableResultType = symTable.semanticError;
                dlog.error(foreachNode.collection.pos, DiagnosticErrorCode.ITERABLE_NOT_SUPPORTED_COLLECTION,
                                 collectionType);
                return;
        }

        BInvokableSymbol iteratorSymbol = (BInvokableSymbol) symResolver.lookupLangLibMethod(collectionType,
                names.fromString(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC));
        BUnionType nextMethodReturnType =
                (BUnionType) getResultTypeOfNextInvocation((BObjectType) iteratorSymbol.retType);
        foreachNode.varType = varType;
        foreachNode.resultType = getRecordType(nextMethodReturnType);
        foreachNode.nillableResultType = nextMethodReturnType;
    }

    public void setInputClauseTypedBindingPatternType(BLangInputClause bLangInputClause) {
        if (bLangInputClause.collection == null) {
            //not-possible
            return;
        }

        BType collectionType = bLangInputClause.collection.type;
        BType varType;
        switch (collectionType.tag) {
            case TypeTags.STRING:
                varType = symTable.stringType;
                break;
            case TypeTags.ARRAY:
                BArrayType arrayType = (BArrayType) collectionType;
                varType = arrayType.eType;
                break;
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) collectionType;
                LinkedHashSet<BType> tupleTypes = new LinkedHashSet<>(tupleType.tupleTypes);
                if (tupleType.restType != null) {
                    tupleTypes.add(tupleType.restType);
                }
                varType = tupleTypes.size() == 1 ?
                        tupleTypes.iterator().next() : BUnionType.create(null, tupleTypes);
                break;
            case TypeTags.MAP:
                BMapType bMapType = (BMapType) collectionType;
                varType = bMapType.constraint;

                break;
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) collectionType;
                varType = inferRecordFieldType(recordType);
                break;
            case TypeTags.XML:
                varType = BUnionType.create(null, symTable.xmlType, symTable.stringType);
                break;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) collectionType;
                varType = tableType.constraint;
                break;
            case TypeTags.STREAM:
                BStreamType streamType = (BStreamType) collectionType;
                if (streamType.constraint.tag == TypeTags.NONE) {
                    varType = symTable.anydataType;
                    break;
                }
                varType = streamType.constraint;
                break;
            case TypeTags.OBJECT:
                // check for iterable objects
                BUnionType nextMethodReturnType = getVarTypeFromIterableObject((BObjectType) collectionType);
                if (nextMethodReturnType != null) {
                    bLangInputClause.resultType = getRecordType(nextMethodReturnType);
                    bLangInputClause.nillableResultType = nextMethodReturnType;
                    bLangInputClause.varType = ((BRecordType) bLangInputClause.resultType).fields.get("value").type;
                    return;
                }
                dlog.error(bLangInputClause.collection.pos,
                        DiagnosticErrorCode.INCOMPATIBLE_ITERATOR_FUNCTION_SIGNATURE);
                // fallthrough
            case TypeTags.SEMANTIC_ERROR:
                bLangInputClause.varType = symTable.semanticError;
                bLangInputClause.resultType = symTable.semanticError;
                bLangInputClause.nillableResultType = symTable.semanticError;
                return;
            default:
                bLangInputClause.varType = symTable.semanticError;
                bLangInputClause.resultType = symTable.semanticError;
                bLangInputClause.nillableResultType = symTable.semanticError;
                dlog.error(bLangInputClause.collection.pos, DiagnosticErrorCode.ITERABLE_NOT_SUPPORTED_COLLECTION,
                                 collectionType);
                return;
        }

        BInvokableSymbol iteratorSymbol = (BInvokableSymbol) symResolver.lookupLangLibMethod(collectionType,
                names.fromString(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC));
        BUnionType nextMethodReturnType =
                (BUnionType) getResultTypeOfNextInvocation((BObjectType) iteratorSymbol.retType);
        bLangInputClause.varType = varType;
        bLangInputClause.resultType = getRecordType(nextMethodReturnType);
        bLangInputClause.nillableResultType = nextMethodReturnType;
    }

    public BUnionType getVarTypeFromIterableObject(BObjectType collectionType) {
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) collectionType.tsymbol;
        for (BAttachedFunction func : objectTypeSymbol.attachedFuncs) {
            if (func.funcName.value.equals(BLangCompilerConstants.ITERABLE_OBJECT_ITERATOR_FUNC)) {
                return getVarTypeFromIteratorFunc(func);
            }
        }

        return null;
    }

    private BUnionType getVarTypeFromIteratorFunc(BAttachedFunction candidateIteratorFunc) {
        if (!candidateIteratorFunc.type.paramTypes.isEmpty()) {
            return null;
        }

        BType returnType = candidateIteratorFunc.type.retType;
        // abstract object {public function next() returns record {|int value;|}?;}
        return getVarTypeFromIteratorFuncReturnType(returnType);
    }

    public BUnionType getVarTypeFromIteratorFuncReturnType(BType returnType) {
        BObjectTypeSymbol objectTypeSymbol;
        if (returnType.tag != TypeTags.OBJECT) {
            return null;
        }

        objectTypeSymbol = (BObjectTypeSymbol) returnType.tsymbol;
        for (BAttachedFunction func : objectTypeSymbol.attachedFuncs) {
            if (func.funcName.value.equals(BLangCompilerConstants.NEXT_FUNC)) {
                return getVarTypeFromNextFunc(func);
            }
        }

        return null;
    }

    private BUnionType getVarTypeFromNextFunc(BAttachedFunction nextFunc) {
        BType returnType;
        if (!nextFunc.type.paramTypes.isEmpty()) {
            return null;
        }

        returnType = nextFunc.type.retType;
        // Check if the next function return type has the union type,
        // record {|int value;|}|error|();
        if (checkNextFuncReturnType(returnType)) {
            return (BUnionType) returnType;
        }

        return null;
    }

    private boolean checkNextFuncReturnType(BType returnType) {
        if (returnType.tag != TypeTags.UNION) {
            return false;
        }

        List<BType> types = getAllTypes(returnType);
        types.removeIf(type -> type.tag == TypeTags.NEVER);
        boolean containsCompletionType = types.removeIf(type -> type.tag == TypeTags.NIL);
        containsCompletionType = types.removeIf(type -> type.tag == TypeTags.ERROR) || containsCompletionType;
        if (!containsCompletionType) {
            return false;
        }

        if (types.size() != 1) {
            //TODO: print error
            return false;
        }

        if (types.get(0).tag != TypeTags.RECORD) {
            return false;
        }

        BRecordType recordType = (BRecordType) types.get(0);
        // Check if the union type has the record type,
        // record {|int value;|};
        return checkRecordTypeInNextFuncReturnType(recordType);
    }

    private boolean checkRecordTypeInNextFuncReturnType(BRecordType recordType) {
        if (!recordType.sealed) {
            return false;
        }

        if (recordType.fields.size() != 1) {
            return false;
        }

        return recordType.fields.containsKey(BLangCompilerConstants.VALUE_FIELD);
    }

    private BRecordType getRecordType(BUnionType type) {
        for (BType member : type.getMemberTypes()) {
            if (member.tag == TypeTags.RECORD) {
                return (BRecordType) member;
            }
        }
        return null;
    }

    public BErrorType getErrorType(BUnionType type) {
        for (BType member : type.getMemberTypes()) {
            if (member.tag == TypeTags.ERROR) {
                return (BErrorType) member;
            } else if (member.tag == TypeTags.UNION) {
                BErrorType e = getErrorType((BUnionType) member);
                if (e != null) {
                    return e;
                }
            }
        }
        return null;
    }

    public BType getResultTypeOfNextInvocation(BObjectType iteratorType) {
        BAttachedFunction nextFunc = getAttachedFuncFromObject(iteratorType, BLangCompilerConstants.NEXT_FUNC);
        return Objects.requireNonNull(nextFunc).type.retType;
    }

    public BAttachedFunction getAttachedFuncFromObject(BObjectType objectType, String funcName) {
        BObjectTypeSymbol iteratorSymbol = (BObjectTypeSymbol) objectType.tsymbol;
        for (BAttachedFunction bAttachedFunction : iteratorSymbol.attachedFuncs) {
            if (funcName.equals(bAttachedFunction.funcName.value)) {
                return bAttachedFunction;
            }
        }
        return null;
    }

    public BType inferRecordFieldType(BRecordType recordType) {
        Map<String, BField> fields = recordType.fields;
        BUnionType unionType = BUnionType.create(null);

        if (!recordType.sealed) {
            unionType.add(recordType.restFieldType);
        }

        for (BField field : fields.values()) {
            if (isAssignable(field.type, unionType)) {
                continue;
            }

            if (isAssignable(unionType, field.type)) {
                unionType = BUnionType.create(null);
            }

            unionType.add(field.type);
        }

        if (unionType.getMemberTypes().size() > 1) {
            unionType.tsymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                         Names.EMPTY, recordType.tsymbol.pkgID, null,
                                                         recordType.tsymbol.owner, symTable.builtinPos, VIRTUAL);
            return unionType;
        }

        return unionType.getMemberTypes().iterator().next();
    }

    /**
     * Enum to represent type test result.
     *
     * @since 1.2.0
     */
    enum TypeTestResult {
        NOT_FOUND,
        TRUE,
        FALSE
    }

    TypeTestResult isBuiltInTypeWidenPossible(BType actualType, BType targetType) {

        int targetTag = targetType.tag;
        int actualTag = actualType.tag;

        if (actualTag < TypeTags.JSON && targetTag < TypeTags.JSON) {
            // Fail Fast for value types.
            switch (actualTag) {
                case TypeTags.INT:
                case TypeTags.BYTE:
                case TypeTags.FLOAT:
                case TypeTags.DECIMAL:
                    if (targetTag == TypeTags.BOOLEAN || targetTag == TypeTags.STRING) {
                        return TypeTestResult.FALSE;
                    }
                    break;
                case TypeTags.BOOLEAN:
                    if (targetTag == TypeTags.INT || targetTag == TypeTags.BYTE || targetTag == TypeTags.FLOAT
                            || targetTag == TypeTags.DECIMAL || targetTag == TypeTags.STRING) {
                        return TypeTestResult.FALSE;
                    }
                    break;
                case TypeTags.STRING:
                    if (targetTag == TypeTags.INT || targetTag == TypeTags.BYTE || targetTag == TypeTags.FLOAT
                            || targetTag == TypeTags.DECIMAL || targetTag == TypeTags.BOOLEAN) {
                        return TypeTestResult.FALSE;
                    }
                    break;
            }
        }
        switch (actualTag) {
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.BOOLEAN:
            case TypeTags.STRING:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.CHAR_STRING:
                if (targetTag == TypeTags.JSON || targetTag == TypeTags.ANYDATA || targetTag == TypeTags.ANY ||
                        targetTag == TypeTags.READONLY) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.ANYDATA:
            case TypeTags.TYPEDESC:
                if (targetTag == TypeTags.ANY) {
                    return TypeTestResult.TRUE;
                }
                break;
            default:
        }

        if (TypeTags.isIntegerTypeTag(targetTag) && actualTag == targetTag) {
            return TypeTestResult.FALSE; // No widening.
        }

        // Validate for Integers subtypes.
        if ((TypeTags.isIntegerTypeTag(actualTag) || actualTag == TypeTags.BYTE)
                && (TypeTags.isIntegerTypeTag(targetTag) || targetTag == TypeTags.BYTE)) {
            return checkBuiltInIntSubtypeWidenPossible(actualType, targetType);
        }

        if (actualTag == TypeTags.CHAR_STRING && TypeTags.STRING == targetTag) {
            return TypeTestResult.TRUE;
        }
        return TypeTestResult.NOT_FOUND;
    }

    private TypeTestResult checkBuiltInIntSubtypeWidenPossible(BType actualType, BType targetType) {
        int actualTag = actualType.tag;
        switch (targetType.tag) {
            case TypeTags.INT:
                if (actualTag == TypeTags.BYTE || TypeTags.isIntegerTypeTag(actualTag)) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.SIGNED32_INT:
                if (actualTag == TypeTags.SIGNED16_INT || actualTag == TypeTags.SIGNED8_INT ||
                        actualTag == TypeTags.UNSIGNED16_INT || actualTag == TypeTags.UNSIGNED8_INT ||
                        actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.SIGNED16_INT:
                if (actualTag == TypeTags.SIGNED8_INT || actualTag == TypeTags.UNSIGNED8_INT ||
                        actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.UNSIGNED32_INT:
                if (actualTag == TypeTags.UNSIGNED16_INT || actualTag == TypeTags.UNSIGNED8_INT ||
                        actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.UNSIGNED16_INT:
                if (actualTag == TypeTags.UNSIGNED8_INT || actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.BYTE:
                if (actualTag == TypeTags.UNSIGNED8_INT) {
                    return TypeTestResult.TRUE;
                }
                break;
            case TypeTags.UNSIGNED8_INT:
                if (actualTag == TypeTags.BYTE) {
                    return TypeTestResult.TRUE;
                }
                break;
        }
        return TypeTestResult.NOT_FOUND;
    }

    public boolean isImplicityCastable(BType actualType, BType targetType) {
        /* The word Builtin refers for Compiler known types. */

        BType newTargetType = targetType;
        if ((targetType.tag == TypeTags.UNION || targetType.tag == TypeTags.FINITE) && isValueType(actualType)) {
            newTargetType = symTable.anyType;   // TODO : Check for correctness.
        } else if (targetType.tag == TypeTags.INTERSECTION) {
            newTargetType = ((BIntersectionType) targetType).effectiveType;
        }

        TypeTestResult result = isBuiltInTypeWidenPossible(actualType, newTargetType);
        if (result != TypeTestResult.NOT_FOUND) {
            return result == TypeTestResult.TRUE;
        }

        if (isValueType(targetType) &&
                (actualType.tag == TypeTags.FINITE ||
                        (actualType.tag == TypeTags.UNION && ((BUnionType) actualType).getMemberTypes().stream()
                                .anyMatch(type -> type.tag == TypeTags.FINITE && isAssignable(type, targetType))))) {
            // for decimal or nil, no cast is required
            return targetType.tag == TypeTags.INT || targetType.tag == TypeTags.BYTE || targetType.tag == TypeTags.FLOAT
                    || targetType.tag == TypeTags.STRING || targetType.tag == TypeTags.BOOLEAN;
        } else if (targetType.tag == TypeTags.ERROR
                && (actualType.tag == TypeTags.UNION
                && isAllErrorMembers((BUnionType) actualType))) {
            return true;
        } else if (targetType.tag == TypeTags.STRING) {
            if (actualType.tag == TypeTags.XML) {
                return isXMLTypeAssignable(actualType, targetType, new HashSet<>());
            }
            if (actualType.tag == TypeTags.UNION) {
                return isAssignable(actualType, symTable.stringType);
            }
            return actualType.tag == TypeTags.XML_TEXT;
        }
        return false;
    }

    public boolean isTypeCastable(BLangExpression expr, BType sourceType, BType targetType, SymbolEnv env) {
        if (getTypeIntersection(sourceType, symTable.errorType, env) != symTable.semanticError
                && getTypeIntersection(targetType, symTable.errorType, env) == symTable.semanticError) {
            return false;
        }
        if (sourceType.tag == TypeTags.SEMANTIC_ERROR || targetType.tag == TypeTags.SEMANTIC_ERROR ||
                sourceType == targetType) {
            return true;
        }
        if (isAssignable(sourceType, targetType) || isAssignable(targetType, sourceType)) {
            return true;
        }
        if (isNumericConversionPossible(expr, sourceType, targetType)) {
            return true;
        }
        if (sourceType.tag == TypeTags.ANY && targetType.tag == TypeTags.READONLY) {
            return true;
        }

        boolean validTypeCast = false;

        // Use instanceof to check for anydata and json.
        if (sourceType instanceof BUnionType) {
            if (getTypeForUnionTypeMembersAssignableToType((BUnionType) sourceType, targetType, null)
                    != symTable.semanticError) {
                // string|typedesc v1 = "hello world";
                // json|table<Foo> v2 = <json|table<Foo>> v1;
                validTypeCast = true;
            }
        }

        // Use instanceof to check for anydata and json.
        if (targetType instanceof BUnionType) {
            if (getTypeForUnionTypeMembersAssignableToType((BUnionType) targetType, sourceType, null)
                    != symTable.semanticError) {
                // string|int v1 = "hello world";
                // string|boolean v2 = <string|boolean> v1;
                validTypeCast = true;
            }
        }

        if (sourceType.tag == TypeTags.FINITE) {
            if (getTypeForFiniteTypeValuesAssignableToType((BFiniteType) sourceType, targetType)
                    != symTable.semanticError) {
                validTypeCast = true;
            }
        }

        if (targetType.tag == TypeTags.FINITE) {
            if (getTypeForFiniteTypeValuesAssignableToType((BFiniteType) targetType, sourceType)
                    != symTable.semanticError) {
                validTypeCast = true;
            }
        }

        if (validTypeCast) {
            if (isValueType(sourceType)) {
                setImplicitCastExpr(expr, sourceType, symTable.anyType);
            }
            return true;
        }

        return false;
    }

    boolean isNumericConversionPossible(BLangExpression expr, BType sourceType,
                                        BType targetType) {

        final boolean isSourceNumericType = isBasicNumericType(sourceType);
        final boolean isTargetNumericType = isBasicNumericType(targetType);
        if (isSourceNumericType && isTargetNumericType) {
            // We only reach here for different numeric types.
            // 2019R3 Spec defines numeric conversion between each type.
            return true;
        }
        if (targetType.tag == TypeTags.UNION) {
            HashSet<Integer> typeTags = new HashSet<>();
            for (BType bType : ((BUnionType) targetType).getMemberTypes()) {
                if (isBasicNumericType(bType)) {
                    typeTags.add(bType.tag);
                    if (typeTags.size() > 1) {
                        // Multiple Basic numeric types found in the union.
                        return false;
                    }
                }
            }
        }

        if (!isTargetNumericType && targetType.tag != TypeTags.UNION) {
            return false;
        }

        // Target type has at least one numeric type member.

        if (isSourceNumericType) {
            // i.e., a conversion from a numeric type to another numeric type in a union.
            // int|string u1 = <int|string> 1.0;
            // TODO : Fix me. This doesn't belong here.
            setImplicitCastExpr(expr, sourceType, symTable.anyType);
            return true;
        }

        // TODO : Do we need this? This doesn't belong here.
        switch (sourceType.tag) {
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.JSON:
                // This
                return true;
            case TypeTags.UNION:
                for (BType memType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (isBasicNumericType(memType) ||
                            (memType.tag == TypeTags.FINITE &&
                                    finiteTypeContainsNumericTypeValues((BFiniteType) memType))) {
                        return true;
                    }
                }
                break;
            case TypeTags.FINITE:
                if (finiteTypeContainsNumericTypeValues((BFiniteType) sourceType)) {
                    return true;
                }
                break;
        }
        return false;
    }

    private boolean isAllErrorMembers(BUnionType actualType) {

        return actualType.getMemberTypes().stream().allMatch(t -> isAssignable(t, symTable.errorType));
    }

    public void setImplicitCastExpr(BLangExpression expr, BType actualType, BType expType) {

        if (!isImplicityCastable(actualType, expType)) {
            return;
        }
        BLangTypeConversionExpr implicitConversionExpr =
                (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        implicitConversionExpr.pos = expr.pos;
        implicitConversionExpr.expr = expr.impConversionExpr == null ? expr : expr.impConversionExpr;
        implicitConversionExpr.type = expType;
        implicitConversionExpr.targetType = expType;
        implicitConversionExpr.internal = true;
        expr.impConversionExpr = implicitConversionExpr;
    }

    public BType getElementType(BType type) {
        if (type.tag != TypeTags.ARRAY) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    public boolean checkListenerCompatibilityAtServiceDecl(BType type) {
        if (type.tag == TypeTags.UNION) {
            // There should be at least one listener compatible type and all the member types, except error type
            // should be listener compatible.
            int listenerCompatibleTypeCount = 0;
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (memberType.tag != TypeTags.ERROR) {
                    if (!checkListenerCompatibility(memberType)) {
                        return false;
                    }
                    listenerCompatibleTypeCount++;
                }
            }
            return listenerCompatibleTypeCount > 0;
        }
        return checkListenerCompatibility(type);
    }

    public boolean checkListenerCompatibility(BType type) {
        if (type.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) type;
            for (BType memberType : unionType.getMemberTypes()) {
                if (!checkListenerCompatibility(memberType)) {
                    return false;
                }
            }
            return true;
        }

        if (type.tag != TypeTags.OBJECT) {
            return false;
        }

        BObjectType rhsType = (BObjectType) type;
        List<BAttachedFunction> rhsFuncs = ((BStructureTypeSymbol) rhsType.tsymbol).attachedFuncs;

        ListenerValidationModel listenerValidationModel = new ListenerValidationModel(this, symTable);
        return listenerValidationModel.checkMethods(rhsFuncs);

    }

    public boolean isValidErrorDetailType(BType detailType) {
        switch (detailType.tag) {
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return isAssignable(detailType, symTable.detailType);
        }
        return false;
    }

    // private methods

    private boolean isSealedRecord(BType recordType) {
        return recordType.getKind() == TypeKind.RECORD && ((BRecordType) recordType).sealed;
    }

    private boolean isNullable(BType fieldType) {
        return fieldType.isNullable();
    }

    private class BSameTypeVisitor implements BTypeVisitor<BType, Boolean> {

        Set<TypePair> unresolvedTypes;

        BSameTypeVisitor(Set<TypePair> unresolvedTypes) {
            this.unresolvedTypes = unresolvedTypes;
        }

        @Override
        public Boolean visit(BType t, BType s) {

            if (t == s) {
                return true;
            }
            switch (t.tag) {
                case TypeTags.INT:
                case TypeTags.BYTE:
                case TypeTags.FLOAT:
                case TypeTags.DECIMAL:
                case TypeTags.STRING:
                case TypeTags.BOOLEAN:
                    return t.tag == s.tag
                            && (TypeParamAnalyzer.isTypeParam(t) || TypeParamAnalyzer.isTypeParam(s));
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                    return t.tag == s.tag && hasSameReadonlyFlag(s, t)
                            && (TypeParamAnalyzer.isTypeParam(t) || TypeParamAnalyzer.isTypeParam(s));
                default:
                    break;
            }
            return false;

        }

        @Override
        public Boolean visit(BBuiltInRefType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BAnyType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BAnydataType t, BType s) {
            if (t == s) {
                return true;
            }
            return t.tag == s.tag;
        }

        @Override
        public Boolean visit(BMapType t, BType s) {
            if (s.tag != TypeTags.MAP || !hasSameReadonlyFlag(s, t)) {
                return false;
            }
            // At this point both source and target types are of map types. Inorder to be equal in type as whole
            // constraints should be in equal type.
            BMapType sType = ((BMapType) s);
            return isSameType(sType.constraint, t.constraint, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BFutureType t, BType s) {
            return s.tag == TypeTags.FUTURE && t.constraint.tag == ((BFutureType) s).constraint.tag;
        }

        @Override
        public Boolean visit(BXMLType t, BType s) {
            return visit((BBuiltInRefType) t, s);
        }

        @Override
        public Boolean visit(BJSONType t, BType s) {
            return s.tag == TypeTags.JSON && hasSameReadonlyFlag(s, t);
        }

        @Override
        public Boolean visit(BArrayType t, BType s) {
            return s.tag == TypeTags.ARRAY && hasSameReadonlyFlag(s, t) && isSameArrayType(s, t, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BObjectType t, BType s) {
            if (t == s) {
                return true;
            }

            if (s.tag != TypeTags.OBJECT) {
                return false;
            }

            return t.tsymbol.pkgID.equals(s.tsymbol.pkgID) && t.tsymbol.name.equals(s.tsymbol.name);
        }

        @Override
        public Boolean visit(BRecordType t, BType s) {
            if (t == s) {
                return true;
            }

            if (s.tag != TypeTags.RECORD || !hasSameReadonlyFlag(s, t)) {
                return false;
            }

            BRecordType source = (BRecordType) s;

            if (source.fields.size() != t.fields.size()) {
                return false;
            }

            for (BField sourceField : source.fields.values()) {
                if (t.fields.containsKey(sourceField.name.value)) {
                    BField targetField = t.fields.get(sourceField.name.value);
                    if (isSameType(sourceField.type, targetField.type, this.unresolvedTypes) &&
                            hasSameOptionalFlag(sourceField.symbol, targetField.symbol) &&
                            (!Symbols.isFlagOn(targetField.symbol.flags, Flags.READONLY) ||
                                     Symbols.isFlagOn(sourceField.symbol.flags, Flags.READONLY))) {
                        continue;
                    }
                }
                return false;
            }
            return isSameType(source.restFieldType, t.restFieldType, this.unresolvedTypes);
        }

        private boolean hasSameOptionalFlag(BVarSymbol s, BVarSymbol t) {
            return ((s.flags & Flags.OPTIONAL) ^ (t.flags & Flags.OPTIONAL)) != Flags.OPTIONAL;
        }

        private boolean hasSameReadonlyFlag(BType source, BType target) {
            return Symbols.isFlagOn(target.flags, Flags.READONLY) == Symbols.isFlagOn(source.flags, Flags.READONLY);
        }

        public Boolean visit(BTupleType t, BType s) {
            if (s.tag != TypeTags.TUPLE || !hasSameReadonlyFlag(s, t)) {
                return false;
            }
            BTupleType source = (BTupleType) s;
            if (source.tupleTypes.size() != t.tupleTypes.size()) {
                return false;
            }
            for (int i = 0; i < source.tupleTypes.size(); i++) {
                if (t.getTupleTypes().get(i) == symTable.noType) {
                    continue;
                }
                if (!isSameType(source.getTupleTypes().get(i), t.tupleTypes.get(i), this.unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Boolean visit(BStreamType t, BType s) {
            return s.tag == TypeTags.STREAM && isSameStreamType(s, t, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BTableType t, BType s) {
            return t == s;
        }

        @Override
        public Boolean visit(BInvokableType t, BType s) {
            return s.tag == TypeTags.INVOKABLE && isSameFunctionType((BInvokableType) s, t, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BUnionType tUnionType, BType s) {
            if (s.tag != TypeTags.UNION || !hasSameReadonlyFlag(s, tUnionType)) {
                return false;
            }

            BUnionType sUnionType = (BUnionType) s;

            if (sUnionType.getMemberTypes().size()
                    != tUnionType.getMemberTypes().size()) {
                return false;
            }

            Set<BType> sourceTypes = new LinkedHashSet<>(sUnionType.getMemberTypes().size());
            Set<BType> targetTypes = new LinkedHashSet<>(tUnionType.getMemberTypes().size());

            sourceTypes.add(sUnionType);
            sourceTypes.addAll(sUnionType.getMemberTypes());
            targetTypes.add(tUnionType);
            targetTypes.addAll(tUnionType.getMemberTypes());

            boolean notSameType = sourceTypes
                    .stream()
                    .map(sT -> targetTypes
                            .stream()
                            .anyMatch(it -> isSameType(it, sT, this.unresolvedTypes)))
                    .anyMatch(foundSameType -> !foundSameType);
            return !notSameType;
        }

        @Override
        public Boolean visit(BIntersectionType tIntersectionType, BType s) {
            if (s.tag != TypeTags.INTERSECTION || !hasSameReadonlyFlag(s, tIntersectionType)) {
                return false;
            }

            BIntersectionType sIntersectionType = (BIntersectionType) s;

            if (sIntersectionType.getConstituentTypes().size() != tIntersectionType.getConstituentTypes().size()) {
                return false;
            }

            Set<BType> sourceTypes = new LinkedHashSet<>(sIntersectionType.getConstituentTypes());
            Set<BType> targetTypes = new LinkedHashSet<>(tIntersectionType.getConstituentTypes());

            for (BType sourceType : sourceTypes) {
                boolean foundSameType = false;

                for (BType targetType : targetTypes) {
                    if (isSameType(sourceType, targetType, this.unresolvedTypes)) {
                        foundSameType = true;
                        break;
                    }
                }

                if (!foundSameType) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public Boolean visit(BErrorType t, BType s) {
            if (s.tag != TypeTags.ERROR) {
                return false;
            }
            BErrorType source = (BErrorType) s;

            if (!source.typeIdSet.equals(t.typeIdSet)) {
                return false;
            }

            if (source.detailType == t.detailType) {
                return true;
            }

            return isSameType(source.detailType, t.detailType, this.unresolvedTypes);
        }

        @Override
        public Boolean visit(BTypedescType t, BType s) {

            if (s.tag != TypeTags.TYPEDESC) {
                return false;
            }
            BTypedescType sType = ((BTypedescType) s);
            return isSameType(sType.constraint, t.constraint, this.unresolvedTypes);
        }


        @Override
        public Boolean visit(BFiniteType t, BType s) {

            return s == t;
        }

        @Override
        public Boolean visit(BParameterizedType t, BType s) {
            if (s.tag != TypeTags.PARAMETERIZED_TYPE) {
                return false;
            }

            BParameterizedType sType = (BParameterizedType) s;
            return isSameType(sType.paramValueType, t.paramValueType) && sType.paramSymbol.equals(t.paramSymbol);
        }

    };

    private boolean checkFieldEquivalency(BRecordType lhsType, BRecordType rhsType, Set<TypePair> unresolvedTypes) {
        Map<String, BField> rhsFields = new LinkedHashMap<>(rhsType.fields);

        // Check if the RHS record has corresponding fields to those of the LHS record.
        for (BField lhsField : lhsType.fields.values()) {
            BField rhsField = rhsFields.get(lhsField.name.value);

            // If LHS field is required, there should be a corresponding RHS field
            if (rhsField == null) {
                if (!Symbols.isOptional(lhsField.symbol)) {
                    return false;
                }
                continue;
            }
            if (hasIncompatibleReadOnlyFlags(lhsField.symbol.flags, rhsField.symbol.flags)) {
                return false;
            }

            // If LHS field is required, so should the RHS field
            if (!Symbols.isOptional(lhsField.symbol) && Symbols.isOptional(rhsField.symbol)) {
                return false;
            }

            // The corresponding RHS field should be assignable to the LHS field.
            if (!isAssignable(rhsField.type, lhsField.type, unresolvedTypes)) {
                return false;
            }

            rhsFields.remove(lhsField.name.value);
        }

        // If there are any remaining RHS fields, the types of those should be assignable to the rest field type of
        // the LHS record.
        return rhsFields.entrySet().stream().allMatch(
                fieldEntry -> isAssignable(fieldEntry.getValue().type, lhsType.restFieldType, unresolvedTypes));
    }

    private BAttachedFunction getMatchingInvokableType(List<BAttachedFunction> rhsFuncList, BAttachedFunction lhsFunc,
                                                       Set<TypePair> unresolvedTypes) {
        return rhsFuncList.stream()
                .filter(rhsFunc -> lhsFunc.funcName.equals(rhsFunc.funcName))
                .filter(rhsFunc -> isFunctionTypeAssignable(rhsFunc.type, lhsFunc.type, unresolvedTypes))
                .findFirst()
                .orElse(null);
    }

    private boolean isInSameVisibilityRegion(BSymbol lhsSym, BSymbol rhsSym) {
        if (Symbols.isPrivate(lhsSym)) {
            return Symbols.isPrivate(rhsSym) && lhsSym.pkgID.equals(rhsSym.pkgID)
                    && lhsSym.owner.name.equals(rhsSym.owner.name);
        } else if (Symbols.isPublic(lhsSym)) {
            return Symbols.isPublic(rhsSym);
        }
        return !Symbols.isPrivate(rhsSym) && !Symbols.isPublic(rhsSym) && lhsSym.pkgID.equals(rhsSym.pkgID);
    }

    private boolean isAssignableToUnionType(BType source, BType target, Set<TypePair> unresolvedTypes) {
        TypePair pair = new TypePair(source, target);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }

        Set<BType> sourceTypes = new LinkedHashSet<>();
        Set<BType> targetTypes = new LinkedHashSet<>();

        if (source.tag == TypeTags.UNION || source.tag == TypeTags.JSON || source.tag == TypeTags.ANYDATA) {
            sourceTypes.addAll(getEffectiveMemberTypes((BUnionType) source));
        } else {
            sourceTypes.add(source);
        }

        boolean targetIsAUnion = false;
        if (target.tag == TypeTags.UNION) {
            targetIsAUnion = true;
            targetTypes.addAll(getEffectiveMemberTypes((BUnionType) target));
        } else {
            targetTypes.add(target);
        }

        // check if all the value types are assignable between two unions
        var sourceIterator = sourceTypes.iterator();
        while (sourceIterator.hasNext()) {
            BType sMember = sourceIterator.next();
            if (sMember.tag == TypeTags.NEVER) {
                sourceIterator.remove();
                continue;
            }
            if (sMember.tag == TypeTags.FINITE && isAssignable(sMember, target, unresolvedTypes)) {
                sourceIterator.remove();
                continue;
            }
            if (sMember.tag == TypeTags.XML &&
                    isAssignableToUnionType(expandedXMLBuiltinSubtypes, target, unresolvedTypes)) {
                sourceIterator.remove();
                continue;
            }

            if (!isValueType(sMember)) {
                if (!targetIsAUnion) {
                    continue;
                }
                BUnionType targetUnion = (BUnionType) target;
                // prevent cyclic unions being compared as individual items
                if (sMember instanceof BUnionType) {
                    BUnionType sUnion = (BUnionType) sMember;
                    if (sUnion.isCyclic && targetUnion.isCyclic) {
                        unresolvedTypes.add(new TypePair(sUnion, targetUnion));
                         if (isAssignable(sUnion, targetUnion, unresolvedTypes)) {
                             sourceIterator.remove();
                             continue;
                         }
                    }
                }
                // readonly can match to a union similar to any|error
                if (sMember.tag == TypeTags.READONLY) {
                    unresolvedTypes.add(new TypePair(sMember, targetUnion));
                    if (isAssignable(sMember, targetUnion, unresolvedTypes)) {
                        sourceIterator.remove();
                        continue;
                    }
                }
                continue;
            }

            boolean sourceTypeIsNotAssignableToAnyTargetType = true;
            var targetIterator = targetTypes.iterator();
            while (targetIterator.hasNext()) {
                BType t = targetIterator.next();
                if (isAssignable(sMember, t, unresolvedTypes)) {
                    sourceTypeIsNotAssignableToAnyTargetType = false;
                    break;
                }
            }
            if (sourceTypeIsNotAssignableToAnyTargetType) {
                return false;
            }
        }

        // check the structural values for similarity
        sourceIterator = sourceTypes.iterator();
        while (sourceIterator.hasNext()) {
            BType sourceMember = sourceIterator.next();
            boolean sourceTypeIsNotAssignableToAnyTargetType = true;
            var targetIterator = targetTypes.iterator();

            boolean selfReferencedSource = (sourceMember != source) &&
                    isSelfReferencedStructuredType(source, sourceMember);

            while (targetIterator.hasNext()) {
                BType targetMember = targetIterator.next();

                boolean selfReferencedTarget = isSelfReferencedStructuredType(target, targetMember);
                if (selfReferencedTarget && selfReferencedSource && (sourceMember.tag == targetMember.tag)) {
                    sourceTypeIsNotAssignableToAnyTargetType = false;
                    break;
                }

                if (isAssignable(sourceMember, targetMember, unresolvedTypes)) {
                    sourceTypeIsNotAssignableToAnyTargetType = false;
                    break;
                }
            }
            if (sourceTypeIsNotAssignableToAnyTargetType) {
                return false;
            }
        }

        unresolvedTypes.add(pair);
        return true;
    }

    public boolean isSelfReferencedStructuredType(BType source, BType s) {
        if (source == s) {
            return true;
        }
        if (s.tag == TypeTags.ARRAY) {
            return isSelfReferencedStructuredType(source, ((BArrayType) s).eType);
        }
        if (s.tag == TypeTags.MAP) {
            return isSelfReferencedStructuredType(source, ((BMapType) s).constraint);
        }
        if (s.tag == TypeTags.TABLE) {
            return isSelfReferencedStructuredType(source, ((BTableType) s).constraint);
        }
        return false;
    }

    public BType updateSelfReferencedWithNewType(BType source, BType s, BType target) {
        if (s.tag == TypeTags.ARRAY) {
            BArrayType arrayType = (BArrayType) s;
            if (arrayType.eType == source) {
                return new BArrayType(target, arrayType.tsymbol, arrayType.size,
                        arrayType.state, arrayType.flags);
            }
        }
        if (s.tag == TypeTags.MAP) {
            BMapType mapType = (BMapType) s;
            if (mapType.constraint == source) {
                return new BMapType(mapType.tag, target, mapType.tsymbol, mapType.flags);
            }
        }
        if (s.tag == TypeTags.TABLE) {
            BTableType tableType = (BTableType) s;
            if (tableType.constraint == source) {
                return new BTableType(tableType.tag, target, tableType.tsymbol,
                        tableType.flags);
            } else if (tableType.constraint instanceof BMapType) {
                return updateSelfReferencedWithNewType(source, (BMapType) tableType.constraint, target);
            }
        }
        return s;
    }

    public static void fixSelfReferencingSameUnion(BType originalMemberType, BUnionType origUnionType,
                                                    BType immutableMemberType, BUnionType newImmutableUnion,
                                                    LinkedHashSet<BType> readOnlyMemTypes) {
        boolean sameMember = originalMemberType == immutableMemberType;
        if (originalMemberType.tag == TypeTags.ARRAY) {
            var arrayType = (BArrayType) originalMemberType;
            if (origUnionType == arrayType.eType) {
                if (sameMember) {
                    BArrayType newArrayType = new BArrayType(newImmutableUnion, arrayType.tsymbol, arrayType.size,
                            arrayType.state, arrayType.flags);
                    readOnlyMemTypes.add(newArrayType);
                } else {
                    ((BArrayType) immutableMemberType).eType = newImmutableUnion;
                    readOnlyMemTypes.add(immutableMemberType);
                }
            }
        } else if (originalMemberType.tag == TypeTags.MAP) {
            var mapType = (BMapType) originalMemberType;
            if (origUnionType == mapType.constraint) {
                if (sameMember) {
                    BMapType newMapType = new BMapType(mapType.tag, newImmutableUnion, mapType.tsymbol, mapType.flags);
                    readOnlyMemTypes.add(newMapType);
                } else {
                    ((BMapType) immutableMemberType).constraint = newImmutableUnion;
                    readOnlyMemTypes.add(immutableMemberType);
                }
            }
        } else if (originalMemberType.tag == TypeTags.TABLE) {
            var tableType = (BTableType) originalMemberType;
            if (origUnionType == tableType.constraint) {
                if (sameMember) {
                    BTableType newTableType = new BTableType(tableType.tag, newImmutableUnion, tableType.tsymbol,
                            tableType.flags);
                    readOnlyMemTypes.add(newTableType);
                } else {
                    ((BTableType) immutableMemberType).constraint = newImmutableUnion;
                    readOnlyMemTypes.add(immutableMemberType);
                }
                return;
            }

            var immutableConstraint = ((BTableType) immutableMemberType).constraint;
            if (tableType.constraint.tag == TypeTags.MAP) {
                sameMember = tableType.constraint == immutableConstraint;
                var mapType = (BMapType) tableType.constraint;
                if (origUnionType == mapType.constraint) {
                    if (sameMember) {
                        BMapType newMapType = new BMapType(mapType.tag, newImmutableUnion, mapType.tsymbol,
                                mapType.flags);
                        ((BTableType) immutableMemberType).constraint = newMapType;
                    } else {
                        ((BTableType) immutableMemberType).constraint = newImmutableUnion;
                    }
                    readOnlyMemTypes.add(immutableMemberType);
                }
            }
        } else {
            readOnlyMemTypes.add(immutableMemberType);
        }
    }

    private Set<BType> getEffectiveMemberTypes(BUnionType unionType) {
        Set<BType> memTypes = new LinkedHashSet<>();

        for (BType memberType : unionType.getMemberTypes()) {
            if (memberType.tag == TypeTags.INTERSECTION) {
                BType effectiveType = ((BIntersectionType) memberType).effectiveType;
                if (effectiveType.tag == TypeTags.UNION) {
                    memTypes.addAll(getEffectiveMemberTypes((BUnionType) effectiveType));
                    continue;
                }
                memTypes.add(effectiveType);
                continue;
            }

            memTypes.add(memberType);
        }
        return memTypes;
    }

    private boolean isFiniteTypeAssignable(BFiniteType finiteType, BType targetType, Set<TypePair> unresolvedTypes) {
        if (targetType.tag == TypeTags.FINITE) {
            return finiteType.getValueSpace().stream()
                    .allMatch(expression -> isAssignableToFiniteType(targetType, (BLangLiteral) expression));
        }

        if (targetType.tag == TypeTags.UNION) {
            List<BType> unionMemberTypes = getAllTypes(targetType);
            return finiteType.getValueSpace().stream()
                    .allMatch(valueExpr ->  unionMemberTypes.stream()
                            .anyMatch(targetMemType -> targetMemType.tag == TypeTags.FINITE ?
                                    isAssignableToFiniteType(targetMemType, (BLangLiteral) valueExpr) :
                                    isAssignable(valueExpr.type, targetType, unresolvedTypes)));
        }

        return finiteType.getValueSpace().stream()
                .allMatch(expression -> isAssignable(expression.type, targetType, unresolvedTypes));
    }

    boolean isAssignableToFiniteType(BType type, BLangLiteral literalExpr) {
        if (type.tag != TypeTags.FINITE) {
            return false;
        }

        BFiniteType expType = (BFiniteType) type;
        return expType.getValueSpace().stream().anyMatch(memberLiteral -> {
            if (((BLangLiteral) memberLiteral).value == null) {
                return literalExpr.value == null;
            }
            // Check whether the literal that needs to be tested is assignable to any of the member literal in the
            // value space.
            return checkLiteralAssignabilityBasedOnType((BLangLiteral) memberLiteral, literalExpr);
        });
    }

    /**
     * Method to check the literal assignability based on the types of the literals. For numeric literals the
     * assignability depends on the equivalency of the literals. If the candidate literal could either be a simple
     * literal or a constant. In case of a constant, it is assignable to the base literal if and only if both
     * literals have same type and equivalent values.
     *
     * @param baseLiteral      Literal based on which we check the assignability.
     * @param candidateLiteral Literal to be tested whether it is assignable to the base literal or not.
     * @return true if assignable; false otherwise.
     */
    boolean checkLiteralAssignabilityBasedOnType(BLangLiteral baseLiteral, BLangLiteral candidateLiteral) {
        // Different literal kinds.
        if (baseLiteral.getKind() != candidateLiteral.getKind()) {
            return false;
        }
        Object baseValue = baseLiteral.value;
        Object candidateValue = candidateLiteral.value;
        int candidateTypeTag = candidateLiteral.type.tag;

        // Numeric literal assignability is based on assignable type and numeric equivalency of values.
        // If the base numeric literal is,
        // (1) byte: we can assign byte or a int simple literal (Not an int constant) with the same value.
        // (2) int: we can assign int literal or int constants with the same value.
        // (3) float: we can assign int simple literal(Not an int constant) or a float literal/constant with same value.
        // (4) decimal: we can assign int simple literal or float simple literal (Not int/float constants) or decimal
        // with the same value.
        switch (baseLiteral.type.tag) {
            case TypeTags.BYTE:
                if (candidateTypeTag == TypeTags.BYTE || (candidateTypeTag == TypeTags.INT &&
                        !candidateLiteral.isConstant && isByteLiteralValue((Long) candidateValue))) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.INT:
                if (candidateTypeTag == TypeTags.INT) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.SIGNED32_INT:
                if (candidateTypeTag == TypeTags.INT && isSigned32LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.SIGNED16_INT:
                if (candidateTypeTag == TypeTags.INT && isSigned16LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.SIGNED8_INT:
                if (candidateTypeTag == TypeTags.INT && isSigned8LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.UNSIGNED32_INT:
                if (candidateTypeTag == TypeTags.INT && isUnsigned32LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.UNSIGNED16_INT:
                if (candidateTypeTag == TypeTags.INT && isUnsigned16LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.UNSIGNED8_INT:
                if (candidateTypeTag == TypeTags.INT && isUnsigned8LiteralValue((Long) candidateValue)) {
                    return ((Number) baseValue).longValue() == ((Number) candidateValue).longValue();
                }
                break;
            case TypeTags.FLOAT:
                String baseValueStr = String.valueOf(baseValue);
                String originalValue = baseLiteral.originalValue != null ? baseLiteral.originalValue : baseValueStr;
                if (NumericLiteralSupport.isDecimalDiscriminated(originalValue)) {
                    return false;
                }
                double baseDoubleVal = Double.parseDouble(baseValueStr);
                double candidateDoubleVal;
                if (candidateTypeTag == TypeTags.INT && !candidateLiteral.isConstant) {
                    candidateDoubleVal = ((Long) candidateValue).doubleValue();
                    return baseDoubleVal == candidateDoubleVal;
                } else if (candidateTypeTag == TypeTags.FLOAT) {
                    candidateDoubleVal = Double.parseDouble(String.valueOf(candidateValue));
                    return baseDoubleVal == candidateDoubleVal;
                }
                break;
            case TypeTags.DECIMAL:
                BigDecimal baseDecimalVal = NumericLiteralSupport.parseBigDecimal(baseValue);
                BigDecimal candidateDecimalVal;
                if (candidateTypeTag == TypeTags.INT && !candidateLiteral.isConstant) {
                    candidateDecimalVal = new BigDecimal((long) candidateValue, MathContext.DECIMAL128);
                    return baseDecimalVal.compareTo(candidateDecimalVal) == 0;
                } else if (candidateTypeTag == TypeTags.FLOAT && !candidateLiteral.isConstant ||
                        candidateTypeTag == TypeTags.DECIMAL) {
                    if (NumericLiteralSupport.isFloatDiscriminated(String.valueOf(candidateValue))) {
                        return false;
                    }
                    candidateDecimalVal = NumericLiteralSupport.parseBigDecimal(candidateValue);
                    return baseDecimalVal.compareTo(candidateDecimalVal) == 0;
                }
                break;
            default:
                // Non-numeric literal kind.
                return baseValue.equals(candidateValue);
        }
        return false;
    }

    boolean isByteLiteralValue(Long longObject) {

        return (longObject.intValue() >= BBYTE_MIN_VALUE && longObject.intValue() <= BBYTE_MAX_VALUE);
    }

    boolean isSigned32LiteralValue(Long longObject) {

        return (longObject >= SIGNED32_MIN_VALUE && longObject <= SIGNED32_MAX_VALUE);
    }

    boolean isSigned16LiteralValue(Long longObject) {

        return (longObject.intValue() >= SIGNED16_MIN_VALUE && longObject.intValue() <= SIGNED16_MAX_VALUE);
    }

    boolean isSigned8LiteralValue(Long longObject) {

        return (longObject.intValue() >= SIGNED8_MIN_VALUE && longObject.intValue() <= SIGNED8_MAX_VALUE);
    }

    boolean isUnsigned32LiteralValue(Long longObject) {

        return (longObject >= 0 && longObject <= UNSIGNED32_MAX_VALUE);
    }

    boolean isUnsigned16LiteralValue(Long longObject) {

        return (longObject.intValue() >= 0 && longObject.intValue() <= UNSIGNED16_MAX_VALUE);
    }

    boolean isUnsigned8LiteralValue(Long longObject) {

        return (longObject.intValue() >= 0 && longObject.intValue() <= UNSIGNED8_MAX_VALUE);
    }

    boolean isCharLiteralValue(String literal) {

        return (literal.codePoints().count() == 1);
    }

    /**
     * Method to retrieve a type representing all the values in the value space of a finite type that are assignable to
     * the target type.
     *
     * @param finiteType the finite type
     * @param targetType the target type
     * @return a new finite type if at least one value in the value space of the specified finiteType is
     * assignable to targetType (the same if all are assignable), else semanticError
     */
    BType getTypeForFiniteTypeValuesAssignableToType(BFiniteType finiteType, BType targetType) {
        // finiteType - type Foo "foo";
        // targetType - type FooBar "foo"|"bar";
        if (isAssignable(finiteType, targetType)) {
            return finiteType;
        }

        // Identify all the values from the value space of the finite type that are assignable to the target type.
        // e.g., finiteType - type Foo "foo"|1 ;
        Set<BLangExpression> matchingValues = finiteType.getValueSpace().stream()
                .filter(
                        // case I: targetType - string ("foo" is assignable to string)
                        // case II: targetType - type Bar "foo"|"baz" ; ("foo" is assignable to Bar)
                        expr -> isAssignable(expr.type, targetType) ||
                                isAssignableToFiniteType(targetType, (BLangLiteral) expr) ||
                                // type FooVal "foo";
                                // case III:  targetType - boolean|FooVal ("foo" is assignable to FooVal)
                                (targetType.tag == TypeTags.UNION &&
                                         ((BUnionType) targetType).getMemberTypes().stream()
                                                 .filter(memType ->  memType.tag == TypeTags.FINITE)
                                                 .anyMatch(filteredType -> isAssignableToFiniteType(filteredType,
                                                                                            (BLangLiteral) expr))))
                .collect(Collectors.toSet());

        if (matchingValues.isEmpty()) {
            return symTable.semanticError;
        }

        // Create a new finite type representing the assignable values.
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, finiteType.tsymbol.flags,
                names.fromString("$anonType$" + UNDERSCORE + finiteTypeCount++),
                finiteType.tsymbol.pkgID, null,
                finiteType.tsymbol.owner, finiteType.tsymbol.pos,
                VIRTUAL);
        BFiniteType intersectingFiniteType = new BFiniteType(finiteTypeSymbol, matchingValues);
        finiteTypeSymbol.type = intersectingFiniteType;
        return intersectingFiniteType;
    }

    /**
     * Method to retrieve a type representing all the member types of a union type that are assignable to
     * the target type.
     *
     * @param unionType  the union type
     * @param targetType the target type
     * @return           a single type or a new union type if at least one member type of the union type is
     *                      assignable to targetType, else semanticError
     */
    BType getTypeForUnionTypeMembersAssignableToType(BUnionType unionType, BType targetType, SymbolEnv env) {
        List<BType> intersection = new LinkedList<>();

        unionType.getMemberTypes().forEach(memType -> {
            BType memberIntersectionType = getTypeIntersection(memType, targetType, env);
            if (memberIntersectionType != symTable.semanticError) {
                intersection.add(memberIntersectionType);
            }
        });

        if (intersection.isEmpty()) {
            return symTable.semanticError;
        }

        if (intersection.size() == 1) {
            return intersection.get(0);
        } else {
            return BUnionType.create(null, new LinkedHashSet<>(intersection));
        }
    }

    boolean validEqualityIntersectionExists(BType lhsType, BType rhsType) {

        if (!isPureType(lhsType) || !isPureType(rhsType)) {
            return false;
        }

        if (isAssignable(lhsType, rhsType) || isAssignable(rhsType, lhsType)) {
            return true;
        }

        Set<BType> lhsTypes = expandAndGetMemberTypesRecursive(lhsType);
        Set<BType> rhsTypes = expandAndGetMemberTypesRecursive(rhsType);
        return equalityIntersectionExists(lhsTypes, rhsTypes);
    }

    private boolean equalityIntersectionExists(Set<BType> lhsTypes, Set<BType> rhsTypes) {
        if ((lhsTypes.contains(symTable.anydataType) &&
                     rhsTypes.stream().anyMatch(type -> type.tag != TypeTags.ERROR)) ||
                (rhsTypes.contains(symTable.anydataType) &&
                         lhsTypes.stream().anyMatch(type -> type.tag != TypeTags.ERROR))) {
            return true;
        }

        boolean matchFound = lhsTypes
                .stream()
                .anyMatch(s -> rhsTypes
                        .stream()
                        .anyMatch(t -> isSameType(s, t)));

        if (!matchFound) {
            matchFound = equalityIntersectionExistsForComplexTypes(lhsTypes, rhsTypes);
        }

        return matchFound;
    }

    /**
     * Retrieves member types of the specified type, expanding maps/arrays of/constrained by unions types to individual
     * maps/arrays.
     *
     * e.g., (string|int)[] would cause three entries as string[], int[], (string|int)[]
     *
     * @param bType the type for which member types needs to be identified
     * @return  a set containing all the retrieved member types
     */
    public Set<BType> expandAndGetMemberTypesRecursive(BType bType) {
        Set<BType> memberTypes = new LinkedHashSet<>();
        switch (bType.tag) {
            case TypeTags.BYTE:
            case TypeTags.INT:
                memberTypes.add(symTable.intType);
                memberTypes.add(symTable.byteType);
                break;
            case TypeTags.FINITE:
                BFiniteType expType = (BFiniteType) bType;
                expType.getValueSpace().forEach(value -> {
                    memberTypes.add(value.type);
                });
                break;
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) bType;
                unionType.getMemberTypes().forEach(member -> {
                    memberTypes.addAll(expandAndGetMemberTypesRecursive(member));
                });
                break;
            case TypeTags.ARRAY:
                BType arrayElementType = ((BArrayType) bType).getElementType();

                // add an unsealed array to allow comparison between closed and open arrays
                // TODO: 10/16/18 improve this, since it will allow comparison between sealed arrays of different sizes
                if (((BArrayType) bType).getSize() != -1) {
                    memberTypes.add(new BArrayType(arrayElementType));
                }

                if (arrayElementType.tag == TypeTags.UNION) {
                    Set<BType> elementUnionTypes = expandAndGetMemberTypesRecursive(arrayElementType);
                    elementUnionTypes.forEach(elementUnionType -> {
                        memberTypes.add(new BArrayType(elementUnionType));
                    });
                }
                memberTypes.add(bType);
                break;
            case TypeTags.MAP:
                BType mapConstraintType = ((BMapType) bType).getConstraint();
                if (mapConstraintType.tag == TypeTags.UNION) {
                    Set<BType> constraintUnionTypes = expandAndGetMemberTypesRecursive(mapConstraintType);
                    constraintUnionTypes.forEach(constraintUnionType -> {
                        memberTypes.add(new BMapType(TypeTags.MAP, constraintUnionType, symTable.mapType.tsymbol));
                    });
                }
                memberTypes.add(bType);
                break;
            default:
                memberTypes.add(bType);
        }
        return memberTypes;
    }

    private boolean tupleIntersectionExists(BTupleType lhsType, BTupleType rhsType) {
        if (lhsType.getTupleTypes().size() != rhsType.getTupleTypes().size()) {
            return false;
        }

        List<BType> lhsMemberTypes = lhsType.getTupleTypes();
        List<BType> rhsMemberTypes = rhsType.getTupleTypes();

        for (int i = 0; i < lhsType.getTupleTypes().size(); i++) {
            if (!equalityIntersectionExists(expandAndGetMemberTypesRecursive(lhsMemberTypes.get(i)),
                                            expandAndGetMemberTypesRecursive(rhsMemberTypes.get(i)))) {
                return false;
            }
        }
        return true;
    }

    private boolean equalityIntersectionExistsForComplexTypes(Set<BType> lhsTypes, Set<BType> rhsTypes) {
        for (BType lhsMemberType : lhsTypes) {
            switch (lhsMemberType.tag) {
                case TypeTags.INT:
                case TypeTags.STRING:
                case TypeTags.FLOAT:
                case TypeTags.DECIMAL:
                case TypeTags.BOOLEAN:
                case TypeTags.NIL:
                    if (rhsTypes.stream().anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON)) {
                        return true;
                    }
                    break;
                case TypeTags.JSON:
                    if (jsonEqualityIntersectionExists(rhsTypes)) {
                        return true;
                    }
                    break;
                // When expanding members for tuples, arrays and maps, set isValueDeepEquality to true, to allow
                // comparison between JSON lists/maps and primitive lists/maps since they are all reference types
                case TypeTags.TUPLE:
                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.TUPLE &&
                                    tupleIntersectionExists((BTupleType) lhsMemberType, (BTupleType) rhsMemberType))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.ARRAY &&
                                    arrayTupleEqualityIntersectionExists((BArrayType) rhsMemberType,
                                                                         (BTupleType) lhsMemberType))) {
                        return true;
                    }
                    break;
                case TypeTags.ARRAY:
                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.ARRAY &&
                                    equalityIntersectionExists(
                                            expandAndGetMemberTypesRecursive(((BArrayType) lhsMemberType).eType),
                                            expandAndGetMemberTypesRecursive(((BArrayType) rhsMemberType).eType)))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.TUPLE &&
                                    arrayTupleEqualityIntersectionExists((BArrayType) lhsMemberType,
                                                                         (BTupleType) rhsMemberType))) {
                        return true;
                    }
                    break;
                case TypeTags.MAP:
                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.MAP &&
                                    equalityIntersectionExists(
                                            expandAndGetMemberTypesRecursive(((BMapType) lhsMemberType).constraint),
                                            expandAndGetMemberTypesRecursive(((BMapType) rhsMemberType).constraint)))) {
                        return true;
                    }

                    if (!isAssignable(((BMapType) lhsMemberType).constraint, symTable.errorType) &&
                            rhsTypes.stream().anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON)) {
                        // at this point it is guaranteed that the map is anydata
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.RECORD &&
                                    mapRecordEqualityIntersectionExists((BMapType) lhsMemberType,
                                                                        (BRecordType) rhsMemberType))) {
                        return true;
                    }
                    break;
                case TypeTags.OBJECT:
                case TypeTags.RECORD:
                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> checkStructEquivalency(rhsMemberType, lhsMemberType) ||
                                    checkStructEquivalency(lhsMemberType, rhsMemberType))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.RECORD &&
                                    recordEqualityIntersectionExists((BRecordType) lhsMemberType,
                                                                     (BRecordType) rhsMemberType))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(rhsMemberType -> rhsMemberType.tag == TypeTags.JSON) &&
                            jsonEqualityIntersectionExists(expandAndGetMemberTypesRecursive(lhsMemberType))) {
                        return true;
                    }

                    if (rhsTypes.stream().anyMatch(
                            rhsMemberType -> rhsMemberType.tag == TypeTags.MAP &&
                                    mapRecordEqualityIntersectionExists((BMapType) rhsMemberType,
                                                                        (BRecordType) lhsMemberType))) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private boolean arrayTupleEqualityIntersectionExists(BArrayType arrayType, BTupleType tupleType) {
        Set<BType> elementTypes = expandAndGetMemberTypesRecursive(arrayType.eType);

        return tupleType.tupleTypes.stream()
                .allMatch(tupleMemType -> equalityIntersectionExists(elementTypes,
                                                                     expandAndGetMemberTypesRecursive(tupleMemType)));
    }

    private boolean recordEqualityIntersectionExists(BRecordType lhsType, BRecordType rhsType) {
        Map<String, BField> lhsFields = lhsType.fields;
        Map<String, BField> rhsFields = rhsType.fields;

        List<Name> matchedFieldNames = new ArrayList<>();
        for (BField lhsField : lhsFields.values()) {
            if (rhsFields.containsKey(lhsField.name.value)) {
                if (!equalityIntersectionExists(expandAndGetMemberTypesRecursive(lhsField.type),
                                                expandAndGetMemberTypesRecursive(
                                                        rhsFields.get(lhsField.name.value).type))) {
                    return false;
                }
                matchedFieldNames.add(lhsField.getName());
            } else {
                if (Symbols.isFlagOn(lhsField.symbol.flags, Flags.OPTIONAL)) {
                    break;
                }

                if (rhsType.sealed) {
                    return false;
                }

                if (!equalityIntersectionExists(expandAndGetMemberTypesRecursive(lhsField.type),
                                                expandAndGetMemberTypesRecursive(rhsType.restFieldType))) {
                    return false;
                }
            }
        }

        for (BField rhsField : rhsFields.values()) {
            if (matchedFieldNames.contains(rhsField.getName())) {
                continue;
            }

            if (!Symbols.isFlagOn(rhsField.symbol.flags, Flags.OPTIONAL)) {
                if (lhsType.sealed) {
                    return false;
                }

                if (!equalityIntersectionExists(expandAndGetMemberTypesRecursive(rhsField.type),
                                                expandAndGetMemberTypesRecursive(lhsType.restFieldType))) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean mapRecordEqualityIntersectionExists(BMapType mapType, BRecordType recordType) {
        Set<BType> mapConstrTypes = expandAndGetMemberTypesRecursive(mapType.getConstraint());

        for (BField field : recordType.fields.values()) {
            if (!Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL) &&
                    !equalityIntersectionExists(mapConstrTypes, expandAndGetMemberTypesRecursive(field.type))) {
                return false;
            }
        }

        return true;
    }

    private boolean jsonEqualityIntersectionExists(Set<BType> typeSet) {
        for (BType type : typeSet) {
            switch (type.tag) {
                case TypeTags.MAP:
                    if (!isAssignable(((BMapType) type).constraint, symTable.errorType)) {
                        return true;
                    }
                    break;
                case TypeTags.RECORD:
                    BRecordType recordType = (BRecordType) type;
                    if (recordType.fields.values().stream()
                            .allMatch(field -> Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL) ||
                                    !isAssignable(field.type, symTable.errorType))) {
                        return true;
                    }
                    break;
                default:
                    if (isAssignable(type, symTable.jsonType)) {
                        return true;
                    }
            }
        }
        return false;
    }

    public BType getRemainingMatchExprType(BType originalType, BType typeToRemove) {
        switch (originalType.tag) {
            case TypeTags.UNION:
                return getRemainingType((BUnionType) originalType, getAllTypes(typeToRemove));
            case TypeTags.FINITE:
                return getRemainingType((BFiniteType) originalType, getAllTypes(typeToRemove));
            case TypeTags.TUPLE:
                return getRemainingType((BTupleType) originalType, typeToRemove);
            default:
                return originalType;
        }
    }

    private BType getRemainingType(BTupleType originalType, BType typeToRemove) {
        switch (typeToRemove.tag) {
            case TypeTags.TUPLE:
                return getRemainingType(originalType, (BTupleType) typeToRemove);
            case TypeTags.ARRAY:
                return getRemainingType(originalType, (BArrayType) typeToRemove);
            default:
                return originalType;
        }
    }

    private BType getRemainingType(BTupleType originalType, BTupleType typeToRemove) {
        if (originalType.restType != null) {
            return originalType;
        }

        List<BType> originalTupleTypes = new ArrayList<>(originalType.tupleTypes);
        List<BType> typesToRemove = new ArrayList<>(typeToRemove.tupleTypes);
        if (originalTupleTypes.size() < typesToRemove.size()) {
            return originalType;
        }
        List<BType> tupleTypes = new ArrayList<>();
        for (int i = 0; i < originalTupleTypes.size(); i++) {
            tupleTypes.add(getRemainingMatchExprType(originalTupleTypes.get(i), typesToRemove.get(i)));
        }
        if (typeToRemove.restType == null) {
            return new BTupleType(tupleTypes);
        }
        if (originalTupleTypes.size() == typesToRemove.size()) {
            return originalType;
        }
        for (int i = typesToRemove.size(); i < originalTupleTypes.size(); i++) {
            tupleTypes.add(getRemainingMatchExprType(originalTupleTypes.get(i), typeToRemove.restType));
        }
        return new BTupleType(tupleTypes);
    }

    private BType getRemainingType(BTupleType originalType, BArrayType typeToRemove) {
        BType eType = typeToRemove.eType;
        List<BType> tupleTypes = new ArrayList<>();
        for (BType tupleType : originalType.tupleTypes) {
            tupleTypes.add(getRemainingMatchExprType(tupleType, eType));
        }
        BTupleType remainingType = new BTupleType(tupleTypes);
        if (originalType.restType != null) {
            remainingType.restType = getRemainingMatchExprType(originalType.restType, eType);
        }
        return remainingType;
    }

    public BType getRemainingType(BType originalType, BType typeToRemove) {
        switch (originalType.tag) {
            case TypeTags.UNION:
                return getRemainingType((BUnionType) originalType, getAllTypes(typeToRemove));
            case TypeTags.FINITE:
                return getRemainingType((BFiniteType) originalType, getAllTypes(typeToRemove));
            case TypeTags.READONLY:
                return getRemainingType((BReadonlyType) originalType, typeToRemove);
            default:
                return originalType;
        }
    }

    // TODO: now only works for error. Probably we need to properly define readonly types here.
    private BType getRemainingType(BReadonlyType originalType, BType removeType) {
        if (removeType.tag == TypeTags.ERROR) {
            return symTable.anyAndReadonly;
        }

        return  originalType;
    }

    BType getTypeIntersection(BType lhsType, BType rhsType, SymbolEnv env) {
        List<BType> narrowingTypes = getAllTypes(rhsType);
        LinkedHashSet<BType> intersection = narrowingTypes.stream().map(type -> {
            if (isAssignable(type, lhsType)) {
                return type;
            } else if (isAssignable(lhsType, type)) {
                return lhsType;
            } else if (lhsType.tag == TypeTags.FINITE) {
                BType intersectionType = getTypeForFiniteTypeValuesAssignableToType((BFiniteType) lhsType, type);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (type.tag == TypeTags.FINITE) {
                BType intersectionType = getTypeForFiniteTypeValuesAssignableToType((BFiniteType) type, lhsType);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (lhsType.tag == TypeTags.UNION) {
                BType intersectionType = getTypeForUnionTypeMembersAssignableToType((BUnionType) lhsType, type, env);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (type.tag == TypeTags.UNION) {
                BType intersectionType = getTypeForUnionTypeMembersAssignableToType((BUnionType) type, lhsType, env);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (type.tag == TypeTags.NULL_SET) {
                return type;
            } else if (type.tag == TypeTags.ERROR && lhsType.tag == TypeTags.ERROR) {
                BType intersectionType = getIntersectionForErrorTypes(lhsType, type, env);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (type.tag == TypeTags.RECORD && lhsType.tag == TypeTags.RECORD) {
                BType intersectionType = createRecordIntersection(lhsType, type, env);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (type.tag == TypeTags.MAP && lhsType.tag == TypeTags.RECORD) {
                BType intersectionType = createRecordAndMapIntersection(lhsType, type, env);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (type.tag == TypeTags.RECORD && lhsType.tag == TypeTags.MAP) {
                BType intersectionType = createRecordAndMapIntersection(type, lhsType, env);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (type.tag == TypeTags.MAP && lhsType.tag == TypeTags.MAP) {
                BType intersectionType = createRecordAndMapIntersection(type, lhsType, env);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (type.tag == TypeTags.ARRAY && lhsType.tag == TypeTags.TUPLE) {
                BType intersectionType = createArrayAndTupleIntersection((BArrayType) type, (BTupleType) lhsType, env);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            } else if (type.tag == TypeTags.TUPLE && lhsType.tag == TypeTags.ARRAY) {
                BType intersectionType = createArrayAndTupleIntersection((BArrayType) lhsType, (BTupleType) type, env);
                if (intersectionType != symTable.semanticError) {
                    return intersectionType;
                }
            }
            return null;
        }).filter(type -> type != null).collect(Collectors.toCollection(LinkedHashSet::new));

        if (intersection.isEmpty()) {
            if (lhsType.tag == TypeTags.NULL_SET) {
                return lhsType;
            }
            return symTable.semanticError;
        }

        if (intersection.contains(symTable.semanticError)) {
            return symTable.semanticError;
        } else if (intersection.size() == 1) {
            return intersection.toArray(new BType[0])[0];
        } else {
            return BUnionType.create(null, intersection);
        }
    }

    private BType createArrayAndTupleIntersection(BArrayType arrayType, BTupleType tupleType, SymbolEnv env) {
        List<BType> tupleMemberTypes = new ArrayList<>();
        for (BType memberType : tupleType.tupleTypes) {
            BType intersectionType = getTypeIntersection(memberType, arrayType.eType, env);
            if (intersectionType == symTable.semanticError) {
                return symTable.semanticError;
            }
            tupleMemberTypes.add(intersectionType);
        }

        BType restIntersectionType = getTypeIntersection(tupleType.restType, arrayType.eType, env);
        if (restIntersectionType == symTable.semanticError) {
            return new BTupleType(null, tupleMemberTypes);
        }
        return new BTupleType(null, tupleMemberTypes, restIntersectionType, 0);
    }

    private BType getIntersectionForErrorTypes(BType lhsType, BType rhsType, SymbolEnv env) {

        BType detailTypeOne = ((BErrorType) lhsType).detailType;
        BType detailTypeTwo = ((BErrorType) rhsType).detailType;

        if (isSealedRecord(detailTypeOne) || isSealedRecord(detailTypeTwo)) {
            return symTable.semanticError;
        }

        BType detailIntersectionType = getTypeIntersection(detailTypeOne, detailTypeTwo, env);
        if (detailIntersectionType == symTable.semanticError) {
            return symTable.semanticError;
        }

        BErrorType intersectionErrorType = createErrorType(lhsType, rhsType, detailIntersectionType, env);

        return intersectionErrorType;
    }

    private BType createRecordIntersection(BType recordTypeOne, BType recordTypeTwo, SymbolEnv env) {

        BRecordType recordType = createAnonymousRecord(env);

        if (!populateRecordFields(recordType, recordTypeOne, env, null) ||
                !populateRecordFields(recordType, recordTypeTwo, env, null)) {
            return symTable.semanticError;
        }

        recordType.restFieldType = getTypeIntersection(((BRecordType) recordTypeOne).restFieldType,
                                                       ((BRecordType) recordTypeTwo).restFieldType, env);

        if (recordType.restFieldType == symTable.semanticError) {
            return symTable.semanticError;
        }

        return recordType;
    }

    private BRecordType createAnonymousRecord(SymbolEnv env) {
        EnumSet<Flag> flags = EnumSet.of(Flag.PUBLIC, Flag.ANONYMOUS);
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(flags), Names.EMPTY,
                                                                                env.enclPkg.packageID, null,
                                                                                env.scope.owner, null, VIRTUAL);
        recordSymbol.name = names.fromString(
                anonymousModelHelper.getNextAnonymousTypeKey(env.enclPkg.packageID));
        BInvokableType bInvokableType = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner, false,
                symTable.builtinPos, VIRTUAL);
        initFuncSymbol.retType = symTable.nilType;
        recordSymbol.initializerFunc = new BAttachedFunction(Names.INIT_FUNCTION_SUFFIX, initFuncSymbol,
                                                                         bInvokableType, symTable.builtinPos);
        recordSymbol.scope = new Scope(recordSymbol);

        BRecordType recordType = new BRecordType(recordSymbol);
        recordType.tsymbol = recordSymbol;
        recordSymbol.type = recordType;

        return recordType;
    }

    private BType createRecordAndMapIntersection(BType type, BType mapType, SymbolEnv env) {
        BRecordType intersectionRecord = createAnonymousRecord(env);
        if (!populateRecordFields(intersectionRecord, type, env, ((BMapType) mapType).constraint)) {
            return symTable.semanticError;
        }
        intersectionRecord.restFieldType = getRestFieldIntersectionType(type, (BMapType) mapType, env);
        if (intersectionRecord.restFieldType == symTable.semanticError) {
            return symTable.semanticError;
        }

        return intersectionRecord;
    }

    private BType getRestFieldIntersectionType(BType type, BMapType mapType, SymbolEnv env) {
        if (type.tag == TypeTags.RECORD) {
            return getTypeIntersection(((BRecordType) type).restFieldType, mapType.constraint, env);
        } else {
            return getTypeIntersection(((BMapType) type).constraint, mapType.constraint, env);
        }
    }

    private BErrorType createErrorType(BType lhsType, BType rhsType, BType detailType, SymbolEnv env) {
        BErrorType errorType = createErrorType(detailType, lhsType.flags, env);
        errorType.tsymbol.flags |= rhsType.flags;

        return errorType;
    }

    public BErrorType createErrorType(BType detailType, long flags, SymbolEnv env) {
        BErrorTypeSymbol errorTypeSymbol = Symbols.createErrorSymbol(flags, Names.EMPTY,
                                                                     env.enclPkg.symbol.pkgID, null,
                                                                     env.scope.owner, null, VIRTUAL);
        BErrorType errorType = new BErrorType(errorTypeSymbol, detailType);
        errorType.flags |= errorTypeSymbol.flags;
        errorTypeSymbol.type = errorType;
        errorType.typeIdSet = BTypeIdSet.emptySet();

        return errorType;
    }

    private boolean populateRecordFields(BRecordType recordType, BType originalType, SymbolEnv env, BType constraint) {
        BTypeSymbol intersectionRecordSymbol = recordType.tsymbol;
        // If the detail type is BMapType simply ignore since the resulting detail type has `anydata` as rest type.
        if (originalType.getKind() != TypeKind.RECORD) {
            return true;
        }
        BRecordType originalRecordType = (BRecordType) originalType;
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        for (BField origField : originalRecordType.fields.values()) {
            org.wso2.ballerinalang.compiler.util.Name origFieldName = origField.name;
            String nameString = origFieldName.value;

            BType recordFieldType = validateRecordField(recordType, origField, constraint, env);
            if (recordFieldType == symTable.semanticError) {
                return false;
            }

            BVarSymbol recordFieldSymbol = new BVarSymbol(origField.symbol.flags, origFieldName,
                                                          env.enclPkg.packageID, recordFieldType,
                                                          intersectionRecordSymbol, origField.pos, SOURCE);
            if (recordFieldType.tag == TypeTags.INVOKABLE && recordFieldType.tsymbol != null) {
                BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) recordFieldType.tsymbol;
                BInvokableSymbol invokableSymbol = (BInvokableSymbol) recordFieldSymbol;
                invokableSymbol.params = tsymbol.params;
                invokableSymbol.restParam = tsymbol.restParam;
                invokableSymbol.retType = tsymbol.returnType;
                invokableSymbol.flags = tsymbol.flags;
            }

            fields.put(nameString, new BField(origFieldName, null, recordFieldSymbol));
        }
        recordType.fields.putAll(fields);

        return true;
    }

    private BType validateRecordField(BRecordType recordType, BField origField, BType constraint, SymbolEnv env) {
        BType fieldType = validateOverlappingFields(recordType, origField);
        if (fieldType == symTable.semanticError) {
            return fieldType;
        }

        if (constraint == null) {
            return fieldType;
        }

        fieldType = getTypeIntersection(fieldType, constraint, env);
        if (fieldType != symTable.semanticError) {
            return fieldType;
        }

        if (Symbols.isOptional(origField.symbol)) {
            return null;
        }

        return symTable.semanticError;
    }

    private BType validateOverlappingFields(BRecordType recordType, BField origField) {
        BField overlappingField = recordType.fields.get(origField.name.value);
        if (overlappingField == null) {
            return origField.type;
        }

        if (isAssignable(overlappingField.type, origField.type)) {
            return overlappingField.type;
        }

        if (isAssignable(origField.type, overlappingField.type)) {
            return origField.type;
        }
        return symTable.semanticError;
    }

    private void removeErrorFromReadonlyType(List<BType> remainingTypes) {
        Iterator<BType> remainingIterator = remainingTypes.listIterator();
        boolean addAnyAndReadOnly = false;
        while (remainingIterator.hasNext()) {
            BType remainingType = remainingIterator.next();
            if (remainingType.tag != TypeTags.READONLY) {
                continue;
            }
            remainingIterator.remove();
            addAnyAndReadOnly = true;
        }
        if (addAnyAndReadOnly) {
            remainingTypes.add(symTable.anyAndReadonly);
        }
    }

    private BType getRemainingType(BUnionType originalType, List<BType> removeTypes) {
        List<BType> remainingTypes = getAllTypes(originalType);
        boolean hasErrorToRemove = false;
        for (BType removeType : removeTypes) {
            remainingTypes.removeIf(type -> isAssignable(type, removeType));
            if (!hasErrorToRemove && removeType.tag == TypeTags.ERROR) {
                hasErrorToRemove = true;
            }
        }

        if (hasErrorToRemove) {
            removeErrorFromReadonlyType(remainingTypes);
        }

        List<BType> finiteTypesToRemove = new ArrayList<>();
        List<BType> finiteTypesToAdd = new ArrayList<>();
        for (BType remainingType : remainingTypes) {
            if (remainingType.tag == TypeTags.FINITE) {
                BFiniteType finiteType = (BFiniteType) remainingType;
                finiteTypesToRemove.add(finiteType);
                BType remainingTypeWithMatchesRemoved = getRemainingType(finiteType, removeTypes);
                if (remainingTypeWithMatchesRemoved != symTable.semanticError) {
                    finiteTypesToAdd.add(remainingTypeWithMatchesRemoved);
                }
            }
        }
        remainingTypes.removeAll(finiteTypesToRemove);
        remainingTypes.addAll(finiteTypesToAdd);

        if (remainingTypes.size() == 1) {
            return remainingTypes.get(0);
        }

        if (remainingTypes.isEmpty()) {
            return symTable.nullSet;
        }

        return BUnionType.create(null, new LinkedHashSet<>(remainingTypes));
    }

    private BType getRemainingType(BFiniteType originalType, List<BType> removeTypes) {
        Set<BLangExpression> remainingValueSpace = new LinkedHashSet<>();

        for (BLangExpression valueExpr : originalType.getValueSpace()) {
            boolean matchExists = false;
            for (BType remType : removeTypes) {
                if (isAssignable(valueExpr.type, remType) ||
                        isAssignableToFiniteType(remType, (BLangLiteral) valueExpr)) {
                    matchExists = true;
                    break;
                }
            }

            if (!matchExists) {
                remainingValueSpace.add(valueExpr);
            }
        }

        if (remainingValueSpace.isEmpty()) {
            return symTable.semanticError;
        }

        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, originalType.tsymbol.flags,
                names.fromString("$anonType$" + UNDERSCORE + finiteTypeCount++),
                originalType.tsymbol.pkgID, null,
                originalType.tsymbol.owner, originalType.tsymbol.pos,
                VIRTUAL);
        BFiniteType intersectingFiniteType = new BFiniteType(finiteTypeSymbol, remainingValueSpace);
        finiteTypeSymbol.type = intersectingFiniteType;
        return intersectingFiniteType;
    }

    public BType getSafeType(BType type, boolean liftNil, boolean liftError) {
        // Since JSON, ANY and ANYDATA by default contain null, we need to create a new respective type which
        // is not-nullable.
        switch (type.tag) {
            case TypeTags.JSON:
                return new BJSONType((BJSONType) type, false);
            case TypeTags.ANY:
                return new BAnyType(type.tag, type.tsymbol, false);
            case TypeTags.ANYDATA:
                return new BAnydataType((BAnydataType) type, false);
            case TypeTags.READONLY:
                return new BReadonlyType(type.tag, type.tsymbol, false);
        }

        if (type.tag != TypeTags.UNION) {
            return type;
        }

        BUnionType unionType = (BUnionType) type;
        LinkedHashSet<BType> memTypes = new LinkedHashSet<>(unionType.getMemberTypes());
        BUnionType errorLiftedType = BUnionType.create(null, memTypes);

        if (liftNil) {
            errorLiftedType.remove(symTable.nilType);
        }

        if (liftError) {
            errorLiftedType.remove(symTable.errorType);
        }

        if (errorLiftedType.getMemberTypes().size() == 1) {
            return errorLiftedType.getMemberTypes().toArray(new BType[0])[0];
        }
        return errorLiftedType;
    }

    public List<BType> getAllTypes(BType type) {
        if (type.tag != TypeTags.UNION) {
            return Lists.of(type);
        }

        List<BType> memberTypes = new ArrayList<>();
        ((BUnionType) type).getMemberTypes().forEach(memberType -> memberTypes.addAll(getAllTypes(memberType)));
        return memberTypes;
    }

    public boolean isAllowedConstantType(BType type) {
        switch (type.tag) {
            case TypeTags.BOOLEAN:
            case TypeTags.INT:
                // TODO : Fix this, Issue : #21542
//            case TypeTags.SIGNED32_INT:
//            case TypeTags.SIGNED16_INT:
//            case TypeTags.SIGNED8_INT:
//            case TypeTags.UNSIGNED32_INT:
//            case TypeTags.UNSIGNED16_INT:
//            case TypeTags.UNSIGNED8_INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
                // TODO : Fix this, Issue : #21542
//            case TypeTags.CHAR_STRING:
            case TypeTags.NIL:
                return true;
            case TypeTags.MAP:
                return isAllowedConstantType(((BMapType) type).constraint);
            case TypeTags.FINITE:
                BLangExpression finiteValue = ((BFiniteType) type).getValueSpace().toArray(new BLangExpression[0])[0];
                return isAllowedConstantType(finiteValue.type);
            default:
                return false;
        }
    }

    public boolean isValidLiteral(BLangLiteral literal, BType targetType) {
        BType literalType = literal.type;
        if (literalType.tag == targetType.tag) {
            return true;
        }

        switch (targetType.tag) {
            case TypeTags.BYTE:
                return literalType.tag == TypeTags.INT && isByteLiteralValue((Long) literal.value);
            case TypeTags.DECIMAL:
                return literalType.tag == TypeTags.FLOAT || literalType.tag == TypeTags.INT;
            case TypeTags.FLOAT:
                return literalType.tag == TypeTags.INT;
            case TypeTags.SIGNED32_INT:
                return literalType.tag == TypeTags.INT && isSigned32LiteralValue((Long) literal.value);
            case TypeTags.SIGNED16_INT:
                return literalType.tag == TypeTags.INT && isSigned16LiteralValue((Long) literal.value);
            case TypeTags.SIGNED8_INT:
                return literalType.tag == TypeTags.INT && isSigned8LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED32_INT:
                return literalType.tag == TypeTags.INT && isUnsigned32LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED16_INT:
                return literalType.tag == TypeTags.INT && isUnsigned16LiteralValue((Long) literal.value);
            case TypeTags.UNSIGNED8_INT:
                return literalType.tag == TypeTags.INT && isUnsigned8LiteralValue((Long) literal.value);
            case TypeTags.CHAR_STRING:
                return literalType.tag == TypeTags.STRING && isCharLiteralValue((String) literal.value);
            default:
                return false;
        }
    }

    /**
     * Validate if the return type of the given function is a subtype of `error?`, containing `()`.
     *
     * @param function          The function of which the return type should be validated
     * @param diagnosticCode    The code to log if the return type is invalid
     */
    public void validateErrorOrNilReturn(BLangFunction function, DiagnosticCode diagnosticCode) {
        BType returnType = function.returnTypeNode.type;

        if (returnType.tag == TypeTags.NIL) {
            return;
        }

        if (returnType.tag == TypeTags.UNION) {
            Set<BType> memberTypes = ((BUnionType) returnType).getMemberTypes();
            if (returnType.isNullable() &&
                    memberTypes.stream().allMatch(type -> type.tag == TypeTags.NIL || type.tag == TypeTags.ERROR)) {
                return;
            }
        }

        dlog.error(function.returnTypeNode.pos, diagnosticCode, function.returnTypeNode.type.toString());
    }

    /**
     * Type vector of size two, to hold the source and the target types.
     *
     * @since 0.982.0
     */
    private static class TypePair {
        BType sourceType;
        BType targetType;

        public TypePair(BType sourceType, BType targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypePair)) {
                return false;
            }

            TypePair other = (TypePair) obj;
            return this.sourceType.equals(other.sourceType) && this.targetType.equals(other.targetType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceType, targetType);
        }
    }

    /**
     * A functional interface for parameterizing the type of type checking that needs to be done on the source and
     * target types.
     *
     * @since 0.995.0
     */
    private interface TypeEqualityPredicate {
        boolean test(BType source, BType target, Set<TypePair> unresolvedTypes);
    }

    public boolean hasFillerValue(BType type) {
        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
            case TypeTags.BOOLEAN:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.NIL:
            case TypeTags.TABLE:
            case TypeTags.ANYDATA:
            case TypeTags.MAP:
            case TypeTags.ANY:
                return true;
            case TypeTags.ARRAY:
                return checkFillerValue((BArrayType) type);
            case TypeTags.FINITE:
                return checkFillerValue((BFiniteType) type);
            case TypeTags.UNION:
                return checkFillerValue((BUnionType) type);
            case TypeTags.OBJECT:
                return checkFillerValue((BObjectType) type);
            case TypeTags.RECORD:
                return checkFillerValue((BRecordType) type);
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) type;
                return tupleType.getTupleTypes().stream().allMatch(eleType -> hasFillerValue(eleType));
            default:
                // filler value is 0
                if (TypeTags.isIntegerTypeTag(type.tag)) {
                    return true;
                }
                return false;
        }
    }

    private boolean checkFillerValue(BObjectType type) {
        if ((type.tsymbol.flags & Flags.CLASS) != Flags.CLASS) {
            return false;
        }

        BAttachedFunction initFunction = ((BObjectTypeSymbol) type.tsymbol).initializerFunc;
        if (initFunction == null) {
            return true;
        }
        if (initFunction.symbol.getReturnType().getKind() != TypeKind.NIL) {
            return false;
        }

        for (BVarSymbol bVarSymbol : initFunction.symbol.getParameters()) {
            if (!bVarSymbol.defaultableParam) {
                return false;
            }
        }
        return true;
    }

    /**
     * This will handle two types. Singleton : As singleton can have one value that value should it self be a valid fill
     * value Union : 1. if nil is a member it is the fill values 2. else all the values should belong to same type and
     * the default value for that type should be a member of the union precondition : value space should have at least
     * one element
     *
     * @param type BFiniteType union or finite
     * @return boolean whether type has a valid filler value or not
     */
    private boolean checkFillerValue(BFiniteType type) {
        if (type.isNullable()) {
            return true;
        }
        if (type.getValueSpace().size() == 1) { // For singleton types, that value is the implicit initial value
            return true;
        }
        Iterator iterator = type.getValueSpace().iterator();
        BLangExpression firstElement = (BLangExpression) iterator.next();
        boolean defaultFillValuePresent = isImplicitDefaultValue(firstElement);

        while (iterator.hasNext()) {
            BLangExpression value = (BLangExpression) iterator.next();
            if (!isSameBasicType(value.type, firstElement.type)) {
                return false;
            }
            if (!defaultFillValuePresent && isImplicitDefaultValue(value)) {
                defaultFillValuePresent = true;
            }
        }

        return defaultFillValuePresent;
    }

    private boolean hasImplicitDefaultValue(Set<BLangExpression> valueSpace) {
        for (BLangExpression expression : valueSpace) {
            if (isImplicitDefaultValue(expression)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkFillerValue(BUnionType type) {
        if (type.isNullable()) {
            return true;
        }

        Set<BType> memberTypes = new HashSet<>();
        boolean hasFillerValue = false;
        boolean defaultValuePresent = false;
        boolean finiteTypePresent = false;
        for (BType member : type.getMemberTypes()) {
            if (member.tag == TypeTags.FINITE) {
                Set<BType> uniqueValues = getValueTypes(((BFiniteType) member).getValueSpace());
                memberTypes.addAll(uniqueValues);
                if (!defaultValuePresent && hasImplicitDefaultValue(((BFiniteType) member).getValueSpace())) {
                    defaultValuePresent = true;
                }
                finiteTypePresent = true;
            } else {
                memberTypes.add(member);
            }
            if (!hasFillerValue && hasFillerValue(member)) {
                hasFillerValue = true;
            }
        }
        if (!hasFillerValue) {
            return false;
        }

        Iterator<BType> iterator = memberTypes.iterator();
        BType firstMember = iterator.next();
        while (iterator.hasNext()) {
            if (!isSameBasicType(firstMember, iterator.next())) {
                return false;
            }
        }

        if (finiteTypePresent) {
            return defaultValuePresent;
        }
        return true;
    }

    private boolean isSameBasicType(BType source, BType target) {
        if (isSameType(source, target)) {
            return true;
        }
        if (TypeTags.isIntegerTypeTag(source.tag) && TypeTags.isIntegerTypeTag(target.tag)) {
            return true;
        }
        return false;
    }

    private Set<BType> getValueTypes(Set<BLangExpression> valueSpace) {
        Set<BType> uniqueType = new HashSet<>();
        for (BLangExpression expression : valueSpace) {
            uniqueType.add(expression.type);
        }
        return uniqueType;
    }

    private boolean isImplicitDefaultValue(BLangExpression expression) {
        if ((expression.getKind() == NodeKind.LITERAL) || (expression.getKind() == NodeKind.NUMERIC_LITERAL)) {
            BLangLiteral literalExpression = (BLangLiteral) expression;
            BType literalExprType = literalExpression.type;
            Object value = literalExpression.getValue();
            switch (literalExprType.getKind()) {
                case INT:
                case BYTE:
                    return value.equals(Long.valueOf(0));
                case STRING:
                    return value == null || value.equals("");
                case DECIMAL:
                case FLOAT:
                    return value.equals(String.valueOf(0.0));
                case BOOLEAN:
                    return value.equals(Boolean.valueOf(false));
                case NIL:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private boolean checkFillerValue(BRecordType type) {
        for (BField field : type.fields.values()) {
            if (Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL)) {
                continue;
            }
            if (Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkFillerValue(BArrayType type) {
        if (type.size == -1) {
            return true;
        }
        return hasFillerValue(type.eType);
    }

    /**
     * Get result type of the query output.
     *
     * @param type type of query expression.
     * @return result type.
     */
    public BType resolveExprType(BType type) {
        switch (type.tag) {
            case TypeTags.STREAM:
                return ((BStreamType) type).constraint;
            case TypeTags.TABLE:
                return ((BTableType) type).constraint;
            case TypeTags.ARRAY:
                return ((BArrayType) type).eType;
            case TypeTags.UNION:
                List<BType> exprTypes = new ArrayList<>(((BUnionType) type).getMemberTypes());
                for (BType returnType : exprTypes) {
                    switch (returnType.tag) {
                        case TypeTags.STREAM:
                            return ((BStreamType) returnType).constraint;
                        case TypeTags.TABLE:
                            return ((BTableType) returnType).constraint;
                        case TypeTags.ARRAY:
                            return ((BArrayType) returnType).eType;
                        case TypeTags.STRING:
                        case TypeTags.XML:
                            return returnType;
                    }
                }
            default:
                return type;
        }
    }

    private boolean isSimpleBasicType(int tag) {
        switch (tag) {
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.BOOLEAN:
            case TypeTags.NIL:
                return true;
            default:
                return (TypeTags.isIntegerTypeTag(tag)) || (TypeTags.isStringTypeTag(tag));
        }
    }

    /**
     * Check whether a type is an ordered type.
     *
     * @param type type.
     * @return boolean whether the type is an ordered type or not.
     */
    public boolean isOrderedType(BType type) {
        switch (type.tag) {
            case TypeTags.UNION:
                Set<BType> memberTypes = ((BUnionType) type).getMemberTypes();
                for (BType memType : memberTypes) {
                    if (!isOrderedType(memType)) {
                        return false;
                    }
                }
                // can not sort (string?|int)/(string|int)/(string|int)[]/(string?|int)[], can sort string?/string?[]
                return memberTypes.size() <= 2 && memberTypes.contains(symTable.nilType);
            case TypeTags.ARRAY:
                BType elementType = ((BArrayType) type).eType;
                return isOrderedType(elementType);
            default:
                return isSimpleBasicType(type.tag);
        }
    }

    public boolean isUnionOfSimpleBasicTypes(BType type) {
        if (type.tag == TypeTags.UNION) {
            Set<BType> memberTypes = ((BUnionType) type).getMemberTypes();
            for (BType memType : memberTypes) {
                if (!isSimpleBasicType(memType.tag)) {
                    return false;
                }
            }
            return true;
        }
        return isSimpleBasicType(type.tag);
    }

    public boolean isSubTypeOfReadOnlyOrIsolatedObjectUnion(BType type) {
        if (isInherentlyImmutableType(type) || Symbols.isFlagOn(type.flags, Flags.READONLY)) {
            return true;
        }

        int tag = type.tag;

        if (tag == TypeTags.OBJECT) {
            return isIsolated(type);
        }

        if (tag != TypeTags.UNION) {
            return false;
        }

        for (BType memberType : ((BUnionType) type).getMemberTypes()) {
            if (!isSubTypeOfReadOnlyOrIsolatedObjectUnion(memberType)) {
                return false;
            }
        }
        return true;
    }

    private boolean isIsolated(BType type) {
        return Symbols.isFlagOn(type.flags, Flags.ISOLATED);
    }

    BType getTypeWithoutNil(BType type) {
        if (type.tag != TypeTags.UNION) {
            return type;
        }

        BUnionType unionType = (BUnionType) type;
        if (!unionType.isNullable()) {
            return unionType;
        }

        List<BType> nonNilTypes = new ArrayList<>();
        for (BType memberType : unionType.getMemberTypes()) {
            if (!isAssignable(memberType, symTable.nilType)) {
                nonNilTypes.add(memberType);
            }
        }

        if (nonNilTypes.size() == 1) {
            return nonNilTypes.get(0);
        }

        return BUnionType.create(null, new LinkedHashSet<>(nonNilTypes));
    }

    private static class ListenerValidationModel {
        private final Types types;
        private final SymbolTable symtable;
        private final BType serviceNameType;
        boolean attachFound;
        boolean detachFound;
        boolean startFound;
        boolean gracefulStopFound;
        boolean immediateStopFound;

        public ListenerValidationModel(Types types, SymbolTable symTable) {
            this.types = types;
            this.symtable = symTable;
            this.serviceNameType =
                    BUnionType.create(null, symtable.stringType, symtable.arrayStringType, symtable.nilType);
        }

        boolean isValidListener() {
            return attachFound && detachFound && startFound && gracefulStopFound && immediateStopFound;
        }

        private boolean checkMethods(List<BAttachedFunction> rhsFuncs) {
            for (BAttachedFunction func : rhsFuncs) {
                switch (func.funcName.value) {
                    case "attach":
                        if (!checkAttachMethod(func)) {
                            return false;
                        }
                        break;
                    case "detach":
                        if (!checkDetachMethod(func)) {
                            return false;
                        }
                        break;
                    case "start":
                        if (!checkStartMethod(func)) {
                            return true;
                        }
                        break;
                    case "gracefulStop":
                        if (!checkGracefulStop(func)) {
                            return false;
                        }
                        break;
                    case "immediateStop":
                        if (!checkImmediateStop(func)) {
                            return false;
                        }
                        break;
                }
            }
            return isValidListener();
        }

        private boolean emptyParamList(BAttachedFunction func) {
            return func.type.paramTypes.isEmpty() && func.type.restType != symtable.noType;
        }

        private boolean publicAndReturnsErrorOrNil(BAttachedFunction func) {
            if (!Symbols.isPublic(func.symbol)) {
                return false;
            }

            return types.isAssignable(func.type.retType, symtable.errorOrNilType);
        }

        private boolean isPublicNoParamReturnsErrorOrNil(BAttachedFunction func) {
            if (!publicAndReturnsErrorOrNil(func)) {
                return false;
            }

            return emptyParamList(func);
        }

        private boolean checkImmediateStop(BAttachedFunction func) {
            return immediateStopFound = isPublicNoParamReturnsErrorOrNil(func);
        }

        private boolean checkGracefulStop(BAttachedFunction func) {
            return gracefulStopFound = isPublicNoParamReturnsErrorOrNil(func);
        }

        private boolean checkStartMethod(BAttachedFunction func) {
            return startFound = publicAndReturnsErrorOrNil(func);
        }

        private boolean checkDetachMethod(BAttachedFunction func) {
            if (!publicAndReturnsErrorOrNil(func)) {
                return false;
            }

            if (func.type.paramTypes.size() != 1) {
                return false;
            }

            return detachFound = isServiceObject(func.type.paramTypes.get(0));
        }

        private boolean checkAttachMethod(BAttachedFunction func) {
            if (!publicAndReturnsErrorOrNil(func)) {
                return false;
            }

            if (func.type.paramTypes.size() != 2) {
                return false;
            }

            BType firstParamType = func.type.paramTypes.get(0);
            if (!isServiceObject(firstParamType)) {
                return false;
            }

            BType secondParamType = func.type.paramTypes.get(1);
            boolean sameType = types.isAssignable(secondParamType, this.serviceNameType);
            return attachFound = sameType;

        }

        private boolean isServiceObject(BType type) {
            if (type.tag == TypeTags.UNION) {
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    if (!isServiceObject(memberType)) {
                        return false;
                    }
                }
                return true;
            }

            if (type.tag != TypeTags.OBJECT) {
                return false;
            }

            return Symbols.isService(type.tsymbol);
        }
    }
}

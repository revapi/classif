/*
 * Copyright 2018 Lukas Krejci
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grammar Classif;

fragment LETTER: 'a'..'z' | 'A'..'Z' | '$' | '_';
fragment DIGIT: '0'..'9';

COMMENT: '/' '/' .*? '\r'? '\n' -> skip;

WS: [ \t\r\n]+;
REGEX: '/' (~('/') | '\\/')+ '/';
STRING: '\'' (~'\'' | '\\\'')* '\'';
MATCH: 'match';
VAR_PREFIX: '%';
PRAGMA_PREFIX: '#';
STATEMENT_END: ';';
ANY: '*';
ANY_NUMBER_OF_THINGS: '**';
CLASS: 'class';
INTERFACE: 'interface';
ENUM: 'enum';
ANNOTATION_TYPE: '@interface';
TYPE: 'type';
DOT: '.';
LT: '<';
LE: '<=';
EQ: '=';
NE: '!=';
GE: '>=';
GT: '>';
WILDCARD: '?';
COMMA: ',';
EXTENDS: 'extends';
SUPER: 'super';
AND: '&';
OR: '|';
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
LPAR: '(';
RPAR: ')';
LSQPAR: '[';
RSQPAR: ']';
PACKAGE: 'package';
USES: 'uses';
PUBLIC: 'public';
PROTECTED: 'protected';
PRIVATE: 'private';
PACKAGE_PRIVATE: 'packageprivate';
STATIC: 'static';
FINAL: 'final';
ABSTRACT: 'abstract';
RETURN: '^';
DOUBLE_COLON: '::';
AT: '@';
PLUS: '+';
MINUS: '-';
NUMBER: (PLUS | MINUS)? DIGIT+ (DOT DIGIT+)?;
BOOLEAN: 'true' | 'false';
THROWS: 'throws';
OVERRIDES: 'overrides';
IMPLEMENTS: 'implements';
NOT: '!';
DIRECTLY: 'directly';
DEFAULT: 'default';
VOLATILE: 'volatile';
NATIVE: 'native';
TRANSIENT: 'transient';
SYNCHRONIZED: 'synchronized';
USED_BY: 'usedby';
EXACTLY: 'exactly';
FROM: 'from';
STRICT_HIERARCHY: 'strictHierarchy';
STRICTFP: 'strictfp';

//needs to be the last
WORD: LETTER (LETTER | DIGIT)*;

program
    : pragmas? matchStatement? statement+ WS? EOF
    ;

pragmas
    : (WS? PRAGMA_PREFIX pragma end)+
    ;

pragma
    : STRICT_HIERARCHY
    ;

matchStatement
    : WS? MATCH WS variables end
    ;

statement
    : WS? annotations modifiers typeDefinitionOrGenericStatement
    ;

typeDefinitionOrGenericStatement
    : typeKind WS (returned | returned? possibleTypeAssignment) (WS typeConstraints)? WS?
        (OPEN_BRACE elementStatement* WS? CLOSE_BRACE | end)
    | (returned | returned? not? assignment? ANY) (WS genericConstraints)? end
    ;

not
    : NOT
    ;

end
    : WS? STATEMENT_END
    ;

fqn
    : name (DOT name)*
    ;

variable
    : not? VAR_PREFIX resolvedName
    ;

variables
    : variable (WS? OR WS? variable)*
    ;

assignment
    : VAR_PREFIX resolvedName EQ
    ;

annotations
    : (annotation WS)*
    ;

genericConstraints
    : (DIRECTLY WS)? USES WS typeReference
    ;

annotation
    : not? AT typeReference (LPAR WS? annotationAttributes? WS? RPAR)?
    ;

annotationAttributes
    : annotationAttribute (WS? COMMA WS? annotationAttribute)*
    ;

annotationAttribute
    : name WS? operator WS? annotationValue
    | ANY
    | ANY_NUMBER_OF_THINGS
    ;

operator
    : EQ | NE | LT | LE | GT | GE
    ;

annotationValue
    : STRING
    | REGEX
    | NUMBER
    | BOOLEAN
    | ANY
    | typeReference DOT CLASS
    | fqn DOT name
    | annotation
    | OPEN_BRACE annotationValueArrayContents? CLOSE_BRACE
    | DEFAULT
    ;

annotationValueArrayContents
    : annotationValueArray_strings
    | annotationValueArray_numbers
    | annotationValueArray_booleans
    | annotationValueArray_types
    | annotationValueArray_enums
    | annotationValueArray_annotations
    | REGEX (WS? COMMA WS? annotationValueArrayContents)?
    | ANY (WS? COMMA WS? annotationValueArrayContents)?
    | ANY_NUMBER_OF_THINGS (WS? COMMA WS? annotationValueArrayContents)?
    ;

annotationValueArray_strings
    : STRING (WS? COMMA WS? annotationValueArray_strings_next)*
    ;

annotationValueArray_strings_next
    : STRING | REGEX | ANY | ANY_NUMBER_OF_THINGS
    ;

annotationValueArray_numbers
    : NUMBER (WS? COMMA WS? annotationValueArray_numbers_next)*
    ;

annotationValueArray_numbers_next
    : NUMBER | REGEX | ANY | ANY_NUMBER_OF_THINGS
    ;

annotationValueArray_booleans
    : BOOLEAN (WS? COMMA WS? annotationValueArray_booleans_next)*
    ;

annotationValueArray_booleans_next
    : BOOLEAN | REGEX | ANY | ANY_NUMBER_OF_THINGS
    ;

annotationValueArray_types
    : typeReference DOT CLASS (WS? COMMA WS? annotationValueArray_types_next)*
    ;

annotationValueArray_types_next
    : typeReference DOT CLASS
    | ANY
    | ANY_NUMBER_OF_THINGS
    ;

annotationValueArray_enums
    : fqn DOT name (WS? COMMA WS? annotationValueArray_enums_next)*
    ;

annotationValueArray_enums_next
    : fqn DOT name
    | ANY
    | ANY_NUMBER_OF_THINGS
    ;

annotationValueArray_annotations
    : annotation (WS? COMMA WS? annotationValueArray_annotations_next)*
    ;

annotationValueArray_annotations_next
    : annotation
    | ANY
    | ANY_NUMBER_OF_THINGS
    ;

elementStatement
    : WS? annotations modifiers fieldOrMethodStatement end
    ;

fieldOrMethodStatement
    : typeParameters WS methodAfterTypeParametersStatement
    | fieldOrMethodWithoutTypeParameters
    ;

fieldOrMethodWithoutTypeParameters
    : (typeReference WS)? fieldNameOrMethodWithoutReturnType
    ;

fieldNameOrMethodWithoutReturnType
    : (typeReference DOUBLE_COLON)? returned? assignment? not? name (WS fieldConstraints | methodRestStatement)?
    ;

methodAfterTypeParametersStatement
    : (typeReference WS)? (typeReference DOUBLE_COLON)? methodNameAndRestStatement
    ;

methodNameAndRestStatement
    : returned? assignment? not? name methodRestStatement
    ;

methodRestStatement
    : LPAR WS? parameterList? WS? RPAR (WS methodConstraints)?
    ;


resolvedName
    // everything that is not a Java keyword
    : MATCH | TYPE | USES | PACKAGE_PRIVATE | OVERRIDES | DIRECTLY | USED_BY | EXACTLY | FROM | STRICT_HIERARCHY
    | WORD
    ;

name
    : resolvedName | ANY | ANY_NUMBER_OF_THINGS | REGEX
    ;

returned
    : RETURN
    ;

modifiers
    : (modifierCluster WS)*
    ;

modifier
    : not? (PUBLIC | PROTECTED | PACKAGE_PRIVATE | PRIVATE | STATIC | FINAL | ABSTRACT | VOLATILE | NATIVE | TRANSIENT | SYNCHRONIZED | DEFAULT | STRICTFP)
    ;

modifierCluster
    : modifier (WS? OR WS? modifier)*
    ;

typeReference
    : singleTypeReference (WS? OR WS? singleTypeReference)*
    ;

singleTypeReference
    : variable arrayType*
    | not? fqn typeParameters? arrayType*
    ;

possibleTypeAssignment
    : not? assignment? fqn typeParameters?
    ;

fieldConstraints
    : (DIRECTLY WS)? USES WS typeReference
    ;

parameterList
    : methodParameter (WS? COMMA WS? methodParameter)*
    ;

methodParameter
    : annotations typeReference
    ;

methodConstraints
    : methodConstraint (WS methodConstraint)*
    ;

methodConstraint
    : (DIRECTLY WS)? USES WS typeReference
    | THROWS WS typeReference (WS? COMMA WS? typeReference)*
    | OVERRIDES (WS FROM WS typeReference)?
    | not? DEFAULT
    | DEFAULT WS? operator WS? annotationValue
    ;

typeKind
    : not? (CLASS | INTERFACE | ENUM | ANNOTATION_TYPE | TYPE)
    ;

typeConstraints
    : typeConstraint (WS typeConstraint)*
    ;

typeConstraint
    : (DIRECTLY WS)? USES WS typeReference
    | (DIRECTLY WS)? USED_BY WS variables
    | (DIRECTLY WS)? (EXACTLY WS)? IMPLEMENTS WS typeReference (WS? COMMA WS? typeReference)*
    | (DIRECTLY WS)? EXTENDS WS typeReference
    ;

typeParameters
    : LT WS? typeParam (WS? COMMA WS? typeParam)* WS? GT
    ;

typeParam
    : typeParamWildcard
    | typeReference (WS? AND WS? typeReference)*
    ;

typeParamWildcard
    : WILDCARD
    | WILDCARD WS (EXTENDS | SUPER) WS typeReference (WS? AND WS? typeReference)*
    ;

arrayType
    : LSQPAR RSQPAR
    ;

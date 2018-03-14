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
WS: [ \t\r\n]+;
REGEX: '/' (~('/') | '\\/')+ '/';
STRING: '\'' (~'\'' | '\\\'')* '\'';
MATCH: 'match';
VAR_PREFIX: '%';
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
NUMBER: DIGIT+ (DOT DIGIT+)?;
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

//needs to be the last
WORD: LETTER (LETTER | DIGIT)*;

program
    : matchStatement? statement+ WS? EOF
    ;

matchStatement
    : WS? MATCH WS variables end
    ;

statement
    : WS? annotations modifiers typeDefinitionOrGenericStatement
    ;

typeDefinitionOrGenericStatement
    : typeKind WS (returned | returned? resolveableTypeReference) (WS typeConstraints)? WS?
        (OPEN_BRACE elementStatement* WS? CLOSE_BRACE | end)
    | (returned | ANY) (WS genericConstraints)? end
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
    : USES WS typeReference
    ;

annotation
    : returned? assignment? not? AT typeReference (LPAR WS? annotationAttributes? WS? RPAR)?
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
    | annotation
    | LSQPAR annotationValueArrayContents? RSQPAR
    ;

annotationValueArrayContents
    : STRING (WS? COMMA WS? (STRING | REGEX))*
    | NUMBER (WS? COMMA WS? (NUMBER | REGEX))*
    | BOOLEAN (WS? COMMA WS? (BOOLEAN | REGEX))*
    | typeReference DOT CLASS (WS? COMMA WS? typeReference DOT CLASS)*
    | annotation (WS? COMMA WS? annotation)*
    | REGEX (WS? COMMA WS? annotationValueArrayContents)?
    | ANY (WS? COMMA WS? annotationValueArrayContents)?
    ;

elementStatement
    : WS? annotations modifiers fieldOrMethodStatement end
    ;

fieldOrMethodStatement
    : typeParameters methodAfterTypeParametersStatement
    | fieldOrMethodWithoutTypeParameters
    ;

fieldOrMethodWithoutTypeParameters
    : (typeReference WS)? fieldNameOrMethodWithoutReturnType
    ;

fieldNameOrMethodWithoutReturnType
    : (typeReference DOUBLE_COLON)? returned? assignment? not? name (fieldConstraints | methodRestStatement)?
    ;

methodAfterTypeParametersStatement
    : (typeReference DOUBLE_COLON)? methodNameAndRestStatement
    ;

methodNameAndRestStatement
    : returned? assignment? not? name methodRestStatement
    ;

methodRestStatement
    : LPAR WS? parameterList? WS? RPAR (WS methodConstraints)?
    ;


resolvedName
    // everything that is not a Java keyword
    : MATCH | TYPE | USES | PACKAGE_PRIVATE | OVERRIDES | DIRECTLY | USED_BY | WORD
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
    : not? (PUBLIC | PROTECTED | PACKAGE_PRIVATE | PRIVATE | STATIC | FINAL | ABSTRACT | VOLATILE | NATIVE | TRANSIENT | SYNCHRONIZED)
    ;

modifierCluster
    : modifier
    | LPAR modifier (WS? OR WS? modifier)* RPAR
    ;

typeReference
    : singleTypeReference (WS? OR WS? singleTypeReference)*
    ;

singleTypeReference
    : not? variable arrayType?
    | not? fqn typeParameters? arrayType?
    ;

resolveableTypeReference
    : not? assignment? fqn typeParameters?
    ;

fieldConstraints
    : (DIRECTLY WS)? USES WS typeReference
    ;

parameterList
    : returned? assignment? typeReference (WS? COMMA WS? returned? assignment? typeReference)*
    ;

methodConstraints
    : methodConstraint (WS methodConstraint)*
    ;

methodConstraint
    : (DIRECTLY WS)? USES WS typeReference
    | THROWS WS typeReference
    | (DIRECTLY WS)? OVERRIDES WS typeReference?
    | DEFAULT WS annotationValue
    ;

typeKind
    : not? (CLASS | INTERFACE | ENUM | ANNOTATION_TYPE | TYPE)
    ;

typeConstraints
    : typeConstraint (WS typeConstraint)*
    ;

typeConstraint
    : (DIRECTLY WS)? USES WS typeReference
    | (DIRECTLY WS)? USED_BY WS typeReference
    | (DIRECTLY WS)? IMPLEMENTS WS typeReference (WS? COMMA WS? typeReference)*
    | (DIRECTLY WS)? EXTENDS WS typeReference
    ;

typeParameters
    : LT WS? typeParam (WS? COMMA WS? typeParam)* WS? GT
    ;

typeParam
    : typeParamWildcard
    | typeReference
    ;

typeParamWildcard
    : WILDCARD
    | WILDCARD WS (EXTENDS | SUPER) WS typeReference (WS? AND WS typeReference)*
    ;

arrayType
    : LSQPAR RSQPAR
    ;

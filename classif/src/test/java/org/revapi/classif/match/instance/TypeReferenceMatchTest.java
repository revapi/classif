/*
 * Copyright 2018-2019 Lukas Krejci
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
package org.revapi.classif.match.instance;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.NameMatch;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@SuppressWarnings("WeakerAccess")
@ExtendWith(CompiledJarExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TypeReferenceMatchTest {

    @JarSources(root = "/sources/typeReference/", sources = "TestClass.java")
    CompiledJar.Environment environment;

    MatchContext<Element> ctx;

    TypeElement TestClass;
    TypeElement Generic;
    TypeElement Wildcard;

    TypeParameterElement GenericT;
    TypeParameterElement GenericE;
    TypeParameterElement GenericF;
    VariableElement genericField;
    ExecutableElement methodT;
    ExecutableElement methodE;
    ExecutableElement methodF;
    ExecutableElement methodG;
    TypeParameterElement GenericG;
    VariableElement genericArgument;

    ExecutableElement methodExtends;
    WildcardType methodExtendsWildcard;
    ExecutableElement methodSuper;
    WildcardType methodSuperWildcard;
    ExecutableElement methodWildcard;
    WildcardType methodWildcardWildcard;
    VariableElement wildcardArgument;
    WildcardType wildcardArgumentWildcard;

    VariableElement primitiveField;
    VariableElement referenceField;
    ExecutableElement voidMethod;
    ExecutableElement primitiveMethod;
    ExecutableElement referenceMethod;
    VariableElement primitiveArgument;
    VariableElement referenceArgument;

    @BeforeAll
    void setup() {
        ctx = new MatchContext<>(new MirroringModelInspector(environment.elements(), environment.types()), emptyMap());
        TestClass = environment.elements().getTypeElement("TestClass");
        Generic = environment.elements().getTypeElement("TestClass.Generic");
        Wildcard = environment.elements().getTypeElement("TestClass.Wildcard");

        GenericT = Generic.getTypeParameters().get(0);
        GenericE = Generic.getTypeParameters().get(1);
        GenericF = Generic.getTypeParameters().get(2);
        genericField = findField(Generic, "genericField");
        methodT = findMethod(Generic, "methodT");
        methodE = findMethod(Generic, "methodE");
        methodF = findMethod(Generic, "methodF");
        methodG = findMethod(Generic, "methodG");
        GenericG = methodG.getTypeParameters().get(0);
        genericArgument = findMethod(Generic, "genericArgument").getParameters().get(0);

        methodExtends = findMethod(Wildcard, "methodExtends");
        methodExtendsWildcard = (WildcardType) ((DeclaredType) methodExtends.getReturnType()).getTypeArguments().get(0);
        methodSuper = findMethod(Wildcard, "methodSuper");
        methodSuperWildcard = (WildcardType) ((DeclaredType) methodSuper.getReturnType()).getTypeArguments().get(0);
        methodWildcard = findMethod(Wildcard, "methodWildcard");
        methodWildcardWildcard = (WildcardType) ((DeclaredType) methodWildcard.getReturnType()).getTypeArguments().get(0);
        wildcardArgument = findMethod(Wildcard, "wildcardArgument").getParameters().get(0);
        wildcardArgumentWildcard = (WildcardType) ((DeclaredType) wildcardArgument.asType()).getTypeArguments().get(0);

        primitiveField = findField(TestClass, "primitiveField");
        referenceField = findField(TestClass, "referenceField");
        voidMethod = findMethod(TestClass, "voidMethod");
        primitiveMethod = findMethod(TestClass, "primitiveMethod");
        referenceMethod = findMethod(TestClass, "referenceMethod");
        primitiveArgument = findMethod(TestClass, "primitiveArgument").getParameters().get(0);
        referenceArgument = findMethod(TestClass, "referenceArgument").getParameters().get(0);
    }

    @Test
    void testVoid() {
        doTest("void",
                "TestClass", TestClass.asType(), NOT_PASSED,
                "Generic", Generic.asType(), NOT_PASSED,
                "Wildcard", Wildcard.asType(), NOT_PASSED,
                "Generic<T>", GenericT.asType(), NOT_PASSED,
                "Generic<E>", GenericE.asType(), NOT_PASSED,
                "Generic<F>", GenericF.asType(), NOT_PASSED,
                "genericField", genericField.asType(), NOT_PASSED,
                "methodT", methodT.getReturnType(), NOT_PASSED,
                "methodE", methodE.getReturnType(), NOT_PASSED,
                "methodF", methodF.getReturnType(), NOT_PASSED,
                "methodG", methodG.getReturnType(), NOT_PASSED,
                "<G> methodG", GenericG.asType(), NOT_PASSED,
                "genericArgument", genericArgument.asType(), NOT_PASSED,
                "methodExtends", methodExtends.getReturnType(), NOT_PASSED,
                "<?> methodExtends", methodExtendsWildcard, NOT_PASSED,
                "methodSuper", methodSuper.getReturnType(), NOT_PASSED,
                "<?> methodSuper", methodSuperWildcard, NOT_PASSED,
                "methodWildcard", methodWildcard.getReturnType(), NOT_PASSED,
                "<?> methodWildcard", methodWildcardWildcard, NOT_PASSED,
                "wildcardArgument", wildcardArgument.asType(), NOT_PASSED,
                "<?> wildcardArgument", wildcardArgumentWildcard, NOT_PASSED,
                "primitiveField", primitiveField.asType(), NOT_PASSED,
                "referenceField", referenceField.asType(), NOT_PASSED,
                "voidMethod", voidMethod.getReturnType(), PASSED,
                "primitiveMethod", primitiveMethod.getReturnType(), NOT_PASSED,
                "referenceMethod", referenceMethod.getReturnType(), NOT_PASSED,
                "primitiveArgument", primitiveArgument.asType(), NOT_PASSED,
                "referenceArgument", referenceArgument.asType(), NOT_PASSED
        );
    }

    @Test
    void testPrimitive() {
        doTest("int",
                "TestClass", TestClass.asType(), NOT_PASSED,
                "Generic", Generic.asType(), NOT_PASSED,
                "Wildcard", Wildcard.asType(), NOT_PASSED,
                "Generic<T>", GenericT.asType(), NOT_PASSED,
                "Generic<E>", GenericE.asType(), NOT_PASSED,
                "Generic<F>", GenericF.asType(), NOT_PASSED,
                "genericField", genericField.asType(), NOT_PASSED,
                "methodT", methodT.getReturnType(), NOT_PASSED,
                "methodE", methodE.getReturnType(), NOT_PASSED,
                "methodF", methodF.getReturnType(), NOT_PASSED,
                "methodG", methodG.getReturnType(), NOT_PASSED,
                "<G> methodG", GenericG.asType(), NOT_PASSED,
                "genericArgument", genericArgument.asType(), NOT_PASSED,
                "methodExtends", methodExtends.getReturnType(), NOT_PASSED,
                "<?> methodExtends", methodExtendsWildcard, NOT_PASSED,
                "methodSuper", methodSuper.getReturnType(), NOT_PASSED,
                "<?> methodSuper", methodSuperWildcard, NOT_PASSED,
                "methodWildcard", methodWildcard.getReturnType(), NOT_PASSED,
                "<?> methodWildcard", methodWildcardWildcard, NOT_PASSED,
                "wildcardArgument", wildcardArgument.asType(), NOT_PASSED,
                "<?> wildcardArgument", wildcardArgumentWildcard, NOT_PASSED,
                "primitiveField", primitiveField.asType(), PASSED,
                "referenceField", referenceField.asType(), NOT_PASSED,
                "voidMethod", voidMethod.getReturnType(), NOT_PASSED,
                "primitiveMethod", primitiveMethod.getReturnType(), PASSED,
                "referenceMethod", referenceMethod.getReturnType(), NOT_PASSED,
                "primitiveArgument", primitiveArgument.asType(), PASSED,
                "referenceArgument", referenceArgument.asType(), NOT_PASSED
        );
    }

    @Test
    void testNegationOfPrimitive() {
        doTest("!int",
                "TestClass", TestClass.asType(), PASSED,
                "primitiveField", primitiveField.asType(), NOT_PASSED);
    }

    @Test
    void testGenericsToObject() {
        doTest("java.*.Object", // use a wildcard match for the fun of it
                "TestClass", TestClass.asType(), NOT_PASSED,
                "Generic", Generic.asType(), NOT_PASSED,
                "Wildcard", Wildcard.asType(), NOT_PASSED,
                "Generic<T>", GenericT.asType(), PASSED,
                "Generic<E>", GenericE.asType(), PASSED,
                "Generic<F>", GenericF.asType(), NOT_PASSED,
                "genericField", genericField.asType(), PASSED,
                "methodT", methodT.getReturnType(), PASSED,
                "methodE", methodE.getReturnType(), PASSED,
                "methodF", methodF.getReturnType(), NOT_PASSED,
                "methodG", methodG.getReturnType(), NOT_PASSED,
                "<G> methodG", GenericG.asType(), NOT_PASSED,
                "genericArgument", genericArgument.asType(), NOT_PASSED,
                "methodExtends", methodExtends.getReturnType(), NOT_PASSED,
                "<?> methodExtends", methodExtendsWildcard, NOT_PASSED,
                "methodSuper", methodSuper.getReturnType(), NOT_PASSED,
                "<?> methodSuper", methodSuperWildcard, NOT_PASSED,
                "methodWildcard", methodWildcard.getReturnType(), NOT_PASSED,
                "<?> methodWildcard", methodWildcardWildcard, PASSED,
                "wildcardArgument", wildcardArgument.asType(), NOT_PASSED,
                "<?> wildcardArgument", wildcardArgumentWildcard, PASSED,
                "primitiveField", primitiveField.asType(), NOT_PASSED,
                "referenceField", referenceField.asType(), NOT_PASSED,
                "voidMethod", voidMethod.getReturnType(), NOT_PASSED,
                "primitiveMethod", primitiveMethod.getReturnType(), NOT_PASSED,
                "referenceMethod", referenceMethod.getReturnType(), NOT_PASSED,
                "primitiveArgument", primitiveArgument.asType(), NOT_PASSED,
                "referenceArgument", referenceArgument.asType(), NOT_PASSED
        );
    }

    @Test
    void testGenericsAndString() {
        doTest("java.lang.String",                
                "TestClass", TestClass.asType(), NOT_PASSED,
                "Generic", Generic.asType(), NOT_PASSED,
                "Wildcard", Wildcard.asType(), NOT_PASSED,
                "Generic<T>", GenericT.asType(), NOT_PASSED,
                "Generic<E>", GenericE.asType(), NOT_PASSED,
                "Generic<F>", GenericF.asType(), PASSED,
                "genericField", genericField.asType(), NOT_PASSED,
                "methodT", methodT.getReturnType(), NOT_PASSED,
                "methodE", methodE.getReturnType(), NOT_PASSED,
                "methodF", methodF.getReturnType(), PASSED,
                "methodG", methodG.getReturnType(), PASSED,
                "<G> methodG", GenericG.asType(), PASSED,
                "genericArgument", genericArgument.asType(), PASSED,
                "methodExtends", methodExtends.getReturnType(), NOT_PASSED,
                "<?> methodExtends", methodExtendsWildcard, NOT_PASSED,
                "methodSuper", methodSuper.getReturnType(), NOT_PASSED,
                "<?> methodSuper", methodSuperWildcard, NOT_PASSED,
                "methodWildcard", methodWildcard.getReturnType(), NOT_PASSED,
                "<?> methodWildcard", methodWildcardWildcard, NOT_PASSED,
                "wildcardArgument", wildcardArgument.asType(), NOT_PASSED,
                "<?> wildcardArgument", wildcardArgumentWildcard, NOT_PASSED,
                "primitiveField", primitiveField.asType(), NOT_PASSED,
                "referenceField", referenceField.asType(), PASSED,
                "voidMethod", voidMethod.getReturnType(), NOT_PASSED,
                "primitiveMethod", primitiveMethod.getReturnType(), NOT_PASSED,
                "referenceMethod", referenceMethod.getReturnType(), PASSED,
                "primitiveArgument", primitiveArgument.asType(), NOT_PASSED,
                "referenceArgument", referenceArgument.asType(), PASSED
        );
    }

    @Test
    void testGenericsList() {
        doTest("java.util.List",
                "TestClass", TestClass.asType(), NOT_PASSED,
                "Generic", Generic.asType(), NOT_PASSED,
                "Wildcard", Wildcard.asType(), NOT_PASSED,
                "Generic<T>", GenericT.asType(), NOT_PASSED,
                "Generic<E>", GenericE.asType(), NOT_PASSED,
                "Generic<F>", GenericF.asType(), NOT_PASSED,
                "genericField", genericField.asType(), NOT_PASSED,
                "methodT", methodT.getReturnType(), NOT_PASSED,
                "methodE", methodE.getReturnType(), NOT_PASSED,
                "methodF", methodF.getReturnType(), NOT_PASSED,
                "methodG", methodG.getReturnType(), NOT_PASSED,
                "<G> methodG", GenericG.asType(), NOT_PASSED,
                "genericArgument", genericArgument.asType(), NOT_PASSED,
                "methodExtends", methodExtends.getReturnType(), NOT_PASSED,
                "<?> methodExtends", methodExtendsWildcard, PASSED,
                "methodSuper", methodSuper.getReturnType(), NOT_PASSED,
                "<?> methodSuper", methodSuperWildcard, NOT_PASSED,
                "methodWildcard", methodWildcard.getReturnType(), NOT_PASSED,
                "<?> methodWildcard", methodWildcardWildcard, NOT_PASSED,
                "wildcardArgument", wildcardArgument.asType(), NOT_PASSED,
                "<?> wildcardArgument", wildcardArgumentWildcard, NOT_PASSED,
                "primitiveField", primitiveField.asType(), NOT_PASSED,
                "referenceField", referenceField.asType(), NOT_PASSED,
                "voidMethod", voidMethod.getReturnType(), NOT_PASSED,
                "primitiveMethod", primitiveMethod.getReturnType(), NOT_PASSED,
                "referenceMethod", referenceMethod.getReturnType(), NOT_PASSED,
                "primitiveArgument", primitiveArgument.asType(), NOT_PASSED,
                "referenceArgument", referenceArgument.asType(), NOT_PASSED
        );
    }

    @Test
    void testGenericsComparable() {
        doTest("java.lang.Comparable",
                "TestClass", TestClass.asType(), NOT_PASSED,
                "Generic", Generic.asType(), NOT_PASSED,
                "Wildcard", Wildcard.asType(), NOT_PASSED,
                "Generic<T>", GenericT.asType(), NOT_PASSED,
                "Generic<E>", GenericE.asType(), NOT_PASSED,
                "Generic<F>", GenericF.asType(), NOT_PASSED,
                "genericField", genericField.asType(), NOT_PASSED,
                "methodT", methodT.getReturnType(), NOT_PASSED,
                "methodE", methodE.getReturnType(), NOT_PASSED,
                "methodF", methodF.getReturnType(), NOT_PASSED,
                "methodG", methodG.getReturnType(), NOT_PASSED,
                "<G> methodG", GenericG.asType(), NOT_PASSED,
                "genericArgument", genericArgument.asType(), NOT_PASSED,
                "methodExtends", methodExtends.getReturnType(), NOT_PASSED,
                "<?> methodExtends", methodExtendsWildcard, NOT_PASSED,
                "methodSuper", methodSuper.getReturnType(), NOT_PASSED,
                "<?> methodSuper", methodSuperWildcard, PASSED,
                "methodWildcard", methodWildcard.getReturnType(), NOT_PASSED,
                "<?> methodWildcard", methodWildcardWildcard, NOT_PASSED,
                "wildcardArgument", wildcardArgument.asType(), NOT_PASSED,
                "<?> wildcardArgument", wildcardArgumentWildcard, NOT_PASSED,
                "primitiveField", primitiveField.asType(), NOT_PASSED,
                "referenceField", referenceField.asType(), NOT_PASSED,
                "voidMethod", voidMethod.getReturnType(), NOT_PASSED,
                "primitiveMethod", primitiveMethod.getReturnType(), NOT_PASSED,
                "referenceMethod", referenceMethod.getReturnType(), NOT_PASSED,
                "primitiveArgument", primitiveArgument.asType(), NOT_PASSED,
                "referenceArgument", referenceArgument.asType(), NOT_PASSED
        );
    }
    //TODO implement

    private void doTest(String recipe, Object... typesAndResults) {
        TypeReferenceMatch match = parse(recipe);
        for (int i = 0; i < typesAndResults.length - 1; i += 3) {
            String description = (String) typesAndResults[i];
            TypeMirror type = (TypeMirror) typesAndResults[i + 1];
            TestResult expectedResult = (TestResult) typesAndResults[i + 2];

            assertEquals(expectedResult, match.testInstance(type, ctx), "Matching recipe '" + recipe + "' to " + type
                    + " should have " + expectedResult + " in test " + description);
        }
    }

    private ExecutableElement findMethod(TypeElement type, String methodName) {
        return ElementFilter.methodsIn(type.getEnclosedElements()).stream()
                .filter(m -> m.getSimpleName().contentEquals(methodName))
                .findFirst()
                .orElse(null);
    }

    private VariableElement findField(TypeElement type, String fieldName) {
        return ElementFilter.fieldsIn(type.getEnclosedElements()).stream()
                .filter(f -> f.getSimpleName().contentEquals(fieldName))
                .findFirst()
                .orElse(null);
    }

    private TypeReferenceMatch parse(String recipe) {
        String[] singles = recipe.split("\\|");

        return new TypeReferenceMatch(Stream.of(singles)
                .map(s -> {
                    boolean negation = s.startsWith("!");
                    if (negation) {
                        s = s.substring(1);
                    }

                    if (s.startsWith("%")) {
                        return new SingleTypeReferenceMatch(null, null, s.substring(1), negation, 0);
                    }

                    int arrayIdx = s.indexOf("[]");
                    int arrayDim = arrayIdx > 0 ? 1 : 0;

                    if (arrayDim > 0) {
                        s = s.substring(0, arrayIdx);
                    }

                    return new SingleTypeReferenceMatch(new FqnMatch(parseFqn(s)), null, s, negation, arrayDim);
                })
                .collect(toList()));
    }

    private List<NameMatch> parseFqn(String fqn) {
        String[] parts = fqn.split("\\.");

        return Stream.of(parts)
                .map(p -> {
                    switch (p) {
                        case "*":
                            return NameMatch.any();
                        case "**":
                            return NameMatch.all();
                        default:
                            if (p.startsWith("/")) {
                                return NameMatch.pattern(Pattern.compile(p.substring(1, p.length() - 1)));
                            } else {
                                return NameMatch.exact(p);
                            }
                    }
                }).collect(toList());
    }
}

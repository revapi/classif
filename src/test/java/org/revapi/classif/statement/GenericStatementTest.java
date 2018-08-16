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
package org.revapi.classif.statement;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.testjars.junit5.CompiledJarExtension;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class GenericStatementTest {

// commented out for now, because MatchingProgress doesn't support generic statements ATM

//    @JarSources(root = "/sources/annotations/", sources = "TestClass.java")
//    private CompiledJar.Environment environment;
//
//    private TypeElement TestClass;
//
//    private TypeElement Empty;
//    private TypeElement SingleParam_boolean;
//    private TypeElement SingleParam_byte;
//    private TypeElement SingleParam_short;
//    private TypeElement SingleParam_int;
//    private TypeElement SingleParam_long;
//    private TypeElement SingleParam_float;
//    private TypeElement SingleParam_double;
//    private TypeElement SingleParam_char;
//    private TypeElement SingleParam_string;
//    private TypeElement SingleParam_annotation;
//    private TypeElement SingleParam_class;
//    private TypeElement TwoParams_boolean;
//    private TypeElement TwoParams_byte;
//    private TypeElement TwoParams_short;
//    private TypeElement TwoParams_int;
//    private TypeElement TwoParams_long;
//    private TypeElement TwoParams_float;
//    private TypeElement TwoParams_double;
//    private TypeElement TwoParams_char;
//    private TypeElement TwoParams_string;
//    private TypeElement TwoParams_annotation;
//    private TypeElement TwoParams_class;
//    private TypeElement SingleParam_booleanArray1;
//    private TypeElement SingleParam_byteArray1;
//    private TypeElement SingleParam_shortArray1;
//    private TypeElement SingleParam_intArray1;
//    private TypeElement SingleParam_longArray1;
//    private TypeElement SingleParam_floatArray1;
//    private TypeElement SingleParam_doubleArray1;
//    private TypeElement SingleParam_charArray1;
//    private TypeElement SingleParam_stringArray1;
//    private TypeElement SingleParam_annotationArray1;
//    private TypeElement SingleParam_classArray1;
//    private TypeElement SingleParam_booleanArray2;
//    private TypeElement SingleParam_byteArray2;
//    private TypeElement SingleParam_shortArray2;
//    private TypeElement SingleParam_intArray2;
//    private TypeElement SingleParam_longArray2;
//    private TypeElement SingleParam_floatArray2;
//    private TypeElement SingleParam_doubleArray2;
//    private TypeElement SingleParam_charArray2;
//    private TypeElement SingleParam_stringArray2;
//    private TypeElement SingleParam_annotationArray2;
//    private TypeElement SingleParam_classArray2;
//
//    @BeforeAll
//    void setup() {
//        TestClass = environment.elements().getTypeElement("TestClass");
//
//        Empty = environment.elements().getTypeElement("TestClass.Empty");
//        SingleParam_boolean = environment.elements().getTypeElement("TestClass.SingleParam_boolean");
//        SingleParam_byte = environment.elements().getTypeElement("TestClass.SingleParam_byte");
//        SingleParam_short = environment.elements().getTypeElement("TestClass.SingleParam_short");
//        SingleParam_int = environment.elements().getTypeElement("TestClass.SingleParam_int");
//        SingleParam_long = environment.elements().getTypeElement("TestClass.SingleParam_long");
//        SingleParam_float = environment.elements().getTypeElement("TestClass.SingleParam_float");
//        SingleParam_double = environment.elements().getTypeElement("TestClass.SingleParam_double");
//        SingleParam_char = environment.elements().getTypeElement("TestClass.SingleParam_char");
//        SingleParam_string = environment.elements().getTypeElement("TestClass.SingleParam_string");
//        SingleParam_annotation = environment.elements().getTypeElement("TestClass.SingleParam_annotation");
//        SingleParam_class = environment.elements().getTypeElement("TestClass.SingleParam_class");
//        TwoParams_boolean = environment.elements().getTypeElement("TestClass.TwoParams_boolean");
//        TwoParams_byte = environment.elements().getTypeElement("TestClass.TwoParams_byte");
//        TwoParams_short = environment.elements().getTypeElement("TestClass.TwoParams_short");
//        TwoParams_int = environment.elements().getTypeElement("TestClass.TwoParams_int");
//        TwoParams_long = environment.elements().getTypeElement("TestClass.TwoParams_long");
//        TwoParams_float = environment.elements().getTypeElement("TestClass.TwoParams_float");
//        TwoParams_double = environment.elements().getTypeElement("TestClass.TwoParams_double");
//        TwoParams_char = environment.elements().getTypeElement("TestClass.TwoParams_char");
//        TwoParams_string = environment.elements().getTypeElement("TestClass.TwoParams_string");
//        TwoParams_annotation = environment.elements().getTypeElement("TestClass.TwoParams_annotation");
//        TwoParams_class = environment.elements().getTypeElement("TestClass.TwoParams_class");
//        SingleParam_booleanArray1 = environment.elements().getTypeElement("TestClass.SingleParam_booleanArray1");
//        SingleParam_byteArray1 = environment.elements().getTypeElement("TestClass.SingleParam_byteArray1");
//        SingleParam_shortArray1 = environment.elements().getTypeElement("TestClass.SingleParam_shortArray1");
//        SingleParam_intArray1 = environment.elements().getTypeElement("TestClass.SingleParam_intArray1");
//        SingleParam_longArray1 = environment.elements().getTypeElement("TestClass.SingleParam_longArray1");
//        SingleParam_floatArray1 = environment.elements().getTypeElement("TestClass.SingleParam_floatArray1");
//        SingleParam_doubleArray1 = environment.elements().getTypeElement("TestClass.SingleParam_doubleArray1");
//        SingleParam_charArray1 = environment.elements().getTypeElement("TestClass.SingleParam_charArray1");
//        SingleParam_stringArray1 = environment.elements().getTypeElement("TestClass.SingleParam_stringArray1");
//        SingleParam_annotationArray1 = environment.elements().getTypeElement("TestClass.SingleParam_annotationArray1");
//        SingleParam_classArray1 = environment.elements().getTypeElement("TestClass.SingleParam_classArray1");
//        SingleParam_booleanArray2 = environment.elements().getTypeElement("TestClass.SingleParam_booleanArray2");
//        SingleParam_byteArray2 = environment.elements().getTypeElement("TestClass.SingleParam_byteArray2");
//        SingleParam_shortArray2 = environment.elements().getTypeElement("TestClass.SingleParam_shortArray2");
//        SingleParam_intArray2 = environment.elements().getTypeElement("TestClass.SingleParam_intArray2");
//        SingleParam_longArray2 = environment.elements().getTypeElement("TestClass.SingleParam_longArray2");
//        SingleParam_floatArray2 = environment.elements().getTypeElement("TestClass.SingleParam_floatArray2");
//        SingleParam_doubleArray2 = environment.elements().getTypeElement("TestClass.SingleParam_doubleArray2");
//        SingleParam_charArray2 = environment.elements().getTypeElement("TestClass.SingleParam_charArray2");
//        SingleParam_stringArray2 = environment.elements().getTypeElement("TestClass.SingleParam_stringArray2");
//        SingleParam_annotationArray2 = environment.elements().getTypeElement("TestClass.SingleParam_annotationArray2");
//        SingleParam_classArray2 = environment.elements().getTypeElement("TestClass.SingleParam_classArray2");
//    }
//
//    @Test
//    void testModifiers() {
//        assertPassed(test(environment, TestClass, "^;"));
//        assertPassed(test(environment, TestClass, "public ^;"));
//        assertPassed(test(environment, TestClass, "public ^*;"));
//        assertPassed(test(environment, TestClass, "private ^!*;"));
//        assertPassed(test(environment, TestClass, "packageprivate ^!*;"));
//    }
//
//    @Test
//    void testAnnotations_concreteValues() {
//        assertPassed(test(environment, Empty, "@TestClass.My ^;"));
//
//        assertPassed(test(environment, SingleParam_boolean, "@TestClass.My(booleanValue = true) ^;"));
//        assertPassed(test(environment, SingleParam_boolean, "@TestClass.My(booleanValue != false) ^;"));
//
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue = 1) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue != 0) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue >= 0) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue > 0) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue <= 2) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue < 2) ^;"));
//
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue = 1) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue != 0) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue >= 0) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue > 0) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue <= 2) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue < 2) ^;"));
//
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue = 1) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue != 0) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue >= 0) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue > 0) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue <= 2) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue < 2) ^;"));
//
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue = 1) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue != 0) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue >= 0) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue > 0) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue <= 2) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue < 2) ^;"));
//
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue = 1) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue != 0) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue >= 0) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue > 0) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue <= 2) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue < 2) ^;"));
//
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue = 1) ^;"));
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue != 0) ^;"));
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue >= 0) ^;"));
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue > 0) ^;"));
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue <= 2) ^;"));
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue < 2) ^;"));
//
//        assertPassed(test(environment, SingleParam_char, "@TestClass.My(charValue = 'a') ^;"));
//        assertPassed(test(environment, SingleParam_char, "@TestClass.My(charValue != 'b') ^;"));
//
//        assertPassed(test(environment, SingleParam_string, "@TestClass.My(stringValue = 'ab') ^;"));
//        assertPassed(test(environment, SingleParam_string, "@TestClass.My(stringValue != 'abc') ^;"));
//
//        assertPassed(test(environment, SingleParam_annotation, "@TestClass.My(annotationValue = @java.lang.annotation.Target(**)) ^;"));
//        assertPassed(test(environment, SingleParam_annotation, "@TestClass.My(annotationValue != @java.lang.annotation.Target) ^;"));
//
//        assertPassed(test(environment, SingleParam_class, "@TestClass.My(classValue = java.lang.String.class) ^;"));
//        assertPassed(test(environment, SingleParam_class, "@TestClass.My(classValue != whatever.class) ^;"));
//
//        assertPassed(test(environment, SingleParam_class, "@TestClass.My(* = java.lang.String.class) ^;"));
//        assertPassed(test(environment, SingleParam_class, "@TestClass.My(* = *) ^;"));
//    }
//
//    @Test
//    void testAnnotations_defaultValues() {
//        assertPassed(test(environment, SingleParam_boolean, "@TestClass.My(booleanValue = true, byteValue = 0) ^;"));
//        assertPassed(test(environment, SingleParam_boolean, "@TestClass.My(booleanValue != false, byteValue != 1) ^;"));
//
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue = 1, shortValue = 0) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue != 0, shortValue != 1) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue >= 0, shortValue >= -1) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue > 0, shortValue > -1) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue <= 2, shortValue <= 1) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue < 2, shortValue < 1) ^;"));
//
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue = 1, intValue = 0) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue != 0, intValue != 1) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue >= 0, intValue >= 0) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue > 0, intValue > -1) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue <= 2, intValue <= 1) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue < 2, intValue < 1) ^;"));
//
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue = 1, longValue = 0) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue != 0, longValue != 1) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue >= 0, longValue >= -1) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue > 0, longValue > 1) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue <= 2, longValue <= 1) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue < 2, longValue < 1) ^;"));
//
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue = 1, floatValue = 0) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue != 0, floatValue != 1) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue >= 0, floatValue >= -1) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue > 0, floatValue > -1) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue <= 2, floatValue <= 0) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue < 2, floatValue < 1) ^;"));
//
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue = 1, doubleValue = 0) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue != 0, doubleValue != 1) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue >= 0, doubleValue >= 0) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue > 0, doubleValue > -1) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue <= 2, doubleValue <= 1) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue < 2, doubleValue < 1) ^;"));
//
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue = 1, charValue = ' ') ^;"));
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue != 0, charValue != 'a' ) ^;"));
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue >= 0, charValue = / /) ^;"));
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue > 0, charValue != /a/) ^;"));
//
//        assertPassed(test(environment, SingleParam_char, "@TestClass.My(charValue = 'a', stringValue = '') ^;"));
//        assertPassed(test(environment, SingleParam_char, "@TestClass.My(charValue != 'b', stringValue != ' ') ^;"));
//
//        assertPassed(test(environment, SingleParam_string, "@TestClass.My(stringValue = 'ab', annotationValue = @java.lang.annotation.Target(*)) ^;"));
//        assertPassed(test(environment, SingleParam_string, "@TestClass.My(stringValue != 'abc', annotationValue != @whatever) ^;"));
//
//        assertPassed(test(environment, SingleParam_annotation, "@TestClass.My(annotationValue = @java.lang.annotation.Target(**), classValue = java.lang.Object.class) ^;"));
//        assertPassed(test(environment, SingleParam_annotation, "@TestClass.My(annotationValue != @java.lang.annotation.Target, classValue != java.lang.String.class) ^;"));
//
//        assertPassed(test(environment, SingleParam_class, "@TestClass.My(classValue = java.lang.String.class, booleanValue = false) ^;"));
//        assertPassed(test(environment, SingleParam_class, "@TestClass.My(classValue != whatever.class, booleanValue != true) ^;"));
//    }
//
//    @Test
//    void testAnnotations_2concreteValues() {
//        assertPassed(test(environment, TwoParams_boolean, "@TestClass.My(booleanValue = true, byteValue = 2) ^;"));
//        assertPassed(test(environment, TwoParams_boolean, "@TestClass.My(booleanValue != false, byteValue != 1) ^;"));
//
//        assertPassed(test(environment, TwoParams_byte, "@TestClass.My(byteValue = 1, shortValue = 2) ^;"));
//        assertPassed(test(environment, TwoParams_byte, "@TestClass.My(byteValue != 0, shortValue != 1) ^;"));
//        assertPassed(test(environment, TwoParams_byte, "@TestClass.My(byteValue >= 0, shortValue >= 1) ^;"));
//        assertPassed(test(environment, TwoParams_byte, "@TestClass.My(byteValue > 0, shortValue > 1) ^;"));
//        assertPassed(test(environment, TwoParams_byte, "@TestClass.My(byteValue <= 2, shortValue <= 3) ^;"));
//        assertPassed(test(environment, TwoParams_byte, "@TestClass.My(byteValue < 2, shortValue < 3) ^;"));
//
//        assertPassed(test(environment, TwoParams_short, "@TestClass.My(shortValue = 1, intValue = 2) ^;"));
//        assertPassed(test(environment, TwoParams_short, "@TestClass.My(shortValue != 0, intValue != 1) ^;"));
//        assertPassed(test(environment, TwoParams_short, "@TestClass.My(shortValue >= 0, intValue >= 2) ^;"));
//        assertPassed(test(environment, TwoParams_short, "@TestClass.My(shortValue > 0, intValue > 1) ^;"));
//        assertPassed(test(environment, TwoParams_short, "@TestClass.My(shortValue <= 2, intValue <= 3) ^;"));
//        assertPassed(test(environment, TwoParams_short, "@TestClass.My(shortValue < 2, intValue < 3) ^;"));
//
//        assertPassed(test(environment, TwoParams_int, "@TestClass.My(intValue = 1, longValue = 2) ^;"));
//        assertPassed(test(environment, TwoParams_int, "@TestClass.My(intValue != 0, longValue != 1) ^;"));
//        assertPassed(test(environment, TwoParams_int, "@TestClass.My(intValue >= 0, longValue >= 2) ^;"));
//        assertPassed(test(environment, TwoParams_int, "@TestClass.My(intValue > 0, longValue > 1) ^;"));
//        assertPassed(test(environment, TwoParams_int, "@TestClass.My(intValue <= 2, longValue <= 3) ^;"));
//        assertPassed(test(environment, TwoParams_int, "@TestClass.My(intValue < 2, longValue < 3) ^;"));
//
//        assertPassed(test(environment, TwoParams_long, "@TestClass.My(longValue = 1, floatValue = 2) ^;"));
//        assertPassed(test(environment, TwoParams_long, "@TestClass.My(longValue != 0, floatValue != 2.1) ^;"));
//        assertPassed(test(environment, TwoParams_long, "@TestClass.My(longValue >= 0, floatValue >= 1.9) ^;"));
//        assertPassed(test(environment, TwoParams_long, "@TestClass.My(longValue > 0, floatValue > 1.9) ^;"));
//        assertPassed(test(environment, TwoParams_long, "@TestClass.My(longValue <= 2, floatValue <= 2.1) ^;"));
//        assertPassed(test(environment, TwoParams_long, "@TestClass.My(longValue < 2, floatValue < 2.1) ^;"));
//
//        assertPassed(test(environment, TwoParams_float, "@TestClass.My(floatValue = 1, doubleValue = 2) ^;"));
//        assertPassed(test(environment, TwoParams_float, "@TestClass.My(floatValue != 0, doubleValue != 2.1) ^;"));
//        assertPassed(test(environment, TwoParams_float, "@TestClass.My(floatValue >= 0, doubleValue >= 1.9) ^;"));
//        assertPassed(test(environment, TwoParams_float, "@TestClass.My(floatValue > 0, doubleValue > 1.9) ^;"));
//        assertPassed(test(environment, TwoParams_float, "@TestClass.My(floatValue <= 2, doubleValue <= 2.1) ^;"));
//        assertPassed(test(environment, TwoParams_float, "@TestClass.My(floatValue < 2, doubleValue < 2.1) ^;"));
//
//        assertPassed(test(environment, TwoParams_double, "@TestClass.My(doubleValue = 1, charValue = 'b') ^;"));
//        assertPassed(test(environment, TwoParams_double, "@TestClass.My(doubleValue != 0, charValue != 'a') ^;"));
//        assertPassed(test(environment, TwoParams_double, "@TestClass.My(doubleValue >= 0, charValue = 'b') ^;"));
//        assertPassed(test(environment, TwoParams_double, "@TestClass.My(doubleValue > 0, charValue = 'b') ^;"));
//        assertPassed(test(environment, TwoParams_double, "@TestClass.My(doubleValue <= 2, charValue = *) ^;"));
//        assertPassed(test(environment, TwoParams_double, "@TestClass.My(doubleValue < 2, charValue = *) ^;"));
//
//        assertPassed(test(environment, TwoParams_char, "@TestClass.My(charValue = 'a', stringValue = 'abc') ^;"));
//        assertPassed(test(environment, TwoParams_char, "@TestClass.My(charValue != 'b', stringValue != 'ab') ^;"));
//
//        assertPassed(test(environment, TwoParams_string, "@TestClass.My(stringValue = 'ab', annotationValue = @java.lang.annotation.Target(**)) ^;"));
//        assertPassed(test(environment, TwoParams_string, "@TestClass.My(stringValue != 'abc', annotationValue != java.lang.annotation.Retention) ^;"));
//
//        assertPassed(test(environment, TwoParams_annotation, "@TestClass.My(annotationValue = @java.lang.annotation.Target(**), classValue = java.lang.Object.class) ^;"));
//        assertPassed(test(environment, TwoParams_annotation, "@TestClass.My(annotationValue != @java.lang.annotation.Target, classValue != java.lang.String.class) ^;"));
//
//        assertPassed(test(environment, TwoParams_class, "@TestClass.My(classValue = java.lang.String.class, booleanValue = false) ^;"));
//        assertPassed(test(environment, TwoParams_class, "@TestClass.My(classValue != whatever.class, booleanValue != true) ^;"));
//
//        assertPassed(test(environment, TwoParams_class, "@TestClass.My(* = java.lang.String.class, * = *) ^;"));
//    }
//
//    @Test
//    void testAnnotations_regex() {
//        assertPassed(test(environment, SingleParam_boolean, "@TestClass.My(booleanValue = /t[rR]ue/) ^;"));
//        assertPassed(test(environment, SingleParam_boolean, "@TestClass.My(booleanValue != /false/) ^;"));
//
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue = /1/) ^;"));
//        assertPassed(test(environment, SingleParam_byte, "@TestClass.My(byteValue != /2/) ^;"));
//
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue = /1/) ^;"));
//        assertPassed(test(environment, SingleParam_short, "@TestClass.My(shortValue != /0/) ^;"));
//
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue = /1/) ^;"));
//        assertPassed(test(environment, SingleParam_int, "@TestClass.My(intValue != /0/) ^;"));
//
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue = /1.*/) ^;"));
//        assertPassed(test(environment, SingleParam_long, "@TestClass.My(longValue != /^$/) ^;"));
//
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue = /1\\.0/) ^;"));
//        assertPassed(test(environment, SingleParam_float, "@TestClass.My(floatValue != /0/) ^;"));
//
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue = /1\\.0/) ^;"));
//        assertPassed(test(environment, SingleParam_double, "@TestClass.My(doubleValue != /0/) ^;"));
//
//        assertPassed(test(environment, SingleParam_char, "@TestClass.My(charValue = /a/) ^;"));
//        assertPassed(test(environment, SingleParam_char, "@TestClass.My(charValue != /[A-Z]/) ^;"));
//
//        assertPassed(test(environment, SingleParam_string, "@TestClass.My(stringValue = /a[bc]/) ^;"));
//        assertPassed(test(environment, SingleParam_string, "@TestClass.My(stringValue != /abc/) ^;"));
//
//        assertPassed(test(environment, SingleParam_annotation, "@TestClass.My(annotationValue = @**.Target(**)) ^;"));
//        assertPassed(test(environment, SingleParam_annotation, "@TestClass.My(annotationValue != @**.Retention) ^;"));
//
//        assertPassed(test(environment, SingleParam_class, "@TestClass.My(classValue = **.String.class) ^;"));
//        assertPassed(test(environment, SingleParam_class, "@TestClass.My(classValue != whatever.class) ^;"));
//
//        assertPassed(test(environment, SingleParam_class, "@TestClass.My(* = /.*/) ^;"));
//    }
//
//    @Test
//    void testAnnotations_arrays() {
//        assertPassed(test(environment, SingleParam_booleanArray1, "@TestClass.My(booleanArray = {true}) ^;"));
//        assertPassed(test(environment, SingleParam_booleanArray1, "@TestClass.My(booleanArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_booleanArray1, "@TestClass.My(booleanArray != {false}) ^;"));
//        assertPassed(test(environment, SingleParam_booleanArray1, "@TestClass.My(booleanArray != {true, true}) ^;"));
//
//        assertPassed(test(environment, SingleParam_byteArray1, "@TestClass.My(byteArray = {1}) ^;"));
//        assertPassed(test(environment, SingleParam_byteArray1, "@TestClass.My(byteArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_byteArray1, "@TestClass.My(byteArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_byteArray1, "@TestClass.My(byteArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_shortArray1, "@TestClass.My(shortArray = {1}) ^;"));
//        assertPassed(test(environment, SingleParam_shortArray1, "@TestClass.My(shortArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_shortArray1, "@TestClass.My(shortArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_shortArray1, "@TestClass.My(shortArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_intArray1, "@TestClass.My(intArray = {1}) ^;"));
//        assertPassed(test(environment, SingleParam_intArray1, "@TestClass.My(intArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_intArray1, "@TestClass.My(intArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_intArray1, "@TestClass.My(intArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_longArray1, "@TestClass.My(longArray = {1}) ^;"));
//        assertPassed(test(environment, SingleParam_longArray1, "@TestClass.My(longArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_longArray1, "@TestClass.My(longArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_longArray1, "@TestClass.My(longArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_floatArray1, "@TestClass.My(floatArray = {1}) ^;"));
//        assertPassed(test(environment, SingleParam_floatArray1, "@TestClass.My(floatArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_floatArray1, "@TestClass.My(floatArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_floatArray1, "@TestClass.My(floatArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_doubleArray1, "@TestClass.My(doubleArray = {1}) ^;"));
//        assertPassed(test(environment, SingleParam_doubleArray1, "@TestClass.My(doubleArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_doubleArray1, "@TestClass.My(doubleArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_doubleArray1, "@TestClass.My(doubleArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_charArray1, "@TestClass.My(charArray = {'a'}) ^;"));
//        assertPassed(test(environment, SingleParam_charArray1, "@TestClass.My(charArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_charArray1, "@TestClass.My(charArray != {'b'}) ^;"));
//        assertPassed(test(environment, SingleParam_charArray1, "@TestClass.My(charArray != {'a', 'a'}) ^;"));
//
//        assertPassed(test(environment, SingleParam_stringArray1, "@TestClass.My(stringArray = {'ab'}) ^;"));
//        assertPassed(test(environment, SingleParam_stringArray1, "@TestClass.My(stringArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_stringArray1, "@TestClass.My(stringArray != {'b'}) ^;"));
//        assertPassed(test(environment, SingleParam_stringArray1, "@TestClass.My(stringArray != {'ab', 'ab'}) ^;"));
//
//        assertPassed(test(environment, SingleParam_annotationArray1, "@TestClass.My(annotationArray = {@java.lang.annotation.Target(*)}) ^;"));
//        assertPassed(test(environment, SingleParam_annotationArray1, "@TestClass.My(annotationArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_annotationArray1, "@TestClass.My(annotationArray != {@whatever}) ^;"));
//        assertPassed(test(environment, SingleParam_annotationArray1, "@TestClass.My(annotationArray != {@java.lang.annotation.Target(*), @java.lang.annotation.Target(*)}) ^;"));
//
//        assertPassed(test(environment, SingleParam_classArray1, "@TestClass.My(classArray = {java.lang.String.class}) ^;"));
//        assertPassed(test(environment, SingleParam_classArray1, "@TestClass.My(classArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_classArray1, "@TestClass.My(classArray != {java.lang.Object.class}) ^;"));
//        assertPassed(test(environment, SingleParam_classArray1, "@TestClass.My(classArray != {java.lang.String.class, java.lang.String.class}) ^;"));
//    }
//
//    @Test
//    void testAnnotations_arraysWithMultipleElements() {
//        assertPassed(test(environment, SingleParam_booleanArray2, "@TestClass.My(booleanArray = {true, false}) ^;"));
//        assertPassed(test(environment, SingleParam_booleanArray2, "@TestClass.My(booleanArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_booleanArray2, "@TestClass.My(booleanArray != {false}) ^;"));
//        assertPassed(test(environment, SingleParam_booleanArray2, "@TestClass.My(booleanArray != {true, true}) ^;"));
//
//        assertPassed(test(environment, SingleParam_byteArray2, "@TestClass.My(byteArray = {1, 2}) ^;"));
//        assertPassed(test(environment, SingleParam_byteArray2, "@TestClass.My(byteArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_byteArray2, "@TestClass.My(byteArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_byteArray2, "@TestClass.My(byteArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_shortArray2, "@TestClass.My(shortArray = {1, 2}) ^;"));
//        assertPassed(test(environment, SingleParam_shortArray2, "@TestClass.My(shortArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_shortArray2, "@TestClass.My(shortArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_shortArray2, "@TestClass.My(shortArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_intArray2, "@TestClass.My(intArray = {1, 2}) ^;"));
//        assertPassed(test(environment, SingleParam_intArray2, "@TestClass.My(intArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_intArray2, "@TestClass.My(intArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_intArray2, "@TestClass.My(intArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_longArray2, "@TestClass.My(longArray = {1, 2}) ^;"));
//        assertPassed(test(environment, SingleParam_longArray2, "@TestClass.My(longArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_longArray2, "@TestClass.My(longArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_longArray2, "@TestClass.My(longArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_floatArray2, "@TestClass.My(floatArray = {1, 2}) ^;"));
//        assertPassed(test(environment, SingleParam_floatArray2, "@TestClass.My(floatArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_floatArray2, "@TestClass.My(floatArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_floatArray2, "@TestClass.My(floatArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_doubleArray2, "@TestClass.My(doubleArray = {1, 2}) ^;"));
//        assertPassed(test(environment, SingleParam_doubleArray2, "@TestClass.My(doubleArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_doubleArray2, "@TestClass.My(doubleArray != {0}) ^;"));
//        assertPassed(test(environment, SingleParam_doubleArray2, "@TestClass.My(doubleArray != {1, 1}) ^;"));
//
//        assertPassed(test(environment, SingleParam_charArray2, "@TestClass.My(charArray = {'a', 'b'}) ^;"));
//        assertPassed(test(environment, SingleParam_charArray2, "@TestClass.My(charArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_charArray2, "@TestClass.My(charArray != {'b'}) ^;"));
//        assertPassed(test(environment, SingleParam_charArray2, "@TestClass.My(charArray != {'a', 'a'}) ^;"));
//
//        assertPassed(test(environment, SingleParam_stringArray2, "@TestClass.My(stringArray = {'ab', 'abc'}) ^;"));
//        assertPassed(test(environment, SingleParam_stringArray2, "@TestClass.My(stringArray = {'ab', 'abc', **}) ^;"));
//        assertPassed(test(environment, SingleParam_stringArray2, "@TestClass.My(stringArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_stringArray2, "@TestClass.My(stringArray != {'b'}) ^;"));
//        assertPassed(test(environment, SingleParam_stringArray2, "@TestClass.My(stringArray != {'ab', 'ab'}) ^;"));
//
//        assertPassed(test(environment, SingleParam_annotationArray2, "@TestClass.My(annotationArray = {@java.lang.annotation.Target(*),*}) ^;"));
//        assertPassed(test(environment, SingleParam_annotationArray2, "@TestClass.My(annotationArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_annotationArray2, "@TestClass.My(annotationArray != {@whatever}) ^;"));
//        assertPassed(test(environment, SingleParam_annotationArray2, "@TestClass.My(annotationArray != {*, *, *}) ^;"));
//
//        assertPassed(test(environment, SingleParam_classArray2, "@TestClass.My(classArray = {java.lang.String.class, java.lang.Object.class}) ^;"));
//        assertPassed(test(environment, SingleParam_classArray2, "@TestClass.My(classArray != {}) ^;"));
//        assertPassed(test(environment, SingleParam_classArray2, "@TestClass.My(classArray != {java.lang.Object.class}) ^;"));
//        assertPassed(test(environment, SingleParam_classArray2, "@TestClass.My(classArray != {java.lang.String.class, java.lang.String.class}) ^;"));
//    }
}

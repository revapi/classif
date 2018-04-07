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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.Classif;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.ModelInspector;
import org.revapi.classif.StructuralMatcher;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class GenericStatementTest {
    @JarSources(root = "/sources/annotations/", sources = "TestClass.java")
    private CompiledJar.Environment environment;

    private TypeElement TestClass;

    private TypeElement Empty;
    private TypeElement SingleParam_boolean;
    private TypeElement SingleParam_byte;
    private TypeElement SingleParam_short;
    private TypeElement SingleParam_int;
    private TypeElement SingleParam_long;
    private TypeElement SingleParam_float;
    private TypeElement SingleParam_double;
    private TypeElement SingleParam_char;
    private TypeElement SingleParam_string;
    private TypeElement SingleParam_annotation;
    private TypeElement SingleParam_class;
    private TypeElement TwoParams_boolean;
    private TypeElement TwoParams_byte;
    private TypeElement TwoParams_short;
    private TypeElement TwoParams_int;
    private TypeElement TwoParams_long;
    private TypeElement TwoParams_float;
    private TypeElement TwoParams_double;
    private TypeElement TwoParams_char;
    private TypeElement TwoParams_string;
    private TypeElement TwoParams_annotation;
    private TypeElement TwoParams_class;
    private TypeElement SingleParam_booleanArray1;
    private TypeElement SingleParam_byteArray1;
    private TypeElement SingleParam_shortArray1;
    private TypeElement SingleParam_intArray1;
    private TypeElement SingleParam_longArray1;
    private TypeElement SingleParam_floatArray1;
    private TypeElement SingleParam_doubleArray1;
    private TypeElement SingleParam_charArray1;
    private TypeElement SingleParam_stringArray1;
    private TypeElement SingleParam_annotationArray1;
    private TypeElement SingleParam_classArray1;
    private TypeElement SingleParam_booleanArray2;
    private TypeElement SingleParam_byteArray2;
    private TypeElement SingleParam_shortArray2;
    private TypeElement SingleParam_intArray2;
    private TypeElement SingleParam_longArray2;
    private TypeElement SingleParam_floatArray2;
    private TypeElement SingleParam_doubleArray2;
    private TypeElement SingleParam_charArray2;
    private TypeElement SingleParam_stringArray2;
    private TypeElement SingleParam_annotationArray2;
    private TypeElement SingleParam_classArray2;
    
    @BeforeAll
    void setup() {
        TestClass = environment.elements().getTypeElement("TestClass");
        
        Empty = environment.elements().getTypeElement("TestClass.Empty");
        SingleParam_boolean = environment.elements().getTypeElement("TestClass.SingleParam_boolean");
        SingleParam_byte = environment.elements().getTypeElement("TestClass.SingleParam_byte");
        SingleParam_short = environment.elements().getTypeElement("TestClass.SingleParam_short");
        SingleParam_int = environment.elements().getTypeElement("TestClass.SingleParam_int");
        SingleParam_long = environment.elements().getTypeElement("TestClass.SingleParam_long");
        SingleParam_float = environment.elements().getTypeElement("TestClass.SingleParam_float");
        SingleParam_double = environment.elements().getTypeElement("TestClass.SingleParam_double");
        SingleParam_char = environment.elements().getTypeElement("TestClass.SingleParam_char");
        SingleParam_string = environment.elements().getTypeElement("TestClass.SingleParam_string");
        SingleParam_annotation = environment.elements().getTypeElement("TestClass.SingleParam_annotation");
        SingleParam_class = environment.elements().getTypeElement("TestClass.SingleParam_class");
        TwoParams_boolean = environment.elements().getTypeElement("TestClass.TwoParams_boolean");
        TwoParams_byte = environment.elements().getTypeElement("TestClass.TwoParams_byte");
        TwoParams_short = environment.elements().getTypeElement("TestClass.TwoParams_short");
        TwoParams_int = environment.elements().getTypeElement("TestClass.TwoParams_int");
        TwoParams_long = environment.elements().getTypeElement("TestClass.TwoParams_long");
        TwoParams_float = environment.elements().getTypeElement("TestClass.TwoParams_float");
        TwoParams_double = environment.elements().getTypeElement("TestClass.TwoParams_double");
        TwoParams_char = environment.elements().getTypeElement("TestClass.TwoParams_char");
        TwoParams_string = environment.elements().getTypeElement("TestClass.TwoParams_string");
        TwoParams_annotation = environment.elements().getTypeElement("TestClass.TwoParams_annotation");
        TwoParams_class = environment.elements().getTypeElement("TestClass.TwoParams_class");
        SingleParam_booleanArray1 = environment.elements().getTypeElement("TestClass.SingleParam_booleanArray1");
        SingleParam_byteArray1 = environment.elements().getTypeElement("TestClass.SingleParam_byteArray1");
        SingleParam_shortArray1 = environment.elements().getTypeElement("TestClass.SingleParam_shortArray1");
        SingleParam_intArray1 = environment.elements().getTypeElement("TestClass.SingleParam_intArray1");
        SingleParam_longArray1 = environment.elements().getTypeElement("TestClass.SingleParam_longArray1");
        SingleParam_floatArray1 = environment.elements().getTypeElement("TestClass.SingleParam_floatArray1");
        SingleParam_doubleArray1 = environment.elements().getTypeElement("TestClass.SingleParam_doubleArray1");
        SingleParam_charArray1 = environment.elements().getTypeElement("TestClass.SingleParam_charArray1");
        SingleParam_stringArray1 = environment.elements().getTypeElement("TestClass.SingleParam_stringArray1");
        SingleParam_annotationArray1 = environment.elements().getTypeElement("TestClass.SingleParam_annotationArray1");
        SingleParam_classArray1 = environment.elements().getTypeElement("TestClass.SingleParam_classArray1");
        SingleParam_booleanArray2 = environment.elements().getTypeElement("TestClass.SingleParam_booleanArray2");
        SingleParam_byteArray2 = environment.elements().getTypeElement("TestClass.SingleParam_byteArray2");
        SingleParam_shortArray2 = environment.elements().getTypeElement("TestClass.SingleParam_shortArray2");
        SingleParam_intArray2 = environment.elements().getTypeElement("TestClass.SingleParam_intArray2");
        SingleParam_longArray2 = environment.elements().getTypeElement("TestClass.SingleParam_longArray2");
        SingleParam_floatArray2 = environment.elements().getTypeElement("TestClass.SingleParam_floatArray2");
        SingleParam_doubleArray2 = environment.elements().getTypeElement("TestClass.SingleParam_doubleArray2");
        SingleParam_charArray2 = environment.elements().getTypeElement("TestClass.SingleParam_charArray2");
        SingleParam_stringArray2 = environment.elements().getTypeElement("TestClass.SingleParam_stringArray2");
        SingleParam_annotationArray2 = environment.elements().getTypeElement("TestClass.SingleParam_annotationArray2");
        SingleParam_classArray2 = environment.elements().getTypeElement("TestClass.SingleParam_classArray2");
    }

    @Test
    void testModifiers() {
        assertTrue(doTest(TestClass, "^;"));
        assertTrue(doTest(TestClass, "public ^;"));
        assertTrue(doTest(TestClass, "public ^*;"));
        assertTrue(doTest(TestClass, "private ^!*;"));
        assertTrue(doTest(TestClass, "packageprivate ^!*;"));
    }

    @Test
    void testAnnotations_concreteValues() {
        assertTrue(doTest(Empty, "@TestClass.My ^;"));

        assertTrue(doTest(SingleParam_boolean, "@TestClass.My(booleanValue = true) ^;"));
        assertTrue(doTest(SingleParam_boolean, "@TestClass.My(booleanValue != false) ^;"));

        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue = 1) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue != 0) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue >= 0) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue > 0) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue <= 2) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue < 2) ^;"));

        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue = 1) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue != 0) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue >= 0) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue > 0) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue <= 2) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue < 2) ^;"));

        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue = 1) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue != 0) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue >= 0) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue > 0) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue <= 2) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue < 2) ^;"));

        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue = 1) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue != 0) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue >= 0) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue > 0) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue <= 2) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue < 2) ^;"));

        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue = 1) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue != 0) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue >= 0) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue > 0) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue <= 2) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue < 2) ^;"));

        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue = 1) ^;"));
        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue != 0) ^;"));
        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue >= 0) ^;"));
        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue > 0) ^;"));
        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue <= 2) ^;"));
        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue < 2) ^;"));

        assertTrue(doTest(SingleParam_char, "@TestClass.My(charValue = 'a') ^;"));
        assertTrue(doTest(SingleParam_char, "@TestClass.My(charValue != 'b') ^;"));

        assertTrue(doTest(SingleParam_string, "@TestClass.My(stringValue = 'ab') ^;"));
        assertTrue(doTest(SingleParam_string, "@TestClass.My(stringValue != 'abc') ^;"));

        assertTrue(doTest(SingleParam_annotation, "@TestClass.My(annotationValue = @java.lang.annotation.Target(**)) ^;"));
        assertTrue(doTest(SingleParam_annotation, "@TestClass.My(annotationValue != @java.lang.annotation.Target) ^;"));

        assertTrue(doTest(SingleParam_class, "@TestClass.My(classValue = java.lang.String.class) ^;"));
        assertTrue(doTest(SingleParam_class, "@TestClass.My(classValue != whatever.class) ^;"));

        assertTrue(doTest(SingleParam_class, "@TestClass.My(* = java.lang.String.class) ^;"));
        assertTrue(doTest(SingleParam_class, "@TestClass.My(* = *) ^;"));
    }

    @Test
    void testAnnotations_defaultValues() {
        assertTrue(doTest(SingleParam_boolean, "@TestClass.My(booleanValue = true, byteValue = 0) ^;"));
        assertTrue(doTest(SingleParam_boolean, "@TestClass.My(booleanValue != false, byteValue != 1) ^;"));

        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue = 1, shortValue = 0) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue != 0, shortValue != 1) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue >= 0, shortValue >= -1) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue > 0, shortValue > -1) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue <= 2, shortValue <= 1) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue < 2, shortValue < 1) ^;"));

        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue = 1, intValue = 0) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue != 0, intValue != 1) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue >= 0, intValue >= 0) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue > 0, intValue > -1) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue <= 2, intValue <= 1) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue < 2, intValue < 1) ^;"));

        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue = 1, longValue = 0) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue != 0, longValue != 1) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue >= 0, longValue >= -1) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue > 0, longValue > 1) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue <= 2, longValue <= 1) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue < 2, longValue < 1) ^;"));

        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue = 1, floatValue = 0) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue != 0, floatValue != 1) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue >= 0, floatValue >= -1) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue > 0, floatValue > -1) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue <= 2, floatValue <= 0) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue < 2, floatValue < 1) ^;"));

        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue = 1, doubleValue = 0) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue != 0, doubleValue != 1) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue >= 0, doubleValue >= 0) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue > 0, doubleValue > -1) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue <= 2, doubleValue <= 1) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue < 2, doubleValue < 1) ^;"));

        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue = 1, charValue = ' ') ^;"));
        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue != 0, charValue != 'a' ) ^;"));
        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue >= 0, charValue = / /) ^;"));
        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue > 0, charValue != /a/) ^;"));

        assertTrue(doTest(SingleParam_char, "@TestClass.My(charValue = 'a', stringValue = '') ^;"));
        assertTrue(doTest(SingleParam_char, "@TestClass.My(charValue != 'b', stringValue != ' ') ^;"));

        assertTrue(doTest(SingleParam_string, "@TestClass.My(stringValue = 'ab', annotationValue = @java.lang.annotation.Target(*)) ^;"));
        assertTrue(doTest(SingleParam_string, "@TestClass.My(stringValue != 'abc', annotationValue != @whatever) ^;"));

        assertTrue(doTest(SingleParam_annotation, "@TestClass.My(annotationValue = @java.lang.annotation.Target(**), classValue = java.lang.Object.class) ^;"));
        assertTrue(doTest(SingleParam_annotation, "@TestClass.My(annotationValue != @java.lang.annotation.Target, classValue != java.lang.String.class) ^;"));

        assertTrue(doTest(SingleParam_class, "@TestClass.My(classValue = java.lang.String.class, booleanValue = false) ^;"));
        assertTrue(doTest(SingleParam_class, "@TestClass.My(classValue != whatever.class, booleanValue != true) ^;"));
    }

    @Test
    void testAnnotations_2concreteValues() {
        assertTrue(doTest(TwoParams_boolean, "@TestClass.My(booleanValue = true, byteValue = 2) ^;"));
        assertTrue(doTest(TwoParams_boolean, "@TestClass.My(booleanValue != false, byteValue != 1) ^;"));

        assertTrue(doTest(TwoParams_byte, "@TestClass.My(byteValue = 1, shortValue = 2) ^;"));
        assertTrue(doTest(TwoParams_byte, "@TestClass.My(byteValue != 0, shortValue != 1) ^;"));
        assertTrue(doTest(TwoParams_byte, "@TestClass.My(byteValue >= 0, shortValue >= 1) ^;"));
        assertTrue(doTest(TwoParams_byte, "@TestClass.My(byteValue > 0, shortValue > 1) ^;"));
        assertTrue(doTest(TwoParams_byte, "@TestClass.My(byteValue <= 2, shortValue <= 3) ^;"));
        assertTrue(doTest(TwoParams_byte, "@TestClass.My(byteValue < 2, shortValue < 3) ^;"));

        assertTrue(doTest(TwoParams_short, "@TestClass.My(shortValue = 1, intValue = 2) ^;"));
        assertTrue(doTest(TwoParams_short, "@TestClass.My(shortValue != 0, intValue != 1) ^;"));
        assertTrue(doTest(TwoParams_short, "@TestClass.My(shortValue >= 0, intValue >= 2) ^;"));
        assertTrue(doTest(TwoParams_short, "@TestClass.My(shortValue > 0, intValue > 1) ^;"));
        assertTrue(doTest(TwoParams_short, "@TestClass.My(shortValue <= 2, intValue <= 3) ^;"));
        assertTrue(doTest(TwoParams_short, "@TestClass.My(shortValue < 2, intValue < 3) ^;"));

        assertTrue(doTest(TwoParams_int, "@TestClass.My(intValue = 1, longValue = 2) ^;"));
        assertTrue(doTest(TwoParams_int, "@TestClass.My(intValue != 0, longValue != 1) ^;"));
        assertTrue(doTest(TwoParams_int, "@TestClass.My(intValue >= 0, longValue >= 2) ^;"));
        assertTrue(doTest(TwoParams_int, "@TestClass.My(intValue > 0, longValue > 1) ^;"));
        assertTrue(doTest(TwoParams_int, "@TestClass.My(intValue <= 2, longValue <= 3) ^;"));
        assertTrue(doTest(TwoParams_int, "@TestClass.My(intValue < 2, longValue < 3) ^;"));

        assertTrue(doTest(TwoParams_long, "@TestClass.My(longValue = 1, floatValue = 2) ^;"));
        assertTrue(doTest(TwoParams_long, "@TestClass.My(longValue != 0, floatValue != 2.1) ^;"));
        assertTrue(doTest(TwoParams_long, "@TestClass.My(longValue >= 0, floatValue >= 1.9) ^;"));
        assertTrue(doTest(TwoParams_long, "@TestClass.My(longValue > 0, floatValue > 1.9) ^;"));
        assertTrue(doTest(TwoParams_long, "@TestClass.My(longValue <= 2, floatValue <= 2.1) ^;"));
        assertTrue(doTest(TwoParams_long, "@TestClass.My(longValue < 2, floatValue < 2.1) ^;"));

        assertTrue(doTest(TwoParams_float, "@TestClass.My(floatValue = 1, doubleValue = 2) ^;"));
        assertTrue(doTest(TwoParams_float, "@TestClass.My(floatValue != 0, doubleValue != 2.1) ^;"));
        assertTrue(doTest(TwoParams_float, "@TestClass.My(floatValue >= 0, doubleValue >= 1.9) ^;"));
        assertTrue(doTest(TwoParams_float, "@TestClass.My(floatValue > 0, doubleValue > 1.9) ^;"));
        assertTrue(doTest(TwoParams_float, "@TestClass.My(floatValue <= 2, doubleValue <= 2.1) ^;"));
        assertTrue(doTest(TwoParams_float, "@TestClass.My(floatValue < 2, doubleValue < 2.1) ^;"));

        assertTrue(doTest(TwoParams_double, "@TestClass.My(doubleValue = 1, charValue = 'b') ^;"));
        assertTrue(doTest(TwoParams_double, "@TestClass.My(doubleValue != 0, charValue != 'a') ^;"));
        assertTrue(doTest(TwoParams_double, "@TestClass.My(doubleValue >= 0, charValue = 'b') ^;"));
        assertTrue(doTest(TwoParams_double, "@TestClass.My(doubleValue > 0, charValue = 'b') ^;"));
        assertTrue(doTest(TwoParams_double, "@TestClass.My(doubleValue <= 2, charValue = *) ^;"));
        assertTrue(doTest(TwoParams_double, "@TestClass.My(doubleValue < 2, charValue = *) ^;"));

        assertTrue(doTest(TwoParams_char, "@TestClass.My(charValue = 'a', stringValue = 'abc') ^;"));
        assertTrue(doTest(TwoParams_char, "@TestClass.My(charValue != 'b', stringValue != 'ab') ^;"));

        assertTrue(doTest(TwoParams_string, "@TestClass.My(stringValue = 'ab', annotationValue = @java.lang.annotation.Target(**)) ^;"));
        assertTrue(doTest(TwoParams_string, "@TestClass.My(stringValue != 'abc', annotationValue != java.lang.annotation.Retention) ^;"));

        assertTrue(doTest(TwoParams_annotation, "@TestClass.My(annotationValue = @java.lang.annotation.Target(**), classValue = java.lang.Object.class) ^;"));
        assertTrue(doTest(TwoParams_annotation, "@TestClass.My(annotationValue != @java.lang.annotation.Target, classValue != java.lang.String.class) ^;"));

        assertTrue(doTest(TwoParams_class, "@TestClass.My(classValue = java.lang.String.class, booleanValue = false) ^;"));
        assertTrue(doTest(TwoParams_class, "@TestClass.My(classValue != whatever.class, booleanValue != true) ^;"));

        assertTrue(doTest(TwoParams_class, "@TestClass.My(* = java.lang.String.class, * = *) ^;"));
    }

    @Test
    void testAnnotations_regex() {
        assertTrue(doTest(SingleParam_boolean, "@TestClass.My(booleanValue = /t[rR]ue/) ^;"));
        assertTrue(doTest(SingleParam_boolean, "@TestClass.My(booleanValue != /false/) ^;"));

        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue = /1/) ^;"));
        assertTrue(doTest(SingleParam_byte, "@TestClass.My(byteValue != /2/) ^;"));

        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue = /1/) ^;"));
        assertTrue(doTest(SingleParam_short, "@TestClass.My(shortValue != /0/) ^;"));

        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue = /1/) ^;"));
        assertTrue(doTest(SingleParam_int, "@TestClass.My(intValue != /0/) ^;"));

        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue = /1.*/) ^;"));
        assertTrue(doTest(SingleParam_long, "@TestClass.My(longValue != /^$/) ^;"));

        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue = /1\\.0/) ^;"));
        assertTrue(doTest(SingleParam_float, "@TestClass.My(floatValue != /0/) ^;"));

        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue = /1\\.0/) ^;"));
        assertTrue(doTest(SingleParam_double, "@TestClass.My(doubleValue != /0/) ^;"));

        assertTrue(doTest(SingleParam_char, "@TestClass.My(charValue = /a/) ^;"));
        assertTrue(doTest(SingleParam_char, "@TestClass.My(charValue != /[A-Z]/) ^;"));

        assertTrue(doTest(SingleParam_string, "@TestClass.My(stringValue = /a[bc]/) ^;"));
        assertTrue(doTest(SingleParam_string, "@TestClass.My(stringValue != /abc/) ^;"));

        assertTrue(doTest(SingleParam_annotation, "@TestClass.My(annotationValue = @/.*Target/(**)) ^;"));
        assertTrue(doTest(SingleParam_annotation, "@TestClass.My(annotationValue != @/.*Retention/) ^;"));

        assertTrue(doTest(SingleParam_class, "@TestClass.My(classValue = /.*String$/.class) ^;"));
        assertTrue(doTest(SingleParam_class, "@TestClass.My(classValue != /whatever/.class) ^;"));

        assertTrue(doTest(SingleParam_class, "@TestClass.My(* = /.*/) ^;"));
    }

    @Test
    void testAnnotations_arrays() {
        assertTrue(doTest(SingleParam_booleanArray1, "@TestClass.My(booleanArray = {true}) ^;"));
        assertTrue(doTest(SingleParam_booleanArray1, "@TestClass.My(booleanArray != {}) ^;"));
        assertTrue(doTest(SingleParam_booleanArray1, "@TestClass.My(booleanArray != {false}) ^;"));
        assertTrue(doTest(SingleParam_booleanArray1, "@TestClass.My(booleanArray != {true, true}) ^;"));

        assertTrue(doTest(SingleParam_byteArray1, "@TestClass.My(byteArray = {1}) ^;"));
        assertTrue(doTest(SingleParam_byteArray1, "@TestClass.My(byteArray != {}) ^;"));
        assertTrue(doTest(SingleParam_byteArray1, "@TestClass.My(byteArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_byteArray1, "@TestClass.My(byteArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_shortArray1, "@TestClass.My(shortArray = {1}) ^;"));
        assertTrue(doTest(SingleParam_shortArray1, "@TestClass.My(shortArray != {}) ^;"));
        assertTrue(doTest(SingleParam_shortArray1, "@TestClass.My(shortArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_shortArray1, "@TestClass.My(shortArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_intArray1, "@TestClass.My(intArray = {1}) ^;"));
        assertTrue(doTest(SingleParam_intArray1, "@TestClass.My(intArray != {}) ^;"));
        assertTrue(doTest(SingleParam_intArray1, "@TestClass.My(intArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_intArray1, "@TestClass.My(intArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_longArray1, "@TestClass.My(longArray = {1}) ^;"));
        assertTrue(doTest(SingleParam_longArray1, "@TestClass.My(longArray != {}) ^;"));
        assertTrue(doTest(SingleParam_longArray1, "@TestClass.My(longArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_longArray1, "@TestClass.My(longArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_floatArray1, "@TestClass.My(floatArray = {1}) ^;"));
        assertTrue(doTest(SingleParam_floatArray1, "@TestClass.My(floatArray != {}) ^;"));
        assertTrue(doTest(SingleParam_floatArray1, "@TestClass.My(floatArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_floatArray1, "@TestClass.My(floatArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_doubleArray1, "@TestClass.My(doubleArray = {1}) ^;"));
        assertTrue(doTest(SingleParam_doubleArray1, "@TestClass.My(doubleArray != {}) ^;"));
        assertTrue(doTest(SingleParam_doubleArray1, "@TestClass.My(doubleArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_doubleArray1, "@TestClass.My(doubleArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_charArray1, "@TestClass.My(charArray = {'a'}) ^;"));
        assertTrue(doTest(SingleParam_charArray1, "@TestClass.My(charArray != {}) ^;"));
        assertTrue(doTest(SingleParam_charArray1, "@TestClass.My(charArray != {'b'}) ^;"));
        assertTrue(doTest(SingleParam_charArray1, "@TestClass.My(charArray != {'a', 'a'}) ^;"));

        assertTrue(doTest(SingleParam_stringArray1, "@TestClass.My(stringArray = {'ab'}) ^;"));
        assertTrue(doTest(SingleParam_stringArray1, "@TestClass.My(stringArray != {}) ^;"));
        assertTrue(doTest(SingleParam_stringArray1, "@TestClass.My(stringArray != {'b'}) ^;"));
        assertTrue(doTest(SingleParam_stringArray1, "@TestClass.My(stringArray != {'ab', 'ab'}) ^;"));

        assertTrue(doTest(SingleParam_annotationArray1, "@TestClass.My(annotationArray = {@java.lang.annotation.Target(*)}) ^;"));
        assertTrue(doTest(SingleParam_annotationArray1, "@TestClass.My(annotationArray != {}) ^;"));
        assertTrue(doTest(SingleParam_annotationArray1, "@TestClass.My(annotationArray != {@whatever}) ^;"));
        assertTrue(doTest(SingleParam_annotationArray1, "@TestClass.My(annotationArray != {@java.lang.annotation.Target(*), @java.lang.annotation.Target(*)}) ^;"));

        assertTrue(doTest(SingleParam_classArray1, "@TestClass.My(classArray = {java.lang.String.class}) ^;"));
        assertTrue(doTest(SingleParam_classArray1, "@TestClass.My(classArray != {}) ^;"));
        assertTrue(doTest(SingleParam_classArray1, "@TestClass.My(classArray != {java.lang.Object.class}) ^;"));
        assertTrue(doTest(SingleParam_classArray1, "@TestClass.My(classArray != {java.lang.String.class, java.lang.String.class}) ^;"));
    }

    @Test
    void testAnnotations_arraysWithMultipleElements() {
        assertTrue(doTest(SingleParam_booleanArray2, "@TestClass.My(booleanArray = {true, false}) ^;"));
        assertTrue(doTest(SingleParam_booleanArray2, "@TestClass.My(booleanArray != {}) ^;"));
        assertTrue(doTest(SingleParam_booleanArray2, "@TestClass.My(booleanArray != {false}) ^;"));
        assertTrue(doTest(SingleParam_booleanArray2, "@TestClass.My(booleanArray != {true, true}) ^;"));

        assertTrue(doTest(SingleParam_byteArray2, "@TestClass.My(byteArray = {1, 2}) ^;"));
        assertTrue(doTest(SingleParam_byteArray2, "@TestClass.My(byteArray != {}) ^;"));
        assertTrue(doTest(SingleParam_byteArray2, "@TestClass.My(byteArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_byteArray2, "@TestClass.My(byteArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_shortArray2, "@TestClass.My(shortArray = {1, 2}) ^;"));
        assertTrue(doTest(SingleParam_shortArray2, "@TestClass.My(shortArray != {}) ^;"));
        assertTrue(doTest(SingleParam_shortArray2, "@TestClass.My(shortArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_shortArray2, "@TestClass.My(shortArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_intArray2, "@TestClass.My(intArray = {1, 2}) ^;"));
        assertTrue(doTest(SingleParam_intArray2, "@TestClass.My(intArray != {}) ^;"));
        assertTrue(doTest(SingleParam_intArray2, "@TestClass.My(intArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_intArray2, "@TestClass.My(intArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_longArray2, "@TestClass.My(longArray = {1, 2}) ^;"));
        assertTrue(doTest(SingleParam_longArray2, "@TestClass.My(longArray != {}) ^;"));
        assertTrue(doTest(SingleParam_longArray2, "@TestClass.My(longArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_longArray2, "@TestClass.My(longArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_floatArray2, "@TestClass.My(floatArray = {1, 2}) ^;"));
        assertTrue(doTest(SingleParam_floatArray2, "@TestClass.My(floatArray != {}) ^;"));
        assertTrue(doTest(SingleParam_floatArray2, "@TestClass.My(floatArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_floatArray2, "@TestClass.My(floatArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_doubleArray2, "@TestClass.My(doubleArray = {1, 2}) ^;"));
        assertTrue(doTest(SingleParam_doubleArray2, "@TestClass.My(doubleArray != {}) ^;"));
        assertTrue(doTest(SingleParam_doubleArray2, "@TestClass.My(doubleArray != {0}) ^;"));
        assertTrue(doTest(SingleParam_doubleArray2, "@TestClass.My(doubleArray != {1, 1}) ^;"));

        assertTrue(doTest(SingleParam_charArray2, "@TestClass.My(charArray = {'a', 'b'}) ^;"));
        assertTrue(doTest(SingleParam_charArray2, "@TestClass.My(charArray != {}) ^;"));
        assertTrue(doTest(SingleParam_charArray2, "@TestClass.My(charArray != {'b'}) ^;"));
        assertTrue(doTest(SingleParam_charArray2, "@TestClass.My(charArray != {'a', 'a'}) ^;"));

        assertTrue(doTest(SingleParam_stringArray2, "@TestClass.My(stringArray = {'ab', 'abc'}) ^;"));
        assertTrue(doTest(SingleParam_stringArray2, "@TestClass.My(stringArray = {'ab', 'abc', **}) ^;"));
        assertTrue(doTest(SingleParam_stringArray2, "@TestClass.My(stringArray != {}) ^;"));
        assertTrue(doTest(SingleParam_stringArray2, "@TestClass.My(stringArray != {'b'}) ^;"));
        assertTrue(doTest(SingleParam_stringArray2, "@TestClass.My(stringArray != {'ab', 'ab'}) ^;"));

        assertTrue(doTest(SingleParam_annotationArray2, "@TestClass.My(annotationArray = {@java.lang.annotation.Target(*),*}) ^;"));
        assertTrue(doTest(SingleParam_annotationArray2, "@TestClass.My(annotationArray != {}) ^;"));
        assertTrue(doTest(SingleParam_annotationArray2, "@TestClass.My(annotationArray != {@whatever}) ^;"));
        assertTrue(doTest(SingleParam_annotationArray2, "@TestClass.My(annotationArray != {*, *, *}) ^;"));

        assertTrue(doTest(SingleParam_classArray2, "@TestClass.My(classArray = {java.lang.String.class, java.lang.Object.class}) ^;"));
        assertTrue(doTest(SingleParam_classArray2, "@TestClass.My(classArray != {}) ^;"));
        assertTrue(doTest(SingleParam_classArray2, "@TestClass.My(classArray != {java.lang.Object.class}) ^;"));
        assertTrue(doTest(SingleParam_classArray2, "@TestClass.My(classArray != {java.lang.String.class, java.lang.String.class}) ^;"));
    }

    private boolean doTest(Element expected, String recipe) {
        ModelInspector<Element> insp = new MirroringModelInspector(environment.elements());

        StructuralMatcher matcher = Classif.compile(recipe);

        return matcher.test(expected, insp);
    }
}

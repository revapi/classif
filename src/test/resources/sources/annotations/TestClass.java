import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class TestClass {
    
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface My {
        boolean booleanValue() default false;
        byte byteValue() default 0;
        short shortValue() default 0;
        int intValue() default 0;
        long longValue() default 0;
        float floatValue() default 0;
        double doubleValue() default 0;
        char charValue() default ' ';
        String stringValue() default "";
        Target annotationValue() default @Target(ElementType.TYPE);
        Class<?> classValue() default Object.class;

        boolean[] booleanArray() default {};
        byte[] byteArray() default {};
        short[] shortArray() default {};
        int[] intArray() default {};
        long[] longArray() default {};
        float[] floatArray() default {};
        double[] doubleArray() default {};
        char[] charArray() default {};
        String[] stringArray() default {};
        Target[] annotationArray() default {};
        Class<?>[] classArray() default {};
    }
    
    @My
    public class Empty {}

    @My(booleanValue = true)
    public class SingleParam_boolean {}

    @My(byteValue = 1)
    public class SingleParam_byte {}

    @My(shortValue = 1)
    public class SingleParam_short {}

    @My(intValue = 1)
    public class SingleParam_int {}

    @My(longValue = 1)
    public class SingleParam_long {}

    @My(floatValue = 1)
    public class SingleParam_float {}

    @My(doubleValue = 1)
    public class SingleParam_double {}

    @My(charValue = 'a')
    public class SingleParam_char {}

    @My(stringValue = "ab")
    public class SingleParam_string {}

    @My(annotationValue = @Target(ElementType.METHOD))
    public class SingleParam_annotation {}

    @My(classValue = String.class)
    public class SingleParam_class {}



    @My(booleanValue = true, byteValue = 2)
    public class TwoParams_boolean {}

    @My(byteValue = 1, shortValue = 2)
    public class TwoParams_byte {}

    @My(shortValue = 1, intValue = 2)
    public class TwoParams_short {}

    @My(intValue = 1, longValue = 2)
    public class TwoParams_int {}

    @My(longValue = 1, floatValue = 2)
    public class TwoParams_long {}

    @My(floatValue = 1, doubleValue = 2)
    public class TwoParams_float {}

    @My(doubleValue = 1, charValue = 'b')
    public class TwoParams_double {}

    @My(charValue = 'a', stringValue = "abc")
    public class TwoParams_char {}

    @My(stringValue = "ab", annotationValue = @Target(ElementType.TYPE_USE))
    public class TwoParams_string {}

    @My(annotationValue = @Target(ElementType.METHOD), classValue = Object.class)
    public class TwoParams_annotation {}

    @My(classValue = String.class, booleanValue = false)
    public class TwoParams_class {}






    @My(booleanArray = true)
    public class SingleParam_booleanArray1 {}

    @My(byteArray = 1)
    public class SingleParam_byteArray1 {}

    @My(shortArray = 1)
    public class SingleParam_shortArray1 {}

    @My(intArray = 1)
    public class SingleParam_intArray1 {}

    @My(longArray = 1)
    public class SingleParam_longArray1 {}

    @My(floatArray = 1)
    public class SingleParam_floatArray1 {}

    @My(doubleArray = 1)
    public class SingleParam_doubleArray1 {}

    @My(charArray = 'a')
    public class SingleParam_charArray1 {}

    @My(stringArray = "ab")
    public class SingleParam_stringArray1 {}

    @My(annotationArray = @Target(ElementType.METHOD))
    public class SingleParam_annotationArray1 {}

    @My(classArray = String.class)
    public class SingleParam_classArray1 {}



    @My(booleanArray = {true, false})
    public class SingleParam_booleanArray2 {}

    @My(byteArray = {1,2})
    public class SingleParam_byteArray2 {}

    @My(shortArray = {1,2})
    public class SingleParam_shortArray2 {}

    @My(intArray = {1, 2})
    public class SingleParam_intArray2 {}

    @My(longArray = {1, 2})
    public class SingleParam_longArray2 {}

    @My(floatArray = {1, 2})
    public class SingleParam_floatArray2 {}

    @My(doubleArray = {1, 2})
    public class SingleParam_doubleArray2 {}

    @My(charArray = {'a', 'b'})
    public class SingleParam_charArray2 {}

    @My(stringArray = {"ab", "abc"})
    public class SingleParam_stringArray2 {}

    @My(annotationArray = {@Target(ElementType.METHOD), @Target(ElementType.ANNOTATION_TYPE)})
    public class SingleParam_annotationArray2 {}

    @My(classArray = {String.class, Object.class})
    public class SingleParam_classArray2 {}
}

public class TestClass<
        A,
        B extends Object,
        C extends String,
        D extends C,
        F extends String & Cloneable
        > {

    public java.util.Collection<?> wildcard() {
        return null;
    }

    public java.util.Collection<? extends String[][]> wildcardExtends() {
        return null;
    }

    public java.util.Collection<? super java.util.List> wildcardSuper() {
        return null;
    }
}

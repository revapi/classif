public class Implements {

    public interface Iface {

    }

    public interface GenericIface<T, U extends String> {

    }

    public class Impl implements Iface {

    }

    public class InheritedImpl extends Impl implements java.lang.Cloneable {

    }

    public class GenericImplGeneric<T extends String> implements GenericIface<T, String> {

    }

    public class GenericImplConcrete implements GenericIface<Object, String> {

    }
}

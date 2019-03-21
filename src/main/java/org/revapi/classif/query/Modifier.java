package org.revapi.classif.query;

public enum Modifier {
    PUBLIC, PROTECTED, PRIVATE, PACKAGE_PRIVATE, ABSTRACT, DEFAULT, STATIC, FINAL, TRANSIENT, VOLATILE, SYNCHRONIZED,
    NATIVE, STRICTFP;


    @Override
    public String toString() {
        switch (this) {
        case PUBLIC:
            return "public";
        case PROTECTED:
            return "protected";
        case PRIVATE:
            return "private";
        case PACKAGE_PRIVATE:
            return "packageprivate";
        case ABSTRACT:
            return "abstract";
        case DEFAULT:
            return "default";
        case STATIC:
            return "static";
        case FINAL:
            return "final";
        case TRANSIENT:
            return "transient";
        case VOLATILE:
            return "volatile";
        case SYNCHRONIZED:
            return "synchronized";
        case NATIVE:
            return "native";
        case STRICTFP:
            return "strictfp";
        default:
            throw new IllegalStateException("Modifier variant " + this.name() + " not handled by toString()");
        }
    }
}

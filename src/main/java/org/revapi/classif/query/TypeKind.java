package org.revapi.classif.query;

public enum TypeKind {
    CLASS("class"), INTERFACE("interface"), ENUM("enum"), ANNOTATION_TYPE("@interface"), ANY("type");

    private final String string;

    TypeKind(String string) {
        this.string = string;
    }

    public String toString() {
        return string;
    }
}

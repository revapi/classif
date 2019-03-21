package org.revapi.classif.query.match.declaration;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.revapi.classif.match.declaration.AnnotationMatch;
import org.revapi.classif.match.declaration.AnnotationValueMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;
import org.revapi.classif.util.Operator;

public class AnnotationValueMatchBuilder<P extends AbstractBuilder<P, ?, ?>>
    extends AbstractBuilderWithReferencedVariables<AnnotationValueMatchBuilder<P>, AnnotationValueMatch, P> {

    private final Operator operator;
    private Object value;
    private NestedValueType valueType;

    public AnnotationValueMatchBuilder(P parent, Operator operator,
            BiConsumer<AnnotationValueMatchBuilder<P>, AnnotationValueMatch> productConsumer) {
        super(parent, productConsumer);
        this.operator = operator;
    }

    public P any() {
        return finish(AnnotationValueMatch.any(operator));
    }

    public P all() {
        return finish(AnnotationValueMatch.all());
    }

    public P string(String value) {
        return finish(AnnotationValueMatch.string(operator, value));
    }

    public P regex(Pattern regex) {
        return finish(AnnotationValueMatch.regex(operator, regex));
    }

    public P bool(boolean value) {
        return finish(AnnotationValueMatch.bool(operator, value));
    }

    public P number(Number number) {
        return finish(AnnotationValueMatch.number(operator, number));
    }

    public AnnotationValueEnumMatchBuilder<AnnotationValueMatchBuilder<P>> enumConstant() {
        return new AnnotationValueEnumMatchBuilder<>(this, (b, p) -> {
           value = p;
           valueType = NestedValueType.ENUM_CONST;
        });
    }

    public TypeReferenceMatchBuilder<AnnotationValueMatchBuilder<P>> oneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            valueType = NestedValueType.TYPE;
            value = p;
        });
    }

    public AnnotationMatchBuilder<AnnotationValueMatchBuilder<P>> annotation() {
        return new AnnotationMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            valueType = NestedValueType.ANNO;
            value = p;
        });
    }

    public AnnotationValueArrayMatchBuilder<AnnotationValueMatchBuilder<P>> array() {
        return new AnnotationValueArrayMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            valueType = NestedValueType.ARRAY;
            value = p;
        });
    }

    public P defaultValue() {
        return finish(null);
    }

    public P endValue() {
        if (valueType == null) {
            throw new IllegalStateException("No value defined by the builder.");
        }

        switch (valueType) {
        case ENUM_CONST:
            AnnotationValueEnumMatchBuilder.EnumMatch em = (AnnotationValueEnumMatchBuilder.EnumMatch) value;
            return finish(AnnotationValueMatch.enumConstant(operator, em.typeName, em.name));
        case ANNO:
            AnnotationMatch anno = (AnnotationMatch) value;
            return finish(AnnotationValueMatch.annotation(operator, anno));
        case TYPE:
            TypeReferenceMatch type = (TypeReferenceMatch) value;
            return finish(AnnotationValueMatch.type(operator, type));
        case ARRAY:
            @SuppressWarnings("unchecked") List<AnnotationValueMatch> array = (List<AnnotationValueMatch>) value;
            return finish(AnnotationValueMatch.array(operator, array));
        default:
            throw new IllegalStateException("Unhandled nested value type: " + valueType);
        }
    }

    private enum NestedValueType {
        ENUM_CONST, TYPE, ANNO, ARRAY
    }
}

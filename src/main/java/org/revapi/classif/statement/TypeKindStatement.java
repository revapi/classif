package org.revapi.classif.statement;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.revapi.classif.ModelInspector;

public final class TypeKindStatement extends AbstractStatement {
    private final boolean negation;
    private final ElementKind kind;

    public TypeKindStatement(boolean negation, String typeKind) {
        super(null, emptyList(), false);
        this.negation = negation;

        switch (requireNonNull(typeKind)) {
            case "class":
                kind = ElementKind.CLASS;
                break;
            case "interface":
                kind = ElementKind.INTERFACE;
                break;
            case "enum":
                kind = ElementKind.ENUM;
                break;
            case "@interface":
                kind = ElementKind.ANNOTATION_TYPE;
                break;
            case "type":
                kind = ElementKind.OTHER; // meaning "any"
                break;
            default:
                throw new IllegalArgumentException("The kind '" + typeKind + "' of a type not recognized.");
        }
    }

    @Override
    public AbstractMatcher createMatcher() {
        return new AbstractMatcher() {
            @Override
            public <E> boolean testType(E type, ModelInspector<E> inspector, Map<String, AbstractMatcher> variables) {
                Element el = inspector.toElement(type);
                if (!(el instanceof  TypeElement)) {
                    return false;
                }

                TypeElement typeModel = (TypeElement) el;

                boolean matches = kind == ElementKind.OTHER || typeModel.getKind() == kind;

                return negation != matches;
            }
        };
    }
}

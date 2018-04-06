package org.revapi.classif.match.declaration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.match.util.Glob;

public final class AnnotationMatch {
    private final boolean negation;
    private final TypeReferenceMatch type;
    private final Glob<AnnotationAttributeMatch> attributes;

    public AnnotationMatch(boolean negation, TypeReferenceMatch type,
            List<AnnotationAttributeMatch> attributes) {

        this.negation = negation;
        this.type = type;
        this.attributes = new Glob<>(attributes);
    }

    public <M> boolean test(AnnotationMirror a , MatchContext<M> ctx) {
        //test1: @A(a = 1, b = 2)
        //test2: @A(a = 1)
        //actual: @A(a = 1), and b is an attribute with default value 2
        //both of the above tests need to match
        boolean ret = type.testInstance(a.getAnnotationType(), ctx)
                && attributes.testUnorderedWithOptionals((m, at) -> m.test(at, ctx), explicitAttributes(a).entrySet(),
                defaultAttributes(a).entrySet());

        return negation != ret;
    }

    //needed just to make the compiler happy
    @SuppressWarnings("unchecked")
    private Map<ExecutableElement, AnnotationValue> explicitAttributes(AnnotationMirror a) {
        return (Map<ExecutableElement, AnnotationValue>) a.getElementValues();
    }

    private Map<ExecutableElement, AnnotationValue> defaultAttributes(AnnotationMirror a) {
        Map<ExecutableElement, AnnotationValue> explicit = new LinkedHashMap<>(a.getElementValues());
        Map<ExecutableElement, AnnotationValue> ret = new LinkedHashMap<>(a.getElementValues());

        DeclaredType type = a.getAnnotationType();
        for (ExecutableElement m : ElementFilter.methodsIn(type.asElement().getEnclosedElements())) {
            if (!explicit.containsKey(m)) {
                ret.put(m, m.getDefaultValue());
            }
        }

        return ret;
    }
}

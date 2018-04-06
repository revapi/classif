package org.revapi.classif.match.declaration;

import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.util.Globbed;

public final class AnnotationAttributeMatch implements Globbed {
    private final boolean isAny;
    private final boolean isAll;
    private final NameMatch name;
    private final AnnotationValueMatch valueMatch;

    public AnnotationAttributeMatch(boolean isAny, boolean isAll, NameMatch name,
            AnnotationValueMatch valueMatch) {
        this.isAny = isAny;
        this.isAll = isAll;
        this.name = name;
        this.valueMatch = valueMatch;
    }

    @Override
    public boolean isMatchAny() {
        return isAny || (name != null && valueMatch != null && name.isMatchAny() && valueMatch.isMatchAny());
    }

    @Override
    public boolean isMatchAll() {
        return isAll;
    }

    public <M> boolean test(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> attribute, MatchContext<M> matchContext) {
        return isMatchAny() || isMatchAll()
                || (name.matches(attribute.getKey().getSimpleName().toString())
                && valueMatch.test(attribute.getValue(), matchContext));
    }
}

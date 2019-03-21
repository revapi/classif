package org.revapi.classif.query;

import java.util.function.BiConsumer;

public abstract class AbstractBuilder<This extends AbstractBuilder<This, ProducedType, ParentBuilder>, ProducedType, ParentBuilder extends AbstractBuilder<ParentBuilder, ?, ?>> {

    protected final ParentBuilder parent;
    private final BiConsumer<This, ProducedType> productConsumer;

    protected AbstractBuilder(ParentBuilder parent, BiConsumer<This, ProducedType> productConsumer) {
        this.parent = parent;
        this.productConsumer = productConsumer;
    }

    protected final ParentBuilder finish(ProducedType product) {
        productConsumer.accept(castThis(), product);
        return parent;
    }

    @SuppressWarnings("unchecked")
    protected final <T> T castThis() {
        return (T) this;
    }
}

package org.revapi.classif.match.declaration;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.ModelInspector;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.util.Glob;
import org.revapi.classif.util.Nullable;

public final class ImplementsMatch extends DeclarationMatch {
    private final boolean onlyDirect;
    private final @Nullable List<TypeReferenceMatch> types;
    private final @Nullable Glob<TypeReferenceMatch> glob;

    public ImplementsMatch(boolean onlyDirect, boolean exactList, List<TypeReferenceMatch> types) {
        this.onlyDirect = onlyDirect;
        this.types = exactList ? null : types;
        this.glob = exactList ? new Glob<>(types) : null;
    }

    @Override
    protected <M> boolean testType(TypeElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        if (glob == null) {
            assert types != null;
            List<DeclaredType> impld = getImplemented(instantiation, ctx.modelInspector);
            return types.stream().allMatch(m -> impld.stream().anyMatch(i -> m.testInstance(i, ctx)));
        } else {
            return glob.testUnordered((m, t) -> m.testInstance(t, ctx), getImplemented(instantiation, ctx.modelInspector));
        }
    }

    private List<DeclaredType> getImplemented(TypeMirror type, ModelInspector<?> insp) {
        List<DeclaredType> superTypes = directSuperTypes(type, insp);

        List<DeclaredType> ret;

        if (!onlyDirect) {
            ret = new ArrayList<>(superTypes.size());
            resolveAllImplemented(superTypes, ret, insp);
        } else if (superTypes.size() > 1) {
            ret = superTypes.subList(1, superTypes.size());
        } else {
            ret = Collections.emptyList();
        }

        return ret;
    }

    private void resolveAllImplemented(List<DeclaredType> superTypes, List<DeclaredType> ifaces,
            ModelInspector<?> insp) {

        if (!superTypes.isEmpty()) {
            DeclaredType superClass = superTypes.get(0);
            List<DeclaredType> superIfaces = superTypes.subList(1, superTypes.size());
            resolveAllImplemented(superClass, superIfaces, ifaces, insp);
        }
    }

    private void resolveAllImplemented(DeclaredType superClass, List<DeclaredType> superIfaces,
            List<DeclaredType> result, ModelInspector<?> insp) {

        List<DeclaredType> superTypesOfSuperClass = directSuperTypes(superClass, insp);
        if (!superTypesOfSuperClass.isEmpty()) {
            resolveAllImplemented(superTypesOfSuperClass.get(0),
                    superTypesOfSuperClass.subList(1, superTypesOfSuperClass.size()), result, insp);
        }

        for (DeclaredType si : superIfaces) {
            if (result.contains(si)) {
                continue;
            }

            result.add(si);

            List<DeclaredType> superTypesOfSuperInterface = directSuperTypes(si, insp);
            if (!superTypesOfSuperInterface.isEmpty()) {
                resolveAllImplemented(superTypesOfSuperInterface.get(0),
                        superTypesOfSuperInterface.subList(1, superTypesOfSuperInterface.size()), result, insp);
            }
        }
    }

    private List<DeclaredType> directSuperTypes(TypeMirror t, ModelInspector<?> insp) {
        return insp.directSupertypes(t).stream().map(s -> (DeclaredType) s).collect(toList());
    }
}

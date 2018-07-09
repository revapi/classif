/*
 * Copyright 2018 Lukas Krejci
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.revapi.classif;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.util.Nullable;

/**
 * Provides {@link Classif} with the means to navigate the element hierarchy. In addition to providing
 * the conversion from and to the {@link Element} instances (to enable analysis of the elements) the implementors also
 * need to provide additional information not readily available in the {@code javax.lang.model} APIs.
 */
public interface ModelInspector<M> {
    /**
     * This is needed for example when matching wildcards but Classif itself doesn't have a way of obtaining elements
     * that are not supplied to it directly. Therefore it needs to kindly ask the inspector for providing it.
     *
     * @return the type element of {@code java.lang.Object}
     */
    TypeElement getJavaLangObjectElement();

    /**
     * Converts the model representation to the {@link Element}.
     * @param model the model of the code element
     * @return the javax.lang.model element
     */
    Element toElement(M model);

    /**
     * In case the model represents a type use, this returns the type mirror representing that use.
     *
     * This should never return null, if the model represents a declaration, the mirror should just reflect that
     * declaration.
     *
     * @param model the model of the code element
     * @return the type mirror of the code element, never null
     */
    TypeMirror toMirror(M model);

    /**
     * @param model the model representation of an element
     * @return a model representation of the element directly enclosing the provided element or null if there is none.
     */
    M getEnclosing(M model);

    /**
     * The set of model representations of the elements directly enclosed in the provided one.
     *
     * @param model the model representation of the element
     * @return the set of enclosed elements
     */
    Set<M> getEnclosed(M model);

    /**
     * Provides the model representations of the types that are directly used by the element. The provided model
     * is guaranteed to represent a Java type.
     *
     * <p>The returned set should only contain types that cannot be directly deduced from the element. I.e. when
     * looking for what a method uses, the return type, the type of type parameters, arguments and the type of the
     * thrown exception are automatically considered and should not be part of the returned set.
     *
     * <p>So in another words this method is useful only for some non-standard uses somehow known to the inspector
     * that cannot otherwise be deduced from the declaration of the element itself.
     *
     * <p>Note that this method is allowed to return null in the case when the inspector doesn't have enough information
     * to provide the information at the time it is called. In this case the result of the matching that requires this
     * check will be deferred to the {@link MatchingProgress#finish()} method.
     *
     * @param model the model of the element
     * @return the set of model representations of the types directly used by the provided element
     */
    @Nullable Set<M> getUses(M model);

    /**
     * Provides the model representations of elements that directly use the provided element. The provided model
     * is guaranteed to represent a Java type.
     *
     * <p>Unlike with {@link #getUses(Object)}, Classif cannot find any use sites of the element on its own. That would
     * require a full-scan of all the model elements during the matching.
     *
     * <p>Note that this method is allowed to return null in the case when the inspector doesn't have enough information
     * to provide the information at the time it is called. In this case the result of the matching that requires this
     * check will be deferred to the {@link MatchingProgress#finish()} method.
     *
     * @param model the model of the element
     * @return the set of model representations of the elements that directly use the provided type element. Null if
     * such set cannot be provided at the time the method is called.
     */
    @Nullable Set<M> getUseSites(M model);

    /**
     * Tells whether the provided element is inherited from some super type. The method is only ever called on
     * inheritable like fields and methods.
     *
     * @param model the model representation of the field or method element
     * @return true if the element is inherited, false otherwise
     */
    boolean isInherited(M model);

    /**
     * Transforms the provided element back to its model representation. The model representation must be usable by the
     * methods of this class again (e.g. methods like {@link #getUseSites(Object)} still need to work on the returned
     * object).
     *
     * @param element the element to convert back to the model representation
     * @return the model representation of the element
     */
    M fromType(TypeElement element);

    /**
     * This is assumed to be equivalent to {@link javax.lang.model.util.Types#directSupertypes(TypeMirror)}.
     *
     * @param type the type to get the supertypes of
     * @return the same as the above mentioned method would return
     */
    List<? extends TypeMirror> directSupertypes(TypeMirror type);
}

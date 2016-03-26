package fr.inria.autojmh.generators.transformations.substitutes;

import fr.inria.autojmh.generators.transformations.printer.AJMHPrettyPrinter;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.declaration.ParentNotInitializedException;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtVisitor;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.ReferenceFilter;
import spoon.support.reflect.code.CtVariableAccessImpl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * Created by marodrig on 24/03/2016.
 */
public class CtVariableAccessDecorator extends CtVariableAccessImpl {

    private final CtVariableAccess wrap;

    public CtVariableAccessDecorator(CtVariableAccess a) {
        wrap = a;
    }

    public String getSignature() {
        return wrap.getSignature();
    }

    public Factory getFactory() {
        return wrap.getFactory();
    }

    public void setFactory(Factory factory) {
        wrap.setFactory(factory);
    }

    public int compareTo(CtElement o) {
        return wrap.compareTo(o);
    }

    public boolean equals(Object o) {
        return wrap.equals(o);
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return wrap.getAnnotation(annotationType);
    }

    public <A extends Annotation> CtAnnotation<A> getAnnotation(CtTypeReference<A> annotationType) {
        return wrap.getAnnotation(annotationType);
    }

    public List<CtAnnotation<? extends Annotation>> getAnnotations() {
        return wrap.getAnnotations();
    }

    public String getDocComment() {
        return wrap.getDocComment();
    }

    public CtElement getParent() throws ParentNotInitializedException {
        return wrap.getParent();
    }

    public boolean isRootElement() {
        return wrap.isRootElement();
    }

    public void setRootElement(boolean rootElement) {
        wrap.setRootElement(rootElement);
    }

    public <P extends CtElement> P getParent(Class<P> parentType) throws ParentNotInitializedException {
        return wrap.getParent(parentType);
    }

    public boolean hasParent(CtElement candidate) throws ParentNotInitializedException {
        return wrap.hasParent(candidate);
    }

    public SourcePosition getPosition() {
        return wrap.getPosition();
    }

    public int hashCode() {
        return wrap.hashCode();
    }

    public void replace(CtElement element) {
        wrap.replace(element);
    }

    public void setAnnotations(List<CtAnnotation<? extends Annotation>> annotations) {
        wrap.setAnnotations(annotations);
    }

    public boolean addAnnotation(CtAnnotation<? extends Annotation> annotation) {
        return wrap.addAnnotation(annotation);
    }

    public boolean removeAnnotation(CtAnnotation<? extends Annotation> annotation) {
        return wrap.removeAnnotation(annotation);
    }

    public void setDocComment(String docComment) {
        wrap.setDocComment(docComment);
    }

    public void setParent(CtElement parentElement) {
        wrap.setParent(parentElement);
    }

    public void setPosition(SourcePosition position) {
        wrap.setPosition(position);
    }

    public void setPositions(SourcePosition position) {
        wrap.setPositions(position);
    }

    @Override
    public String toString() {
        AJMHPrettyPrinter printer = new AJMHPrettyPrinter(getFactory().getEnvironment());
        printer.scan(this);
        return printer.toString();
    }

    public <E extends CtElement> List<E> getAnnotatedChildren(Class<? extends Annotation> annotationType) {
        return wrap.getAnnotatedChildren(annotationType);
    }

    public boolean isImplicit() {
        return wrap.isImplicit();
    }

    public void setImplicit(boolean implicit) {
        wrap.setImplicit(implicit);
    }

    public Set<CtTypeReference<?>> getReferencedTypes() {
        return wrap.getReferencedTypes();
    }

    public <E extends CtElement> List<E> getElements(Filter<E> filter) {
        return wrap.getElements(filter);
    }

    public <T extends CtReference> List<T> getReferences(ReferenceFilter<T> filter) {
        return wrap.getReferences(filter);
    }

    public void updateAllParentsBelow() {
        wrap.updateAllParentsBelow();
    }

    public boolean isParentInitialized() {
        return wrap.isParentInitialized();
    }

    public <R extends CtCodeElement> R partiallyEvaluate() {
        return wrap.partiallyEvaluate();
    }

    public CtCodeElement getSubstitution(CtSimpleType targetType) {
        return wrap.getSubstitution(targetType);
    }

    public CtTypeReference getType() {
        return wrap.getType();
    }

    public List<CtTypeReference<?>> getTypeCasts() {
        return wrap.getTypeCasts();
    }


    public void setTypeCasts(List casts) {
        wrap.setTypeCasts(casts);
    }

    public void addTypeCast(CtTypeReference type) {
        wrap.addTypeCast(type);
    }

    public void accept(CtVisitor visitor) {
        wrap.accept(visitor);
    }



    public CtVariableReference getVariable() {
        return wrap.getVariable();
    }

    public Object S() {
        return wrap.S();
    }

    public void setType(CtTypeReference type) {
        wrap.setType(type);
    }

    public void setVariable(CtVariableReference variable) {
        wrap.setVariable(variable);
    }
}

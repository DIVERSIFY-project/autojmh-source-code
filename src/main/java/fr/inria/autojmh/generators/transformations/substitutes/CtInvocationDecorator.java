package fr.inria.autojmh.generators.transformations.substitutes;

import fr.inria.autojmh.generators.transformations.printer.AJMHPrettyPrinter;
import spoon.reflect.code.*;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.declaration.ParentNotInitializedException;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtVisitor;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.ReferenceFilter;
import spoon.support.reflect.code.CtInvocationImpl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * A class that acts like an invocation but overriding the printing part.
 * <p>
 * Automatically generated with IntelliJ IDEA:
 * http://stackoverflow.com/questions/4325699/can-intellij-automatically-create-a-decorator-class
 * <p>
 * <p>
 * Created by marodrig on 24/03/2016.
 */
public class CtInvocationDecorator<E> extends CtInvocationImpl<E> {

    CtInvocation<E> wrap;

    public CtInvocationDecorator(CtInvocation<E> wrap) {
        this.wrap = wrap;
    }

    @Override
    public String toString() {
        AJMHPrettyPrinter printer = new AJMHPrettyPrinter(getFactory().getEnvironment());
        printer.scan(this);
        return printer.toString();
    }

    @Override
    public void setGenericTypes(List<CtTypeReference<?>> list) {
        wrap.setGenericTypes(list);
    }

    @Override
    public List<CtTypeReference<?>> getGenericTypes() {
        return wrap.getGenericTypes();
    }

    @Override
    public List<CtExpression<?>> getArguments() {
        return wrap.getArguments();
    }

    @Override
    public void addArgument(CtExpression<?> argument) {
        wrap.addArgument(argument);
    }

    @Override
    public void removeArgument(CtExpression<?> argument) {
        wrap.removeArgument(argument);
    }

    @Override
    public CtExecutableReference<E> getExecutable() {
        return wrap.getExecutable();
    }

    @Override
    public void setArguments(List<CtExpression<?>> arguments) {
        wrap.setArguments(arguments);
    }

    @Override
    public void setExecutable(CtExecutableReference<E> executable) {
        wrap.setExecutable(executable);
    }

    @Override
    public void accept(CtVisitor visitor) {
        wrap.accept(visitor);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return wrap.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> CtAnnotation<A> getAnnotation(CtTypeReference<A> annotationType) {
        return wrap.getAnnotation(annotationType);
    }

    @Override
    public List<CtAnnotation<? extends Annotation>> getAnnotations() {
        return wrap.getAnnotations();
    }

    @Override
    public String getDocComment() {
        return wrap.getDocComment();
    }

    @Override
    public CtElement getParent() throws ParentNotInitializedException {
        return wrap.getParent();
    }

    @Override
    public boolean isParentInitialized() {
        return wrap.isParentInitialized();
    }

    @Override
    public boolean isRootElement() {
        return wrap.isRootElement();
    }

    @Override
    public void setRootElement(boolean rootElement) {
        wrap.setRootElement(rootElement);
    }

    @Override
    public String getSignature() {
        return wrap.getSignature();
    }

    @Override
    public <P extends CtElement> P getParent(Class<P> parentType) throws ParentNotInitializedException {
        return wrap.getParent(parentType);
    }

    @Override
    public boolean hasParent(CtElement candidate) throws ParentNotInitializedException {
        return wrap.hasParent(candidate);
    }

    @Override
    public SourcePosition getPosition() {
        return wrap.getPosition();
    }

    @Override
    public void replace(CtElement element) {
        wrap.replace(element);
    }

    @Override
    public boolean addAnnotation(CtAnnotation<? extends Annotation> annotation) {
        return wrap.addAnnotation(annotation);
    }

    @Override
    public boolean removeAnnotation(CtAnnotation<? extends Annotation> annotation) {
        return wrap.removeAnnotation(annotation);
    }

    @Override
    public void setDocComment(String docComment) {
        wrap.setDocComment(docComment);
    }

    @Override
    public void setParent(CtElement element) {
        wrap.setParent(element);
    }

    @Override
    public void updateAllParentsBelow() {
        wrap.updateAllParentsBelow();
    }

    @Override
    public void setPosition(SourcePosition position) {
        wrap.setPosition(position);
    }

    @Override
    public <E extends CtElement> List<E> getAnnotatedChildren(Class<? extends Annotation> annotationType) {
        return wrap.getAnnotatedChildren(annotationType);
    }

    @Override
    public boolean isImplicit() {
        return wrap.isImplicit();
    }

    @Override
    public void setImplicit(boolean b) {
        wrap.setImplicit(b);
    }

    @Override
    public Set<CtTypeReference<?>> getReferencedTypes() {
        return wrap.getReferencedTypes();
    }

    @Override
    public <E extends CtElement> List<E> getElements(Filter<E> filter) {
        return wrap.getElements(filter);
    }

    @Override
    public <T extends CtReference> List<T> getReferences(ReferenceFilter<T> filter) {
        return wrap.getReferences(filter);
    }

    @Override
    public void setPositions(SourcePosition position) {
        wrap.setPositions(position);
    }

    @Override
    public void setAnnotations(List<CtAnnotation<? extends Annotation>> annotation) {
        wrap.setAnnotations(annotation);
    }

    @Override
    public Factory getFactory() {
        return wrap.getFactory();
    }

    @Override
    public void setFactory(Factory factory) {
        wrap.setFactory(factory);
    }

    @Override
    public int compareTo(CtElement o) {
        return wrap.compareTo(o);
    }

    @Override
    public void insertAfter(CtStatement statement) throws ParentNotInitializedException {
        wrap.insertAfter(statement);
    }

    @Override
    public void insertAfter(CtStatementList<?> statements) throws ParentNotInitializedException {
        wrap.insertAfter(statements);
    }

    @Override
    public void insertBefore(CtStatement statement) throws ParentNotInitializedException {
        wrap.insertBefore(statement);
    }

    @Override
    public void insertBefore(CtStatementList<?> statements) throws ParentNotInitializedException {
        wrap.insertBefore(statements);
    }

    @Override
    public String getLabel() {
        return wrap.getLabel();
    }

    @Override
    public void setLabel(String label) {
        wrap.setLabel(label);
    }

    @Override
    public <R extends CtCodeElement> R partiallyEvaluate() {
        return wrap.partiallyEvaluate();
    }

    @Override
    public CtExpression<?> getTarget() {
        return wrap.getTarget();
    }

    @Override
    public void setTarget(CtExpression<?> target) {
        wrap.setTarget(target);
    }

    @Override
    public List<CtTypeReference<?>> getTypeCasts() {
        return wrap.getTypeCasts();
    }

    @Override
    public void setTypeCasts(List<CtTypeReference<?>> types) {
        wrap.setTypeCasts(types);
    }

    @Override
    public void addTypeCast(CtTypeReference<?> type) {
        wrap.addTypeCast(type);
    }

    @Override
    public CtTypeReference<E> getType() {
        return wrap.getType();
    }

    @Override
    public void setType(CtTypeReference<E> type) {
        wrap.setType(type);
    }

    @Override
    public E S() {
        return wrap.S();
    }

    @Override
    public CtCodeElement getSubstitution(CtSimpleType<?> targetType) {
        return wrap.getSubstitution(targetType);
    }
}

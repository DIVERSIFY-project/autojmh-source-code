package fr.inria.autojmh.generators.printer;

import fr.inria.autojmh.snippets.BenchSnippet;
import fr.inria.autojmh.snippets.Preconditions;
import fr.inria.autojmh.snippets.modelattrib.MethodAttributes;
import fr.inria.autojmh.snippets.modelattrib.TypeAttributes;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

import java.lang.annotation.Annotation;

import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.fieldTargetIsThis;
import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.visibility;
import static fr.inria.autojmh.snippets.modelattrib.VariableAccessAttributes.isTargetAllowed;
import static spoon.reflect.declaration.ModifierKind.PUBLIC;
import static spoon.reflect.declaration.ModifierKind.STATIC;

/**
 * Created by marodrig on 24/03/2016.
 */
public class AJMHPrettyPrinter extends DefaultJavaPrettyPrinter {


    private boolean printingMethods = false;

    /**
     * Creates a new code generator visitor.
     *
     * @param env
     */
    public AJMHPrettyPrinter(Environment env) {
        super(env);
    }

    public AJMHPrettyPrinter(BenchSnippet snippet) {
        super(snippet.getASTElement().getFactory().getEnvironment());
    }


    public <T> void visitCtTargetedAccess(CtTargetedAccess<T> targetedAccess) {
        visitCtVariableAccess(targetedAccess);
        /*
        enterCtExpression(targetedAccess);
        if (targetedAccess.getTarget() != null) {
            scan(targetedAccess.getTarget());
            write(".");
            context.ignoreStaticAccess = true;
        }
        context.ignoreGenerics = true;
        scan(targetedAccess.getVariable());

        context.ignoreGenerics = false;
        context.ignoreStaticAccess = false;
        exitCtExpression(targetedAccess);*/
    }

    @Override
    public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
        enterCtExpression(thisAccess);
        if (thisAccess.isQualified() && thisAccess.isImplicit()) {
            throw new RuntimeException("inconsistent this definition");
        }
        if (thisAccess.isQualified()) {
            visitCtTypeReferenceWithoutGenerics(thisAccess.getType());
            write(".");
        }
        if (!thisAccess.isImplicit()) {
            write("THIZ");
            //write("this");
        }
        exitCtExpression(thisAccess);
    }


    public <T> void visitCtVariableAccess(CtVariableAccess<T> variableAccess) {
        try {
            boolean isField = variableAccess instanceof CtFieldAccess;
            CtVariable var = null;
            CtFieldAccess access = null;
            if (isField) {

                access = (CtFieldAccess) variableAccess;
                var = access.getVariable().getDeclaration();

                //CONSTANT OR NOT CONSTANT
                if (access.getVariable().isStatic()) {

                    //PUBLIC OR PRIVATE CONSTANT
                    if (var == null || var.getModifiers().contains(PUBLIC)) {
                        write(access.getVariable().toString());
                    } else {
                        enterCtExpression(variableAccess);
                        write(access.getParent(CtClass.class).getQualifiedName().replace(".", "_"));
                        write("_");
                        write(variableAccess.getVariable().getSimpleName());
                        exitCtExpression(variableAccess);
                    }
                } else {
                    //NOT CONSTANT:

                    //PUBLIC OR PRIVATE FIELD
                    if (var == null || var.getModifiers().contains(PUBLIC)) {
                        if (isTargetAllowed(access) || printingMethods) { // Target can be stored
                            //TARGET THIZ OR NOT
                            if (fieldTargetIsThis(access)) {
                                enterCtExpression(variableAccess);
                                write("THIZ.");
                                write(variableAccess.getVariable().getSimpleName());
                                exitCtExpression(variableAccess);
                            } else {
                                enterCtExpression(variableAccess);
                                scan(access.getTarget());
                                write(".");
                                write(variableAccess.getVariable().getSimpleName());
                                exitCtExpression(variableAccess);
                            }
                        } else {
                            enterCtExpression(variableAccess);
                            scan(access.getTarget());
                            write("_");
                            write(variableAccess.getVariable().getSimpleName());
                            exitCtExpression(variableAccess);
                        }
                    } else {
                        if (fieldTargetIsThis(access)) {
                            enterCtExpression(variableAccess);
                            write("THIZ_");
                            write(variableAccess.getVariable().getSimpleName());
                            exitCtExpression(variableAccess);
                        } else {
                            //TODO: This may fail if the target is not a variable.
                            enterCtExpression(variableAccess);
                            scan(access.getTarget());
                            write("_");
                            write(variableAccess.getVariable().getSimpleName());
                            exitCtExpression(variableAccess);
                        }
                    }
                }
            } else {
                super.visitCtVariableAccess(variableAccess);
            }

        } catch (NullPointerException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> annotation) {
        if ( !annotation.getAnnotationType().getQualifiedName().equals("java.lang.Override") )
            super.visitCtAnnotation(annotation);
    }

    /**
     * Prints the method modifieds including the THIZ parameter
     */
    public <T> void visitCtMethod(CtMethod<T> m) {
        boolean isStatic = m.getModifiers().contains(STATIC);
        printingMethods = true;

        visitCtNamedElement(m);
        writeGenericsParameter(m.getFormalTypeParameters());
        scan(m.getType());
        write(" ");
        if (isStatic) {
            if (visibility(m) != ModifierKind.PUBLIC) {
                write(m.getDeclaringType().getQualifiedName().replace(".", "_"));
                write("_");
            } else write(m.getDeclaringType().getQualifiedName());
        }
        write(m.getSimpleName());
        write("(");
        if (!isStatic && new Preconditions().checkTypeRef(m.getDeclaringType().getReference())) {
            write(m.getDeclaringType().getQualifiedName());
            write(" THIZ");
            if (m.getParameters().size() > 0) write(",");
        }
        writeExecutableParameters(m);
        write(")");
        writeThrowsClause(m);
        if (m.getBody() != null) {
            write(" ");
            scan(m.getBody());
            /*
            if (m.getBody().getPosition() != null) {
                if (m.getBody().getPosition().getCompilationUnit() == sourceCompilationUnit) {
                    if (m.getBody().getStatements().isEmpty()
                            || !(m.getBody()
                            .getStatements()
                            .get(m.getBody().getStatements().size() - 1) instanceof CtReturn)) {
                        lineNumberMapping.put(line, m.getBody().getPosition()
                                .getEndLine());
                    }
                } else {
                    undefLine(line);
                }
            } else {
                undefLine(line);
            }*/
        } else {
            write(";");
        }
        printingMethods = false;
    }


    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
        /*
        if (!(invocation instanceof CtInvocationDecorator)) {
            super.visitCtInvocation(invocation);
            return;
        }*/

        enterCtStatement(invocation);
        enterCtExpression(invocation);
//BCUTAG ???
//		if (invocation.getExecutable() ==null || invocation.getExecutable().getSimpleName() == null){
//			exitCtExpression(invocation);
//			return;
//		}
        String target = null;
        ModifierKind modifier = visibility(invocation);

        if (invocation.getExecutable().getSimpleName().equals("<init>")) {
            // It's a constructor (super or this)
            try {
                CtType<?> parentType = invocation.getParent(CtType.class);
                if ((parentType != null)
                        && (parentType.getQualifiedName() != null)
                        && parentType.getQualifiedName().equals(
                        invocation.getExecutable().getDeclaringType()
                                .getQualifiedName())) {
                    write("this");
                } else {
                    write("super");
                }
            } catch (Exception e) {
                Launcher.logger.error(e.getMessage(), e);
            }
        } else {
            // It's a method invocation

            if (invocation.getExecutable().isStatic()) {
                try {
                    CtTypeReference<?> type = invocation.getExecutable().getDeclaringType();
                    if (visibility(invocation) != ModifierKind.PUBLIC) {
                        write(type.toString().replace(".", "_"));
                        write("_");
                    } else {
                        write(type.toString());
                        write(".");
                    }
                } catch (Exception e) {
                    Launcher.logger.error(e.getMessage(), e);
                }
            } else if (invocation.getTarget() != null) {
                target = invocation.getTarget().toString();
            } else if (invocation.getGenericTypes() != null && invocation.getGenericTypes().size() > 0) {
                target = "THIZ";
            } else if ( isTargetAllowed(invocation) ) {
                target = "THIZ";
            }

            boolean removeLastChar = false;
            if (invocation.getGenericTypes() != null
                    && invocation.getGenericTypes().size() > 0) {
                write("<");
                for (CtTypeReference<?> ref : invocation.getGenericTypes()) {
                    //context.isInvocation = true;
                    write(ref.getQualifiedName());
                    //context.isInvocation = false;
                    write(",");
                    removeLastChar = true;
                }
                if (removeLastChar) removeLastChar();
                write(">");
            }
            if (modifier == PUBLIC && !invocation.getExecutable().isStatic()) {
                if (MethodAttributes.invocationTargetIsThis(invocation))
                    write("THIZ.");
                else {
                    scan(invocation.getTarget());
                    write(".");
                }
            }
            write(invocation.getExecutable().getSimpleName());
        }

        if (target != null && visibility(invocation) != PUBLIC && !invocation.getExecutable().isStatic()) {
            write("(");
            write(target);
            for (CtExpression<?> e : invocation.getArguments()) {
                write(", ");
                scan(e);
            }
        } else {
            write("(");
            if (invocation.getArguments().size() > 0) scan(invocation.getArguments().get(0));
            for (int i = 1; i < invocation.getArguments().size(); i++) {
                write(",");
                scan(invocation.getArguments().get(i));
            }
        }

        write(")");
        exitCtExpression(invocation);
    }
}

package fr.inria.autojmh.generators.printer;

import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtInvocationDecorator;
import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtVariableAccessDecorator;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.fieldTargetIsThis;
import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.visibility;
import static fr.inria.autojmh.snippets.modelattrib.TypeAttributes.isSerializable;
import static spoon.reflect.declaration.ModifierKind.PUBLIC;
import static spoon.reflect.declaration.ModifierKind.STATIC;

/**
 * Created by marodrig on 24/03/2016.
 */
public class AJMHPrettyPrinter extends DefaultJavaPrettyPrinter {

    /**
     * Creates a new code generator visitor.
     *
     * @param env
     */
    public AJMHPrettyPrinter(Environment env) {
        super(env);
    }

    public <T> void visitCtVariableAccess(CtVariableAccess<T> variableAccess) {
        try {
            boolean isField = variableAccess instanceof CtFieldAccess ||
                    (variableAccess instanceof CtVariableAccessDecorator &&
                            ((CtVariableAccessDecorator) variableAccess).isField());
            CtVariable var = null;
            CtFieldAccess access = null;
            if (isField) {

                access = (CtFieldAccess) (variableAccess instanceof CtVariableAccessDecorator ?
                        ((CtVariableAccessDecorator) variableAccess).getWrap() : variableAccess);
                var = access.getVariable().getDeclaration();

                //CONSTANT OR NOT CONSTANT
                if (access.getVariable().isStatic()) {

                    //PUBLIC OR PRIVATE CONSTANT
                    if (var == null || var.getModifiers().contains(PUBLIC)) {
                        super.visitCtVariableAccess(variableAccess);
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
                        if (isSerializable(access.getType())) {
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

    /**
     * Prints the method modifieds including the THIZ parameter
     */
    public <T> void visitCtMethod(CtMethod<T> m) {
        boolean isStatic = m.getModifiers().contains(STATIC);

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
        if (!isStatic) {
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
    }


    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
        if (!(invocation instanceof CtInvocationDecorator)) {
            super.visitCtInvocation(invocation);
            return;
        }

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
                    if ( visibility(invocation) != ModifierKind.PUBLIC ) {
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
            } else {
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
            if (modifier == PUBLIC && !invocation.getExecutable().isStatic())
                write("THIZ.");
            write(invocation.getExecutable().getSimpleName());
        }

        if (visibility(invocation) != PUBLIC && !invocation.getExecutable().isStatic()) {
            write("(");
            if (target != null) write(target);
            for (CtExpression<?> e : invocation.getArguments()) {
                write(", ");
                scan(e);
            }
        } else {
            write("(");
            if (invocation.getArguments().size() > 0) scan(invocation.getArguments().get(0));
            for (int i = 1; i < invocation.getArguments().size(); i++) {
                write(",");
                scan(invocation.getArguments().get(0));
            }
        }

        write(")");
        exitCtExpression(invocation);
    }
}

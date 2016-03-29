package fr.inria.autojmh.generators.printer;

import fr.inria.autojmh.generators.microbenchmark.parts.substitutes.CtInvocationDecorator;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

import static fr.inria.autojmh.snippets.modelattrib.MethodAttributes.visibility;

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

    /**
     * Prints the method modifieds including the THIZ parameter
     */
    public <T> void visitCtMethod(CtMethod<T> m) {
        if (m.getModifiers().contains(ModifierKind.STATIC)) {
            super.visitCtMethod(m);
            return;
        }

        visitCtNamedElement(m);
        writeGenericsParameter(m.getFormalTypeParameters());
        scan(m.getType());
        write(" ");
        write(m.getSimpleName());
        write("(");
        write(m.getDeclaringType().getQualifiedName());
        write(" THIZ");
        if (m.getParameters().size() > 0) write(",");
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
        if (!(invocation instanceof CtInvocationDecorator) || invocation.getExecutable().isStatic()) {
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
                    String typeName = type.toString().replace(".", "_");
                    write(typeName);
                    write("_");
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
            if (modifier == ModifierKind.PUBLIC) write("THIZ.");
            write(invocation.getExecutable().getSimpleName());
        }

        if (visibility(invocation) != ModifierKind.PUBLIC) {
            write("(");
            if (target != null) write(target);
            for (CtExpression<?> e : invocation.getArguments()) {
                write(", ");
                scan(e);
            }
        } else {
            write("(");
            if ( invocation.getArguments().size() > 0 ) scan(invocation.getArguments().get(0));
            for (int i = 1; i < invocation.getArguments().size(); i++) {
                write(",");
                scan(invocation.getArguments().get(0));
            }
        }

        write(")");
        exitCtExpression(invocation);
    }
}

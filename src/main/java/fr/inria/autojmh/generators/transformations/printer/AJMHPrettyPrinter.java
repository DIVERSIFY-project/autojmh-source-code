package fr.inria.autojmh.generators.transformations.printer;

import fr.inria.autojmh.generators.transformations.substitutes.CtInvocationDecorator;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

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


    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
        if (!(invocation instanceof CtInvocationDecorator)) super.visitCtInvocation(invocation);

        enterCtStatement(invocation);
        enterCtExpression(invocation);
//BCUTAG ???
//		if (invocation.getExecutable() ==null || invocation.getExecutable().getSimpleName() == null){
//			exitCtExpression(invocation);
//			return;
//		}
        String target = null;
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
                target = "this";
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
            // TODO: this does not work because the invocation does not have the
            // right line number
            /*if (env.isPreserveLineNumbers()) {
                adjustPosition(invocation);
            }*/
            write(invocation.getExecutable().getSimpleName());
        }
        write("(");
        if (target != null) write(target);

        boolean remove = false;
        for (CtExpression<?> e : invocation.getArguments()) {
            write(", ");
            scan(e);
        }
        write(")");
        exitCtExpression(invocation);
    }
}

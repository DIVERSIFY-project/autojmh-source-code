package fr.inria.autojmh.snippets;

import fr.inria.autojmh.snippets.modelattrib.TypeAttributes;
import fr.inria.autojmh.snippets.modelattrib.VariableAccessAttributes;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import static fr.inria.autojmh.snippets.modelattrib.VariableAccessAttributes.isAConstant;
import static fr.inria.autojmh.snippets.modelattrib.VariableAccessAttributes.visibility;
import static spoon.reflect.declaration.ModifierKind.PUBLIC;

/**
 * Class holding some meta data of a variable used in a statement needed to generate the benchmark.
 * <p/>
 * Created by marodrig on 29/09/2015.
 */
public class TemplateInputVariable {

    /**
     * Name of the variable being wrapped
     */
    private String variableName;

    /**
     * Method that will load the recorded value from file (loadInteger, loadFloat, etc.)
     */
    private String loadMethodName;

    /**
     * Name of the method that will save the value to file
     */
    private String logMethodName;

    /**
     * Name of the type of the variable
     */
    private String variableTypeName;

    /**
     * Indicates if it initialized before the statement
     */
    private boolean initialized;

    /**
     * Indicates if it is an array
     */
    private boolean isArray;

    /**
     * Actual variable being wrapped
     */
    private CtVariableAccess variableAccess;

    private boolean isCollection;

    private boolean isSerializable;

    /**
     * Logging signature of the variable when logged in the instrumented run
     */
    private String loggingSignature;

    /**
     * Fully qualified name of the package containing the variable
     */
    private String packageQualifiedName;

    private DefaultJavaPrettyPrinter printer;


    private void doInitialize(BenchSnippet parent, CtTypeReference typeRef) {

        TypeAttributes refAttr = new TypeAttributes(typeRef);

        if ( typeRef instanceof CtArrayTypeReference) {
            CtArrayTypeReference ref = (CtArrayTypeReference) typeRef;

            int arrayDimentions = 1;
            while (ref.getComponentType() instanceof CtArrayTypeReference) {
                ref = (CtArrayTypeReference) ref.getComponentType();
                arrayDimentions++;
            }

            String methodName = "Array" + arrayDimentions;
            if (arrayDimentions == 1 && !ref.getComponentType().isPrimitive()
                    && new TypeAttributes(ref.getComponentType()).isSerializable())
                //So far 1 array dimention of serializables is supported
                methodName += "Serializable";
            else methodName += ref.getComponentType().getSimpleName();
            setLoadMethodName(methodName);
            logMethodName = methodName;
            this.setIsArray(true);
            this.setIsCollection(false);
        } else if (refAttr.isCollection()) {
            //this.setSerializable(refAttr.isSerializableCollection());
            if (refAttr.isSerializableCollection()) {
                this.setLoadMethodName("Serializable" + typeRef.getSimpleName());
                logMethodName = "SerializableCollection";
            }
            else {
                CtTypeReference ref = typeRef;
                ref = ref.getActualTypeArguments().get(0);
                setLoadMethodName(ref.getSimpleName() + typeRef.getSimpleName());
                //setLoadMethodName(ref.getSimpleName() + "Collection");
                logMethodName = ref.getSimpleName() + "Collection";
            }
            this.setSerializable(refAttr.isSerializable());
            this.setIsCollection(true);
            this.setIsArray(false);

        } else {
            this.setIsArray(false);
            this.setIsCollection(false);
            if (refAttr.isSerializable()) this.setLoadMethodName("Serializable");
            else this.setLoadMethodName(typeRef.toString());
            logMethodName = getLoadMethodName();
        }

        SourcePosition pos = parent.getASTElement().getPosition();
        loggingSignature = pos.getCompilationUnit().getMainType().getQualifiedName().replace(".", "-")
                + "-" + pos.getLine() + "-" + getVariableName();
        if ( typeRef.isPrimitive() ) packageQualifiedName = "java.lang." + typeRef.getQualifiedName();
        else packageQualifiedName = typeRef.getQualifiedName();
    }

    /**
     * Initializes the Template Input Variable
     */
    void initialize(BenchSnippet parent, CtVariableAccess access) {
        this.setInitialized(parent.getInitialized().contains(access));
        this.setVariableAccess(access);
        doInitialize(parent, access.getVariable().getType());
    }

    void initializeAsThiz(BenchSnippet parent) {
        setInitialized(true);
        setVariableAccess(null);
        setVariableName("this");
        try {
            CtTypeReference typeRef = parent.getASTElement().getParent(CtClass.class).getReference();
            setVariableTypeName(typeRef.getSimpleName());
            setIsArray(typeRef instanceof CtArrayTypeReference);
            this.setVariableTypeName(typeRef.getSimpleName());
            doInitialize(parent, typeRef);
        } catch (NullPointerException ex) {
            throw new RuntimeException("Unable to obtain the type name for " + parent);
        }
    }

    public String getLogMethodName() {
        return logMethodName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
        if (this.variableName.contains("."))
            this.variableName = getInstrumentedCodeCompilableName();
    }

    public void setLoadMethodName(String loadMethodName) {
        this.loadMethodName = loadMethodName;
    }

    public String getLoadMethodName() {
        return loadMethodName;
    }

    public void setVariableTypeName(String variableType) {
        this.variableTypeName = variableType;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableTypeName() {
        return variableTypeName;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    public boolean getIsArray() {
        return isArray;
    }

    public void setVariableAccess(CtVariableAccess variableAccess) {
        this.variableAccess = variableAccess;
        if ( variableAccess != null ) {
            setVariableName(getCompilableName(variableAccess, '_'));
            setVariableTypeName(variableAccess.getVariable().getType().toString());
            setIsArray(variableAccess.getVariable().getType() instanceof CtArrayTypeReference);
        }
        //TODO: set other properties
    }

    /**
     * Indicates if the variable is a private constant
     */
    public boolean getIsPrivateConstant() {
        return isAConstant(variableAccess) && (visibility(variableAccess) != PUBLIC);
    }

    /**
     * Indicates if the variable is a private constant
     */
    public boolean getIsPublicConstant() {
        return isAConstant(variableAccess) && (visibility(variableAccess) == PUBLIC);
    }

    /**
     * If the wrapper is wrapping a constant, tell the value
     * @return A string with the value
     */
    public String getConstantValue() {
        if ( variableAccess != null ) {
            printer.reset();
            printer.scan(variableAccess.getVariable().getDeclaration().getDefaultExpression());
            return printer.toString();
        } return "";
    }


    /**
     * Gets a name for a variable access that can be placed in the code of the instrumented code and compiled.
     */
    public String getInstrumentedCodeCompilableName() {
        String result = "";
        if ( variableAccess != null )
            result = getCompilableName(variableAccess, '.');
        else result = getVariableName();
        if ( result.equals("THIZ") ) result = "this";
        return result;
    }

    /**
     * Gets a name for a variable access that can be placed in the microbenchmark code.
     */
    public String getTemplateCodeCompilableName() {
        if ( variableAccess != null ) {
            printer.reset();
            printer.scan(variableAccess);
            String result = printer.toString().replaceAll("\\(|\\)","");
            if ( result.contains(".") )
                throw new IllegalArgumentException("Invalid compilable name");
            return result;
            //return getCompilableName(variableAccess, '_');
        }
        else return "THIZ";
    }

    /**
     * Gets a name for a variable access that can be placed in the code of the template and compiled.
     *
     * @return
     */
    public String getLoggingSignature() {
        return loggingSignature;
    }

    /**
     * Gets a name for a variable access that can be placed in the code and compiled.
     * <p/>
     * For example myOject.myVar's name is "myVar". However, this will not compile
     * compiler, being myObject_myVar needed
     *
     * @param access Access who's compilable name is required
     * @return
     */
    public static String getCompilableName(CtVariableAccess access, char sep) {
        /*
        if (access.getPosition().getCompilationUnit().getMainType().getQualifiedName().equals("mikera.vectorz.impl.JoinedArrayVector")
                && access.getVariable().getSimpleName().equals("data") ) {
            System.out.println();
        }*/

        if (access instanceof CtFieldAccess) {
            StringBuilder sb = new StringBuilder();
            CtFieldAccess field = (CtFieldAccess) access;
            if (field.getTarget() instanceof CtVariableAccess) {
                sb.append(getCompilableName((CtVariableAccess) field.getTarget(), sep)).append(sep);
            } else if (field.getTarget() instanceof CtThisAccess ) {
                sb.append("this").append(sep);
            }
            if (sep != '.')
                sb.append(field.getVariable().toString().replace(".", String.valueOf(sep)));
            else
                sb.append(field.getVariable().toString());
            return sb.toString();
        }
        return access.getVariable().toString();
    }

    public boolean getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(boolean collection) {
        this.isCollection = collection;
    }

    public boolean getIsSerializable() {
        return isSerializable;
    }

    public boolean isSerializable() {
        return isSerializable;
    }

    public void setSerializable(boolean isSerializable) {
        this.isSerializable = isSerializable;
    }


    public String getPackageQualifiedName() {
        return packageQualifiedName;
    }

    public void setPackageQualifiedName(String packageQualifiedName) {
        this.packageQualifiedName = packageQualifiedName;
    }

    public DefaultJavaPrettyPrinter getPrinter() {
        return printer;
    }

    public void setPrinter(DefaultJavaPrettyPrinter printer) {
        this.printer = printer;
    }
}

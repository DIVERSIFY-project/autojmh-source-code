package fr.inria.autojmh.snippets;

import org.apache.log4j.Logger;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to reflect some important high level attributes of types
 * <p>
 * Created by marodrig on 22/03/2016.
 */
public class TypeAttributes {

    private static Logger log = Logger.getLogger(Preconditions.class);

    public CtTypeReference ref;

    private Boolean isClassPrimitive;
    private Boolean isCollection;
    private Boolean isSerializable;

    public TypeAttributes(CtTypeReference ref) {
        this.ref = ref;
    }

    public boolean isClassPrimitive() {
        if (isClassPrimitive == null) {
            isClassPrimitive = ref.getQualifiedName().equals("java.lang.Byte") ||
                    ref.getQualifiedName().equals("java.lang.Boolean") ||
                    ref.getQualifiedName().equals("java.lang.Character") ||
                    ref.getQualifiedName().equals("java.lang.Double") ||
                    ref.getQualifiedName().equals("java.lang.Float") ||
                    ref.getQualifiedName().equals("java.lang.Integer") ||
                    ref.getQualifiedName().equals("java.lang.Long") ||
                    ref.getQualifiedName().equals("java.lang.Number") ||
                    ref.getQualifiedName().equals("java.lang.Short") ||
                    ref.getQualifiedName().equals("java.lang.String");
        }
        return isClassPrimitive;
    }

    public boolean isCollection() {
        if (isCollection == null) isCollection = isCollection(ref);
        return isCollection;
    }

    /**
     * Indicates if the type is a collection
     *
     * @param ref
     * @return
     */
    private boolean isCollection(CtTypeReference ref) {
        try {
            Set<CtTypeReference> refs = ref.getSuperInterfaces();
            if (refs == null) return false;
            for (CtTypeReference r : refs)
                if (r.getQualifiedName().equals("java.util.Collection") || isCollection(r))
                    return true;
            return false;
        } catch (Exception ex) {
            log.warn("Unexpected exception " + ex.getMessage());
            return false;
        }
    }

    //public CtTypeRefere

    public boolean isSerializable() {
        if (isSerializable != null) return isSerializable;
        else isSerializable = isSerializable(ref);
        return isSerializable;
    }

    /**
     * Indicates if the type is serializable
     *
     * @param componentType
     * @return
     */
    private boolean isSerializable(CtTypeReference componentType) {
        try {
            HashSet<CtTypeReference> refs = new HashSet<>();
            if (componentType.getSuperInterfaces() != null) refs.addAll(componentType.getSuperInterfaces());
            CtTypeReference superRef = componentType.getSuperclass();
            if (superRef != null) refs.add(superRef);
            if (refs.size() == 0) return false;
            for (CtTypeReference ref : refs) {
                if (ref.getQualifiedName().equals("java.io.Serializable") || isSerializable(ref))
                    return true;
            }
            return false;
        } catch (Exception ex) {
            log.warn("Unexpected exception " + ex);
            return false;
        }
    }

}

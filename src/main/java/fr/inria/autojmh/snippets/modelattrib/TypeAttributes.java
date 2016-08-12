package fr.inria.autojmh.snippets.modelattrib;

import fr.inria.autojmh.snippets.Preconditions;
import org.apache.log4j.Logger;
import spoon.reflect.reference.CtArrayTypeReference;
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

    public TypeAttributes(Boolean isClassPrimitive, Boolean isCollection, Boolean isSerializable) {
        this.isClassPrimitive = isClassPrimitive;
        this.isCollection = isCollection;
        this.isSerializable = isSerializable;
    }

    public TypeAttributes(CtTypeReference ref) {
        this.ref = ref;
    }

    public boolean isClassPrimitive() {
        if (isClassPrimitive == null) isClassPrimitive = isClassPrimitive(ref);
        return isClassPrimitive;
    }

    /**
     * Indicates whether the type is a class primitive (Byte, Double, String)
     */
    public static boolean isClassPrimitive(CtTypeReference ref) {
        return ref.getQualifiedName().equals("java.lang.Byte") ||
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
    public static boolean isCollection(CtTypeReference ref) {
        try {
            if (ref instanceof CtArrayTypeReference) return true;
            Set<CtTypeReference<?>> refs = ref.getSuperInterfaces();
            if (refs == null) return false;
            if ( ref.getQualifiedName().equals("java.util.Collection") ) return true;
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
    public static boolean isSerializable(CtTypeReference componentType) {
        try {
            HashSet<CtTypeReference> refs = new HashSet<>();
            if (componentType.getSuperInterfaces() != null)
                refs.addAll(componentType.getSuperInterfaces());
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

    Boolean isSerializableCollection = null;

    public boolean isSerializableCollection() {
        if (isSerializableCollection == null)
            isSerializableCollection = isSerializableCollection(ref);
        return isSerializableCollection;
    }

    public static boolean isSerializableCollection(CtTypeReference type) {
        if ( isCollection(type) && type.getActualTypeArguments().size() > 0 ) {
            for ( CtTypeReference ref : type.getActualTypeArguments() )
                if ( !isSerializable(ref) || isClassPrimitive(ref) ) return false;
        }
        return true;
    }
}

package com.nitorcreations.scanners;

import com.nitorcreations.domain.Edge;
import com.nitorcreations.domain.EdgeType;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.nitorcreations.scanners.EdgeOperations.createEdge;
import static com.nitorcreations.scanners.EdgeOperations.mergeBiDirectionals;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class FieldScanner extends AbstractScanner {

    private static final String NAME_FOR_INNERCLASS = null;
    private static final String innerClassFieldReferenceInBytecode = "this$0";

    private final Logger logger = LoggerFactory.getLogger(FieldScanner.class);

    public FieldScanner(final List<Class<?>> classes) {
        super(classes);
    }

    public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<>();
        for (Class<?> clazz : classes) {
            edges.addAll(extractFieldEdges(clazz));
        }
        return mergeBiDirectionals(edges);
    }

    private List<Edge> extractFieldEdges(Class<?> clazz) {
        List<Edge> fieldEdges = new ArrayList<>();
        try {
            InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace(".", "/") + ".class");
            ClassReader reader = new ClassReader(is);
            reader.accept(new ClassVisitor(Opcodes.ASM4) {
                @Override
                public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                    try {
                        Optional<Edge> fieldEdge = createFieldEdge(clazz, clazz.getDeclaredField(name));
                        if (fieldEdge.isPresent()) {
                            fieldEdges.add(fieldEdge.get());
                        }
                    } catch (NoSuchFieldException e) {
                        // should never happen
                    } catch (NoClassDefFoundError e) {
                        logger.warn("Skipped field " + name + " in class " + clazz.getName() + " because it's type class is not available. Field description: " + desc);
                    }
                    return super.visitField(access, name, desc, signature, value);
                }
            }, ClassReader.SKIP_CODE);
        } catch (IOException e) {
            logger.warn("Failed to read bytecode for class " + clazz.getName(), e);
        }
        return fieldEdges;
    }

    private Optional<Edge> createFieldEdge(Class<?> clazz, Field field) {
        if (isDomainClass(field.getType())) {
            if (innerClassFieldReferenceInBytecode.equals(field.getName())) {
                return of(createEdge(clazz, (Class) field.getType(), EdgeType.INNER_CLASS, NAME_FOR_INNERCLASS));
            }
            return of(createEdge(clazz, (Class) field.getType(), EdgeType.ONE_TO_ONE, field.getName()));
        }
        if (isCollection(field)) {
            Optional<Class<?>> classInCollection = getDomainClassFromCollection(field);
            if (classInCollection.isPresent() && isDomainClass(classInCollection.get())) {
                return of(createEdge(clazz, classInCollection.get(), EdgeType.ONE_TO_MANY, field.getName()));
            }
        }
        return empty();
    }

    private Optional<Class<?>> getDomainClassFromCollection(final Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            for (Type t : pt.getActualTypeArguments()) {
                if (isDomainClass(t.toString())) {
                    return Optional.of((Class) t);
                }
            }
        }
        return empty();
    }

    private boolean isCollection(final Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }
}

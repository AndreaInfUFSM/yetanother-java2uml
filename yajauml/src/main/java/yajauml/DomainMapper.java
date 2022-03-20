package yajauml;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import yajauml.domain.DomainClass;
import yajauml.domain.DomainClassType;
import yajauml.domain.DomainConstructor;
import yajauml.domain.DomainField;
import yajauml.domain.DomainMethod;
import yajauml.domain.Edge;
import yajauml.domain.EdgeType;
import yajauml.domain.Visibility;
import yajauml.presenters.Presenter;
import yajauml.presenters.Representation;


public class DomainMapper {

  private List<DomainClass> classes;
  private Map<String, List<RelationNode>> relations; 
  private List<Edge> edges; 
  private final Presenter presenter;

  static class RelationNode {

    public static enum RelationType {
      IMPLEMENTS, EXTENDS, HAS, NESTED
    }
  
    private String name;
    private RelationType type;
  
    RelationNode(String name, RelationType type) {
      this.name = name;
      this.type = type;
    }
  
    public String getName() {
      return name;
    }
  
    public RelationType getType() {
      return type;
    }
  }

  DomainMapper(Presenter presenter) {
    this.presenter = presenter;
    this.classes = new ArrayList<DomainClass>();
    this.relations = new HashMap<String, List<RelationNode>>();
    this.edges = new ArrayList<Edge>();
  }

  /**
   * create a DommainMapper to parse classes in a directory 
   * @return DomainMapper
   * @throws IOException
   */
  public static DomainMapper create(Presenter presenter, String directory, List<String> ignores) throws IOException {
    DomainMapper result = new DomainMapper(presenter);
    result.parseClasses(directory, ignores);
    return result;
  }  

  /**
   * get a representation from this domain using a presenter
   * @return Representation type
   */
  public Representation describeDomain() { 

    Representation representation = presenter.describe(this.classes, this.edges);
    return representation;
    
  }

  private String extractParams(String declaration) {
    Pattern pat = Pattern.compile("\\(([^)]*)\\)");
    Matcher mat = pat.matcher(declaration);
    String params = mat.find() ? mat.group() : "";
    return params;
  }
 

//   public static ResolvedReferenceTypeDeclaration toTypeDeclaration(Node node, TypeSolver typeSolver) {
//     if (node instanceof ClassOrInterfaceDeclaration) {
//         if (((ClassOrInterfaceDeclaration) node).isInterface()) {
//             return new JavaParserInterfaceDeclaration((ClassOrInterfaceDeclaration) node, typeSolver);
//         } else {
//             return new JavaParserClassDeclaration((ClassOrInterfaceDeclaration) node, typeSolver);
//         }
//     } else if (node instanceof TypeParameter) {
//         return new JavaParserTypeParameter((TypeParameter) node, typeSolver);
//     } else if (node instanceof EnumDeclaration) {
//         return new JavaParserEnumDeclaration((EnumDeclaration) node, typeSolver);
//     } else if (node instanceof AnnotationDeclaration) {
//         return new JavaParserAnnotationDeclaration((AnnotationDeclaration) node, typeSolver);
//     } else {
//         throw new IllegalArgumentException(node.getClass().getCanonicalName());
//     }
// }

  // Please see the documentation on dealing with nested types in parseType
  private <P extends TypeDeclaration<?>> boolean isBottomLevel(BodyDeclaration<?> child, Class<P> parentType, TypeDeclaration<?> td) {
    
    if (parentType.isInstance(child.getParentNode().get())) {
      P parent = parentType.cast(child.getParentNode().get());
      if (parent.getName().toString().equals(td.getName().toString())) {
        return true;
      }
    } 
    return false;
  }

  private boolean isBottomLevel(EnumConstantDeclaration ed, TypeDeclaration<?> td) {
    // Check if the parent node of this EnumConstantDeclaration is the TypeDeclaration we are processing
    // (false when this EnumConstantDeclaration comes from a nested class of the TypeDeclaration)
    return isBottomLevel(ed, EnumDeclaration.class, td);
  }

  // TODO: Refactor redundant code below (isBottomLevel)
  private boolean isBottomLevel(FieldDeclaration fd, TypeDeclaration<?> td) {
    return isBottomLevel(fd, ClassOrInterfaceDeclaration.class, td) || isBottomLevel(fd, EnumDeclaration.class, td);
  }
  private boolean isBottomLevel(ConstructorDeclaration cd, TypeDeclaration<?> td) {
    return isBottomLevel(cd, ClassOrInterfaceDeclaration.class, td) || isBottomLevel(cd, EnumDeclaration.class, td);
  }
  private boolean isBottomLevel(MethodDeclaration md, TypeDeclaration<?> td) {
    return isBottomLevel(md, ClassOrInterfaceDeclaration.class, td) || isBottomLevel(md, EnumDeclaration.class, td);
  }

  // Drive all the parsing work for a given type declaration
  private void parseType(TypeDeclaration<?> td, CompilationUnit cunit) {

    String className = td.getName().toString();
    DomainClass aclass = new DomainClass(className);
    
    if (td instanceof EnumDeclaration) {
      aclass.setClassType(DomainClassType.ENUM);
      aclass.setAbstract(false);
      // EnumDeclaration ed = (EnumDeclaration) td;
      // System.err.println(ed.getEntries());
    } else {
      ClassOrInterfaceDeclaration cd = (ClassOrInterfaceDeclaration) td;
      aclass.setClassType(cd.isInterface() ? DomainClassType.INTERFACE : DomainClassType.CLASS);
      aclass.setAbstract(cd.isAbstract() ? true : false);
    }
    aclass.setStatic(td.isStatic() ? true : false);
    String packageName = cunit.getPackageDeclaration().isPresent() ? cunit.getPackageDeclaration().get().getName().toString() : "";
    aclass.setPackageName(packageName);
    aclass.setVisibility(mapVisibility(td.getAccessSpecifier().toString()));

    // Dealing with nested types
    // -------------------------
    // TypeDeclaration td comes from findAll(TypeDeclaration.class), so it may refer 
    // to a TypeDeclaration at any level of this CompilationUnit 
    // (not only top level types that getTypes would return)
    // In this case, when traversing nested types, nodes will be visited more than once, 
    // so we must prevent visitors to operate multiple times on a single node
    // Ideally, visitors must only operate at the bottom level of the TypeDeclaration td
    // We achieve this by comparing the names of the TypeDeclaration and the parent node
    // (supposing they are different, but this solution may be error-prone)
    td.accept(new VoidVisitorAdapter<Object>() {

      public void visit(EnumConstantDeclaration ed, Object arg) {               
        if (isBottomLevel(ed, td)) {
          DomainField field = new DomainField(ed.getName().toString());
          field.setVisibility(mapVisibility(AccessSpecifier.PUBLIC.toString()));
          field.setStatic(true);
          aclass.addDomainField(field);
          //addToRelations(className, fd.getElementType().toString(), RelationNode.RelationType.HAS);        
        }
      }
      public void visit(FieldDeclaration fd, Object arg) {       
        if (isBottomLevel(fd, td)) {
          String fieldName = 
            fd.getVariable(0).getName().toString() + " : " +
            fd.getElementType().toString();
          DomainField field = new DomainField(fieldName);
          field.setVisibility(mapVisibility(fd.getAccessSpecifier().toString()));
          field.setStatic(fd.isStatic());
          aclass.addDomainField(field);
          addToRelations(className, fd.getElementType().toString(), RelationNode.RelationType.HAS);
        }
      }
      public void visit(ConstructorDeclaration cd, Object arg) {
        if (isBottomLevel(cd, td)) {
          String params = extractParams(cd.getDeclarationAsString());
          DomainConstructor constructor = new DomainConstructor(cd.getName().toString() + params);
          constructor.setVisibility(mapVisibility(cd.getAccessSpecifier().toString()));
          aclass.addDomainConstructor(constructor);
        }
      }
      public void visit(MethodDeclaration md, Object arg) {
        if (isBottomLevel(md, td)) {                        
          String params = extractParams(md.getDeclarationAsString());
          String methodName = 
            md.getName().toString() + params + " : " + md.getType().toString();
            
          DomainMethod method = new DomainMethod(methodName);
          method.setVisibility(mapVisibility(md.getAccessSpecifier().toString()));
          method.setStatic(md.isStatic());
          method.setAbstract(md.isAbstract());
          aclass.addDomainMethod(method);
        }
      }
      // public void visit(ClassOrInterfaceDeclaration c, Object arg) {
      //   if (c.isInnerClass()) {
      //     System.err.println(c.getNameAsString());
      //     super.visit(c, arg);
      //   }
      // }
    }, null);
    classes.add(aclass);
    if (td.isNestedType()) {
      TypeDeclaration<?> p = (TypeDeclaration<?>) td.getParentNode().get();
      addToRelations(className, p.getName().toString(), RelationNode.RelationType.NESTED);
    }
    if (td instanceof ClassOrInterfaceDeclaration) {
      ClassOrInterfaceDeclaration cd = (ClassOrInterfaceDeclaration) td;
      addToRelations(className, cd.getImplementedTypes(), RelationNode.RelationType.IMPLEMENTS);
      addToRelations(className, cd.getExtendedTypes(), RelationNode.RelationType.EXTENDS);
    }
  }


  private void parseClasses(String directory, List<String> ignores) throws IOException {

    SourceRoot sourceRoot = new SourceRoot(Paths.get(directory));
    List<ParseResult<CompilationUnit>> results = sourceRoot.tryToParse();
    results.forEach(r -> {
      CompilationUnit cunit = r.getResult().get();
      //cu.findAll(CompilationUnit.class).
      cunit
        //.getTypes() // only top-level types, no inner classes
        .findAll(TypeDeclaration.class) // including inner classes
        .stream() // not a parallel stream!
        .filter(td -> td.isClassOrInterfaceDeclaration() || td.isEnumDeclaration())
        .filter(td -> !ignores.contains(td.getName().toString()))
        .forEach(td -> parseType((TypeDeclaration<?>) td, cunit)); // writes to collections, so no parallel stream here!
    });
    // class relationships
    for (var entry : relations.entrySet()) {
      if (entry.getValue().size() > 0) { // relationship
        for (var extended : entry.getValue()) {
          if (relations.containsKey(extended.getName())) {
            if (extended.getType() == RelationNode.RelationType.IMPLEMENTS) {
              edges.add(new Edge(entry.getKey(), extended.getName(), EdgeType.IMPLEMENTS));
            } else if (extended.getType() == RelationNode.RelationType.EXTENDS) {
              edges.add(new Edge(entry.getKey(), extended.getName(), EdgeType.EXTENDS));
            } else if (extended.getType() == RelationNode.RelationType.HAS) {
              edges.add(new Edge(entry.getKey(), extended.getName(), EdgeType.ONE_TO_ONE));
            } else if (extended.getType() == RelationNode.RelationType.NESTED) {
              edges.add(new Edge(entry.getKey(), extended.getName(), EdgeType.INNER_CLASS));
            }
          }
        }
      }
    }

  }

  private void addKeyIfNotExists(String name) {
    if (!relations.containsKey(name)) {
      relations.put(name, new ArrayList<RelationNode>());
    }
  }

  // Add class to relations map
  private void addToRelations(String className, 
      String elementType, 
      RelationNode.RelationType relation) {

    addKeyIfNotExists(className);
    relations
        .get(className)
        .add(new RelationNode(elementType, relation));

  }

  // Add extended or implemented types to relations map
  private void addToRelations(String className,
      NodeList<ClassOrInterfaceType> types,
      RelationNode.RelationType relation) {

    addKeyIfNotExists(className);
    for (var extended : types) {
      relations
          .get(className)
          .add(new RelationNode(extended.getName().toString(), relation));
    }
  }

  private Visibility mapVisibility(String fromParser) {

    if ("PUBLIC".equals(fromParser)) {
      return Visibility.PUBLIC;
    } else if ("PRIVATE".equals(fromParser)) {
      return Visibility.PRIVATE;
    } else if ("PROTECTED".equals(fromParser)) {
      return Visibility.PROTECTED;
    } else if ("PACKAGE_PRIVATE".equals(fromParser)) {
      return Visibility.DEFAULT;
    }
    return null;

  }  

}


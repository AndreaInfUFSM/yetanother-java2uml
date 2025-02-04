package yajauml.presenters;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yajauml.domain.DomainClass;
import yajauml.domain.Edge;
import yajauml.domain.EdgeType;
import yajauml.domain.Visibility;


/**
 * Created by moe on 06.04.16.
 *
 * <p>More info about syntax: http://de.plantuml.com/classes.html</p>
 */
public class PlantUmlPresenter implements Presenter {

  public static final String FILE_PREAMBLE = "@startuml";
  public static final String FILE_POSTAMBLE = "@enduml";

  private String describeInheritance(List<Edge> edges) {
    return edges.stream()
        .filter(e -> e.type == EdgeType.EXTENDS || e.type == EdgeType.IMPLEMENTS)
        .map(this::describeInheritance)
        .collect(joining());
  }

  private String describeInheritance(Edge hierarchyEdge) {
    String arrow = "--|>";
    if (hierarchyEdge.type == EdgeType.IMPLEMENTS) {
      // TODO: Check if it would be possible to implement what was commented out in this code 
      // from uml-reverse-mapper: 
      // "if target is an interface and source is not, it is officially called
      // realization and uses a dashed line"
      arrow = "..|>";
    }

    return String.format("%s %s %s \n",
        hierarchyEdge.source,
        arrow,
        hierarchyEdge.target);
  }

  private String describePackages(List<DomainClass> domainClasss) {
    //System.out.println(domainClasss.size());
    // domainClasss.stream().forEach(c -> System.out.println(c.getPackageName()));
    // return "";
    // domainClass.stream()
    //   .collect(groupingBy(DomainClass::getPackageName))
    //   .entrySet().stream()
    //   .map(this::describePackage)
    //   .collect(joining());

    return domainClasss.stream()
        .collect(groupingBy(DomainClass::getPackageName))
        .entrySet().stream()
        .map(this::describePackage)
        .collect(joining());
  }

  private String describePackage(Map.Entry<String, List<DomainClass>> entry) {
    return String.format("package %s {\n%s}\n",
        entry.getKey(),
        describeDomainClasses(entry.getValue()));
  }

  private String describeDomainClasses(List<DomainClass> domainClasses) {
    return domainClasses.stream()
        .map(this::describeDomainClass)
        .distinct()
        .collect(joining());
  }

  private String describeDomainClass(DomainClass domainClass) {
    return String.format("  %s {%s%s%s\n  }\n",
        describeDomainClassType(domainClass),
        describeDomainClassFields(domainClass),
        describeDomainClassConstructors(domainClass),
        describeDomainClassMethods(domainClass));
  }

  private String describeDomainClassType(DomainClass domainClass) {
    String visi = "";
    if (domainClass.getVisibility() != Visibility.PUBLIC) {
      visi = domainClass.getVisibility().toString();
    }
    String className = domainClass.getUmlName();
    switch (domainClass.getClassType()) {
      case CLASS:
        return (domainClass.isAbstract() ? "abstract " : "")
            + visi + "class " + className;
      case INTERFACE:
        return visi + "interface " + className;
      case ENUM:
        return visi + "enum " + className;
      // case ANNOTATION:
      //   return visi + "annotation " + className;
      default:
        break;
    }
    return className;
  }

  private String describeDomainClassFields(DomainClass domainClass) {
    String description = domainClass.getFields().stream()
        .map(f -> f.getVisibility() + " " + f.getUmlName()
            + (f.isStatic() ? " {static}" : "") + (f.isAbstract() ? " {abstract}" : ""))
        .collect(Collectors.joining("\n    "));
    return !description.equals("") ? "\n    " + description : "";
  }

  private String describeDomainClassConstructors(DomainClass domainClass) {
    String description = domainClass.getConstructors().stream()
        .map(c -> c.getVisibility() + " " + c.getUmlName())
        .collect(Collectors.joining("\n    "));
    return !description.equals("") ? "\n    " + description : "";
  }

  private String describeDomainClassMethods(DomainClass domainClass) {
    String description = domainClass.getMethods().stream()
        .map(m -> m.getVisibility() + " " + m.getUmlName()
            + (m.isStatic() ? " {static}" : "") + (m.isAbstract() ? " {abstract}" : ""))
        .collect(Collectors.joining("\n    "));
    return !description.equals("") ? "\n    " + description : "";
  }

  private String describeCompositions(List<Edge> edges) {
    return edges.stream()
        .filter(e -> e.type != EdgeType.EXTENDS && e.type != EdgeType.IMPLEMENTS)
        .map(this::describeComposition)
        .collect(joining());
  }

  private String describeComposition(Edge compositionEdge) {
    return String.format("%s\n", describeEdge(compositionEdge));
  }

  private String describeEdge(Edge edge) {
    String sourceName = edge.source;
    String targetName = edge.target;
    
    String arrow = "--";
    String arrowDescription = null;
    // Arrows pointing from Source to Target!
    switch (edge.type) {
      // // this comes from uml-reverse-mapper
      // case STATIC_INNER_CLASS: 
      //   arrow = "..+";
      //   break;
      case INNER_CLASS:
        arrow = "--+";
        break;
      default:
        arrow = "-->";
        break;
    }

    // why ???
    if (edge.source == null) {
      arrow = flip(arrow);
    }
    // } else {
    //   // targetName = " \"-" + edge.source + "\" " + targetName;
    //   targetName = " \"-" + edge.source + "\" " + targetName;
    // }

    return String.format("%s %s %s", sourceName, arrow, targetName)
        + (arrowDescription != null ? " : " + arrowDescription : "");
  }

  @Override
  public Representation describe(List<DomainClass> domainClasses, List<Edge> edges) {
    //this.domainClasses = domainClasses;
    String content = FILE_PREAMBLE + "\n"
        + describePackages(domainClasses)
        + describeCompositions(edges)
        + describeInheritance(edges)
        + FILE_POSTAMBLE;
    
    return new Representation(content, "puml");
  }

  @Override
  public String getFileEnding() {
    return "puml";
  }

  private static String flip(String s) {
    String reversedString = new StringBuilder(s).reverse().toString();
    return reversedString.replaceAll("<", ">").replaceAll(">", "<");
  }

}

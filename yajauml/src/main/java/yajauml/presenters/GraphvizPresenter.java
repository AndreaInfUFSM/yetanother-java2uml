package yajauml.presenters;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import yajauml.domain.DomainClass;
import yajauml.domain.Edge;
import yajauml.domain.EdgeType;
//import com.iluwatar.urm.domain.Edge;
//import com.iluwatar.urm.domain.EdgeType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class GraphvizPresenter implements Presenter {

  public static final String DOMAIN_DECLARATION = "digraph domain {\n";
  public static final String DEFAULTS = "  edge [ fontsize = 11 ];\n  node [ shape=record ];";
  private static final String INHERITANCE_STYLE = "arrowhead=empty style=dashed color=slategray";
  private final AtomicInteger count = new AtomicInteger();

  private Object getEdgeDescription(Edge edge) {
    StringBuilder sb = new StringBuilder();
    sb.append(" ").append(linkDirection(edge));
    return sb.toString();
  }

  private String linkDirection(Edge edge) {
    // TODO: Check if this code from uml-reverse-mapper applies to the current project
    // if (edge.source == null) {
    //   return "dir=forward arrowhead=odiamond color=slategray";
    // }
    // if (edge.target == null) {
    //   return "dir=back arrowtail=odiamond color=slategray";
    // }
    switch (edge.type) {
      //case STATIC_INNER_CLASS:
      case INNER_CLASS:
        return "dir=forward arrowhead=odot arrowtail=none color=slategray";
      default:
        return "dir=forward arrowhead=normal arrowtail=none color=slategray";
    }
  }

 



  private String describeInheritance(List<Edge> edges) {
    return edges.stream()
        .filter(e -> e.type == EdgeType.EXTENDS || e.type == EdgeType.IMPLEMENTS)
        .map(this::describeInheritance)
        .collect(joining());
  }

  private String describeInheritance(Edge hierarchyEdge) {
    String style = INHERITANCE_STYLE;
    if (hierarchyEdge.type == EdgeType.IMPLEMENTS) {
      // TODO: Check if it would be possible to implement what was commented out in this code 
      // from uml-reverse-mapper: 
      // "if target is an interface and source is not, it is officially called
      // realization and uses a dashed line"
      style = style + " style=dashed";
    }
    return String.format("  %s -> %s [%s];\n",
        hierarchyEdge.source,
        hierarchyEdge.target,
        style);
  }

  private String describePackages(List<DomainClass> domainObjects) {
    count.set(0);
    return domainObjects.stream()
        .collect(groupingBy(DomainClass::getPackageName))
        .entrySet().stream()
        .map(this::describePackage)
        .collect(joining());
  }

  private String describePackage(Map.Entry<String, List<DomainClass>> entry) {
    return String.format("  subgraph cluster_%s {\n    label = \"%s\";\n%s  }\n",
        count.getAndIncrement(),
        entry.getKey(),
        listDomainObjects(entry.getValue()));
  }

  private String listDomainObjects(List<DomainClass> domainObjects) {
    return domainObjects.stream()
        .map(domainObject -> describeDomainObject(domainObject))
        .distinct()
        .collect(joining());
  }

  private String describeDomainObject(DomainClass domainObject) {
    return String.format("    %s [ label = \"{%s | %s | %s}\" ] \n",
        domainObject.getClassName(), domainObject.getClassName(),
        describeDomainObjectFields(domainObject),
        describeDomainObjectMethods(domainObject));
  }

  // private String describeDomainClassFields(DomainClass domainClass) {
  //   String description = domainClass.getFields().stream()
  //       .map(f -> f.getVisibility() + " " + f.getUmlName()
  //           + (f.isStatic() ? " {static}" : "") + (f.isAbstract() ? " {abstract}" : ""))
  //       .collect(Collectors.joining("\n    "));
  //   return !description.equals("") ? "\n    " + description : "";
  // }

  private String describeDomainObjectFields(DomainClass domainObject) {
    StringBuilder sb = new StringBuilder();
    domainObject.getFields().stream().forEach((f) -> 
      sb.append(f.getVisibility() + " " + f.getUmlName() + "\\l"));
    return sb.toString();
  }

  private String describeDomainObjectMethods(DomainClass domainObject) {
    StringBuilder sb = new StringBuilder();
    domainObject.getMethods().stream().forEach((m) -> 
      sb.append(m.getVisibility() + " " + m + "\\l"));
    return sb.toString();
  }

  private String describeCompositions(List<Edge> edges) {
    return edges.stream()
        .filter(e -> e.type != EdgeType.EXTENDS && e.type != EdgeType.IMPLEMENTS)
        .map(this::describeComposition)
        .collect(joining());
  }

  private String describeComposition(Edge compositionEdge) {
    return String.format("  %s\n", describeEdge(compositionEdge));
  }

  private String describeEdge(Edge edge) {
    return String.format("%s -> %s [%s];", edge.source,
        edge.target, getEdgeDescription(edge));
  }

  @Override
  public Representation describe(List<DomainClass> domainObjects, List<Edge> edges) {
    String content = DOMAIN_DECLARATION + DEFAULTS + "\n"
        + describePackages(domainObjects)
        + describeCompositions(edges)
        + describeInheritance(edges)
        + "}";
    return new Representation(content, "dot");
  }

  @Override
  public String getFileEnding() {
    return "dot";
  }
}

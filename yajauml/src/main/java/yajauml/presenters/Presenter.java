package yajauml.presenters;

import yajauml.domain.DomainClass;
import yajauml.domain.Edge;
import java.util.List;

public interface Presenter {

  Representation describe(List<DomainClass> domainObjects, List<Edge> edges);
  String getFileEnding();

  /**
   * Factory method for {@link Presenter}.
   * @param presenterString as a String
   * @return chosen Presenter
   */
  static Presenter parse(String presenterString) {
    if (presenterString == null || presenterString.equalsIgnoreCase("plantuml")) {
      return new PlantUmlPresenter();
    } else if (presenterString.equalsIgnoreCase("graphviz")) {
       return new GraphvizPresenter();
     } else if (presenterString.equalsIgnoreCase("mermaid")) {
       return new MermaidPresenter();
    }
    return new PlantUmlPresenter();
  }
}

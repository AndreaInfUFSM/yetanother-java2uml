package yajauml.domain;

public class Edge {

  public final String source;
  public final String target;
  public final EdgeType type;
  
  public Edge(String source, String target, EdgeType type) {
    this.source = source;
    this.target = target;
    this.type = type;
  }

  // This code is from uml-reverse-mapper
  // It is temporarily commented out so we don't have to import Apache Commons
  // (so to keep this project as simple and small as we can)
  // TODO: Track this class to confirm we don't need to follow all best practices here :-)

  // @Override
  // public final int hashCode() {
  //   return HashCodeBuilder.reflectionHashCode(this);
  // }

  // @Override
  // public final boolean equals(Object obj) {
  //   return EqualsBuilder.reflectionEquals(this, obj);
  // }

  // @Override
  // public String toString() {
  //   return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  // }
}
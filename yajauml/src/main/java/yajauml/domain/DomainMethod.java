package yajauml.domain;

public class DomainMethod {

  private String method;
  private Visibility visibility;
  private boolean isStatic;
  private boolean isAbstract;

  public DomainMethod(String method) {
    this.method = method;
    this.isStatic = false;
    this.isAbstract = false;
  }

  public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
  }

  protected String getName() {
    return method;
  }

  public String getUmlName() {
   
    return method;
  }

  // public Class<?> getDeclaringClass() {
  //   return executable.getDeclaringClass();
  // }

  public Visibility getVisibility() {
    return visibility;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  public boolean isAbstract() {
    return isAbstract;
  }

  public void setAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
  }

  @Override
  public String toString() {
    return getUmlName();
  }
}
package yajauml.domain;

public class DomainField {
  private String field;
  private Visibility visibility;
  private boolean isStatic;
  private boolean isAbstract;

  public DomainField(String field) {
    this.field = field;
  }

  /**
   * get the name of the field.
   * @return
   */
  public String getUmlName() {
    // if (field.isEnumConstant()) {
    //   // If this is an enum constant, we dont need the type
    //   return field.getName();
    // }
    // return field.getName() + " : " + TypeUtils.getSimpleName(field.getGenericType());
    return field;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
  }
  // public DomainClass getType() {
  //   return new DomainClass(field.getType());
  // }

  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public void setAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
  }
  
  public boolean isAbstract() {
    return isAbstract;
  }

}

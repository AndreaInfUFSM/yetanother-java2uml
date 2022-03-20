package yajauml.domain;

public class DomainConstructor {

  private String constructor;
  private Visibility visibility;


  public DomainConstructor(String constructor) {
    this.constructor = constructor;
  }

  public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  protected String getName() {

    return constructor;
  }
  
  public String getUmlName() {
   
    return constructor;
  }  
}


package yajauml.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DomainClass {

  private String description;
  private String packageName;
  private Visibility visibility;
  private DomainClassType classType;
  private boolean isAbstract;
  private boolean isStatic;
  private transient List<DomainField> fieldList;
  private transient List<DomainConstructor> constructorList;
  private transient List<DomainMethod> methodList;

  public DomainClass(String description) {
    this.description = description;
  }
  
  public void addDomainField(DomainField field) {
    if (fieldList == null) {
      fieldList = new ArrayList<DomainField>();
    }
    fieldList.add(field);
  }

  public void addDomainMethod(DomainMethod method) {
    if (methodList == null) {
      methodList = new ArrayList<DomainMethod>();
    }
    methodList.add(method);
  }

  public void addDomainConstructor(DomainConstructor constructor) {
    if (constructorList == null) {
      constructorList = new ArrayList<DomainConstructor>();
    }
    constructorList.add(constructor);
  }

  public String getPackageName() {
    return packageName; 
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getUmlName() {
    return description; 
  }

  public String getClassName() {
    return description; 
  }

  public String getDescription() {
    return description;
  }

  public List<DomainField> getFields() {
    return fieldList == null ? Collections.emptyList() : fieldList;
  }

  public List<DomainConstructor> getConstructors() {
    return constructorList == null ? Collections.emptyList() : constructorList;
  }
  
  public List<DomainMethod> getMethods() {
    return methodList == null ? Collections.emptyList() : methodList;
  }

  public Visibility getVisibility() {
    return visibility; 
  }

  public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
  }
  
  public DomainClassType getClassType() {
    return classType;
  }

  public void setClassType(DomainClassType classType) {
    this.classType = classType;
  }

  public void setAbstract(boolean isAbstract) {
    this.isAbstract = isAbstract;
  }

  public boolean isAbstract() {
    return isAbstract; 
  }

  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  public boolean isStatic() {
    return isStatic; 
  }

}

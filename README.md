[![Build Status](https://travis-ci.org/iluwatar/uml-reverse-mapper.svg?branch=master)](https://travis-ci.org/iluwatar/uml-reverse-mapper)
[![Coverage Status](https://coveralls.io/repos/github/iluwatar/uml-reverse-mapper/badge.svg?branch=master)](https://coveralls.io/github/iluwatar/uml-reverse-mapper?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.iluwatar.urm/urm-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.iluwatar.urm/urm-maven-plugin/)

# UML Reverse Mapper

Automatically generate class diagram from your code.

Using reflection, UML Reverse Mapper scans your packages that contain your code. It then builds a graph of class relations and outputs either a [Graphviz](https://www.graphviz.org/) .dot file or a [PlantUML](http://www.plantuml.com/) .puml file.

The tool is available in command line version (urm-core) and Maven plugin (urm-maven-plugin).

### Using from the command-line

Build the `urm-core` project with `mvn clean package` and grab the generated artifact `urm-core.jar`. Then you need the archive that will be analyzed. In this example we use `abstract-factory.jar` and assume the package name to be `com.iluwatar.abstractfactory`. Place the jar-files in the same directory and execute the following command.

    java -cp abstract-factory.jar:urm-core.jar com.iluwatar.urm.DomainMapperCli -p com.iluwatar.abstractfactory -i com.iluwatar.abstractfactory.Castle

This will scan all the classes under the package `com.iluwatar.abstractfactory` except `Castle` that was marked to be ignored and output the markup to your console output. By default PlantUML presenter is used, but it can be changed with switch `-s graphviz`. If you want to write it to file use switch `-f filename`. If you need to scan multiple packages use format `-p "com.package1, com.package2"`. Note that under Windows OS the classpath separator is `;` instead of `:`

### Using the Maven plugin

Add to your pom.xml the following:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.iluwatar.urm</groupId>
      <artifactId>urm-maven-plugin</artifactId>
      <version>1.4.7</version>
      <configuration>
        <!-- if outputDirectory is not set explicitly it will default to your build dir -->
        <outputDirectory>${project.basedir}/etc</outputDirectory>
        <packages>
          <param>com.mycompany.mypackage</param>
          <param>com.mycompany.other_package</param>
        </packages>
        <ignores>
          <param>com.mycompany.mypackage.MyClass</param>
          <param>com.mycompany.other_package.OtherClass</param>
        </ignores>
        <includeMainDirectory>true</includeMainDirectory>
        <includeTestDirectory>false</includeTestDirectory>
        <presenter>graphviz</presenter>
      </configuration>
      <executions>
        <execution>
          <phase>process-test-classes</phase>
          <goals>
            <goal>map</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

- `packages` configuration parameter contains a list of packages that should be included in the class
diagram 
- `ignores` configuration parameter contains a list of types that should be excluded from the class
diagram
- `Dependencies` list should contain the artifacts where the classes are found. See https://maven.apache.org/guides/mini/guide-configuring-plugins.html#Using_the_dependencies_Tag
- `includeMainDirectory` configuration parameter indicates to include classes of src/main/java 
directory. Default value of `includeMainDirectory` is true. 
- `includeTestDirectory` configuration parameter indicates to include classes of src/test/java 
directory. Default value of `includeTestDirectory` configuration parameter is false.
- `presenter` parameter control which presenter is used. Can be either `graphviz` or `plantuml`.

When `process-test-classes` life-cycle phase gets executed, the class diagram will be saved to
`/target/${project.name}.urm.dot` or `/target/${project.name}.urm.puml`. Use this file with your local
or online tools to show your class diagram.

### Showcases

Here are some class diagrams generated with the `urm-maven-plugin` and the PlantUML Presenter as well as PlantUML's
free generation/hosting service.

[![Async Method Invocation](http://plantuml.com/plantuml/png/3SlB3G8n303HLg2090TkT6Ey5easjYD_5kYUdERmDFSXEFEWj7dh4SkVhHbywdj4prSw6Qe4ILHKRWnsfhC-Ml8iHXUPKs5OYsoZnmvzWTSaR-0_mS8KNOyov5A462erZUlQ_ny0)](https://github.com/markusmo3/uml-reverse-mapper/blob/master/examples/async-method-invocation.urm.puml)
[![Builder](http://plantuml.com/plantuml/png/3ShB4S8m34NHLg20M0jsTECuRuW7oTRear0-Njt5kSy-6kU1D7wS4Ufl8gjt-VGuSq-7jJa28qgRGbBjcoxpHIcy6IwOOvEg2bleiO9V5MKuxTdvW9KqARh-Fm00)](https://github.com/markusmo3/uml-reverse-mapper/blob/master/examples/builder.urm.puml)
[![Datamapper](http://plantuml.com/plantuml/png/BSkx3SCm34NHLPm1B1Rkl0qZFyH6H8dW9_7uKP7g5WVtSVNQya1QMyu8zPt8-5jULvpvJ8VLqGCzIXr2mlPEbx5HIbiD7vXZ5LQ5JVIOmSsY3Ku71_-jf4dH-Vm0)](https://github.com/markusmo3/uml-reverse-mapper/blob/master/examples/data-mapper.urm.puml)

### Deploy Instructions

[Performing a Release Deployment](http://central.sonatype.org/pages/apache-maven.html#performing-a-release-deployment)


```
mvn clean deploy -P release
```

### Release notes

- 1.4.7
    - Update README.md instruction [#20](https://github.com/iluwatar/uml-reverse-mapper/issues/20)
    - Presenter can not be chosen via command line parameter [#22](https://github.com/iluwatar/uml-reverse-mapper/issues/22)
    - Presenter can not be chosen via plugin parameters [#23](https://github.com/iluwatar/uml-reverse-mapper/issues/23)

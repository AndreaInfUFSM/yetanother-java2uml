package yajauml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import yajauml.presenters.Presenter;
import yajauml.presenters.Representation;

public class Main {

  DomainMapper domainMapper;

  public static void main(final String[] args) throws ClassNotFoundException, IOException {
    new Main().run(args);
  }

  /**
   * run method for cli class.
   * @param args input arguments
   * @throws ClassNotFoundException exception
   * @throws IOException exception
   */
  public void run(final String[] args) throws ClassNotFoundException, IOException {
    // create the command line parser
    CommandLineParser parser = new DefaultParser();
    // create the Options
    Options options = new Options();
    
    options.addOption(Option
      .builder("d").argName("directory").desc("directory to search for .java files")
      .hasArg().required().build());
    options.addOption("f", "file", true, "write to file");
    options.addOption(Option
      .builder("i").argName("ignore").desc("comma separated list of ignored types")
      .hasArgs().required(false).build());
    options.addOption(Option
      .builder("s").argName("presenter").desc("presenter format to be used: plantuml, graphviz or mermaid")
      .hasArg().required(false).build());

    try {
      
      CommandLine line = parser.parse(options, args);
    
      String[] ignores = null;
      if (line.hasOption("i")) {
        ignores = line.getOptionValue("i").split(",[ ]*");
      }
      Presenter presenter = Presenter.parse(line.getOptionValue("s"));
      String directory = line.getOptionValue("d");

      domainMapper = DomainMapper.create(presenter, directory, 
        ignores == null ? new ArrayList<>() : Arrays.asList(ignores));

      Representation representation = domainMapper.describeDomain();

      if (line.hasOption('f')) {
        String filename = line.getOptionValue('f');
        Path parent = Paths.get(filename).getParent();
        if (parent != null) {
          Files.createDirectories(Paths.get(filename).getParent());
        }
        Files.write(Paths.get(filename), representation.getContent().getBytes());
        System.out.println("Wrote to file " + filename);
      } else {
        System.out.println(representation.getContent());
      }

    } catch (ParseException exp) {

      System.out.println(exp.getMessage());
      // automatically generate the help statement
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar yajauml.jar", options);

    }
  }
}

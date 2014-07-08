package nl.surfnet.coin.selfservice;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will load configuration from whichever application.properties file is first on the classpath
 */
public class Application {

  private static final String PORT_ARG = "p";

  public static final String APP_NAME = "selfservice";
  public static final Logger LOGGER = LoggerFactory.getLogger(Application.class);


  public static void main(String[] args) throws Exception{

    CommandLineParser parser = new GnuParser();
    HelpFormatter formatter = new HelpFormatter();

    Options options = new Options();
    options.addOption("h", false, "Print help message");
    options.addOption(PORT_ARG, true, "Tcp/ip port you want the server to bind to, e.g. 8080");

    try {
      CommandLine cmd = parser.parse(options, args);
      if (cmd.hasOption("h")) {
        formatter.printHelp(APP_NAME, options);
        return;
      } else if (cmd.hasOption(PORT_ARG)) {

        Integer port = Integer.parseInt(cmd.getOptionValue(PORT_ARG));
        LOGGER.info("Starting application on port {}", port);

        Server server = new Server(port);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setResourceBase(Application.class.getClassLoader().getResource("webapp/").toExternalForm());
        server.setHandler(webAppContext);

        server.start();
        server.join();
      } else {
        System.err.println("Can not start application. Did you provide a port?");
        formatter.printHelp(APP_NAME, options);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Unexpected exception: " + e.getMessage());
      formatter.printHelp(APP_NAME, options);
    }
  }
}

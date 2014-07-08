package nl.surfnet.coin.selfservice;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class Application {

  private static final String PORT_ARG = "p";
  public static final String APP_NAME = "selfservice";

  public static void main(String[] args) {
    CommandLineParser parser = new GnuParser();
    HelpFormatter formatter = new HelpFormatter();

    Options options = new Options();
    options.addOption("h", "help", false, "Print help message");
    options.addOption(PORT_ARG, "port", true, "Tcp/ip port you want the server to bind to, e.g. 8080");

    try {
      CommandLine cmd = parser.parse(options, args);
      if (cmd.hasOption("h")) {
        formatter.printHelp(APP_NAME, options);
        return;
      } else if (cmd.hasOption(PORT_ARG)) {
        System.out.println("Starting application on port " + cmd.getOptionValue("p"));
        Integer port = Integer.parseInt(cmd.getOptionValue(cmd.getOptionValue(PORT_ARG)));
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
      System.err.println("Unexpected exception: " + e.getMessage());
      formatter.printHelp(APP_NAME, options);
    }
  }
}

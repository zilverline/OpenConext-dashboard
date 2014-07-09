package nl.surfnet.coin.selfservice;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class Application {

  public static final String APP_NAME = "java -jar selfservice.jar";
  public static final Logger LOG = LoggerFactory.getLogger(Application.class);

  private static final String DEFAULT_PROPERTIES_LOCATION = "application.properties";
  public static final String APP_LOGCONFIGFILE_PROPERTYNAME = "app.logconfigfile";
  public static final String APP_PORT_PROPERTYNAME = "app.port";

  private Server server;
  private Integer port;

  public Application(Optional<String> customPropertiesLocation) {
    Properties properties = new Properties();
    if (customPropertiesLocation.isPresent()) {
      try(InputStream input = new FileInputStream(customPropertiesLocation.get())) {
        properties.load(input);
      } catch (IOException ex) {
        System.err.println("Unable to load config file at path: " + customPropertiesLocation.get() + ", does it exist?");
        throw new RuntimeException(ex.getMessage());
      }
    } else {
      try(InputStream input = Application.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_LOCATION)) {
        properties.load(input);
      } catch (IOException ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }
    String logConfigFile = properties.getProperty(APP_LOGCONFIGFILE_PROPERTYNAME);
    initLogging(logConfigFile);
    this.port = Integer.parseInt(properties.getProperty(APP_PORT_PROPERTYNAME));
    LOG.info("Application configured to be started on port {}", this.port);
  }

  public Application start() throws Exception {
    startJetty();
    return this;
  }

  public void stop() throws Exception {
    if (server != null) {
      LOG.info("Shutting Down");
      server.stop();
    }
  }

  public void join() throws InterruptedException {
    server.join();
  }

  public static void main(String[] args)  {

    CommandLineParser parser = new GnuParser();
    HelpFormatter formatter = new HelpFormatter();

    Options options = new Options();
    options.addOption("h", false, "Prints this help message");
    Option configOption = new Option("c", "configFile", true, "The application config file (e.g application.properties) you want to use that overrides the default settings suitable for development by default. This is not what you want if you're not a developer.");
    options.addOption(configOption);
    try {
      CommandLine cmd = parser.parse(options, args);
      if (cmd.hasOption("h")) {
        formatter.printHelp(APP_NAME, options);
        return;
      } else {
        Optional<String> configLocation = cmd.hasOption(configOption.getOpt())? Optional.of(cmd.getOptionValue(configOption.getOpt())) : Optional.<String>absent();
        Application application = new Application(configLocation);
        application.start().join();
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Unexpected exception: " + e.getMessage());
      formatter.printHelp(APP_NAME, options);
    }
  }

  private void initLogging(final String logConfigFilePath) {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      // Call context.reset() to clear any previous configuration, e.g. default
      // configuration. For multi-step configuration, omit calling context.reset().
      context.reset();
      configurator.doConfigure(logConfigFilePath);
    } catch (JoranException ex) {
      // StatusPrinter will handle this
      StatusPrinter.printInCaseOfErrorsOrWarnings(context);
      throw new RuntimeException("Unable to configure logging, exiting", ex);
    }
    LOG.info("Logging configured, using: {}", logConfigFilePath);
  }
  private void startJetty() throws Exception{
    this.server = new Server(port);
    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setContextPath("/");
    webAppContext.setResourceBase(Application.class.getClassLoader().getResource("webapp/").toExternalForm());
    server.setHandler(webAppContext);

    server.start();
  }
}

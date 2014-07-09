package nl.surfnet.coin.selfservice;


import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Optional;

public class ApplicationITTest {

  @Test
  @Ignore
  public void startIt() throws Exception {
    Application application = new Application(Optional.<String>absent());
    application.start().join();
  }
}

package me.gregorias.dfuntest;

import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class LocalEnvironmentFactoryBuilderTest {
  private final LocalEnvironmentFactoryBuilder mFFactory = new LocalEnvironmentFactoryBuilder();
  private final Executor mMockExecutor = mock(Executor.class);

  @Test
  public void newEnvironmentFactoryShouldCreateNewFactory() {
    XMLConfiguration config = new XMLConfiguration();
    config.setProperty(LocalEnvironmentFactoryBuilder.XML_ENV_CNT_FIELD, 1);
    config.setProperty(LocalEnvironmentFactoryBuilder.XML_DIR_PREFIX_FIELD, "dirPrefix");
    EnvironmentFactory factory = mFFactory.newEnvironmentFactory(config, mMockExecutor);
    assertThat(factory, instanceOf(LocalEnvironmentFactory.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void newEnvironmentFactoryShouldThrowExceptionOnEnvCntFieldMissing() {
    XMLConfiguration config = new XMLConfiguration();
    config.setProperty(LocalEnvironmentFactoryBuilder.XML_DIR_PREFIX_FIELD, "dirPrefix");
    mFFactory.newEnvironmentFactory(config, mMockExecutor);
  }

  @Test(expected = IllegalArgumentException.class)
  public void newEnvironmentFactoryShouldThrowExceptionOnDirPrefixFieldMissing() {
    XMLConfiguration config = new XMLConfiguration();
    config.setProperty(LocalEnvironmentFactoryBuilder.XML_ENV_CNT_FIELD, 1);
    mFFactory.newEnvironmentFactory(config, mMockExecutor);
  }
}

package me.gregorias.dfuntest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import me.gregorias.dfuntest.testrunnerbuilders.GuiceTestRunnerModule;
import me.gregorias.dfuntest.util.FileUtils;
import me.gregorias.dfuntest.util.FileUtilsImpl;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiceTestRunnerModuleTest {
  @Test
  public void configurationToPropertiesShouldGenerateValidMap() {
    HierarchicalConfiguration configuration = new HierarchicalConfiguration();
    configuration.getRootNode().setName("root");

    HierarchicalConfiguration.Node node = new HierarchicalConfiguration.Node("b", "bValue");
    Collection<HierarchicalConfiguration.Node> nodes = new ArrayList<>();
    nodes.add(node);
    configuration.addNodes("a0", nodes);
    configuration.addProperty("a1", "a1Value");

    Map<String, String> properties = GuiceTestRunnerModule.configurationToProperties(configuration);
    assertEquals("bValue", properties.get("root.a0.b"));
    assertEquals("a1Value", properties.get("root.a1"));
  }

  @Test
  public void addingPropertiesShouldAllowToInstantiateThemLater() {
    String[] keys = {"a.b", "b0.b", "b1.b"};
    String[] values = {"a.bValue", "b0.bValue", "b1.bValue"};
    GuiceTestRunnerModule<Environment, App<Environment>> module = new GuiceTestRunnerModule<>();
    module.addProperty(keys[0], values[0]);
    Map<String, String> properties = new HashMap<>();
    properties.put(keys[1], values[1]);
    properties.put(keys[2], values[2]);
    module.addProperties(properties);

    Injector injector = Guice.createInjector(module);
    for (int idx = 0; idx < keys.length; ++idx) {
      assertEquals(values[idx], injector.getInstance(
          Key.get(String.class, Names.named(keys[idx]))));
    }
  }

  @Test
  public void stringShouldBeCollectionOfStrings() {
    String key = "a";
    String value = "a, b, c";
    GuiceTestRunnerModule<Environment, App<Environment>> module = new GuiceTestRunnerModule<>();
    module.addProperty(key, value);
    Injector injector = Guice.createInjector(module);
    Collection<String> collectionOfStrings =
        injector.getInstance(Key.get(new CollectionOfStringsTypeLiteral(), Names.named(key)));
    List<String> listOfStrings = new ArrayList<>(collectionOfStrings);
    assertEquals("a", listOfStrings.get(0));
    assertEquals("b", listOfStrings.get(1));
    assertEquals("c", listOfStrings.get(2));
    assertEquals(3, collectionOfStrings.size());
  }

  @Test
  public void stringShouldBeConvertedToPath() {
    String key = "a";
    String value = "b";
    GuiceTestRunnerModule<Environment, App<Environment>> module = new GuiceTestRunnerModule<>();
    module.addProperty(key, value);
    Injector injector = Guice.createInjector(module);
    Path path = injector.getInstance(Key.get(Path.class, Names.named(key)));
    assertEquals(path, FileSystems.getDefault().getPath(value));
  }

  @Test
  public void fileUtilsImplShouldBeProvided() {
    GuiceTestRunnerModule<Environment, App<Environment>> module = new GuiceTestRunnerModule<>();
    Injector injector = Guice.createInjector(module);
    FileUtils fileUtils = injector.getInstance(FileUtils.class);
    assertThat(fileUtils, instanceOf(FileUtilsImpl.class));
  }
  private static class CollectionOfStringsTypeLiteral extends TypeLiteral<Collection<String>> {
  }
}

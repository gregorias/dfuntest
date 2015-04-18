package me.gregorias.dfuntest.testrunnerbuilders;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeConverter;
import me.gregorias.dfuntest.util.FileUtils;
import me.gregorias.dfuntest.util.FileUtilsImpl;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ConfigurationNodeVisitor;
import org.apache.commons.lang.StringUtils;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * <p>
 * Guice's AbstractModule useful for creating new TestRunner.
 * </p>
 *
 * <p>
 * This module adds String to Path converter, String to Collection<String> converter
 * (using comma as separator) and automatic properties generation.
 * </p>
 */
public class GuiceTestRunnerModule extends AbstractModule {
  private final Map<String, String> mProperties = new HashMap<>();

  /**
   * Traverses Hierarchical configuration and return node's values to properties maps.
   * Keys form the full path to node, for example the following XMLConfiguration
   *
   * <pre>
   * {@code
   * <a>
   *   <b>
   *     value
   *   </b>
   * </a>
   * </code>}
   * </pre>
   *
   * will generate ("a.b", "value") pair.
   *
   * @param configuration Configuration to traverse
   * @return Properties generated from configuration
   */
  public static Map<String, String> configurationToProperties(
      HierarchicalConfiguration configuration) {
    PropertiesBuilderVisitor visitor = new PropertiesBuilderVisitor();
    configuration.getRootNode().visit(visitor);
    return visitor.getProperties();
  }

  public void addProperty(String key, String value) {
    mProperties.put(key, value);
  }

  public void addProperties(Map<String, String> properties) {
    mProperties.putAll(properties);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void configure() {
    Names.bindProperties(binder(), mProperties);
    binder().convertToTypes(Matchers.only(TypeLiteral.get(Path.class)),
        new StringToPathTypeConverter());
    binder().convertToTypes(Matchers.only(new CollectionOfStringsTypeLiteral()),
        new StringToCollectionOfStringsTypeConverter());
  }

  @Provides
  @Singleton
  @SuppressWarnings("unused")
  FileUtils provideFileUtils() {
    return FileUtilsImpl.getFileUtilsImpl();
  }

  private static class PropertiesBuilderVisitor implements ConfigurationNodeVisitor {
    private final Map<String, String> mProperties = new HashMap<>();
    private final Stack<String> mPrefixes = new Stack<>();

    public Map<String, String> getProperties() {
      return mProperties;
    }

    @Override
    public void visitBeforeChildren(ConfigurationNode node) {
      mPrefixes.push(node.getName());
      if (node.getValue() != null) {
        String key = StringUtils.join(mPrefixes, ".");
        if (mProperties.containsKey(key)) {
          String value = mProperties.get(key);
          mProperties.put(key, String.format("%s, %s", value, (String) node.getValue()));
        } else {
          mProperties.put(key, (String) node.getValue());
        }
      }
    }

    @Override
    public void visitAfterChildren(ConfigurationNode node) {
      mPrefixes.pop();
    }

    @Override
    public boolean terminate() {
      return false;
    }
  }

  private static class CollectionOfStringsTypeLiteral extends TypeLiteral<Collection<String>> {
  }

  private static class StringToCollectionOfStringsTypeConverter implements TypeConverter {
    @Override
    public Object convert(String value, TypeLiteral<?> toType) {
      String[] values = value.split(",");
      Collection<String> valuesCollection = new ArrayList<>();
      for (String arrayValue : values) {
        valuesCollection.add(arrayValue.trim());
      }
      return valuesCollection;
    }
  }

  private static class StringToPathTypeConverter implements TypeConverter {
    @Override
    public Object convert(String value, TypeLiteral<?> toType) {
      return FileSystems.getDefault().getPath(value);
    }
  }
}

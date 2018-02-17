/*
    Copyright 2016 Mario Macías

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package info.macias.kaconf.sources;

import info.macias.kaconf.ConfiguratorException;

import java.io.*;
import java.util.Properties;

/**
 * Wrapper that allows handling {@link java.util.Properties} instances though
 * {@link info.macias.kaconf.PropertySource} interface
 */
public class JavaUtilPropertySource extends AbstractPropertySource {
    private Properties properties;

    /**
     * <p>Instantiates the class by loading the File specified as argument.</p>
     * <p>If the {@link java.util.Properties} cannot be loaded (e.g. because the file does not exist or
     * the user does not have permissions), no exceptions will be thrown and the object will be
     * instantiated anyway. However, the {@link JavaUtilPropertySource#isAvailable()} method will
     * return <code>false</code>.</p>
     *
     * @param filePath The path to reach the Properties file.
     * @deprecated Use {@link #from(String)} instead. This constructor will be removed in version 0.9.0.
     */
    public JavaUtilPropertySource(String filePath) {
        try(FileInputStream fis = new FileInputStream(filePath)) {
            properties.load(fis);
        } catch (IOException e) {
            properties = null;
        }
    }
    /**
     * <p>Instantiates the class by loading the {@link java.util.Properties} object that is
     * reachable by the {@link InputStream} specified as argument</p>
     * <p>If the {@link java.util.Properties} cannot be loaded (e.g. because the file format is wrong or
     * the user does not have permissions), no exceptions will be thrown and the object will be
     * instantiated anyway. However, the {@link JavaUtilPropertySource#isAvailable()} method will
     * return <code>false</code>.</p>
     *
     * @param is The {@link InputStream} to access the Properties file
     * @deprecated Use {@link #from(InputStream)} instead. This constructor will be removed in version 0.9.0.
     */
    public JavaUtilPropertySource(InputStream is) {
        try {
            Properties props = new Properties();
            props.load(is);
            properties = props;
        } catch (NullPointerException | IOException e) {
            properties = null;
        }
    }

    /**
     * <p>Creates a properties source by loading the properties file with the path that is specified as argument.</p>
     *
     * @param filePath The path to reach the Properties file.
     * @throws ConfiguratorException if there is something wrong when loading the properties.
     */
    public static JavaUtilPropertySource from(String filePath) {
        File file = new File(filePath);
        return from(file);
    }

    /**
     * <p>Creates a properties source by loading the File specified as argument.</p>
     *
     * @param file The {@link File} object to reach the Properties file.
     * @throws ConfiguratorException if there is something wrong when loading the properties.
     */
    public static JavaUtilPropertySource from(File file) {
        if (file == null)
            throw new IllegalArgumentException("'" + file + "' is not a valid file.");
        Properties properties;
        try (FileInputStream fis = new FileInputStream(file)) {
            properties = new Properties();
            properties.load(fis);
        } catch (IOException e) {
            throw new ConfiguratorException(e);
        }
        return new JavaUtilPropertySource(properties);
    }

    /**
     * <p>Creates a properties source by loading the {@link java.util.Properties} object that is
     * reachable by the {@link InputStream} specified as argument</p>
     *
     * @param is The {@link InputStream} to access the Properties file
     * @throws ConfiguratorException if there is something wrong when loading the properties.
     */
    public static JavaUtilPropertySource from(InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("The provided Input Stream can't be null");
        }
        Properties properties;
        try {
            properties = new Properties();
            properties.load(is);
        } catch (NullPointerException | IOException e) {
            properties = null;
        }
        return new JavaUtilPropertySource(properties);
    }


    /**
     * <p>Instantiates the class to handle the {@link java.util.Properties} object passed as an
     * argument</p>
     *
     * @param properties The properties that will be handled by the instantiated object
     */
    public JavaUtilPropertySource(Properties properties) {
        this.properties = properties;
    }

    /**
     * Returns <code>true</code> if the {@link java.util.Properties} object has been correctly
     * loaded. Otherwise, returns <code>false</code>.
     *
     * @return <code>true</code> if the {@link java.util.Properties} object has been correctly
     * loaded. <code>false</code> otherwise
     */
    @Override
    public boolean isAvailable() {
        return properties != null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String get(String key) {
        if (!isAvailable()) {
            return null;
        }
        return properties.getProperty(key);
    }
}

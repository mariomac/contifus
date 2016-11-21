package info.macias.kaconf;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class that handles the configuration of objects
 */
public class Configurator {
    private List<PropertySource> sources;

    /**
     * Instantiates a configurator
     *
     * @param sources a list of {@link PropertySource} instances, in order of priority
     */
    Configurator(List<PropertySource> sources) {
        this.sources = sources;
    }

    /**
     * <p>Configures the object passed as argument. It looks for {@link Property} annotations in the
     * object passed as parameter and assigns the annotated property name present in the
     * {@link PropertySource} with the hightes priority.</p>
     * <p>
     * <p>The invocation of this method will also configure the static fields of the class that
     * the destination object belongs to.</p>
     *
     * @param dst the object to configure
     * @throws ConfiguratorException if an invalid assignment has been intended (for example, assign
     *                               an alphanumeric value into an integer) or if the access to the destination field is
     *                               enforced by Java language access control and the underlying field is either inaccessible
     *                               or final.
     */
    public void configure(Object dst) {
        // Configure properties for this class and its superclasses
        for (Class c = dst.getClass(); c != null; c = c.getSuperclass()) {
            configure(dst, c);
        }
    }

    /**
     * Configures the static fields of a class passed as an argument, in a similar manner to
     * {@link #configure(Object)}
     *
     * @param clazz The class whose static fields have to be configured
     */
    public void configure(Class<?> clazz) {
        while (clazz != null) {
            configure(null, clazz);
            clazz = clazz.getSuperclass();
        }
    }

    private void configure(Object dst, Class clazz) {

        for (Field f : clazz.getDeclaredFields()) {
            Stream.of(f.getAnnotations())
                    .filter(a -> a.annotationType().isAssignableFrom(Property.class))
                    .map(a -> (Property) a)
                    .findFirst()
                    .ifPresent(
                            p -> findPriorValue(p.value(), f.getType())
                                    .ifPresent(value -> {
                                        boolean isAcessible = f.isAccessible();
                                        f.setAccessible(true);

                                        try {
                                            f.set(dst, value);
                                        } catch (NumberFormatException | IllegalAccessException e) {
                                            throw new ConfiguratorException(e);
                                        }

                                        f.setAccessible(isAcessible);
                                    })
                    );
        }
    }

    private <T> Optional<T> findPriorValue(String key, Class<T> pType) {
        return sources.stream()
                .map(ps -> ps.get(key, pType))
                .filter(value -> value != null)
                .findFirst();
    }
}

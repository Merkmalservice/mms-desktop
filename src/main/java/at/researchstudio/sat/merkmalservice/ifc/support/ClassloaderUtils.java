package at.researchstudio.sat.merkmalservice.ifc.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ClassloaderUtils {
    /**
     * Finds all classes in the specified <code>packageName</code> that are sublcasses of the
     * specified <code>superType</code>.
     *
     * @param packageName the package to scan
     * @param superType the top-level type of all resulting classes
     * @return the classes in the package
     */
    public static Set<Class> findClassesInPackage(String packageName, Class<?> superType) {
        InputStream stream =
                ClassLoader.getSystemClassLoader()
                        .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        if (stream == null) {
            return Collections.emptySet();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .filter(superType::isAssignableFrom)
                .collect(Collectors.toSet());
    }

    private static Class getClass(String className, String packageName) {
        try {
            return Class.forName(
                    packageName + "." + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }
}

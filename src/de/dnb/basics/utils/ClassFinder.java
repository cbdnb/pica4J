/**
 *
 */
package de.dnb.basics.utils;

/**
 * @author baumann
 *
 */
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.zip.*;

/**
 * Test-Aufruf-Beispiele (der letzte Aufruf mit junit-...jar im Classpath):
 *   java classfinder.ClassFinder
 *   java classfinder.ClassFinder classfinder
 *   java classfinder.ClassFinder "" classfinder.MeinInterface
 *   java classfinder.ClassFinder "" classfinder.MeineAbstrakteKlasse
 *   java classfinder.ClassFinder "" org.junit.runner.Result
 */
public class ClassFinder {

  // Die main()-Methode ist hauptsächlich für Tests:
  public static void main(final String[] args) throws Exception {
    System.out.println(getPathsFromClasspath());

    final String packageName = (args.length > 0) ? args[0] : null;
    final String classNameSearched = (args.length > 1) ? args[1] : null;
    System.out.println("\n---- Gefundene Klassen:");
    final List<Class<?>> classes = getClassesStr(packageName, classNameSearched);
    for (final Class<?> clazz : classes)
      System.out.println(clazz);
    System.out.println("\n---- Instanziierte Objekte:");
    //    final List<Object> objects = getInstances(packageName, classNameSearched);
    //    for (final Object obj : objects)
    //      System.out.println(obj.getClass());
  }

  /**
   * Finde Klassen und instanziiere sie. Man erhält so alle instanziierbaren
   * Klassen, also die , die nicht Interface oder abstrakt sind.
   *
   * @param packageName       Package und alle Unterpackages werden rekursiv durchsucht. Wenn null,
   *                          dann wird ab dem Top-Level-Package gesucht.
   * @param classNameSearched muss voll qualifiziert sein (z.B. de.dnb.basics.utils.TimeUtils)
   *                          Nur Unterklassen -also zuweisungskompatible-
   *                          von classNameSearched werden gesucht.
   *                          Wenn null, dann werden alle akzeptiert.
   * @return
   * @throws ClassNotFoundException
   */
  public static List<Object> getInstances(final String packageName, final String classNameSearched)
    throws ClassNotFoundException {
    final List<Class<?>> classes = getClassesStr(packageName, classNameSearched);
    final List<Object> objects = new ArrayList<Object>();
    for (final Class<?> clazz : classes) {
      if (!clazz.isInterface() && (clazz.getModifiers() & Modifier.ABSTRACT) == 0) {
        try {
          objects.add(clazz.newInstance());
        } catch (final Exception ex) {
          // nur instanziierbare Klassen sind interessant
        }
      }
    }
    return objects;
  }

  /**
   * Finde Klassen (über Interface- oder Klassennamen bzw. Package-Namen):
   *
   * @param packageName       Package und alle Unterpackages werden rekursiv durchsucht. Wenn null,
   *                          dann wird ab dem Top-Level-Package gesucht.
   * @param classNameSearched       Muss voll qualifiziert sein (z.B. de.dnb.basics.utils.TimeUtils)
   *                                Nur Unterklassen -also zuweisungskompatible-
   *                                von classNameSearched werden gesucht.
   *                                Wenn null, dann werden alle akzeptiert.
   * @return
   * @throws ClassNotFoundException
   */
  public static
    List<Class<?>>
    getClassesStr(final String packageName, final String classNameSearched)
      throws ClassNotFoundException {
    final Class<?> classSearched =
      (classNameSearched != null) ? Class.forName(classNameSearched) : null;

    final List<Class<?>> classes = getClasses(packageName, classSearched);
    //    System.err.println(classes);
    return classes;
  }

  /**
   * Finde Klassen (über Interface oder Klasse bzw. Package-Namen):
   *
   * @param packageName       Package und alle Unterpackages werden rekursiv durchsucht. Wenn null,
   *                          dann wird ab dem Top-Level-Package gesucht.
   * @param classSearched Nur Unterklassen von classSearched werden gesucht. Wenn null,
   *                      dann werden alle akzeptiert.
   * @return
   */
  public static List<Class<?>> getClasses(final String packageName, final Class<?> classSearched) {
    final List<Class<?>> classes = new ArrayList<Class<?>>();
    for (final String path : getPathsFromClasspath()) {
      final File fileOrDir = new File(path);
      if (fileOrDir.isDirectory()) {
        final List<Class<?>> classesFromDir =
          getClassesFromDir(fileOrDir, packageName, classSearched);
        classes.addAll(classesFromDir);
      }
      if (fileOrDir.isFile() && (fileOrDir.getName().toLowerCase().endsWith(".jar")
        || fileOrDir.getName().toLowerCase().endsWith(".zip"))) {
        classes.addAll(getClassesFromJar(fileOrDir, packageName, classSearched));
      }
    }
    return Collections.unmodifiableList(classes);
  }

  /**
   *
   * @return alle im Classpath verzeichneten Pfade
   */
  public static List<String> getPathsFromClasspath() {
    final String classpath = System.getProperty("java.class.path");
    final String pathseparator = System.getProperty("path.separator");
    final StringTokenizer tokenizer = new StringTokenizer(classpath, pathseparator);
    final List<String> paths = new ArrayList<String>();
    while (tokenizer.hasMoreElements())
      paths.add(tokenizer.nextToken());
    return Collections.unmodifiableList(paths);
  }

  /**
   *
   * @param file          Pfad zum jar, der aus dem Klassenpfad gewonnen wurde
   * @param packageName   Package und alle Unterpackages werden rekursiv durchsucht. Wenn null,
   *                      dann wird ab dem Top-Level-Package gesucht.
   * @param classSearched Nur Unterklassen -also zuweisungskompatible-
   *                      von classSearched werden gesucht.
   *                      Wenn null, dann werden alle akzeptiert.
   * @return              evenutell leere Liste
   */
  public static
    List<Class<?>>
    getClassesFromJar(final File file, String packageName, final Class<?> classSearched) {
    if (packageName == null)
      packageName = "";
    final List<Class<?>> classes = new ArrayList<Class<?>>();
    final String dirSearched = packageName.replace(".", "/");
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(file);
    } catch (final Exception ex) {
      // nur Dateien, die gezippt sind und geöffnet werden können, sind interessant
      return classes;
    }
    for (final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries(); zipEntries
      .hasMoreElements();) {
      String entryName = zipEntries.nextElement().getName();
      if (!entryName.startsWith(dirSearched) || !entryName.toLowerCase().endsWith(".class"))
        continue;
      entryName = entryName.substring(0, entryName.length() - ".class".length());
      entryName = entryName.replace("/", ".");
      try {
        final Class<?> clazz = Class.forName(entryName);
        if (classSearched == null || classSearched.isAssignableFrom(clazz))
          classes.add(clazz);
      } catch (final Throwable ex) {
        // nur 'verwendbare' Klassen sind interessant
      }
    }
    try {
      zipFile.close();
    } catch (final Exception ex) {
      /* wird ignoriert */ }
    return Collections.unmodifiableList(classes);
  }

  /**
   *
   * @param dir           File (Directory), das aus dem Klassenpfad gewonnen wurde
   * @param packageName   Package und alle Unterpackages werden rekursiv durchsucht. Wenn null,
   *                      dann wird ab dem Top-Level-Package gesucht.
   * @param classSearched Nur Unterklassen -also zuweisungskompatible-
   *                      von classSearched werden gesucht.
   *                      Wenn null, dann werden alle akzeptiert.
   * @return
   */
  public static
    List<Class<?>>
    getClassesFromDir(final File dir, String packageName, final Class<?> classSearched) {
    if (packageName == null)
      packageName = "";
    final List<Class<?>> classes = new ArrayList<Class<?>>();
    final File dirSearched =
      new File(dir.getPath() + File.separator + packageName.replace(".", "/"));
    if (dirSearched.isDirectory())
      getClassesFromFileOrDirIntern(true, dirSearched, packageName, classSearched, classes);
    return Collections.unmodifiableList(classes);
  }

  /**
   *
   */
  private ClassFinder() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   *
   * @param first
   * @param fileOrDir
   * @param packageName   Package und alle Unterpackages werden rekursiv durchsucht.
   * @param classSearched Nur Unterklassen -also zuweisungskompatible-
   *                      von classSearched werden gesucht.
   *                      Wenn null, dann werden alle akzeptiert.
   * @param classes       Vorbereitete Liste, die gefüllt wird
   */
  private static void getClassesFromFileOrDirIntern(
    final boolean first,
    final File fileOrDir,
    String packageName,
    final Class<?> classSearched,
    final List<Class<?>> classes) {
    if (fileOrDir.isDirectory()) {
      if (!first) {
        packageName = (packageName + "." + fileOrDir.getName()).replaceAll("^\\.", "");
      }
      for (final String subFileOrDir : fileOrDir.list()) {
        getClassesFromFileOrDirIntern(false, new File(fileOrDir, subFileOrDir), packageName,
          classSearched, classes);
      }
    } else {
      if (fileOrDir.getName().toLowerCase().endsWith(".class")) {
        String classFile = fileOrDir.getName();
        classFile =
          packageName + "." + classFile.substring(0, classFile.length() - ".class".length());
        try {

          final Class<?> clazz = Class.forName(classFile);
          if (classSearched == null || classSearched.isAssignableFrom(clazz))
            classes.add(clazz);
        } catch (final Throwable ex) {
          // nur 'verwendbare' Klassen sind interessant
        }
      }
    }
  }
}

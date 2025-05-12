package pcd.ass02.reactive.model;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.*;
import static com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import io.reactivex.rxjava3.core.Observable;
import pcd.ass02.reactive.model.ClassDependencyInfo.DependencyChangeType;

public class ReactiveDependencyFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveDependencyFinder.class);
    private static final String FILE_EXTENSION = ".java";

    /**
     * Finds all classes dependencies in the given project directory.
     * The method returns an Observable that emits ClassDependencyInfo objects
     * representing the dependencies found in the project directory, reacting to
     * changes in the directory.
     * 
     * @param projectDirectory
     * @return the observable that emits ClassDependencyInfo objects
     */
    public Observable<ClassDependencyInfo> findAllClassesDependencies(final Path projectDirectory) {
        // Create an observable that emits dependencies found in the project directory
        return Observable.create(emitter -> {
            // TODO: Passare un qualche observable qui che riceva il comando di chiusura
            // dell'emitter su cui chiamiamo l'onComplete() e l'interrupt del thread, le
            // risorse in teoria sono chiuse in automatico dai try-with-resources
            final Thread thread = new Thread(() -> {
                try (Stream<Path> filesStream = Files.walk(projectDirectory)) {
                    filesStream.filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(FILE_EXTENSION))
                            .forEach(classFile -> {
                                LOGGER.info("Processing file: " + classFile);
                                try {
                                    Entry<String, Set<String>> dependency = getClassDependencies(classFile);
                                    LOGGER.info("Found dependencies of: " + dependency.getKey());
                                    emitter.onNext(new ClassDependencyInfo(dependency, DependencyChangeType.DISCOVER,
                                            classFile));
                                } catch (Exception e) {
                                    LOGGER.error("Error processing file: " + classFile, e);
                                    emitter.onError(e);
                                }
                            });
                } catch (Exception e) {
                    LOGGER.error("Error finding dependencies: ", e);
                    emitter.onError(e);
                }

                // Dopo aver processato tutti i file, continiamo a controllare per nuovi
                // file o modifiche di quelli esistenti
                try (WatchService ws = FileSystems.getDefault().newWatchService()) {
                    projectDirectory.register(ws,
                            new WatchEvent.Kind[] { ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE },
                            FILE_TREE);
                    LOGGER.info("Watching directory: " + projectDirectory);
                    while (!Thread.currentThread().isInterrupted()) {
                        // Wait for a key to be available
                        WatchKey k = ws.take();
                        for (WatchEvent<?> event : k.pollEvents()) {
                            Object classObject = event.context();
                            LOGGER.info(event.kind().toString() + " " + event.count() + " " + classObject);
                            if (classObject.toString().endsWith(FILE_EXTENSION)) {
                                var classFile = Path.of(projectDirectory + File.separator + classObject.toString());
                                var changeType = event.kind() == ENTRY_CREATE
                                        ? DependencyChangeType.CREATE
                                        : event.kind() == ENTRY_MODIFY
                                                ? DependencyChangeType.MODIFY
                                                : DependencyChangeType.DELETE;
                                try {
                                    // nodo dipende da esso (limitatamente a dipendenze esternerne)
                                    // il rename viene considerato come delete del file originale e creazione di un
                                    // nuovo file
                                    if (changeType != DependencyChangeType.DELETE) {
                                        Entry<String, Set<String>> dependency = getClassDependencies(classFile);
                                        LOGGER.info("Found dependencies of: " + dependency.getKey());
                                        emitter.onNext(new ClassDependencyInfo(dependency, changeType, classFile));
                                    } else {
                                        LOGGER.info("Class: " + classObject.toString() + " has been deleted");
                                        emitter.onNext(new ClassDependencyInfo(null, changeType, classFile));
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("Error processing file: " + classFile, e);
                                    emitter.onError(e);
                                }
                            }
                        }
                        k.reset();
                    }
                } catch (InterruptedException e) {
                    // TODO handle exception
                    LOGGER.info("Thread interrupted, stopping analysis");
                } catch (Exception e) {
                    LOGGER.error("Error watching directory: ", e);
                    emitter.onError(e);
                } finally {
                    // Close the emitter when the thread is interrupted
                    LOGGER.info("Stopping analysis thread and completing emitter");
                    emitter.onComplete();
                }
            });
            emitter.setCancellable(() -> {
                // Close the emitter when the thread is interrupted
                LOGGER.info("Interrupting analysis thread");
                thread.interrupt();
                try {
                    thread.join();
                    LOGGER.info("Analysis thread interrupted");
                } catch (InterruptedException e) {
                    LOGGER.error("Error interrupting analysis thread: ", e);
                }
            });
            thread.start();
        });
    }

    private Entry<String, Set<String>> getClassDependencies(final Path classFile) {
        // Process the file content and generate the deps set
        Set<String> dependencies = new HashSet<>();
        var classPath = classFile.getParent();
        try (var buffer = Files.newBufferedReader(classFile)) {
            // Create a JavaParser instance with the desired configuration
            JavaParser parser = new JavaParser(new ParserConfiguration()
                    .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21));

            // Parse the class source file
            CompilationUnit compilationUnit = parser.parse(buffer).getResult()
                    .orElseThrow(() -> new RuntimeException("Failed to parse class file"));

            // Collect imported classes
            for (ImportDeclaration importDecl : compilationUnit.findAll(ImportDeclaration.class)) {
                dependencies.add(importDecl.getNameAsString());
            }

            // Collect classes dependencies in the same package
            var packageName = compilationUnit.getPackageDeclaration()
                    .map(pkg -> pkg.getNameAsString())
                    .orElse("");
            compilationUnit.findAll(ClassOrInterfaceType.class).forEach(type -> {
                var typeName = type.getNameAsString();
                var fullyQualifiedName = packageName + "." + typeName;
                var classFilePath = classPath + File.separator + typeName + FILE_EXTENSION;
                if (dependencies.stream().noneMatch(dep -> dep.endsWith("." + typeName))) {
                    if (Files.exists(Paths.get(classFilePath))) {
                        // Add the dependency to the list
                        dependencies.add(fullyQualifiedName);
                    }
                }
            });

            var className = classFile.getFileName().toString().replace(FILE_EXTENSION, "");

            var fullyQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;

            return new AbstractMap.SimpleImmutableEntry<String, Set<String>>(fullyQualifiedName, dependencies);
        } catch (Exception e) {
            LOGGER.error("Error processing class file: " + classFile, e);
            throw new RuntimeException("Error processing class file: " + classFile, e);
        }
    }
}

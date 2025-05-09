package pcd.ass02.reactive.model;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class ReactiveDependencyFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveDependencyFinder.class);
    private static final String FILE_EXTENSION = ".java";

    public Observable<Entry<String, Set<String>>> findAllClassesDependencies(final Path projectDirectory) {
        // Create an observable that emits dependencies found in the project directory
        return Observable.create(emitter -> {
            // TODO: Passare un qualche observable qui che riceva il comando di chiusura
            // dell'emitter su cui chiamiamo l'onComplete() e l'interrupt del thread, le
            // risorse in teoria sono chiuse in automatico dai try-with-resources
            new Thread(() -> {
                try (Stream<Path> filesStream = Files.walk(projectDirectory)) {
                    filesStream.filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(FILE_EXTENSION))
                            .forEach(classFile -> {
                                try {
                                    Entry<String, Set<String>> dependency = getClassDependencies(classFile);
                                    LOGGER.info("Found dependencies of: " + dependency.getKey());
                                    emitter.onNext(dependency);
                                } catch (Exception e) {
                                    LOGGER.error("Error processing file: " + classFile, e);
                                    emitter.onError(e);
                                }
                            });
                } catch (Exception e) {
                    LOGGER.error("Error finding dependencies: ", e);
                    emitter.onError(e);
                }
                // TODO: Register for file changes in the project directory
                // TODO: Something to stop the thread
            }).start();
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

            return new AbstractMap.SimpleImmutableEntry<String, Set<String>>(packageName + "." + className,
                    dependencies);
        } catch (Exception e) {
            LOGGER.error("Error processing class file: " + classFile, e);
            throw new RuntimeException("Error processing class file: " + classFile, e);
        }
    }
}

package pcd.ass02.async.library;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import pcd.ass02.async.common.reports.AbstractReport;
import pcd.ass02.async.common.reports.ClassDepsReport;
import pcd.ass02.async.common.reports.PackageDepsReport;
import pcd.ass02.async.common.reports.ProjectDepsReport;

/**
 * DependencyAnalyzer is a class that analyzes the dependencies of Java classes,
 * packages, and projects. It uses the Vert.x library for asynchronous file
 * operations and the JavaParser library for parsing Java source files.
 */
public class DependencyAnalyzer {

    /*
     * Since this is a library class we cannot assume if it will execute standalone
     * or in a user-deployed verticle. For this reason we need check if the current
     * context is null and create a new Vertx instance if it is, leaving the
     * possibility to the user to pass a Vertx instance in constructor if it has
     * already been created.
     */

    private final Vertx vertx;

    public DependencyAnalyzer() {
        this.vertx = Vertx.currentContext() == null ? Vertx.vertx() : Vertx.currentContext().owner();
    }

    public DependencyAnalyzer(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Get the dependencies of a class. The class is a Java file. The
     * dependencies are retrieved by searching the class for import statements and
     * then adding used types from the same package.
     * 
     * @param classSrcFile The path to the class source file.
     * @return A future that will be completed with the class dependencies or failed
     *         with an error if the class source file does not exist or is not a
     *         Java file.
     */
    public Future<ClassDepsReport> getClassDependencies(final String classSrcFile) {
        return getClassDependencies(classSrcFile, null);
    }

    /**
     * Get the dependencies of a class. The class is a Java file. The
     * dependencies are retrieved by searching the class for import statements and
     * then adding used types from the same package.
     * 
     * @param classSrcFile       The path to the class source file.
     * @param excludedPackageSrc The path to the excluded package folder. This is
     *                           used to exclude dependencies that are internal to
     *                           the package.
     * @return A future that will be completed with the class dependencies or failed
     *         with an error if the class source file does not exist or is not a
     *         Java file.
     */
    private Future<ClassDepsReport> getClassDependencies(final String classSrcFile, final String excludedPackageSrc) {

        Promise<ClassDepsReport> classPromise = Promise.promise();
        FileSystem fs = vertx.fileSystem();

        var classPath = classSrcFile.substring(0, classSrcFile.lastIndexOf(File.separator));
        fs.exists(classSrcFile)
                .compose((Boolean result) -> {
                    // Check if the class source file exists
                    return result ? fs.readFile(classSrcFile) : Future.failedFuture("File not found: " + classSrcFile);
                })
                .onSuccess((Buffer buffer) -> {
                    // Process the file content and generate the ClassDepsReport
                    List<String> dependencies = new ArrayList<>();
                    try {
                        // Create a JavaParser instance with the desired configuration
                        JavaParser parser = new JavaParser(new ParserConfiguration()
                                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21));

                        // Parse the class source file
                        CompilationUnit compilationUnit = parser.parse(buffer.toString()).getResult()
                                .orElseThrow(() -> new RuntimeException("Failed to parse class file"));

                        // Collect imported classes
                        for (ImportDeclaration importDecl : compilationUnit.findAll(ImportDeclaration.class)) {
                            dependencies.add(importDecl.getNameAsString());
                        }

                        // Collect classes dependencies in the same package
                        var packageName = compilationUnit.getPackageDeclaration()
                                .map(pkg -> pkg.getNameAsString())
                                .orElse("");
                        // Checking same package dependencies require an fs.exists() call for each
                        // dependency, so we start and put them in a list
                        List<Future<Boolean>> packageFutures = new ArrayList<>();
                        compilationUnit.findAll(ClassOrInterfaceType.class).forEach(type -> {
                            var typeName = type.getNameAsString();
                            var fullyQualifiedName = packageName + "." + typeName;
                            var classFilePath = classPath + File.separator + typeName + ".java";
                            if (dependencies.stream().noneMatch(dep -> dep.endsWith("." + typeName))) {
                                packageFutures.add(fs.exists(classFilePath).onComplete(res -> {
                                    if (res.succeeded() && res.result()) {
                                        // Add the dependency to the list
                                        dependencies.add(fullyQualifiedName);
                                    }
                                }));
                            }
                        });

                        // Wait for all futures to complete
                        Future.all(packageFutures).onComplete(res -> {
                            if (res.failed()) {
                                classPromise.fail(res.cause());
                            } else {
                                // Filter out dependencies from the excluded package
                                compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDec -> {
                                    var classQualifiedName = classDec.getFullyQualifiedName().get();
                                    if (excludedPackageSrc != null) {
                                        String srcRoot = classSrcFile.replace(".java", ""); // Remove .java extension
                                        // Remove the class name from the path
                                        srcRoot = srcRoot.substring(0, srcRoot.length() - classQualifiedName.length());
                                        String excludedPackage = excludedPackageSrc.replace(srcRoot, "")
                                                .replace(File.separatorChar, '.');
                                        // Remove all dependencies that start with the excluded package name
                                        dependencies.removeIf(dep -> dep.startsWith(excludedPackage));
                                    }
                                });
                                classPromise.complete(new ClassDepsReport(dependencies));
                            }
                        });

                    } catch (Exception e) {
                        // Error while parsing the class file
                        classPromise.fail(e);
                        System.err.println("Error parsing class file: " + e.getMessage());
                    }
                })
                .onFailure((Throwable t) -> {
                    // Handle the error
                    classPromise.fail(t);
                });

        return classPromise.future();
    }

    /**
     * Get the dependencies of a package. The package is a folder that contains Java
     * files and directories. The dependencies are the Java files that are not in
     * the same package. The dependencies are retrieved by searching each package
     * recursively and retrieving the dependency for each Java file
     * 
     * @param packageSrcFolder The path to the package folder.
     * @return A future that will be completed with the package dependencies or
     *         failed with an error if the package folder does not exist or is not a
     *         directory.
     */
    public Future<PackageDepsReport> getPackageDependencies(final String packageSrcFolder) {
        return getPackageDependencies(packageSrcFolder, null);
    }

    /**
     * Get the dependencies of a package. The package is a folder that contains Java
     * files and directories. The dependencies are the Java files that are not in
     * the same package. The dependencies are retrieved by searching each package
     * recursively and retrieving the dependency for each Java file
     * 
     * @param packageSrcFolder   The path to the package folder.
     * @param excludedPackageSrc The path to the excluded package folder. This is
     *                           used to exclude dependencies that are internal to
     *                           the package.
     * @return A future that will be completed with the package dependencies or
     *         failed with an error if the package folder does not exist or is not a
     *         directory.
     */
    private Future<PackageDepsReport> getPackageDependencies(final String packageSrcFolder,
            final String excludedPackageSrc) {
        Promise<PackageDepsReport> packagePromise = Promise.promise();
        FileSystem fileSystem = vertx.fileSystem();

        fileSystem.exists(packageSrcFolder)
                .compose((Boolean result) -> {
                    // Check if the package folder exists
                    if (result) {
                        return fileSystem.readDir(packageSrcFolder);
                    } else {
                        return Future.failedFuture("Package folder not found: " + packageSrcFolder);
                    }
                })
                .onSuccess(pkgContent -> {
                    Set<String> dependencies = new HashSet<>(); // Use a Set to avoid duplicates
                    List<Future<? extends AbstractReport>> futureList = new ArrayList<>();
                    for (String fileOrDir : pkgContent) {
                        // For each file or directory in the package retrieve its dependencies
                        futureList.add(
                                fileSystem
                                        .props(fileOrDir)
                                        .compose(props -> {
                                            Future<? extends AbstractReport> future;
                                            // Check if the file is a directory or a Java file
                                            if (props.isDirectory()) {
                                                // Recursively get dependencies from the subdirectory
                                                future = getPackageDependencies(fileOrDir, packageSrcFolder)
                                                        .onSuccess(report -> {
                                                            dependencies.addAll(report.getDependencies());
                                                        });
                                            } else if (fileOrDir.endsWith(".java")) {
                                                // Get dependencies from the Java file, excluding dependencies between
                                                // packages based on the higher level package
                                                String excludedPackage = excludedPackageSrc != null ? excludedPackageSrc
                                                        : packageSrcFolder;
                                                future = getClassDependencies(fileOrDir, excludedPackage)
                                                        .onSuccess(report -> {
                                                            dependencies.addAll(report.getDependencies());
                                                        });
                                            } else {
                                                // Unsupported file type, handle as needed
                                                // We can just ignore it
                                                future = Future.succeededFuture();
                                            }
                                            return future;
                                        }));
                    }
                    // Wait for all futures to complete
                    Future.all(futureList).onComplete(ar -> {
                        if (ar.succeeded()) {
                            // Filter out dependencies from the same package
                            packagePromise.complete(new PackageDepsReport(new ArrayList<>(dependencies)));
                        } else {
                            packagePromise.fail(ar.cause());
                        }
                    });
                })
                .onFailure(throwable -> {
                    // Package folder not found or error reading it
                    packagePromise.fail(throwable);
                });
        return packagePromise.future();
    }

    /**
     * Get the dependencies of a project. The project is a folder that contains
     * Java files and directories. The dependencies are the Java files that are not
     * in
     * the same package as the project. The dependencies are retrieved by
     * treating the project as a package and then removing the dependencies that
     * are internal to the project.
     * 
     * @param projectSrcFolder The path to the project folder.
     * @return A future that will be completed with the project dependencies or
     *         failed with an error if the project folder does not exist or is not a
     *         directory.
     */
    public Future<ProjectDepsReport> getProjectDependencies(final String projectSrcFolder) {
        Promise<ProjectDepsReport> projectPromise = Promise.promise();
        FileSystem fileSystem = vertx.fileSystem();
        Set<String> dependencies = new HashSet<>(); // Use a Set to avoid duplicates

        fileSystem.exists(projectSrcFolder)
                .compose((Boolean result) -> {
                    // Check if the project folder exists
                    if (result) {
                        return fileSystem.props(projectSrcFolder);
                    } else {
                        return Future.failedFuture("Project folder not found: " + projectSrcFolder);
                    }
                })
                .compose((FileProps props) -> {
                    // Check if the project folder is a directory
                    if (props.isDirectory()) {
                        return fileSystem.readDir(projectSrcFolder);
                    } else {
                        return Future.failedFuture("Project folder is not a directory: " + projectSrcFolder);
                    }
                })
                .compose((List<String> projectContent) -> {
                    return getPackageDependencies(projectSrcFolder)
                            .compose(dependenciesReport -> {
                                // Add the dependencies from the package report to the project dependencies
                                dependencies.addAll(dependenciesReport.getDependencies());
                                List<Future<?>> futureList = new ArrayList<>();
                                // For each file or directory in the project, check if it is a directory and
                                // remove dependencies that start with the directory name (to remove internal
                                // packages)
                                for (String fileOrDir : projectContent) {
                                    futureList.add(
                                            fileSystem.props(fileOrDir)
                                                    .onSuccess(props -> {
                                                        if (props.isDirectory()) {
                                                            dependencies.removeIf(dep -> dep.startsWith(fileOrDir
                                                                    .substring(fileOrDir.lastIndexOf(File.separator)
                                                                            + 1)));
                                                        }
                                                    }));
                                }
                                return Future.all(futureList);
                            });
                })
                .onComplete(ar -> {
                    // Wait for all futures to complete and resolve the project promise
                    if (ar.succeeded()) {
                        projectPromise.complete(new ProjectDepsReport(new ArrayList<>(dependencies)));
                    } else {
                        projectPromise.fail(ar.cause());
                    }
                });
        ;
        return projectPromise.future();
    }

}

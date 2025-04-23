package pcd.ass02.library;

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
import io.vertx.core.file.FileSystem;
import pcd.ass02.common.reports.AbstractReport;
import pcd.ass02.common.reports.ClassDepsReport;
import pcd.ass02.common.reports.PackageDepsReport;
import pcd.ass02.common.reports.ProjectDepsReport;

public class DependencyAnalyzer {

    private final Vertx vertx; // TODO: Valutare quando si deploya il verticle

    public DependencyAnalyzer() {
        this.vertx = Vertx.vertx();
    }

    public DependencyAnalyzer(Vertx vertx) {
        this.vertx = vertx;
    }

    public Future<ClassDepsReport> getClassDependencies(final String classSrcFile) {
        return getClassDependencies(classSrcFile, null);
    }

    private Future<ClassDepsReport> getClassDependencies(final String classSrcFile, final String excludedPackageSrc) {

        Promise<ClassDepsReport> classPromise = Promise.promise();
        FileSystem fs = vertx.fileSystem();

        var classPath = classSrcFile.substring(0, classSrcFile.lastIndexOf(File.separator));
        fs.exists(classSrcFile)
                .compose((Boolean result) -> {
                    // TODO: Check if the file exists, this code is maybe correct right now
                    return result ? fs.readFile(classSrcFile) : Future.failedFuture("File not found: " + classSrcFile);
                })
                .onSuccess((Buffer buffer) -> {
                    // Process the file content and generate the ClassDepsReport
                    List<String> dependencies = new ArrayList<>();
                    try {
                        JavaParser parser = new JavaParser(new ParserConfiguration()
                                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21));

                        // Parse the class source file
                        CompilationUnit compilationUnit = parser.parse(buffer.toString()).getResult()
                                .orElseThrow(() -> new RuntimeException("Failed to parse class file"));

                        // Collect imported classes
                        for (ImportDeclaration importDecl : compilationUnit.findAll(ImportDeclaration.class)) {
                            dependencies.add(importDecl.getNameAsString());
                        }

                        var packageName = compilationUnit.getPackageDeclaration()
                                .map(pkg -> pkg.getNameAsString())
                                .orElse("");

                        compilationUnit.findAll(ClassOrInterfaceType.class).forEach(type -> {
                            var typeName = type.getNameAsString();
                            var fullyQualifiedName = packageName + "." + typeName;
                            var classFilePath = classPath + File.separator + typeName + ".java";
                            if (dependencies.stream().noneMatch(dep -> dep.endsWith("." + typeName))) {
                                if (fs.existsBlocking(classFilePath)) { // TODO: Change to async
                                    // Add the dependency to the list
                                    dependencies.add(fullyQualifiedName);
                                }
                            }
                        });

                        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(classDec -> {
                            // Filter out dependencies from the excluded package
                            var classQualifiedName = classDec.getFullyQualifiedName().get();
                            if (excludedPackageSrc != null) {
                                String srcRoot = classSrcFile.replace(".java", ""); // Remove .java extension
                                srcRoot = srcRoot.substring(0, srcRoot.length() - classQualifiedName.length()); // Remove
                                                                                                                // class
                                                                                                                // name
                                var excludedPackage = excludedPackageSrc.replace(srcRoot, "")
                                        .replace(File.separatorChar, '.'); // Remove .java extension
                                dependencies.removeIf(dep -> dep.startsWith(excludedPackage));
                            }
                        });
                    } catch (Exception e) {
                        classPromise.fail(e);
                        System.err.println("Error parsing class file: " + e.getMessage());
                        return;
                    }
                    classPromise.complete(new ClassDepsReport(dependencies));
                })
                .onFailure((Throwable t) -> {
                    // Handle the error
                    classPromise.fail(t);
                });

        return classPromise.future();
    }

    public Future<PackageDepsReport> getPackageDependencies(final String packageSrcFolder) {
        return getPackageDependencies(packageSrcFolder, null);
    }

    private Future<PackageDepsReport> getPackageDependencies(final String packageSrcFolder,
            final String excludedPackageSrc) {
        Promise<PackageDepsReport> packagePromise = Promise.promise();
        FileSystem fileSystem = vertx.fileSystem();

        fileSystem.exists(packageSrcFolder)
                .compose((Boolean result) -> {
                    if (result) {
                        return fileSystem.readDir(packageSrcFolder);
                    } else {
                        return Future.failedFuture("Package folder not found: " + packageSrcFolder);
                    }
                })
                .onSuccess(pkgContent -> {
                    Set<String> dependencies = new HashSet<>();
                    List<Future<? extends AbstractReport>> futureList = new ArrayList<>();
                    for (String fileOrDir : pkgContent) {
                        futureList.add(fileSystem.props(fileOrDir)
                                .compose(props -> {
                                    Future<? extends AbstractReport> future;
                                    if (props.isDirectory()) {
                                        future = getPackageDependencies(fileOrDir, packageSrcFolder)
                                                .onSuccess(report -> {
                                                    dependencies.addAll(report.getDependencies());
                                                });
                                    } else if (fileOrDir.endsWith(".java")) {
                                        var excludedPackage = excludedPackageSrc != null ? excludedPackageSrc
                                                : packageSrcFolder;
                                        future = getClassDependencies(fileOrDir, excludedPackage).onSuccess(report -> {
                                            dependencies.addAll(report.getDependencies());
                                        });
                                    } else {
                                        future = Future.failedFuture("Unsupported file type: " + fileOrDir);
                                    }
                                    return future;
                                }));
                    }
                    Future.all(futureList).onComplete(ar -> {
                        if (ar.succeeded()) {
                            // Filter out dependencies from the same package
                            packagePromise.complete(new PackageDepsReport(new ArrayList<>(dependencies)));
                        } else {
                            packagePromise.fail(ar.cause());
                        }
                    });
                });
        return packagePromise.future();
    }

    public Future<ProjectDepsReport> getProjectDependencies(final String projectSrcFolder) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}

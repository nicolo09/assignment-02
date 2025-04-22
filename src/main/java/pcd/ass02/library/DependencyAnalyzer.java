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

    private Vertx vertx = Vertx.vertx();

    public DependencyAnalyzer() {
        // Constructor
    }

    public Future<ClassDepsReport> getClassDependencies(final String classSrcFile) {

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
                            if (dependencies.stream().noneMatch(dep -> dep.endsWith("." + typeName))) {
                                var classFilePath = classPath + File.separator + typeName + ".java";
                                if (fs.existsBlocking(classFilePath)) {
                                    dependencies.add(packageName + "." + typeName);
                                }
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
                                        future = getPackageDependencies(fileOrDir).onSuccess(report -> {
                                            dependencies.addAll(report.getDependencies());
                                        });
                                    } else if (fileOrDir.endsWith(".java")) {
                                        future = getClassDependencies(fileOrDir).onSuccess(report -> {
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

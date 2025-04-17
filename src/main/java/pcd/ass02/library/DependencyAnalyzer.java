package pcd.ass02.library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import pcd.ass02.common.reports.ClassDepsReport;
import pcd.ass02.common.reports.PackageDepsReport;
import pcd.ass02.common.reports.ProjectDepsReport;

public class DependencyAnalyzer {

    public Future<ClassDepsReport> getClassDependencies(final String classSrcFile) {

        Promise<ClassDepsReport> classPromise = Promise.promise();
        FileSystem fs = Vertx.vertx().fileSystem();

        var classPath = classSrcFile.substring(0, classSrcFile.lastIndexOf(File.separator));
        fs.exists(classSrcFile)
        .compose((Boolean result) -> {
            //TODO: Check if the file exists, this code is not correct right now
            return result ? fs.readFile(classSrcFile) : Future.failedFuture("File not found: " + classSrcFile);
        })
        .onComplete((AsyncResult<Buffer> buffer) -> {
            // Process the file content and generate the ClassDepsReport
            //TODO: Are import the only dependencies we need to check? NO, class in the same package are not imported
            if (buffer.succeeded()) {
                List<String> dependencies = new ArrayList<>();
                try {
                    // Parse the class source file
                    var compilationUnit = StaticJavaParser.parse(buffer.result().toString());

                    // Collect imported classes
                    for (ImportDeclaration importDecl : compilationUnit.findAll(ImportDeclaration.class)) {
                        dependencies.add(importDecl.getNameAsString());
                    }
                    
                    var packageName = compilationUnit.getPackageDeclaration()
                        .map(pkg -> pkg.getNameAsString())
                        .orElse("");

                    compilationUnit.findAll(com.github.javaparser.ast.type.ClassOrInterfaceType.class).forEach(type -> {
                        var typeName = type.getNameAsString();
                        if (dependencies.stream().noneMatch(dep -> dep.endsWith("." + typeName))) {
                            var classFilePath = classPath + File.separator +  typeName + ".java";
                            if (fs.existsBlocking(classFilePath)) {
                                dependencies.add(packageName + "." + typeName);
                            }
                        }
                    });

                } catch (Exception e) {
                    classPromise.fail(e);
                }

                classPromise.complete(new ClassDepsReport(dependencies));

            } else {
                classPromise.fail(buffer.cause());
            }

        });

        return classPromise.future();
    }

    public Future<PackageDepsReport> getPackageDependencies(final String packageSrcFolder) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public Future<ProjectDepsReport> getProjectDependencies(final String projectSrcFolder) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}

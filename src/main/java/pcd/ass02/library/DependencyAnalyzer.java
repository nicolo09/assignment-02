package pcd.ass02.library;

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

        fs.exists(classSrcFile)
        .compose((Boolean result) -> {
            //TODO: Check if the file exists, this code is not correct right now
            return result ? fs.readFile(classSrcFile) : Future.failedFuture("File not found: " + classSrcFile);
        })
        .onComplete((AsyncResult<Buffer> buffer) -> {
            // Process the file content and generate the ClassDepsReport
            //TODO: Are import the only dependencies we need to check? NO, class in the same package are not imported
            if (buffer.succeeded()) {
                try {
                    String content = buffer.result().toString();
                    var compilationUnit = StaticJavaParser.parse(content);
                    
                    // Extract import declarations
                    List<ImportDeclaration> imports = compilationUnit.findAll(ImportDeclaration.class);
                    List<String> dependencies = new ArrayList<>(imports.stream()
                                                                      .map(ImportDeclaration::getNameAsString)
                                                                      .toList());

                    // Add dependencies from the same package (only the ones actually used in the class)
                    String packageName = compilationUnit.getPackageDeclaration()
                                                        .map(pkg -> pkg.getNameAsString())
                                                        .orElse("");
                    List<String> usedTypes = compilationUnit.findAll(com.github.javaparser.ast.type.ClassOrInterfaceType.class)
                                                   .stream()
                                                   .map(type -> type.getNameAsString())
                                                   .toList();
                    compilationUnit.findAll(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class)
                                   .stream()
                                   .filter(cls -> usedTypes.contains(cls.getNameAsString()))
                                   .forEach(cls -> dependencies.add(packageName + "." + cls.getNameAsString()));

                    classPromise.complete(new ClassDepsReport(dependencies));
                } catch (Exception e) {
                    classPromise.fail(e);
                }
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

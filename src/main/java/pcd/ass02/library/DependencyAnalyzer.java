package pcd.ass02.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.SymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.utils.SourceRoot;

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
                    // TODO: Check if the file exists, this code is not correct right now
                    return result ? fs.readFile(classSrcFile) : Future.failedFuture("File not found: " + classSrcFile);
                })
                .onComplete((AsyncResult<Buffer> buffer) -> {
                    // Process the file content and generate the ClassDepsReport
                    // TODO: Are import the only dependencies we need to check? NO, class in the
                    // same package are not imported
                    if (buffer.succeeded()) {
                        try {
                            String content = buffer.result().toString();
                            CompilationUnit compilationUnit = StaticJavaParser.parse(content);

                            Optional<String> pkg = compilationUnit.getPackageDeclaration()
                                    .map(pack -> pack.getNameAsString());
                            if (pkg.isPresent()) {
                                var newCompUnit = getResolverCompilationUnit(classSrcFile, pkg.get(), content);
                                if (newCompUnit.isPresent()) {
                                    compilationUnit = newCompUnit.get();
                                } else {
                                    //TODO: Handle this
                                }
                            }

                            compilationUnit.findAll(ClassOrInterfaceType.class).get(1).resolve();

                            List<String> dependencies = new ArrayList<String>();

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

    private Optional<CompilationUnit> getResolverCompilationUnit(final String classSrcFile, final String pkg, final String content) {
        String classFolder = classSrcFile.substring(0, classSrcFile.lastIndexOf(File.separator));
        if (classFolder.endsWith(pkg.replace(".", File.separator))) {
            // Symbol solver
            String srcDir = classFolder.substring(0, classFolder.length() - pkg.length() - 1);
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(new JavaParserTypeSolver(srcDir));
            // compilationUnit.setTypeSolver(typeSolver);
            ParserConfiguration parserConfig = new ParserConfiguration().setSymbolResolver(symbolSolver);
            SourceRoot sourceRoot = new SourceRoot(Paths.get(classSrcFile).getParent());
            JavaParser parser = new JavaParser(parserConfig);
            return Optional.of(parser.parse(content).getResult().get());
        }
        else{
            //TODO: Handle this
        }
        return Optional.empty();
    }

    public Future<PackageDepsReport> getPackageDependencies(final String packageSrcFolder) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public Future<ProjectDepsReport> getProjectDependencies(final String projectSrcFolder) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}

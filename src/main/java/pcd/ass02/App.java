package pcd.ass02;

import java.nio.file.Paths;

import pcd.ass02.library.DependencyAnalyzer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        DependencyAnalyzer analyzer = new DependencyAnalyzer();
        
        /*
        analyzer.getClassDependencies(Paths.get("src/main/java/pcd/ass02/library/DependencyAnalyzer.java").toString())
            .onComplete(result -> {
                if (result.succeeded()) {
                    System.out.println("Dependencies: " + System.lineSeparator());
                    result.result().getDependencies().stream().sorted().forEachOrdered(System.out::println);
                } else {
                    System.err.println("Failed to get dependencies: " + result.cause().getMessage());
                }
            });
        */

        analyzer.getPackageDependencies(Paths.get("src/main/java/pcd/ass02/library").toAbsolutePath().toString()).onComplete(result -> {
            if (result.succeeded()) {
                System.out.println("Package Dependencies: " + System.lineSeparator());
                result.result().getDependencies().stream().sorted().forEachOrdered(System.out::println);
                System.exit(0);
            } else {
                System.err.println("Failed to get package dependencies: " + result.cause().getMessage());
                System.exit(1);
            }
        });
    }
}

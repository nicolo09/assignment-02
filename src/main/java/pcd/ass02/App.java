package pcd.ass02;

import javafx.application.Application;
import pcd.ass02.library.DependencyAnalyzer;
import pcd.ass02.view.DependencyView;

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

        /*
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
        */

        /*
        analyzer.getProjectDependencies(Paths.get("src/main/java").toAbsolutePath().toString()).onComplete(result -> {
            if (result.succeeded()) {
                System.out.println("Project Dependencies: " + System.lineSeparator());
                result.result().getDependencies().stream().sorted().forEachOrdered(System.out::println);
                System.exit(0);
            } else {
                System.err.println("Failed to get package dependencies: " + result.cause().getMessage());
                System.exit(1);
            }
        });
        */

        // Start the JavaFX application
        Application.launch(DependencyView.class, args);
    }
}

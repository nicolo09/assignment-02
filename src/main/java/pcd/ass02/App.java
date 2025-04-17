package pcd.ass02;

import pcd.ass02.library.DependencyAnalyzer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        new DependencyAnalyzer().getClassDependencies("src\\main\\java\\pcd\\ass02\\library\\DependencyAnalyzer.java")
            .onComplete(result -> {
                if (result.succeeded()) {
                    System.out.println("Dependencies: " + System.lineSeparator());
                    result.result().dependencies().forEach(System.out::println);
                } else {
                    System.err.println("Failed to get dependencies: " + result.cause().getMessage());
                }
                System.exit(0);
            });
    }
}

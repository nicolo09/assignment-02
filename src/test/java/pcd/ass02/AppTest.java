package pcd.ass02;

import org.junit.jupiter.api.Test;
import pcd.ass02.library.DependencyAnalyzer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void classDependenciesTest() {
        new DependencyAnalyzer().getClassDependencies("src/main/java/pcd/ass02/library/DependencyAnalyzer.java")
                .onComplete(result -> {
                    if (result.succeeded()) {
                        assertFalse(result.result().dependencies().isEmpty());
                    } else {
                        System.err.println("Failed to get dependencies: " + result.cause().getMessage());
                    }
                    System.exit(0);
                });
        
    }

    @Test
    public void packageDependenciesTest(){
        /*
        new DependencyAnalyzer().getPackageDependencies("src/main/java/pcd/ass02/library").onComplete(result->{
            //todo: implement get package dependencies
        });
        */
        assertThrows(UnsupportedOperationException.class, ()->new DependencyAnalyzer().getPackageDependencies("src/main/java/pcd/ass02/library"));
    }

    @Test
    public void projectDependenciesTest(){
        /*
        new DependencyAnalyzer().getProjectDependencies("src/main/java/pcd/ass02/library").onComplete(result->{
            //todo: implement get package dependencies
        });
        */
        assertThrows(UnsupportedOperationException.class, ()->new DependencyAnalyzer().getProjectDependencies("src/main/java/pcd"));
    }
}

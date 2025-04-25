package pcd.ass02;

import org.junit.jupiter.api.Test;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import pcd.ass02.async.library.DependencyAnalyzer;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private static final int AWAIT_TIME = 10;

    Vertx vertx = Vertx.vertx();
    DependencyAnalyzer analyzer = new DependencyAnalyzer(vertx);

    @Test
    public void cubeDependenciesTest() {
        Path testFile = Paths.get("src/test/java/mock/model/Cube.java");
        VertxTestContext testContext = new VertxTestContext();
        analyzer.getClassDependencies(testFile.toAbsolutePath().toString())
                .onComplete(result -> {
                    if (result.succeeded()) {
                        if (Set.copyOf(result.result().getDependencies()).equals(Set.of("mock.model.Point3D"))) {
                            testContext.completeNow();
                        } else {
                            testContext.failNow(new Throwable("Dependencies do not match"));
                        }
                    } else {
                        testContext.failNow(result.cause());
                    }
                });
        try {
            assertTrue(testContext.awaitCompletion(AWAIT_TIME, TimeUnit.SECONDS));
        } catch (Exception e) {
            fail(e);
        }
        if (testContext.failed()) {
            fail(testContext.causeOfFailure());
        }
    }

    @Test
    public void packageDependenciesTest() {
        Path testPackage = Paths.get("src/test/java/mock/view");
        VertxTestContext testContext = new VertxTestContext();
        analyzer.getPackageDependencies(testPackage.toAbsolutePath().toString())
                .onComplete(result -> {
                    if (result.succeeded()) {
                        if (Set.copyOf(result.result().getDependencies()).equals(Set.of("mock.model.Cube"))) {
                            testContext.completeNow();
                        } else {
                            testContext.failNow(new Throwable("Dependencies do not match"));
                        }
                    } else {
                        testContext.failNow(result.cause());
                    }
                });
        try {
            assertTrue(testContext.awaitCompletion(AWAIT_TIME, TimeUnit.SECONDS));
        } catch (Exception e) {
            fail(e);
        }
        if (testContext.failed()) {
            fail(testContext.causeOfFailure());
        }
    }

    @Test
    public void projectDependenciesTest() {
        Path testProject = Paths.get("src/test/java/mock");
        VertxTestContext testContext = new VertxTestContext();
        analyzer.getProjectDependencies(testProject.toAbsolutePath().toString())
                .onComplete(result -> {
                    if (result.succeeded()) {
                        if (Set.copyOf(result.result().getDependencies()).equals(Set.of("java.util.List", "java.util.ArrayList"))) {
                            testContext.completeNow();
                        } else {
                            testContext.failNow(new Throwable("Dependencies do not match"));
                        }
                    } else {
                        testContext.failNow(result.cause());
                    }
                });
        try {
            assertTrue(testContext.awaitCompletion(AWAIT_TIME, TimeUnit.SECONDS));
        } catch (Exception e) {
            fail(e);
        }
        if (testContext.failed()) {
            fail(testContext.causeOfFailure());
        }
    }
}

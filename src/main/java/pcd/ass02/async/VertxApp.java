package pcd.ass02.async;

import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import io.vertx.core.AsyncResult;
import pcd.ass02.async.common.reports.AbstractReport;
import pcd.ass02.async.library.DependencyAnalyzer;

public class VertxApp {

    private static void printDepsAndExit(AsyncResult<? extends AbstractReport> result) {
        if (result.succeeded()) {
            System.out.println("Dependencies: " + System.lineSeparator());
            result.result().getDependencies().stream().sorted().forEachOrdered(System.out::println);
            System.exit(0);
        } else {
            System.err.println("Failed to get dependencies: " + result.cause().getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {

        Options options = new Options();
        OptionGroup optionGroup = new OptionGroup();

        optionGroup.addOption(new Option("c", "class", true, "Class to analyze"));
        optionGroup.addOption(new Option("p", "package", true, "Package to analyze"));
        optionGroup.addOption(new Option("j", "project", true, "Project to analyze"));

        options.addOptionGroup(optionGroup);

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = null;

        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (Exception e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("DependencyAnalyzer", options);
            System.exit(1);
        }

        if (!Objects.isNull(commandLine)) {
            DependencyAnalyzer analyzer = new DependencyAnalyzer();
            if (commandLine.hasOption("class")) {
                String classPath = commandLine.getOptionValue("class");
                analyzer.getClassDependencies(Paths.get(classPath).toString())
                        .onComplete(VertxApp::printDepsAndExit);
            } else if (commandLine.hasOption("package")) {
                String packagePath = commandLine.getOptionValue("package");
                analyzer.getPackageDependencies(Paths.get(packagePath).toAbsolutePath().toString())
                        .onComplete(VertxApp::printDepsAndExit);
            } else if (commandLine.hasOption("project")) {
                String projectPath = commandLine.getOptionValue("project");
                analyzer.getProjectDependencies(Paths.get(projectPath).toAbsolutePath().toString())
                        .onComplete(VertxApp::printDepsAndExit);
            }
        }

    }
}

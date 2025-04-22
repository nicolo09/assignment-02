PCD a.y. 2024-2025 - ISI LM UNIBO - Cesena Campus

# Assignment #02 -  Find the Dependencies

v0.10.0-20250414

The assignment includes two points, the first about asynchronous programming and the second about reactive programming.

1) Develop a `DependecyAnalyserLib` asynchronous library providing asynchronous methods to analyse the dependencies in a Java project, tracking what are the types (interfaces/classes) and related packages that are used/accessed by any interface/class/package belonging to the project. In particular, the signatures of the async methods should correspond to the following pseudocode definitions:  

- `getClassDependencies(classSrcFile)` asynchronously  producing a result of type `ClassDepsReport`, including the list of types (classes or interfaces) used or accessed in this class; 
- `getPackageDependencies(packageSrcFolder)` asynchronously  producing a result of type `PackageDepsReport`, including the list of all types (classes or interfaces) used or accessed by any class or interface in this package; 
- `getProjectDependencies(projectSrcFolder)` asynchronously producing a result of type `ProjectDepsReport`, including the list of types (classes or interfaces) used or accessed by any class or interface belonging to any package in this project.

Further notes:
  
- The library and testing program should be based on asynchronous programming based on event-loops. Different programming languages can be used. In Java, the suggested framework to be used is [Vert.x](https://vertx.io/), while in Javascript is [Node.js](https://node.js).
     
- [JavaParser](https://javaparser.org/) is the suggested library that can be used for parsing individual Java source files and build the AST.

- Besides the library, a program testing each async method should be included. 
       

2) Develop a GUI-based program called `DependecyAnalyser`, using a reactive programming based approach. The program should give the possibility to analyse and display dynamically/incrementally the dependencies found by interfaces/classes belonging to the project as a graph, possibly grouping interfaces/classes in packages.

The GUI should provide a component to select the source root folder, a button to start the process, and a panel to display the output of the ongoing process, including a couple of output boxed reporting the number of classes/interfaces analysed and the number of dependencies found.

Further notes:
  
- The suggested reactive programming framework to be adopted is [ReactiveX](https://reactivex.io/), e.g. [RxJava](https://github.com/ReactiveX/RxJava) if working in Java or the JVM.

- As for the previous point, [JavaParser](https://javaparser.org/) can be used for parsing individual Java source files and build the AST.

- The point (2) is **not** meant to reuse the library developed in point (1), based on async programming: the whole solution should be re-designed, non only the GUI part, exploiting specifically reactive programming.
 

### The deliverable

The deliverable must be a zipped folder `Assignment-02`, to be submitted on the course web site, including:  
- `src` directory with sources
- `doc` directory with a short report in PDF (`report.pdf`). The report should include:
	- A brief analsysis of the problem, focusing in particular aspects that are relevant from concurrent point of view.
	- A description of the adopted design, the strategy and architecture.
	- A description of the behaviour of the system using one or multiple Petri Nets, choosing the proper level of abstraction.

### FAQ

- **[Q]** *Do we need to track dependencies related to basic Java types and classes belonging to `java.lang`?* 
    - **[A]** You don't to track dependencies related to basic Java types and classes belonging to `java.lang`, but you can if you want. Personally I would see this not very useful, since, by default, every class depends on `java.lang`. 

- **[Q]** *In point 2, can we reuse the strategy adopted in point 1, so as to apply reactive programming just for the GUI part?*
    - **[A]** No, point 2 calls for rethinking also the strategy (not only the GUI) fully exploiting reactive programming, without reusing async programming as used in point 1.
 




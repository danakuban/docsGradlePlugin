# Docs Gradle Plugin

This project provides a gradle plugin to support the documentation of your project. 
It aggregates doc snippets of every spring module and collects documentation of entites, jobs and apis.

## Quickstart

Add the plugin 

``` id("io.github.danakuban.docs-gradle-plugin") ```

to your gradle build file. Then run

``` gradlew build```

and

``` gradlew buildDocs```

The latter task will create among others a file _index.md in $buildDir/docs, and a separate file _index.md for each module in $buildDir/docs/system/$module


The plugin must be included in every module and submodule you want an individual documentation for.

## Features

You can add several files to describe a module:

### Markdown files

**description.md:** You need to place a description.md file in the root of each module. It must not be empty. This description is parsed in every module's documentation and the overview of all modules.

**footer.md:** For further information of a module you may place a footer.md inside the /docs directory in the module's root.
This will be appended at the end of the module's documentation

### PlantUML

**system.puml:** To describe a module's structure you may place a document called system.puml inside the /docs directory in the module's root.
This will be included in the documentation of the module.

**erm.puml:** To describe the entity relations of a module you may place a document called erm.puml nside the /docs directory in the module's root.
This will be included in the documentation of the module.

### Annotations

**@EntityAnnotation:** Annotate each Spring entity with @EntityAnnotation to describe it. 
A list of all entities and their description will be included in the documentation of the module.\
__NOTE:__ This Annotation is required for each class marked with @Entity.

**@JobAnnotation:** Annotate each job with @JobAnnotation to describe it. 
A list of all jobs and their description will be included in the documentation of the module.\
__NOTE:__ This Annotation is required for each method marked with @PostConstruct, @EventListener or @Scheduled.

### Endpoints

This plugin locates all kubernetes services deployed by the [mayope deployment plugin](https://github.com/mayope/deployment-plugin) and lists it at the beginning of the module's documentation.
An endpoint will only be included, if a get request returns the HTTP status OK.
For each service the endpoint with an additional "/swagger-ui.html" wil be the first guess, if a request fails, the plain endpoint is requested.
This feature simplifies directly linking your swagger api if you have one.


# Demonstration

If you want to look for a practical example, check out the ExampleProject. 
It consists of two modules, one with a submodule and demonstrates nearly every aspect of this plugin (except the Endpoint Feature, because it is not deployed in a Kubernetes cluster).

### Integration with Hugo

In the ExampleProject you find the application of the DocsGradlePlugin. Furthermore you will find an integration with [Hugo](https://gohugo.io/) in the docs module.
Hugo is a framework to build static websites, for example from markdown files.
It is used here to assemble a website containing the documentation built with the DocsGradlePlugin.
To see it in action you have to install Hugo, then run

``` gradlew :exampleProject:docs:hugo ```

then navigate to the build folder in the ExampleProject and further $buildDir/docs/hugo and run

``` hugo server ```

A Hugo server will start providing a complete website with the documentation, accessible on localhost:1313 (or some other port, it will be stated in the output of the command).

If you want to create an own Hugo site for your documentation, check out the [Hugo get started guide](https://gohugo.io/getting-started/). 
The implementation of the tasks in the build.gradle.kts in the docs module in the exampleProject may be of interest as well.

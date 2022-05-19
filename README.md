<p align="center">
    <img src="docs/exodus.svg" alt="Exodus" height="142"/>
    <br>
    Exodus is a lightweight, drop-in migration runner for Spring.
</p>

<p>
    <img id="badge--java" src="https://img.shields.io/badge/Java-17%2B-b07219" alt="Java17" />
    <img id="badge--spring" src="https://img.shields.io/badge/Spring-5%2B-6db33f" alt="test coverage" />
    <img id="badge--tests" src="https://img.shields.io/badge/tests-100%25%20%E2%9C%94-brightgreen" alt="test coverage" />
    <img id="badge--version" src="https://img.shields.io/badge/version-1.0.0-white" alt="version" />
</p>


## Use Exodus in a Spring project

1. Download the latest `exodus.jar` from the [`/dist`](dist) directory.
2. Add this JAR under `src/main/resources/lib` in your Spring project.
3. Add the following lines to your application's entrypoint:
    1. Pass `scanBasePackages = {"com.albertomh.exodus"}` as a parameter to the `@SpringBootApplication` decorator.
    2. Instantiate an application context with: `SpringApplication.run(YourApplication.class, args)`.
    3. Call `applicationContext.start()` inside `main()` to emit a `ContextStartedEvent`. This is the cue for Exodus to run any pending migrations.

Use the [sample application entrypoint](docs/SampleApplicationEntrypoint.java) as a guide to have Exodus run migrations on startup for your Spring project.


## Develop

### Build

Build with `./mvnw clean package`. This will create a JAR under `target/`.  
JARs should be placed under `dist/` for new releases — avoid doing this manually and instead run the `new_release.sh` script that takes care of this and other release-time tasks for you.


### Testing

Tests are located under `src/test` and laid out in the way common to Java projects, replicating the file structure of the application source code. 
Verify any changes you make by running `./mvnw test` (suite of unit & integration tests) from the project root.


### Cutting a release

1. Merge all changes into the `main` branch and update the `<version>` property in the POM.
2. Run `new_release.sh` to run all tests, create a new JAR & place this in `/dist`, and update README badges.
3. Commit the new JAR, tag the new release in git, and push to origin.


---

Copyright 2022 Alberto Morón Hernández  
This software is provided as open-source under the MIT License.

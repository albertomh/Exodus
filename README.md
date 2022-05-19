<p align="center">
    <img src="docs/exodus.svg" alt="Exodus" height="142"/>
    <br>
    Exodus is a lightweight, drop-in migration runner for Spring.
</p>

<p>
    <img id="badge--java" src="https://img.shields.io/badge/Java-17%2B-b07219" alt="Java17" />
    <img id="badge--spring" src="https://img.shields.io/badge/Spring-5%2B-6db33f" alt="test coverage" />
    <img id="badge--tests" src="https://img.shields.io/badge/tests-100%25%20%E2%9C%94-brightgreen" alt="test coverage" />
    <img id="badge--version" src="https://img.shields.io/badge/version-0.0.1-white" alt="version" />
</p>


## Use Exodus in a Spring project

1. Download the latest `exodus.jar` from the [`/dist`](dist) directory.
2. Add this JAR under `src/main/resources/lib` in your Spring project.
3. Add the following lines to your application's entrypoint:
    1. Pass `scanBasePackages = {"com.albertomh.exodus"}` as a parameter to the `@SpringBootApplication` decorator.
    2. Instantiate an application context with: `SpringApplication.run(YourApplication.class, args)`
    3. Call `applicationContext.start()` inside `main()` to emit a `ContextStartedEvent` which is picked up by Exodus and used as the cue to run any pending migrations.

[Click here](docs/SampleApplicationEntrypoint.java) to see a sample Spring application entrypoint configured to have Exodus run migrations on startup.


---

Copyright 2022 Alberto Morón Hernández  
This software is provided as open-source under the MIT License.

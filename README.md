<p align="center">
    <img src="docs/exodus.svg" alt="Exodus" height="142"/>
    <br>
    Exodus is a light, drop-in migration runner for Spring.
</p>

<p>
    <img id="badge--java" src="https://img.shields.io/badge/Java-17%2B-b07219" alt="Java17" />
    <img id="badge--spring" src="https://img.shields.io/badge/Spring-5%2B-6db33f" alt="test coverage" />
    <img id="badge--tests" src="https://img.shields.io/badge/tests-100%25%20%E2%9C%94-brightgreen" alt="test coverage" />
    <img id="badge--size" src="https://img.shields.io/badge/JAR%20size-~7%20kb-blueviolet" alt="size" />
    <img id="badge--version" src="https://img.shields.io/badge/version-1.0.0-white" alt="version" />
</p>

Exodus' aim is not to compete with incumbent migration runners on number of features, but rather to remove bloat and offer a simple solution to migrations in Spring applications.


## Use Exodus in a Spring project
1. Download the latest `exodus.jar` from [`dist/`](dist) above and place this JAR in your Spring project's `src/main/resources/lib/` directory.
2. Add Exodus as a dependency in your POM, taking care to set the version property as appropriate:
```
<dependency>
    <groupId>com.albertomh</groupId>
    <artifactId>exodus</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${basedir}/src/main/resources/lib/exodus-1.0.0.jar</systemPath>
</dependency>
```
3. Using the [sample application entrypoint](docs/SampleApplicationEntrypoint.java) as a guide, do the following:
    - Pass `scanBasePackages = {"com.albertomh.exodus"}` as a parameter to the `@SpringBootApplication` decorator.
    - Instantiate an application context with `SpringApplication.run(YourApplication.class, args)`.
    - Call `applicationContext.start()` inside `main()` to emit a `ContextStartedEvent`. This is the cue for Exodus to run any pending migrations.

These three steps are all that is needed to add Exodus to a project — you can now start writing migrations.


### Write migrations
Exodus will pick up any `.sql` files you place under `src/main/resources/db/migration/` in your Spring application.  
The following two best practices are recommended (but not enforced by Exodus):
- Subdivide `db/migration/` into directories named after the year the migrations they hold were written in.
- Have migrations follow the naming convention `YYYY-MM-DD_HH.MM__<MODULE>__<CHANGE>.sql` where `<MODULE>` is a subdivision of your app's functionality and `<CHANGE>` is a concise summary of the change enacted by the migration. For instance: `1970-01-01_09.00__auth__create-user.sql`.


### Sample output
The following line will appear in your Spring application's log the first time you run it after installing Exodus:
```
| exodus - Table `_schema_migration` has been created.
```
Exodus will create the table `_schema_migration` to keep track of the migrations that have been applied:
| id          | applied_at  | file_name      | checksum       |
| ----------- | ----------- | -------------- | -------------- | 
| `SERIAL PK` | `TIMESTAMP` | `VARCHAR(255)` | `VARCHAR(128)` |

If a valid migration is found within `db/migration/` (at any depth, see above) `_schema_migration` will be queried. If the migration is not listed in this table it will be applied and the following logged to the console:
```
| exodus - Migration `1970-01-01_09.00__auth__create-user.sql` has been applied.
```

After every run Exodus will log to the console a summary of operations taken: 
```
| exodus - Ignored [5] existing migrations. Applied [2] new migrations.
```


## Develop

### Build
Build Exodus with `./mvnw clean package`. This will create a JAR under `target/`.  
JARs should be placed under `dist/` for new releases — avoid doing this manually and instead run the `new_release.sh` script that takes care of this and other release-time tasks for you.


### Test
Tests are located under `src/test/` and laid out in the way common to Java projects, replicating the file structure of the application source code. 
Verify any changes you make by running `./mvnw test` (suite of unit & integration tests) from the project root.


### Cut a release
1. Merge all changes into the `main` branch and update the `<version>` property in the POM.
2. Run `new_release.sh`. This will run all tests, create a new JAR & place it in `dist/`, and update README badges.
3. Commit the new JAR and update `RELEASES.md`.
4. Tag the new release in git and push to origin.


## Project structure

### Dependencies
Exodus makes use of components in `org.springframework.core`, `o.s.context`, `o.s.util`, and `o.s.jdbc`.  
At compile-time these are provided by the `spring-boot-starter-web` & `spring-boot-starter-data-jpa` libraries.  
At run-time these are **not** provided as part of an uberJAR since Exodus is designed to be a dependency of a Spring application and as such will make use of the Spring libraries available in its run-time context.

In a test context two further dependencies — `spring-boot-starter-test` & the `H2` database — are required. The former provides testing libraries such as JUnit5, while the latter is an in-memory database used to quickly build and tear down databases during unit and integration testing.


### Entrypoint
#### MigrationRunner
- A datasource is injected into `MigrationRunner`'s constructor and this sets up a connection and statement for use throughout the Exodus instance's lifetime.
- `getMigrationScripts()` fetches migration scripts from the resources directory.
- `createSchemaMigrationTable()` creates the `_schema_migration` table if one does not already exist.
- `onApplicationEvent()` is Exodus' main loop, invoked when a `ContextStartedEvent` is emitted by the Spring application on startup. It will create the table in which migrations are recorded, loop through every migration script & assess whether it needs to be applied, and log appropriate messages to the console.


### Utilities
#### DatabaseUtils
A collection of utilities to interact with the database (more accurately, the `DataSource` injected into the `MigrationRunner`). Provides ways to inspect the tables in the database, list applied migrations, and apply a pending migration.


### Tests
#### TestingUtils
This module provides utilities used exclusively by the test suite.
- `createSchemaMigrationTable()` creates the `_schema_migration` table as part of setting up unit tests so that they don't depend on the same functionality implemented in the `MigrationRunner`.
- `addRowToSchemaMigrationTable()` simulates the behaviour of the runner by appending a row with arbitrary data to the `_schema_migration` table.

#### MigrationRunnerTest
A suite of tests for Exodus' main loop. A utility creates synthetic `ContextStartedEvent`s to simulate a Spring application's starting up.  
Unit tests check methods that fetch migration scripts from the resources directory or create the `_schema_migration` table. Integration tests assess the main loop's functionality under different conditions (blank database, previously-applied migrations, etc.).

#### DatabaseUtilsTest
A suite of tests for the `DatabaseUtils` tools. Unit tests verify simple functions such as counting the number of tables or listing applied migrations. Integration tests check the functionality of more complex procedures such as applying a migration.

**For both test suites:**
- The constructor sets up a new ephemeral H2 datasource, connection, and statement when a suite is instantiated.
- `beforeEach()` drops all objects in the H2 test database to make each test independent.


#### Test resources
Test resources are provided under `src/test/resources/db/migration/`, mimicking their location in a production application using Exodus. Two migrations are provided:
- `test_migration.sql` a simple script that creates a basic `auth__user` table.
- `already_applied_migration.sql` an inert migration used to test Exodus' handling of an already-applied migration.


---

Copyright 2022 Alberto Morón Hernández  
This software is provided as open-source under the MIT License.

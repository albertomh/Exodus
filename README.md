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

Exodus' aim is not to compete with more mature migration runners in terms of features, but rather the opposite: to remove complexity and offer a light, simple solution to migrations in Spring applications.


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
    - Instantiate an application context with: `SpringApplication.run(YourApplication.class, args)`.
    - Call `applicationContext.start()` inside `main()` to emit a `ContextStartedEvent`. This is the cue for Exodus to run any pending migrations.

These three steps are all that is needed to add Exodus to a project — you can now start writing migrations.


### Writing migrations
Exodus will pick up any `.sql` files you place under `src/main/resources/db/migration/` in your Spring application.  
The following two best practices are recommended (but not enforced by Exodus):
- Subdivide `db/migrations/` into directories named after the year the migrations they hold were written in.
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

If a valid migration is found within `db/migrations` (at any depth, see above) `_schema_migration` will be queried. If the migration is not listed in this table it will be applied and the following logged to the console:
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

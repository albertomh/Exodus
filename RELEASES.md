### 1.0.0
2022-05-20  
**MVP release**  
- Exodus integrates with Spring's application lifecycle as a drop-in dependency to run & manage database migrations.
- Checks for new migrations and applies them on application startup, keeping track of them in a dedicated table.
- Full test coverage: a suite of unit & integration tests covering database utilities and Exodus' main loop.
- Compiles to a lightweight JAR (~7 kb).

---

Copyright 2022 Alberto Morón Hernández

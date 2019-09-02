# Example App using AgenaRisk 10 Java API

## Initialization

~~~~
git clone https://github.com/AgenaRisk/api-example-app.git
cd api-example-app
mvn clean compile
~~~~

## Launch Example

~~~~
mvn exec:java
~~~~

If you want to launch from terminal with another main class, add `'-Dexec.mainClass="example.Demo"'` argument to the `mvn exec:java` command, where `example.Demo` is your main package and class.

## Notes

* The project requires non-Java libraries to run
* Perform `mvn clean` to trigger downloading these libraries into `project/lib` directory
* On build, these will also be copied to `target/lib`
* Valid AgenaRisk Developer license (or a currently active Timed Trial) will be required to run this example
* See [API Readme](https://github.com/AgenaRisk/api/blob/master/README.md) for notes on license activation
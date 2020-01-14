# Example App using AgenaRisk 10 Java API

## Prerequisites
* JDK 8 (we recommend jdk1.8.0_192)
* (Linux: net-tools, iproute2)

## Initialization
Run the following commands in the terminal:
~~~~
git clone https://github.com/AgenaRisk/api-example-app.git
cd api-example-app
mvn clean compile
~~~~

## Launch Example

### API Demo
~~~~
mvn exec:java@demo
~~~~

### Legacy API Demo
~~~~
mvn exec:java@demo-legacy
~~~~

### Custom Main Class
You can launch the project with your own main class by running command with your full class name:
~~~~
mvn exec:java@custom '-Dexec.mainClass="my.main.NewClass"'
~~~~

## License Activation
See [API Readme](https://github.com/AgenaRisk/api/blob/master/README.md) for activation instructions.

To see a list of available activation commands, use:
~~~~
mvn exec:java@activate '-Dexec.args="-h"'
~~~~
If using Windows CMD, the quotation marks should be used as follows:
~~~~
mvn exec:java@activate "-Dexec.args=\"-h\""
~~~~

## Notes

* The project requires non-Java libraries to run
* Perform `mvn clean` to trigger downloading these libraries into `project/lib` directory
* On build, these will also be copied to `target/lib`
* Valid AgenaRisk Developer license (or a currently active Timed Trial) will be required to run this example
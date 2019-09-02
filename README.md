# Example App using AgenaRisk 10 Java API

## Initialization
Run the following commands in the terminal:
~~~~
git clone https://github.com/AgenaRisk/api-example-app.git
cd api-example-app
mvn clean compile
~~~~

## Launch Example

### New API Demo
~~~~
mvn exec:java@demo
~~~~

### Classic API Demo
~~~~
mvn exec:java@demo-classic
~~~~

### Custom Main Class
You can launch the project with your own main class by running command with your full class name:
~~~~
mvn exec:java@custom '-Dexec.mainClass="my.main.NewClass"'
~~~~

## Activation
You can activate AgenaRisk 10 Developer by running the following command with your valid license key:
~~~~
mvn exec:java@activate '-Dexec.args="--keyActivate 1234-ABCD-5678-EFGH"'
~~~~
For more details on activation, see [API Readme](https://github.com/AgenaRisk/api/blob/master/README.md).

To see a list of available activation commands, use:
~~~~
mvn exec:java@activate '-Dexec.args="-h"'
~~~~

## Notes

* The project requires non-Java libraries to run
* Perform `mvn clean` to trigger downloading these libraries into `project/lib` directory
* On build, these will also be copied to `target/lib`
* Valid AgenaRisk Developer license (or a currently active Timed Trial) will be required to run this example
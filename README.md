# Example App using AgenaRisk 10 Java API

## Documentation
[JavaDoc](https://agenarisk.github.io/api/)

## Prerequisites
* JDK 8
<br>We recommend jdk1.8.0_192
<br>Note: versions of Java above 8 have not been tested
* Maven
<br>Version >= 3.6.1
* Linux: net-tools, iproute2

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

For Linux bash:
~~~~
mvn exec:java@custom '-Dexec.mainClass=my.main.NewClass'
~~~~

## License Activation
See [API Readme](https://github.com/AgenaRisk/api/blob/master/README.md) for activation instructions.

To see a list of available activation commands, use (Linux shell or Windows Powershell):
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
* If you are working with SNAPSHOT dependencies, then some dependencies may have to be re-downloaded by executing `mvn dependency:purge-local-repository`
* Valid AgenaRisk Developer license (or a currently active Timed Trial) will be required to run this example
* You can also checkout the project with [TortoiseSVN](https://tortoisesvn.net/) and use an IDE like [NetBeans](https://netbeans.apache.org/download/) if you don't want to use the CLI approach
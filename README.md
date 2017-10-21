# jvm-prober
Give me a method signature and optionally an encoder and I will instrument it for you.

## Configuring

1. Edit the file `build.gradle` to include the project you wish to instrument. For example, the following snippet includes Spark:
```
dependencies {
    // framework to be probed
    compile 'org.apache.spark:spark-core_2.11:2.2.0'
}
```

2. Edit the file `src/main/resources/probes_spec.yaml` with the probe specifications, basically meaning that you have to provide a list of method signatures.
You can also provide custom encoders to modify method returns or to inspect the method call in a particular way.
By default we just log the calls.

## Building

1. Build the probes with the following command. It will read the probe specs and create aspects for instrumenting a JVM application.
```
./gradlew clean buildProbes build
```

## Running your application

1. Add `build/libs/jvm-prober.jar` to your application's classpath, which contains the configured probes.

2. Add the following argument to your JVM. This will override the default classloader to the Aspectj one supporting bytecode weaving:

```
-javaagent:${thisProjectAbsolutePath}/lib/aspectjweaver-1.8.10.jar
```

Hint: If you export the java tools options in your env, all JVM launched for that context will pick up the java agent above:

```
export JAVA_TOOLS_OPTIONS="-javaagent:${thisProjectAbsolutePath}/lib/aspectjweaver-1.8.10.jar"
```

3. This is it! One observation is that if the application is distributed, all the JVM instances must be configured according to steps 1 and 2.

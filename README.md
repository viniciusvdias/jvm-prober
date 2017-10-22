# jvm-prober
Give me a method signature and optionally an encoder and I will instrument it for you.

## Configuring

1. Edit the file `build.gradle` to include the project you wish to instrument. For example, the following snippet includes [Apache Spark](https://github.com/apache/spark):
```
dependencies {
    // framework to be probed
    compile 'org.apache.spark:spark-core_2.11:2.2.0'
}
```

2. Edit the file `src/main/resources/probes_spec.yaml` with the probe specifications, basically meaning that you have to provide a list of method signatures.
You can also provide custom [encoders](#encoders) to modify method returns or to inspect the method call in a particular way.
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

## Encoders

You can also bypass the instrumented methods by implementing a custom encoder for that particular probe. The default encoder
is `encoder.Encoder`, which just returns the original result. However you can implement your own encoder by extending that
class in this project. The following steps explain how would you do that:

1. Extend the class encoder and override the method `encode`. For example, the following encoder will bypass the original
return:

```
package encoder;

import example.Number;

// encoder for method [ example.Number example.Number.subtract(int) ]
// this encoder encoder bypasses the method by summing the argument instead of subtracting it
public class NumberSubtractToAddEncoder extends Encoder {
   public Object encode(Object caller, Object[] args, Object originalReturn) {
      Number number = (Number) caller;
      Integer n = (Integer) args[0];
      Number modifiedReturn = new Number(number.i + n);
      return modifiedReturn;
   }
}
```

Note that the arguments of `encode` are all of `Object` type, even for primitive types.
This is because `encode` could be instrumenting any
other method, but because we know we want to use it to instrument `subtract` it is safe to cast arguments and the
caller.

2. Now you just need to inform in the probe specifications file that you want to apply the encoder above to the probe.

```
probes:
    number_subtract:
        method: example.Number.subtract
        args:
            - int
        return: example.Number
        encoder: encoder.NumberSubtractToAddEncoder
```

3. Ok, now you are ready to [build](#building) and [run](#running-your-application) your application.

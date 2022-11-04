# Component Concept

There are 3 basic components that can be used within studies, namely:
* _Measurements_: which are used to collect data from sensors and store it to the DSB
* _Triggers_: that react on external or time-based events and trigger actions, and
* _Actions_: that executes actions with given parameters (configured and or runtime)

## Development:
Components should be developed in stand-alone modules or repositories.
They all depend on a shared mmb core module:
```xml
<dependency>
    <groupId>io.redlink.more.mmb</groupId>
    <artifactId>mmb-core</artifactId>
    <version>...</version>
</dependency>
```

All components consist at least of 2 classes, a factory and a component class.
*Hint:* for the matter of readability we use a simplified version of interfaces here (e.g. spare genertics, etc.)
To get more concrete information of how to use it, have a look into the `mmb-plugin-sample` module and included tests.

## Component Factory
```java
interface ComponentFactory {
  // a global unique id for the component
  String getId();
  // a meaningful title
  String getTitle();
  // a meaningful description (should include functionality and configuration details)
  String getDescription();
  // (Optional) a custom configuration or interaction component
  WebComponent getWebComponent();
  // instantiate a component with properties and a scoped SDK
  Component create(MorePlatformSDK sdk, P properties) throws ConfigurationValidationException;
}
```

## Components
```java
interface ComponentInterface {
  // validation of configuration properties  
  Properties validate(Properties properties) throws ConfigurationValidationException;
  // things that are done once per activation (e.g. add schedule via more sdk)
  void activate();
  // called on deactivation
  void deactivate();
}
```

## Executable Components
Triggers and Actions has an additional execute mechanism:
### Trigger Component
```java
Parameters execute(Parameters parameters);
```
### Action Components
```java
void execute(Parameters parameters);
```
These methods are called, when (in case of a trigger) a created schedule or hook is callen or
(in case of an action) when a trigger of the shared intervention is executed.

## More SDK
The more SDK has functions to store and retrieve values (scoped on the regarding component).
In addition, it provides methods to create and remove schedulers and webhooks for Triggers.
For Action it supports things like sendPushNotification, etc.
The SDK that is given to the components is scoped to a specific study (plus, in case of actions, to a specific participant).

Grouper-Docker-Dev-Example
==========================
This project is an example of how one might use Docker to facilitate Grouper development. The first part of this readme explains how to use the sample project with Docker, the second is the documentation for the sample project.

To use the image, you'll need to install the [Docker-Toolbox](https://www.docker.com/toolbox) if you are running Windows or OS X. Linux has native support for Docker (reference the Docker docs for your distro).

1. Start the docker machine: `docker-machine start <virtal machine name>`.
1. Setup your environmental variables: `docker-machine env <virtual machine name>`.
1. Reset and start the container from in the project root: `gradle clean runContainer`.
1. Browser to http://<docker-ip>:8080/grouper. 
1. Login via banderson/password (admin) or jsmith/password (standard).
1. Run through the test scenarios.
1. Make code changes, and start over at step 3.
1. When complete and time to clean up, call `gradle clean` then `docker-machine stop <virtal machine name>`.

> The first time this is run the grouper-demo will need to be pulled from the [Docker Hub](https://hub.docker.com/r/unicon/grouper-demo/). This will take significant time to pull the image the first time, especially over slower network links). Subsequent builds will be seconds.

The baseline `SelfOptOutPrivilegeRevocationVeto.java` has a bug that prevents "mailing list" groups from being deleted. Uncommenting the marked code block will "fix" the bug, when the cycle is re-run.

---

Set Self Opt Out privs Hook
==============================

### Assign self opt-out privilege hook
For ad hoc groups, users should be able to remove themselves from the group. Group admins should not be able to remove this.

## Requirements
This membership change hooks requires the following resources:

* A configured Grouper API, UI, and/or Web Services instance.

## Build
To build the jar, run:

```
gradle clean jar
```

## Installation
Installing the library is easy:

1. Build the jar (see above).
1. Copy the jar file to `$GROUPER_HOME/lib/custom` (or WEB-INF/lib).
1. Configure grouper logging to output as desired (see the tail of src/test/docker/log4j.properties for a reasonable default.
1. Override properties as needed (see below).
1. Create the `OptOutRequired` AttributeDef and AttributeDefName using something similar to:

```
addStem("etc:attribute", "custom", "Custom")
attributeStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "etc:attribute:custom", true);
attrDef = attributeStem.addChildAttributeDef("OptOutRequiredDef", AttributeDefType.attr);
attrDef.setAssignToGroup(true);
attrDef.setAssignToStem(true);
attrDef.setValueType(AttributeDefValueType.string);
attrDef.store();

attrDefName = attributeStem.addChildAttributeDefName(attrDef,  "optOutRequired", "Opt Out Required");
attrDefName.setDescription("Any value will indicate that OptOut will be enforced.");
attrDefName.store();
```

## Execution
(There is nothing to directly execute.)

## Properties
There are three sets of properties used by the hooks.
The **grouper settings** are used to specify the hook's settings. The **logging** settings are used to output the happenings
of the program. 

### Grouper Settings
The `GROUPER_HOME/conf/grouper.properties` file is used to specify the hooks settings.

|Property Name|Default Value|Notes|
|-------------|-------------|-----|
|hooks.group.class|(none)|Add package `edu.contoso.middleware.grouper.groupHooks.AssignSelfOptOutPrivilege` to the property value. (separate classes with a comma.) |
|hooks.membership.class|(none)|Add package `edu.contoso.middleware.grouper.membershipHooks.SelfOptOutPrivilegeRevocationVeto` to the property value. (separate classes with a comma.) |

|contoso.optOutRequired.attributeDefName|(required)|The Attribute DefName to check the group or parent tree to determine if OptOut is required.|
### Logging
The logging properties follow standard log4j settings.

> If the logging properties are not setup then no output will be returned from the program.

The following example can be appended to the `conf/log4j.properties` and will output to the console
(previously defined in a baseline grouper log4j.properties) and to a static file (`logs/customHooks.log`).

```
#######################################################
## Custom Hooks
#######################################################
## appender
log4j.appender.customHooks                            = org.apache.log4j.DailyRollingFileAppender
log4j.appender.customHooks.File                       = ${grouper.home}logs/customHooks.log
log4j.appender.customHooks.DatePattern                = '.'yyyy-MM-dd
log4j.appender.customHooks.layout                     = org.apache.log4j.PatternLayout
log4j.appender.customHooks.layout.ConversionPattern   = %d{ISO8601}: [%t] %-5p %C{1}.%M(line %L) - %m%n

## ND Hooks Logger
log4j.logger.edu.nd.middleware.grouper.groupHooks      = DEBUG, customHooks, grouper_stdout
log4j.logger.edu.nd.middleware.grouper.membershipHooks      = DEBUG, customHooks, grouper_stdout

```

## Local Development
This project has been supplemented with Docker. Docker's usage allows for quickly deploying the deployed artifact to a
consistent, repeatable, local Grouper environment, which facilitates consistent testing.

Docker (or boot2docker/docker-machine) for Windows and OS X installations) should be locally installed. If using boot2docker is being used
the proper environment variables must be setup (i.e. those displayed by running `boot2docker up` or `boot2docker shellinit`).

Running `gradle clean runContainer` will compile the jar, build the on top of the `grouper-demo` image (this could take 10-20 minutes
 the first time depending upon the bandwidth speed), and start an image. `docker ps` will display info about the running container. Running
 `docker exec -it grouper-dev bash` will allow one to connect into the running image. The image can be connected to from a browser
 by going to the port listed in the `docker ps` 8080 mapping (probably 8080). The customHooks.log can be dumped with
 `docker exec -t grouper-dev cat /logs/customHooks.log`.

 When testing is complete, `exit` to leave the running container. Then run `gradle clean` and `gradle resetContainer` to clean
  the environment. Now you are ready to make the necessary code changes and start over again.

The following test work against this container:

1. Add a group to `test:Mailing Lists`. Check the privs and the group should have its own priv listed as OptOut.
1. Try removing the OptOut priv. It should error.
1. Add a group to `test:Regular Groups`. Check the privs and no OptOut should be assigned.
1. Deleting the group(s) succeeds.

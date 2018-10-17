# CyberArk Conjur Plugin #

## Overview ##

The Conjur Plugin adds two capabilities to XL Deploy:

* New DynamicDictionary to __Environments__
* Conjur override to retrieve credentials for __Hosts__

## Requirements ##

* **XLDeploy**: version 8.0.0+
* **Conjur**: version 5.0+

## Installation ##

Place the plugin .xldp file into your `SERVER_HOME/plugins` directory.

## Configuration ##

Begin by configuring a Conjur server in the Configuration repository.

### Conjur URL ###

The URL to your Conjur Server.  The value should include the protocol.  E.g. http:// or https://.

### Conjur Account ###

The Conjur account or namespace that holds the variables.

### Conjur Username ###

The username XL Deploy will use to authenticate to the Conjur server.

### Conjur Password ###

The password XL Deploy will use to authenticate to the Conjur server.

### Check Connection ###

If true, XL Deploy will check connection to the host with the credentials supplied by Conjur before the deployment begins.



## DynamicDictionary ##

The Conjur DynamicDictionary is available under __Environments -> New -> Conjur -> DynamicDictionary__.  It behaves like a regular dictionary with the added feature that values can be looked up on Conjur at deployment time.  An entry that has a Conjur lookup for its value has the form:

```
$conjur:<key path>
```

At deployment time, when the dictionary is used for property placeholder substitution in the application, any key value that starts with '$conjur:' is a signal to the application to lookup the value in the associated Conjur server.  The plugin will lookup the Conjur value using the supplied key path and place that value in the dictionary.  Note that this is runtime behavior when values are retrieved from the dictionary.  The dictionary itself is not modified.

### Key Path Property Substitution ###

The key path you specify for the dictionary entry value may itself have property placeholders.  The following context variables are available to you to construct the key:

* app.name
* env.name
* host.name
* host.address
* host.username

#### Example: ####
 
The simplest key template might be:

```
host/<host.name>
```

If the host name were 'vm123' and the Credential Type is CT_USERNAME, this would result in these two variable keys:

```
host/vm123/username
host/vm123/password
```

## Notes ##
- The 'checkConnection' property allows to generate CheckConnection Step on all the hosts involved in the Conjur credentials process.
- Use _gradlew clean build_ for gradle build

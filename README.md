# XLD Conjur Plugin #

[![Build Status](https://travis-ci.org/xebialabs-community/xld-conjur-plugin.svg?branch=master)](https://travis-ci.org/xebialabs-community/xld-conjur-plugin)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/<insert project id>)](https://www.codacy.com/app/erasmussen39/xld-conjur-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=erasmussen39/xld-conjur-plugin&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/<insert project id>/maintainability)](https://codeclimate.com/github/erasmussen39/xld-conjur-plugin/maintainability)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Github All Releases][xld-conjur-plugin-downloads-image]]()


## Overview ##

The Conjur Plugin adds two capabilities to XL Deploy:

* New DynamicDictionary to __Environments__
* Retrieve credentials for __overthere.Hosts__

## Requirements ##

* **XLDeploy**: version 8.0.0+
* **Cyberark Conjur**: version 5.0+

## Installation ##

Place the plugin .xldp file into your `SERVER_HOME/plugins` directory.

## Configuration ##

Begin by configuring a Conjur server in the Configuration repository.

![ConjurServerConfiguration](images/conjur_server_config.png)

### Conjur URL ###

The URL to your Conjur Server.  The value should include the protocol e.g. http:// or https://.

### Conjur Account ###

The Conjur account or namespace that holds the variables.

### Conjur Username ###

The username XL Deploy will use to authenticate to the Conjur server.

### Conjur Password / API Key ###

The password or API Key XL Deploy will use to authenticate to the Conjur server.

### Control Task : Check Connection ###

The Conjur Plugin will check connection to the Conjur host with the credentials supplied.

## DynamicDictionary ##

The Conjur DynamicDictionary is available under __Environments -> New -> Conjur -> DynamicDictionary__.  It behaves like a regular dictionary with the added feature that values can be looked up on Conjur at deployment time.  An entry that has a Conjur lookup for its value has the form:

```
$conjur:<key path>
```

![ConjurDynamicDictionary](images/conjur_dynamic_dictionary.png)

At deployment time, when the dictionary is used for property placeholder substitution in the application, any key value that starts with '$conjur:' is a signal to the application to lookup the value in the associated Conjur server.  The plugin will lookup the Conjur value using the supplied key path and place that value in the dictionary.  Note that this is runtime behavior.  The values are looked up and supplied when values are retrieved from the dictionary.  The dictionary itself is not modified.

### Conjur Server ###

Indicate the Conjur Server to use for lookups.

### Control Task : Test Dictionary ###

The Conjur Plugin will call the Conjur server and attempt to retrieve values.  If any key referenced in the dictionary cannot be found in Conjur, an error will be raised.

## Host Credentials ##

Any __Infrastructure__ host-type that has __overthere.Host__ as its parent (e.g. overthere.SshHost or overthere.SmbHost), can now use Conjur values for any of its properties.

![ConjurHostProperties](images/conjur_host_properties.png)

Like the DynamicDictionary, instead of entering the actual value, enter '$conjur:\<key path\>'.  (Note that password fields will not show the entry as in the example above.)  During the deployment process, the plugin will retrieve the values from Conjur.

### Conjur Server ###

Indicate the Conjur Server to use for lookups.

## Developer Notes ##

* Set __xlDeployHome__ in __gradle.properties__ to your local XL Deploy instance.  Some XL Deploy packages are needed for the plugin to compile.
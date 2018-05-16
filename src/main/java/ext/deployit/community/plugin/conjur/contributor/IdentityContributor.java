/**
 * Copyright 2018 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ext.deployit.community.plugin.conjur.contributor;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.reflect.PropertyDescriptor;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugin.overthere.HostContainer;
import com.xebialabs.deployit.plugin.overthere.step.CheckCommandExecutionStep;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ext.deployit.community.plugin.conjur.ci.CredentialsType;
import net.conjur.api.Conjur;

public class IdentityContributor 
{
    // These are from the conjur endpoints class.  Unfortunately, private.
    private static final String CONJUR_URL = "CONJUR_APPLIANCE_URL";
    private static final String CONJUR_ACCOUNT = "CONJUR_ACCOUNT";

    @PrePlanProcessor
    static public List<Step> injectConjur(DeltaSpecification specification) {
        logger.trace("injectConjur()");

        final List<Delta> deltas = specification.getDeltas();
        final DeployedApplication deployedApplication = specification.getDeployedApplication();
        final Environment environment = deployedApplication.getEnvironment();

        Boolean override = environment.getProperty("overrideHostCredentials");
        if (!override)
            return null;

        // create conjur client
        System.setProperty(CONJUR_URL, environment.getProperty("conjurURL"));
        System.setProperty(CONJUR_ACCOUNT, environment.getProperty("conjurAccount"));
        String username = environment.getProperty("conjurUsername");
        String password = environment.getProperty("conjurPassword");

        Conjur conjur = new Conjur(username, password);

        // create set of hosts
        final Set<Host> hosts = ImmutableSet.<Host>builder()
                .addAll(filter(transform(deltas, DEPLOYED_TO_HOST), notNull()))
                .addAll(filter(transform(deltas, PREVIOUS_TO_HOST), notNull())).build();

        logger.info("Hosts {}", hosts);

        CredentialsType credentialType = environment.<CredentialsType>getProperty("credentialsType");
        logger.debug("CredentialsType {}", credentialType);

        switch (credentialType) {
            case CT_USERNAME:
                return injectUsernames(conjur, environment, hosts, deployedApplication);
            case CT_PRIVATEKEY:
                return injectSshKeys(conjur, environment, hosts, deployedApplication);
            default:
                return null;
        }
    }

    protected static List<Step> injectSshKeys(Conjur conjur, Environment environment, Set<Host> hosts, final DeployedApplication deployedApplication) {
        logger.trace("injectSshKeys()");

        final Iterable<List<Step>> transform = transform(hosts, new Function<Host, List<Step>>() {
            @Override
            public List<Step> apply(final Host host) 
            {
                String keyroot = formKeyRoot(host, environment, deployedApplication);

                host.setProperty("privateKeyFile", conjur.variables().retrieveSecret(String.format("%s/%s", keyroot, "privateKeyFile")));
                host.setProperty("passphrase", conjur.variables().retrieveSecret(String.format("%s/%s", keyroot, "passphrase")));

                if (!deployedApplication.hasProperty("checkConnection")) {
                    return null;
                }

                final Boolean checkConnection = deployedApplication.getProperty("checkConnection");
                return (checkConnection ? Collections.singletonList(new CheckCommandExecutionStep(host))
                        : Collections.EMPTY_LIST);
            }
        });

        return newArrayList(concat(transform));
    }

    protected static List<Step> injectUsernames(Conjur conjur, Environment environment, Set<Host> hosts, final DeployedApplication deployedApplication) {
        logger.trace("injectUsernames()");

        final Iterable<List<Step>> transform = transform(hosts, new Function<Host, List<Step>>() {
            @Override
            public List<Step> apply(final Host host) {
                String keyroot = formKeyRoot(host, environment, deployedApplication);

                logger.info(String.format("Setting credentials for host '%s'", host.getId()));
                host.setProperty("username", conjur.variables().retrieveSecret(String.format("%s/%s", keyroot, "username")));
                host.setProperty("password", conjur.variables().retrieveSecret(String.format("%s/%s", keyroot, "password")));
        
                if (!deployedApplication.hasProperty("checkConnection")) {
                    return null;
                }

                final Boolean checkConnection = deployedApplication.getProperty("checkConnection");
                return (checkConnection ? Collections.singletonList(new CheckCommandExecutionStep(host))
                        : Collections.EMPTY_LIST);
            }
        });
        return newArrayList(concat(transform));
    }

    protected static String formKeyRoot(Host host, final Environment environment, final DeployedApplication deployedApplication)
    {
        Map<String, String> valuesMap = new HashMap<String, String>();

        String appname = deployedApplication.getVersion().getApplication().getName();
        if ( appname.startsWith("Applications") )
        {
            appname = appname.substring("Applications/".length());
        }

        valuesMap.put("app.name", appname);
        valuesMap.put("env.name", environment.getName());
        valuesMap.put("host.name", host.getName());
        valuesMap.put("host.address", host.getProperty("address"));
        valuesMap.put("host.username", host.getProperty("username"));

        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        String keyTemplate = environment.getProperty("conjurKeyTemplate");
        String keyRoot = sub.replace(keyTemplate);

        logger.info(String.format("key root: %s", keyRoot));
        return keyRoot;
    }

    private static final Function<Delta, Host> DEPLOYED_TO_HOST = new ToHost() {
        public Host apply(Delta input) {
            return toHost(input.getDeployed());
        }
    };

    private static final Function<Delta, Host> PREVIOUS_TO_HOST = new ToHost() {
        public Host apply(Delta input) {
            return toHost(input.getPrevious());
        }
    };

    static abstract class ToHost implements Function<Delta, Host> {
        protected Host toHost(Deployed<?, ?> deployed) {
            if (deployed == null) {
                return null;
            }
            return toHost(deployed.getContainer());
        }

        private Host toHost(final ConfigurationItem item) {
            if (item instanceof Host) {
                return (Host) item;
            }
            if (item instanceof HostContainer) {
                HostContainer hostContainer = (HostContainer) item;
                return hostContainer.getHost();
            }
            final Collection<PropertyDescriptor> propertyDescriptors = item.getType().getDescriptor()
                    .getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getReferencedType() == null)
                    continue;
                if (propertyDescriptor.getReferencedType().instanceOf(Type.valueOf(Host.class))
                        || propertyDescriptor.isAsContainment()) {
                    final Host host = toHost((ConfigurationItem) propertyDescriptor.get(item));
                    if (host != null)
                        return host;
                }
            }
            return null;
        }
    }

    protected static final Logger logger = LoggerFactory.getLogger(IdentityContributor.class);

    public static String dump(Object o) {
        StringBuffer buffer = new StringBuffer();
        Class oClass = o.getClass();
         if (oClass.isArray()) {
             buffer.append("Array: ");
            buffer.append("[");
            for (int i = 0; i < Array.getLength(o); i++) {
                Object value = Array.get(o, i);
                if (value.getClass().isPrimitive() ||
                        value.getClass() == java.lang.Long.class ||
                        value.getClass() == java.lang.Integer.class ||
                        value.getClass() == java.lang.Boolean.class ||
                        value.getClass() == java.lang.String.class ||
                        value.getClass() == java.lang.Double.class ||
                        value.getClass() == java.lang.Short.class ||
                        value.getClass() == java.lang.Byte.class
                        ) {
                    buffer.append(value);
                    if(i != (Array.getLength(o)-1)) buffer.append(",");
                } else {
                    buffer.append(dump(value));
                 }
            }
            buffer.append("]\n");
        } else {
             buffer.append("Class: " + oClass.getName());
             buffer.append("{\n");
            while (oClass != null) {
                Field[] fields = oClass.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    buffer.append(fields[i].getName());
                    buffer.append("=");
                    try {
                        Object value = fields[i].get(o);
                        if (value != null) {
                            // if (value.getClass().isPrimitive() ||
                            //         value.getClass() == java.lang.Long.class ||
                            //         value.getClass() == java.lang.String.class ||
                            //         value.getClass() == java.lang.Integer.class ||
                            //         value.getClass() == java.lang.Boolean.class ||
                            //             value.getClass() == java.lang.Double.class ||
                            //         value.getClass() == java.lang.Short.class ||
                            //         value.getClass() == java.lang.Byte.class
                            //         ) {
                            //     buffer.append(value);
                            // } else {
                            //     buffer.append(dump(value));
                            // }
                            buffer.append(value);
                        }
                    } catch (IllegalAccessException e) {
                        buffer.append(e.getMessage());
                    }
                    buffer.append("\n");
                }
                oClass = oClass.getSuperclass();
            }
            buffer.append("}\n");
        }
        return buffer.toString();
    }
}

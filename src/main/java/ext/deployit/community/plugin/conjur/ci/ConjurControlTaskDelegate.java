/**
 * Copyright 2018 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ext.deployit.community.plugin.conjur.ci;


import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.reflect.Descriptor;
import com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry;
import com.xebialabs.deployit.plugin.api.reflect.PropertyDescriptor;
import com.xebialabs.deployit.plugin.api.udm.*;
import com.xebialabs.deployit.plugin.generic.step.ScriptExecutionStep;
import com.xebialabs.deployit.plugin.overthere.HostContainer;
import com.xebialabs.overthere.OperatingSystemFamily;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.xebialabs.deployit.plugin.generic.freemarker.ConfigurationHolder.RESOLVED_STRING_COLLECTION_SPLITTER;
import static com.xebialabs.deployit.plugin.generic.freemarker.ConfigurationHolder.resolveExpression;
import static java.lang.String.format;

public class ConjurControlTaskDelegate {

    @Delegate(name = "pcShellScript")
    public static List<Step> executedPCScriptDelegate(ConfigurationItem item, String name, Map<String, String> args, Parameters params) {
        System.out.println("LAD - In executedPCScriptDelegate");
        HostContainer targetHost = determineHost(item, name, args.get("host"), params);
        if (targetHost.getHost().getOs().equals(OperatingSystemFamily.UNIX)) {
            if (params.hasProperty("unixUsername")) {
                targetHost.getHost().setProperty("username", params.getProperty("unixUsername"));
            }
            if (params.hasProperty("unixPassword")) {
                targetHost.getHost().setProperty("password", params.getProperty("unixPassword"));
            }
            if (params.hasProperty("privateKey")) {
                targetHost.getHost().setProperty("privateKeyFile", params.getProperty("privateKey"));
            }
            if (params.hasProperty("passphrase")) {
                targetHost.getHost().setProperty("passphrase", params.getProperty("passphrase"));
            }
        }
        if (targetHost.getHost().getOs().equals(OperatingSystemFamily.WINDOWS)) {
            if (params.hasProperty("windowsUsername")) {
                targetHost.getHost().setProperty("username", params.getProperty("windowsUsername"));
            }
            if (params.hasProperty("windowsPassword")) {
                targetHost.getHost().setProperty("password", params.getProperty("windowsPassword"));
            }
        }
        return doShellScriptDelegate(item, name, args, params, targetHost);
    }

    private static List<Step> doShellScriptDelegate(ConfigurationItem item, String name, Map<String, String> args, Parameters params, HostContainer targetHost) {
        Map<String, Object> thisVarContext = createContext(item, params, targetHost);
        String script = determineScript(item, name, args);

        Step step = createStep(script, targetHost, args.get("classpathResources"), args.get("templateClasspathResources"), thisVarContext);

        return Collections.<Step>singletonList(step);
    }

    private static Map<String,Object> createContext(ConfigurationItem item, Parameters params, HostContainer host) {
        Map<String, Object> thisVarContext = newHashMap();

        thisVarContext.put("thisCi", item);
        System.out.println("LAD- Just placed thisCi in context");
        thisVarContext.put("targetHost", host);

        if (item instanceof Container) {
            thisVarContext.put("container", item);
        } else if (item instanceof Deployed) {
            thisVarContext.put("deployed", item);
            thisVarContext.put("container", item.getProperty("container"));
        } else {
            thisVarContext.put("container", host);
        }

        if (params != null) {
            thisVarContext.put("params", params);
        }

        return thisVarContext;
    }

    private static String determineScript(ConfigurationItem item, String name, Map<String, String> args) {
        String script = args.get("script");
        if (isNullOrEmpty(script)) {  //backward compatibility
            String scriptPropertyName = name + "Script";
            Descriptor descriptor = DescriptorRegistry.getDescriptor(item.getType());
            PropertyDescriptor propertyDescriptor = descriptor.getPropertyDescriptor(scriptPropertyName);
            checkArgument(propertyDescriptor!= null, "Control task script property %s not defined for CI type %s", scriptPropertyName, item.getType());
            script = (String) propertyDescriptor.get(item);
        }
        checkArgument(!isNullOrEmpty(script), "Argument [script] is required.");
        return script;
    }

    private static Step createStep(String script, HostContainer host, String classpathResources, String templateClasspathResources, Map<String,Object> thisVarContext) {
        String scriptName = resolveExpression(script, thisVarContext);
        String targetFile = ScriptExecutionStep.resolveOsSpecificFileName(scriptName, host);
        String desc = "Executing " + targetFile + " on " + host;
        ScriptExecutionStep step = new ScriptExecutionStep(1, scriptName, host, thisVarContext, desc);
        if (!isNullOrEmpty(classpathResources)) {
            Iterable<String> resources = RESOLVED_STRING_COLLECTION_SPLITTER.split(classpathResources);
            step.setClasspathResources(resolveExpression(newArrayList(resources), thisVarContext));
        }

        if (!isNullOrEmpty(templateClasspathResources)) {
            Iterable<String> resources = RESOLVED_STRING_COLLECTION_SPLITTER.split(templateClasspathResources);
            step.setTemplateClasspathResources(resolveExpression(newArrayList(resources), thisVarContext));
        }

        return step;
    }

    private static HostContainer determineHost(ConfigurationItem item, String name, String hostExpression, Parameters params) {
        if (hostExpression == null) {
            if (item instanceof HostContainer) {
                return (HostContainer) item;
            } else if (item instanceof Deployed && item.getProperty("container") instanceof HostContainer) {
                return item.getProperty("container");
            } else {
                throw new IllegalStateException(format("Cannot determine host for method [%s.%s], please use [host] argument to specify target host on which to run script(s).", item.getType(),name));
            }
        }

        Pattern expressionPattern = Pattern.compile("^\\$\\{([\\w.]+)\\}$");
        Matcher matcher = expressionPattern.matcher(hostExpression);
        if (matcher.matches()) {
            Deque<String> pathStack = new ArrayDeque<String>(Arrays.asList(matcher.group(1).split("[.]")));
            String startingCiName = pathStack.pop();
            ConfigurationItem startingCi = null;

            if (startingCiName.equals("thisCi") || startingCiName.equals("container") || startingCiName.equals("deployed")) {
                startingCi = item;
            } else if (startingCiName.equals("params")) {
                startingCi = params;
            } else {
                startingCi = item;
                pathStack.addFirst(startingCiName);
            }

            try {
                ConfigurationItem potentialHostCi = getProperty(startingCi, pathStack);
                if (potentialHostCi instanceof HostContainer) {
                    return (HostContainer) potentialHostCi;
                } else {
                    throw new IllegalArgumentException(format("Expression [%s] for argument [%s.%s.host] resolved to a type other than [ . ", hostExpression, item.getType(),name));
                }
            } catch (IllegalArgumentException e) {
                String invalidExpressionMsg = format("Cannot resolve expression [%s] for argument [%s.%s.host]. %s ", hostExpression, item.getType(),name, e.getMessage());
                throw new IllegalArgumentException(invalidExpressionMsg);
            }
        }

        throw new IllegalArgumentException(format("Argument %s.%s.host has an invalid expression format. Expected to be [${..}] but was [%s].", item.getType(),name,hostExpression));
    }

    private static ConfigurationItem getProperty(ConfigurationItem item, Deque<String> pathStack) {
        String propName = pathStack.pop();
        if (item.hasProperty(propName)) {
            Object nextItem = item.getProperty(propName);
            if (nextItem == null) {
                throw new IllegalArgumentException(format("Property [%.s.%s] is null.", item.getType(), propName));
            } else if (nextItem instanceof ConfigurationItem) {
                if (pathStack.isEmpty()) {
                    return (ConfigurationItem) nextItem;
                } else {
                    return getProperty((ConfigurationItem) nextItem, pathStack);
                }
            } else {
                throw new IllegalArgumentException(format("Property [%s.%s] kind invalid. Expecting kind [CI] but was [%s].", item.getType(), propName, item.getType().getDescriptor().getPropertyDescriptor(propName).getKind()));
            }
        } else {
            throw new IllegalArgumentException(format("Property [%s] not defined for type [%s].", propName, item.getType()));
        }

    }
}

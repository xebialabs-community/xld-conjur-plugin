/**
 * Copyright 2017 XebiaLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ext.deployit.community.ci.dictionary;

import com.google.common.collect.Maps;
import com.xebialabs.deployit.plugin.api.reflect.Descriptor;
import com.xebialabs.deployit.plugin.api.reflect.PropertyDescriptor;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;

import java.util.Map;

@Metadata(
        root = Metadata.ConfigurationItemRoot.ENVIRONMENTS,
        description = "A Dictionary that resolves the value dynamically using a pythons script",
        virtual = true
)
public class PythonDynamicDictionary extends BaseDynamicDictionary {

    @Property(description = "python script use to load the data", required = true, category = "Advanced")
    private String scriptFile;

    public Map<String, String> loadData() {
        final Map<String, Object> map = Maps.newHashMap();
        final Descriptor descriptor = this.getType().getDescriptor();
        for (PropertyDescriptor propertyDescriptor : descriptor.getPropertyDescriptors()) {
            final String name = propertyDescriptor.getName();
            map.put(name, this.getProperty(name));
        }
        return (Map<String, String>) ScriptRunner.executeScript(map, scriptFile).get(ScriptRunner.KEY_ENTRIES);
    }
}

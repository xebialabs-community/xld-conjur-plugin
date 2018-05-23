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


import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.*;

import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
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

import net.conjur.api.Conjur;

@Metadata(
        root = Metadata.ConfigurationItemRoot.ENVIRONMENTS,
        description = "A Dictionary that retrieves the secret value from conjur",
        virtual = true
)
public class ConjurDictionary extends Dictionary {

    // These are from the conjur endpoints class.  Unfortunately, private.
    private static final String CONJUR_URL = "CONJUR_APPLIANCE_URL";
    private static final String CONJUR_ACCOUNT = "CONJUR_ACCOUNT";

    @Override
    public Map<String, String> getEntries() {

        ConfigurationItem conjurServer = this.getProperty("conjurServer");
        logger.trace(String.format("Using conjurServer: %s", conjurServer));

        System.setProperty(CONJUR_URL, conjurServer.getProperty("url"));
        System.setProperty(CONJUR_ACCOUNT, conjurServer.getProperty("account"));
        String username = conjurServer.getProperty("username");
        String password = conjurServer.getProperty("password");

        Conjur conjur = new Conjur(username, password);

        Map<String, String> data = super.getEntries();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            logger.info(key + ":" + val);
            if (val != null && val.startsWith("$conjur:"))
            {
                // get the path substring
                String conjurPath = val.substring(val.indexOf(":"));
                if(conjurPath != null && !conjurPath.isEmpty())
                {
                    logger.info("ConjurPath = "+conjurPath);
                    // retrieve secret value from conjur
                    String secretVal = getConjurValue(conjur, conjurPath);
                    if (secretVal != null && !secretVal.isEmpty())
                    {
                        // update the map with new value
                        data.put(key, secretVal);
                    }
                }
            }
        }
        return data;
    }

    protected String getConjurValue(Conjur conjur, String key)
    {
        return conjur.variables().retrieveSecret(key);
    }

    private static Logger logger = LoggerFactory.getLogger(ConjurDictionary.class);
}

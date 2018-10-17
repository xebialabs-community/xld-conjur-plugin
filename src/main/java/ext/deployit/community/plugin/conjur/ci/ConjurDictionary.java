/**
 * Copyright 2017 XebiaLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ext.deployit.community.plugin.conjur.ci;


import java.util.HashMap;
import java.util.Map;

import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.plugin.api.udm.Metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.conjur.api.Conjur;

@Metadata(
        root = Metadata.ConfigurationItemRoot.ENVIRONMENTS,
        description = "A Dictionary that retrieves the secret value from conjur",
        virtual = true
)
public class ConjurDictionary extends Dictionary 
{
    private static final String CONJUR_PREFIX = "$conjur:";

    @Override
    public Map<String, String> getEntries() 
    {
        ConfigurationItem conjurServer = this.getProperty("conjurServer");
        logger.info(String.format("Using conjurServer: %s", conjurServer));

        System.setProperty("CONJUR_ACCOUNT", conjurServer.getProperty("account"));
        System.setProperty("CONJUR_AUTHN_LOGIN", conjurServer.getProperty("username"));
        System.setProperty("CONJUR_AUTHN_API_KEY", conjurServer.getProperty("password"));
        System.setProperty("CONJUR_APPLIANCE_URL", conjurServer.getProperty("url"));

        Conjur conjur = new Conjur();

        ConfigurationItem env = this.getProperty("environment");
        ConfigurationItem app = this.getProperty("deployedApplication");

        // set context variables
        Map<String, String> contextVars = new HashMap<>();
        contextVars.put("{{env.id}}", env.getProperty("id"));
        contextVars.put("{{env.name}}", env.getProperty("name"));
        contextVars.put("{{app.id}}", app.getProperty("id"));
        contextVars.put("{{app.name}}", app.getProperty("name"));
        
        Map<String, String> data = super.getEntries();

        for (Map.Entry<String, String> entry : data.entrySet()) 
        {
            String key = entry.getKey();
            String val = entry.getValue();
            logger.info(String.format("Checking %s=%s", key, val));

            if (val != null && val.startsWith(CONJUR_PREFIX))
            {
                // get the path substring
                String conjurPath = val.substring(CONJUR_PREFIX.length());
                if ( conjurPath != null && !conjurPath.isEmpty() )
                {
                    // do property placeholders substitution in the path; e.g. app.name, env.name
                    if ( conjurPath.indexOf("{{") >= 0)
                    {
                        for (String ckey : contextVars.keySet()) 
                        {
                            if ( conjurPath.indexOf(ckey) >= 0)
                            {
                                conjurPath = conjurPath.replaceAll(ckey, contextVars.get(ckey));
                            }
                        }
                    }

                    logger.info("ConjurPath = "+conjurPath);

                    // retrieve secret value from conjur
                    String secretVal = getConjurValue(conjur, conjurPath);
                    if (secretVal != null && !secretVal.isEmpty())
                    {
                        logger.trace("We have a secret value and it is ->"+secretVal);

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

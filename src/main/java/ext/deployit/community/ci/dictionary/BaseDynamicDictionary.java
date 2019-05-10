/**
 * Copyright 2019 XEBIALABS
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
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.*;

import com.xebialabs.deployit.plugin.api.udm.Dictionary;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;

@Metadata(
        root = Metadata.ConfigurationItemRoot.ENVIRONMENTS,
        description = "A Dictionary that resolves the value dynamically",
        virtual = true
)
public abstract class BaseDynamicDictionary extends Dictionary {

    @Property(description = "Reload the entries during the planning phase", defaultValue = "True", category = "Advanced")
    private boolean dynamicLoad;

    @Property(description = "Use a cache (10 seconds)...", defaultValue = "True", category = "Advanced", hidden = true)
    private boolean useCache;

    @Override
    public Map<String, String> getEntries() {
        logger.info("LAD-in baseDynamic getEntries()");
        if (dynamicLoad) {
            logger.debug("dynamicLoad True");
            Map data;
            if (useCache) {
                System.out.println("LAD-useCache is true");
                data = cache.getUnchecked(this);
            } else {
                data = loadData();
            }
            logger.debug("docker-machines " + data);
            return data;
        } else {
            System.out.println("LAD-about to call super getEntries()");
            return super.getEntries();
        }
    }


    private static LoadingCache<BaseDynamicDictionary, Map<String, String>> cache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(10, TimeUnit.SECONDS)
                    .removalListener(new RemovalListener<Object, Object>() {
                        @Override
                        public void onRemoval(final RemovalNotification<Object, Object> removalNotification) {
                            logger.info("Remove " + removalNotification.getKey() + " from docker-machine cache.....");
                        }
                    })
                    .build(new CacheLoader<BaseDynamicDictionary, Map<String, String>>() {
                        @Override
                        public Map<String, String> load(final BaseDynamicDictionary s) throws Exception {
                            return s.loadData();
                        }
                    });

    public Map<String, String> loadData() {
        logger.error("Return empty data");
        return Collections.emptyMap();
    }

    private static Logger logger = LoggerFactory.getLogger(BaseDynamicDictionary.class);
}

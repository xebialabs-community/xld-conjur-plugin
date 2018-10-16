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

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.xebialabs.deployit.engine.spi.exception.DeployitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScriptRunner {

    public final static String KEY_DICTIONARY = "ci";
    public final static String KEY_ENTRIES = "entries";
    public final static String KEY_LOGGER = "logger";
    public final static String KEY_SCRIPT_NAME = "dictionaryLoaderScriptName";

    public final static String SCRIPT_PATH = "./ext";

    public static Map<String, Object> executeScript(Map<String, Object> ci, String scriptName) {
        Map<String, Object> pythonContext = new HashMap<String, Object>();
        pythonContext.put(KEY_DICTIONARY, ci);
        pythonContext.put(KEY_ENTRIES, new HashMap<String, String>());
        pythonContext.put(KEY_LOGGER, logger);
        pythonContext.put(KEY_SCRIPT_NAME, scriptName);
        if (logger.isDebugEnabled()) {
            logger.debug("execute script " + scriptName + " with the context " + pythonContext);
        }

        ScriptEngine se;
        final String scriptClasspath = "";
        try {
            se = loadScriptEngine(getLibraryScripts(scriptClasspath));
            Bindings bindings = createBindings(pythonContext);
            loadLibraryScriptsAndEval(scriptName, se, bindings, scriptClasspath);
            return pythonContext;
        } catch (IOException e) {
            logger.error("Error on orchestrator script : scriptname  {} scriptclasspath {} for dictionary {} ", scriptName, scriptClasspath, ci);
            logger.error("Exception on script ", e);
            throw new DeployitException(e);
        }
    }

    protected static void loadLibraryScriptsAndEval(String scriptName, ScriptEngine scriptEngine, Bindings localBindings, String scriptClasspath) {
        Bindings origEngineBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        String script = "";
        try {
            script = loadScript(scriptName);
            Bindings engineAndLocalScope = new SimpleBindings();
            engineAndLocalScope.putAll(origEngineBindings);
            engineAndLocalScope.putAll(localBindings);
            scriptEngine.setBindings(engineAndLocalScope, ScriptContext.ENGINE_SCOPE);
            loadLibraryScripts(getLibraryScripts(scriptClasspath), scriptEngine);
            logger.debug("Executing script " + scriptName);
            if (logger.isTraceEnabled()) {
                logger.trace(script);
            }
            scriptEngine.eval(script);
        } catch (IOException e) {
            logger.error("IOException caught during script load : {}", scriptName, e);
        } catch (ScriptException e) {
            logger.error("Error while executing script", e);
            throw new ScriptExecutionException(scriptName + " " + e.getMessage(), e);
        } finally {
            scriptEngine.setBindings(origEngineBindings, ScriptContext.ENGINE_SCOPE);
        }
    }

    protected static Bindings createBindings(Map<String, Object> variables) {
        Bindings bindings = new SimpleBindings();
        bindings.putAll(variables);
        return bindings;
    }

    protected static ScriptEngine loadScriptEngine(List<String> libraryScripts) throws IOException {
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("jython");
        checkNotNull(scriptEngine, "Jython Script Engine cannot be initialized. Make sure jython jars are on the class path.");
        loadLibraryScripts(libraryScripts, scriptEngine);
        return scriptEngine;
    }

    protected static void loadLibraryScripts(List<String> libs, ScriptEngine scriptEngine) throws IOException {
        if (!libs.isEmpty()) {
            for (String library : libs) {
                String script = loadScript(library);
                checkNotNull(script, "Library {} cannot be found on class path.", library);
                try {
                    scriptEngine.eval(script);
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected static String loadScript(String scriptName) {
        String script = null;
        try {
            script = loadScriptFs(scriptName);
        } catch (IOException e1) {
            logger.warn("Cannot locate script on filesystem " + scriptName);
        }
        if (script != null) return script;
        try {
            script = loadScriptResource(scriptName);
        } catch (IOException e) {
            throw new DeployitException(e);
        }
        if (script == null) throw new DeployitException("Cannot locate script " + scriptName);
        return script;
    }

    protected static String loadScriptResource(String scriptName) throws IOException {
        String scriptPath = scriptName;
        String script = Resources.toString(Resources.getResource(scriptPath), Charset.defaultCharset());
        return script;
    }

    protected static String loadScriptFs(String path) throws IOException {
        File f = new File(SCRIPT_PATH, path);
        if (!f.exists()) {
            logger.debug("directory script not found at {}", f.getAbsolutePath());
            return null;
        }
        String script = Files.toString(f, Charset.defaultCharset());
        return script;
    }

    protected static List<String> getLibraryScripts(String scriptClassPath) {
        List<String> scripts = Lists.newArrayList();
        if (scriptClassPath == null) return scripts;
        if (scriptClassPath.trim().length() == 0) return scripts;
        scripts.addAll(Arrays.asList(scriptClassPath.split(":")));
        return scripts;
    }

    protected static final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);
}

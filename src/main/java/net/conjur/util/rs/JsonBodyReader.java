/**
 * Copyright 2018 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.conjur.util.rs;

import com.google.gson.Gson;
import edu.emory.mathcs.backport.java.util.Collections;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Can be registered (or provided during the JAXRS scanning phase) to process Json responses.
 */
@Provider
public class JsonBodyReader implements MessageBodyReader<Object> {
    private final Set<Class<?>> readableClasses =
            (Set<Class<?>>) Collections.synchronizedSet(new HashSet<Class<?>>());

    public JsonBodyReader registerClass(Class<?> klass){
        readableClasses.add(klass);
        return this;
    }

    public boolean isReadable(Class<?> klass,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)
                && isReadable(klass);
    }

    public Object readFrom(Class<Object> klass,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, String> httpHeaders,
                            InputStream bodyInputStream) throws IOException, WebApplicationException {
        final Reader reader = new BufferedReader(new InputStreamReader(bodyInputStream));
        return new Gson().fromJson(reader, klass);
    }

    /**
     * Check if the class is registered or a JsonReadable element is in Annotations.  Register the class if such an
     * annotation is found.
     * @param klass the class we're interested in deserializing
     * @return whether we should can read this class.
     */
    private boolean isReadable(Class<?> klass){
        if(readableClasses.contains(klass)) return true;
        if(klass.getAnnotation(JsonReadable.class) != null){
            readableClasses.add(klass);
            return true;
        }
        return false;
    }


}

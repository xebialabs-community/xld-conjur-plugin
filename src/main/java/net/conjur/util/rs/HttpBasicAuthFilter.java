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

import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Adds basic auth to a request.
 */
public class HttpBasicAuthFilter implements ClientRequestFilter {
    /* This is based on the jersey implementation of this class...
     * Worth another look at some point.
     */
    private static final Charset CHARACTER_SET = Charset.forName("iso-8859-1");

    private final String authorizationHeader;

    public HttpBasicAuthFilter(String username, String password){
        String raw = username + ":" + password;
        authorizationHeader = "Basic " +
                Base64.encodeBase64String(raw.getBytes(CHARACTER_SET));
    }

    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        final MultivaluedMap<String, Object> headers = clientRequestContext.getHeaders();
        // Multiple Authorization headers result in undefined behavior, and deleting existing
        // ones seems to violate the principle of least surprise.
        if(!headers.containsKey("Authorization")){
            headers.add("Authorization", authorizationHeader);
        }
    }
}

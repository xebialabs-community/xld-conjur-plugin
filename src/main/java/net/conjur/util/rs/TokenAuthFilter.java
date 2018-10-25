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

import net.conjur.api.AuthnProvider;
import net.conjur.api.Token;
import net.conjur.util.Args;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * Filter to add Conjur authentication tokens to requests.
 */
public class TokenAuthFilter implements ClientRequestFilter {

    private static final int EXPIRATION_TIME_BUFFER = 2 * 60;
    private static final String HEADER = "Authorization";

    private final AuthnProvider authn;
    private Token currentToken;

    public TokenAuthFilter(final AuthnProvider authn){
        this.authn = Args.notNull(authn);
    }

    public void filter(ClientRequestContext rc) throws IOException {
        if (!isTokenValid()) {
            currentToken = authn.authenticate();
        }

        rc.getHeaders().putSingle(HEADER, currentToken.toHeader());
    }

    private boolean isTokenValid() {
        return currentToken != null && !currentToken.willExpireWithin(EXPIRATION_TIME_BUFFER);
    }
}
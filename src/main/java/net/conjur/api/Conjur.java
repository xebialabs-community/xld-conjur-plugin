/**
 * Copyright 2018 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.conjur.api;

/**
 * Entry point for the Conjur API client.
 */
public class Conjur {

    private Variables variables;

    /**
     * Create a Conjur instance that uses credentials from the system properties
     */
    public Conjur(){
        this(Credentials.fromSystemProperties());
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     */
    public Conjur(String username, String password) {
        this(new Credentials(username, password));
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param credentials the conjur identity to authenticate as
     */
    public Conjur(Credentials credentials) {
        variables = new Variables(credentials);
    }

    /**
     * Get a Variables instance configured with the same parameters as this instance.
     * @return the variables instance
     */
    public Variables variables() {
        return variables;
    }

}

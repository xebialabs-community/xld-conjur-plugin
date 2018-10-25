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

import net.conjur.util.Args;
import java.io.Serializable;
import java.net.URI;

/**
 * An <code>Endpoints</code> instance provides endpoint URIs for the various conjur services.
 */
public class Endpoints implements Serializable {
    private static final String URL_PROPERTY_NAME = "CONJUR_APPLIANCE_URL";
    private static final String ACCOUNT_PROPERTY_NAME = "CONJUR_ACCOUNT";

    private final URI authnUri;
    private final URI secretsUri;

    public Endpoints(final URI authnUri, final URI secretsUri){
        this.authnUri = Args.notNull(authnUri, "authnUri");
        this.secretsUri = Args.notNull(secretsUri, "secretsUri");
    }

    public Endpoints(String authnUri, String secretsUri){
        this(URI.create(authnUri), URI.create(secretsUri));
    }

    public URI getAuthnUri(){ return authnUri; }

    public URI getSecretsUri() {
        return secretsUri;
    }

    public static Endpoints fromSystemProperties(){

        String account = System.getProperty(ACCOUNT_PROPERTY_NAME);

        return new Endpoints(
                getServiceUri("authn", account),
                getServiceUri("secrets", account, "variable")
        );
    }

    private static URI getServiceUri(String service, String accountName){
        return getServiceUri(service, accountName, "");
    }

    private static URI getServiceUri(String service, String accountName, String path){
        return URI.create(String.format("%s/%s/%s/%s", System.getProperty(URL_PROPERTY_NAME), service, accountName, path));
    }

    @Override
    public String toString() {
        return "Endpoints{" +
                "authnUri=" + authnUri +
                "secretsUri=" + secretsUri +
                '}';
    }
}

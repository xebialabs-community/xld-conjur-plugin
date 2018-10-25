/**
 * Copyright 2018 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.conjur.api.clients;

import net.conjur.api.Endpoints;
import net.conjur.api.ResourceProvider;
import net.conjur.util.EncodeUriComponent;
import net.conjur.util.rs.TokenAuthFilter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Conjur service client.
 */
public class ResourceClient implements ResourceProvider {

    private WebTarget secrets;
    private final Endpoints endpoints;

	public ResourceClient(final String username, final String password, final Endpoints endpoints) {
        this.endpoints = endpoints;

        init(username, password);
	}

    public String retrieveSecret(String variableId) {
        Response response = secrets.path(variableId).request().get(Response.class);
        validateResponse(response);

        return response.readEntity(String.class);
    }

    public void addSecret(String variableId, String secret) {
        Response response = secrets.path(EncodeUriComponent.encodeUriComponent(variableId)).request().post(Entity.text(secret), Response.class);
        validateResponse(response);
    }

    private Endpoints getEndpoints() {
        return endpoints;
    }

    private void init(String username, String password){
        final ClientBuilder builder = ClientBuilder.newBuilder()
                .register(new TokenAuthFilter(new AuthnClient(username, password, endpoints)));

        Client client = builder.build();

        secrets = client.target(getEndpoints().getSecretsUri());
    }

    // TODO orenbm: Remove when we have a response filter to handle this
    private void validateResponse(Response response) {
        int status = response.getStatus();
        if (status < 200 || status >= 400) {
            String errorMessage = String.format("Error code: %d, Error message: %s", status, response.readEntity(String.class));
            throw new WebApplicationException(errorMessage, status);
        }
    }
}

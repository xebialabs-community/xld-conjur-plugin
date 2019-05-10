#
# Copyright 2019 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

from net.conjur.api import Endpoints
from net.conjur.api.clients import ResourceClient

from java.net import URI

""" Conjur client """
class ConjurClient(object):

    @staticmethod
    def new_instance(container):
        return ConjurClient(container.url, container.account, container.username, container.password)


    def __init__(self, url, account, username, password):
        print "Creating Conjur client for '%s', account '%s', user '%s'" % (url, account, username)
        
        authnUri = URI.create("%s/authn/%s/" % (url, account))
        secretsUri = URI.create("%s/secrets/%s/variable" % (url, account))

        endpoints = Endpoints(authnUri, secretsUri)

        self.resourceClient = ResourceClient(username, password, endpoints)


    def retrieve_secret(self, path):
        return self.resourceClient.retrieveSecret(path)


#
# Copyright 2019 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

import sys

from conjur.core.client import ConjurClient

PREFIX = "$conjur:"

def process(task_vars):
    target = task_vars['target']

    conjur_server = target.conjurServer

    conjur = ConjurClient.new_instance(conjur_server)

    # get properties of container
    descr = metadataService.findDescriptor(target.type)

    # scan property values for '$conjur:' prefix
    for prop in descr.propertyDescriptors:
        val = target.getProperty(prop.name)

        if isinstance(val, basestring) and val.startswith(PREFIX):
            # this is a host we'll process during deployment
            path = val[8:]
            secret = conjur.retrieve_secret(path)
            print "..overiding {0}.{1} property with conjur value".format(target.name, prop.name)
            target.setProperty(prop.name, secret)


if __name__ == '__main__' or __name__ == '__builtin__':
    process(locals())

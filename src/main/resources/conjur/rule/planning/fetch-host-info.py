#
# Copyright 2019 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

PREFIX = "$conjur:"

"""
During planning, look for hosts that have property values with '$conjur:' prefix.  Those targets will get
a deployment step added that calls the conjur server for secrets.
"""
def targets():
    targets = []
    for delta in specification.deltas:
        deployed = delta.deployedOrPrevious
        container = deployed.container

        # get properties of container
        descr = metadataService.findDescriptor(container.type)

        # scan property values for '$conjur:' prefix
        for prop in descr.propertyDescriptors:
            val = container.getProperty(prop.name)

            if isinstance(val, basestring) and val.startswith(PREFIX):
                # this is a host we'll process during deployment
                targets.append(container)
                break

    return set(targets)


for t in targets():
    if t.conjurServer is None:
        raise Exception("Conjur server is not set for '{0}' CI".format(t.id))

    context.addStep(steps.jython(
        description="Update the '{0}' CI with values from the '{1}' conjur".format(t.id,t.conjurServer.name),
        order=20,
        script="conjur/rule/read_values.py",
        jython_context={'target': t},
    ))

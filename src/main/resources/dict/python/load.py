#
# Copyright 2017 XebiaLabs
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#
from net.conjur.api import Conjur
from net.conjur.api import Credentials
from net.conjur.api import Endpoints
from java.lang import System
from java.lang import Exception


print "LAD- in load.py"
print "About to print thisCi.id"
print "thisCi.id = ", thisCi.id
print "thisCi.conjurServer.id = ", thisCi.conjurServer.id

server = thisCi.conjurServer

System.setProperty('CONJUR_ACCOUNT', server.account)
System.setProperty('CONJUR_AUTHN_LOGIN', server.username)
System.setProperty('CONJUR_AUTHN_API_KEY', server.password)
System.setProperty('CONJUR_APPLIANCE_URL', server.url)

print "CONJUR_ACCOUNT = ", System.getProperty('CONJUR_ACCOUNT')
print "CONJUR_AUTHN_LOGIN = ", System.getProperty('CONJUR_AUTHN_LOGIN')
print "CONJUR_APPLIANCE_URL = ", System.getProperty('CONJUR_APPLIANCE_URL')

try:
    print "About to get entries"
    entries = thisCi.getEntries()
    print "have entries"
except:
    print("Unexpected error:", sys.exc_info()[0])
    raise


print " -- done -- "

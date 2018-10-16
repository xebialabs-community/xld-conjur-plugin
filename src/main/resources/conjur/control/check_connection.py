from net.conjur.api import Conjur
from net.conjur.api import Credentials
from net.conjur.api import Endpoints
from java.lang import System
from java.lang import Exception



def process(task_vars):
    server = task_vars['thisCi']

    System.setProperty('CONJUR_ACCOUNT', server.account)
    System.setProperty('CONJUR_AUTHN_LOGIN', server.username)
    System.setProperty('CONJUR_AUTHN_API_KEY', server.password)
    System.setProperty('CONJUR_APPLIANCE_URL', server.url)

    print "CONJUR_ACCOUNT = ", System.getProperty('CONJUR_ACCOUNT')
    print "CONJUR_AUTHN_LOGIN = ", System.getProperty('CONJUR_AUTHN_LOGIN')
    print "CONJUR_APPLIANCE_URL = ", System.getProperty('CONJUR_APPLIANCE_URL')

    conjur = Conjur()
    retrievedSecret = conjur.variables().retrieveSecret("db/password")
    
    print "The password is -> ", retrievedSecret
    print "Done"


if __name__ == '__main__' or __name__ == '__builtin__':
    process(locals())
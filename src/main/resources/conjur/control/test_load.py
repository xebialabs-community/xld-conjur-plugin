#
# Copyright 2019 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

from conjur.core.client import ConjurClient

PREFIX = "$conjur:"

def check_dict_value(conjur, key, val):
    print "..checking dictionary entry '%s' " % key,
    if val.startswith(PREFIX):
        path = val[8:]
        secret = conjur.retrieve_secret(path)
        if secret is None:
            print "ERROR: Secret not found for path '%s'" % path
            return 1
        else:
            print "FOUND"
    else:
        print "skipping"

    return 0

def process(task_vars):
    conjur_dict = task_vars['thisCi']

    conjur = ConjurClient.new_instance(conjur_dict.conjurServer)

    err_cnt = 0
    for k in conjur_dict.entries:
        v = conjur_dict.entries[k]
        err_cnt = err_cnt + check_dict_value(conjur, k, v)

    for k in conjur_dict.encryptedEntries:
        v = conjur_dict.entries[k]
        err_cnt = err_cnt + check_dict_value(conjur, k, v)

    if err_cnt > 0:
        raise Exception("Missing keys in Conjur")

    print "Done"


if __name__ == '__main__' or __name__ == '__builtin__':
    process(locals())

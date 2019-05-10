#
# Copyright 2019 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

import unittest
import subprocess

from conjur.core.client import ConjurClient

class ConjurTest(unittest.TestCase):
    def setUp(self):
        self._init_conjur()

        # create client
        container = {}
        container.url = "http://localhost:8088"
        container.account = "quick-start"
        container.username = "host/xld/xld-01"
        container.password = self.xld_key

        self.client = ConjurClient.new_instance(container)

    def tearDown(self):
        process = subprocess.Popen("docker-compose down -f ./resources/docker-compose.yml".split(), stdout = subprocess.PIPE)
        process.wait()

    def test_check_connection(self):
        pass

    def _init_conjur(self):
        # create account, will return an api key for admin
        process = subprocess.Popen("docker-compose exec conjur conjurctl account create quick-start".split(), stdout = subprocess.PIPE)

        process.wait()

        # get admin api key
        print process.stdout.read()
        self.admin_key = "xxx"

        conjur_env = {}
        conjur_env['API_KEY'] = self.admin_key

        # start up docker-compose
        process = subprocess.Popen("docker-compose up -f ./resources/docker-compose.yml".split(), stdout = subprocess.PIPE, env=conjur_env)

        # root command
        cmd = ['docker', 'run']
        cmd.extend('-e CONJUR_APPLIANCE_URL=http://localhost:8088'.split())
        cmd.extend('-e CONJUR_ACCOUNT=quick-start'.split())
        cmd.extend(('-e CONJUR_AUTHN_API_KEY=%s' % self.admin_key).split())
        cmd.extend('-e CONJUR_AUTHN_LOGIN=admin'.split())

        # 1. load conjur policy
        cmd_load = list(cmd)
        cmd_load.extend('conjur_cli load -f /conjur.yml'.split())
        print "Docker command (conjur):"
        print cmd_load

        process = subprocess.Popen(cmd_load, stdout = subprocess.PIPE)
        process.wait()
        print process.stdout.read()

        # 2. load xld host policy
        cmd_load = list(cmd)
        cmd_load.extend('conjur_cli load -f /xld.yml'.split())
        print "Docker command (load xld host):"
        print cmd_load

        process = subprocess.Popen(cmd_load, stdout = subprocess.PIPE)
        process.wait()
        # get xld password
        print process.stdout.read()
        self.xld_key = "asdffda"

        # 3. create secret(s) for testing
        cmd_load = list(cmd)
        cmd_load.extend('conjur_cli load -f /db.yml'.split())
        print "Docker command (load db):"
        print cmd_load

        process = subprocess.Popen(cmd_load, stdout = subprocess.PIPE)
        process.wait()
        print process.stdout.read()

# run tests

def main():
    unittest.main()

if __name__ == "__main__":
    main()
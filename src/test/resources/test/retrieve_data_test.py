#
# Copyright 2017 XebiaLabs
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#


import subprocess
from subprocess import call
import os
import sys
import shutil
from conjur.core.client import ConjurClient

class Container(object):
    pass

def runScript(filePath):
    with open(filePath, 'rb') as file:
        script = file.read()
    rc = call(script, shell=True)

def removeFile(fileName):
    if os.path.exists(fileName):
        os.remove(fileName)
    else:
        print("The file does not exist")

def getKey(fileName):
    with open(fileName, 'rb') as file:
        key = file.read()
    return key.rstrip()


def runTest():
    try:
        dockerComposePath = os.path.normpath('./src/test/resources/docker-compose.yml')
        conjurYmlPath = os.path.normpath('./src/test/resources/conjur.yml')
        dbYmlPath = os.path.normpath('./src/test/resources/db.yml')
        xldYmlPath = os.path.normpath('./src/test/resources/xld.yml')
        currentDirPath = os.path.normpath('./')

        shutil.copy2(dockerComposePath, currentDirPath)
        shutil.copy2(conjurYmlPath, currentDirPath)
        shutil.copy2(dbYmlPath, currentDirPath)
        shutil.copy2(xldYmlPath, currentDirPath) 

        print "About to start docker containers"

        runScript(os.path.normpath('./src/test/resources/startUpConjurForGradleTesting.sh'))

        hostKey = getKey("hostKey.txt")

        conjurContainer = Container()
        conjurContainer.url = "http://localhost:8080"
        conjurContainer.account = "quick-start"
        conjurContainer.username = "host/xld/xld-01"
        conjurContainer.password = hostKey


        conjur = ConjurClient.new_instance(conjurContainer)
        retrievedPassword = conjur.retrieve_secret("db/password")
        retrievedUsername = conjur.retrieve_secret("db/username")
        retrievedPath = conjur.retrieve_secret("db/tempPath")
        entries['db/password']=retrievedPassword
        entries['db/username']=retrievedUsername
        entries['db/tempPath']=retrievedPath
    except:
        print("Unexpected error:", sys.exc_info()[0])
        raise
    finally:
        print "About to remove docker containers"
        runScript(os.path.normpath('./src/test/resources/cleanUpConjurForGradleTesting.sh'))
        removeFile("docker-compose.yml")
        removeFile("conjur.yml")
        removeFile("db.yml")
        removeFile("xld.yml")
        # removeFile("dataKey.txt")
        # removeFile("adminKey.txt")
        removeFile("hostKey.txt")

if __name__ == '__main__' or __name__ == '__builtin__':
    runTest()








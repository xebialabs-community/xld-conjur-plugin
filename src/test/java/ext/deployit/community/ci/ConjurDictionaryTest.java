/**
 * Copyright 2017 XebiaLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package ext.deployit.community.ci;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import net.conjur.api.Conjur;

public class ConjurDictionaryTest {

    @Test
    public void TestConjur()
    {
        System.out.println("In TestConjur");
        TestableConjurDictionary dict = new TestableConjurDictionary();
        Map<String, String> origEntries = new HashMap<String, String>();
        origEntries.put("key1", "$conjur:path1");
        dict.setEntries(origEntries);

        Map<String, String> entries = dict.getEntries();

        for (String key : entries.keySet())
        {
            System.out.println("Testing here, "+key);
            if ( key.equals("key1") )
            {
                assertEquals("variable1", entries.get(key));
            }
        }
    }

    class TestableConjurDictionary extends ConjurDictionary
    {
        @Override
        protected String getConjurValue(Conjur conjur, String key)
        {
            if ( key.equals("path1") )
            {
                return "variable1";
            }
            else if ( key.equals("root/host/env/host-01/password"))
            {
                return "variable2";
            }
            return "";
        }
    }

}

    
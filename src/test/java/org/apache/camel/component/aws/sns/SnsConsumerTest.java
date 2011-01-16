/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.aws.sns;

import org.apache.camel.CamelContext;
import org.apache.camel.component.aws.sns.support.SnsSqsObject;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SnsConsumerTest {

    @Test
    public void testVerify() throws Exception {
        String validMessageJson = IOUtils.toString(getClass().getResourceAsStream("/valid-message.json"));
        JSONObject json = new JSONObject(validMessageJson);
        SnsSqsObject snsSqsObject = new SnsSqsObject(json);

        assertTrue("Expected verification to pass since this message was valid", SnsConsumer.verifyMessage(snsSqsObject));
    }

    @Test
    public void testVerifyFails() throws Exception {
        CamelContext context = new DefaultCamelContext();
        
        String validMessageJson = IOUtils.toString(context.getClassResolver().loadResourceAsStream("/invalid-message.json"));
        JSONObject json = new JSONObject(validMessageJson);

        // introduce a change in the message
        SnsSqsObject snsSqsObject = new SnsSqsObject(json);

        assertFalse("Expected sig verification to fail since we tweaked the message", SnsConsumer.verifyMessage(snsSqsObject));

    }
}

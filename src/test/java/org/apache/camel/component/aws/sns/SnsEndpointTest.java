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

import com.amazonaws.services.sns.AmazonSNSClient;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SnsEndpointTest {


    @Test
    public void testToQueueURL() throws Exception {
        String queueARN = "arn:aws:sqs:us-east-1:266383121696:final-project-queue-junit-f0842ce2-6896-4df7-905e-f22efa401878";
        String expectedURL = "https://queue.amazonaws.com/266383121696/final-project-queue-junit-f0842ce2-6896-4df7-905e-f22efa401878";

        String actual = SnsEndpoint.toQueueURL(queueARN);
        assertEquals(expectedURL, actual);
    }

    @Test
    public void stripCreds() throws Exception {
        String endpointURI = "aws-sns:topicName/myTopic?someParam=foo&accessKey=SOMEACCESSKEY&secretKey=SomeAcessKey";
        String expected = "aws-sns:topicName/myTopic?someParam=foo&accessKey=hidden&secretKey=hidden";
        String actual = SnsEndpoint.stripCredentials(endpointURI);
        assertEquals(expected, actual);
    }
}

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
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;

@Ignore
public class SnsComponentConfigurationTest extends CamelTestSupport {

    @Test
    public void createEndpointWithPollingConsumerParameters() throws Exception {
        SnsComponent component = new SnsComponent(context);
        SnsEndpoint endpoint = (SnsEndpoint) component.createEndpoint("aws-sns://topicName/myTopicName?accessKey=xxx&secretKey=yyy&delay=1234&initialDelay=4321&useFixedDelay=true&queueName=myQueue");
        SnsConsumer consumer = (SnsConsumer) endpoint.createConsumer(null);

        assertEquals(1234, consumer.getDelay());
        assertEquals(4321, consumer.getInitialDelay());
        assertTrue(consumer.isUseFixedDelay());
    }

    @Test
    public void createEndpointWithMinimalConfiguration() throws Exception {
        SnsComponent component = new SnsComponent(context);
        SnsEndpoint endpoint = (SnsEndpoint) component.createEndpoint("aws-sns://topicName/myTopicName?accessKey=xxx&secretKey=yyy");

        assertEquals("myTopicName", endpoint.getConfiguration().getTopicName());
        assertEquals("xxx", endpoint.getConfiguration().getAccessKey());
        assertEquals("yyy", endpoint.getConfiguration().getSecretKey());
        assertNull(endpoint.getConfiguration().getAmazonSNSClient());
        assertNull(endpoint.getConfiguration().getAmazonSQSClient());
        assertNull(endpoint.getConfiguration().getSubject());
        assertNull(endpoint.getConfiguration().getQueueName());
    }

    @Test
    public void createEndpointWithConsumerConfigurationMissingQueueNameOrArn() throws Exception {
        SnsComponent component = new SnsComponent(context);
        SnsEndpoint endpoint = (SnsEndpoint) component.createEndpoint("aws-sns://topicName/myTopicName?accessKey=xxx&secretKey=yyy&initialDelay=300&delay=400");
        try {
            SnsConsumer consumer = (SnsConsumer) endpoint.createConsumer(null);
        } catch (Exception e) {
            String err = "Must provide either a queueName or queueARN";
            assertTrue(e.toString().contains(err));
            return;
        }
        fail("Missing QueueName or QueueARN not detected");

    }

    @Test
    public void createEndpointWithConsumerConfiguration() throws Exception {
        SnsComponent component = new SnsComponent(context);
        SnsEndpoint endpoint = (SnsEndpoint) component.createEndpoint("aws-sns://topicName/myTopicName?queueName=stockQueue&accessKey=xxx&secretKey=yyy&initialDelay=300&delay=400");

        SnsConsumer consumer = (SnsConsumer) endpoint.createConsumer(null);

        assertEquals("stockQueue", consumer.getEndpoint().getConfiguration().getQueueName());
    }

    @Test
    public void createEndpointWithConsumerConfigurationWithoutSqsClientOrCredentials() throws Exception {
        AmazonSNSClient client = mock(AmazonSNSClient.class);

        ((JndiRegistry) ((PropertyPlaceholderDelegateRegistry) context.getRegistry()).getRegistry()).bind("amazonSNSClient", client);

        SnsComponent component = new SnsComponent(context);
        SnsEndpoint endpoint = (SnsEndpoint) component.createEndpoint("aws-sns://topicName/myTopicName?amazonSNSClient=#amazonSNSClient&queueName=foo&initialDelay=300&delay=400");

        // check here instead of expected to ensure it's the SQS client that's reported missing
        try {
            SnsConsumer consumer = (SnsConsumer) endpoint.createConsumer(null);
        } catch (Exception e) {
            String err = "AmazonSQSClient or accessKey and secretKey must be set";
            assertTrue(e.toString().contains(err));
            return;
        }
        fail("Missing SQSClient not detected");
    }


    @Test(expected = IllegalArgumentException.class)
    public void createEndpointWithoutAccessKeyConfiguration() throws Exception {
        SnsComponent component = new SnsComponent(context);
        component.createEndpoint("aws-sns://topicName/myTopic?secretKey=yyy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEndpointWithoutSecretKeyConfiguration() throws Exception {
        SnsComponent component = new SnsComponent(context);
        component.createEndpoint("aws-sns://topicName/myTopic?accessKey=yyy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEndpointWithoutTopicConfiguration() throws Exception {
        SnsComponent component = new SnsComponent(context);
        component.createEndpoint("aws-sns://?accessKey=yyy&secretKey=xxx");
    }


}

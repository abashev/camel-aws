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
package org.apache.camel.component.aws.sns.integration;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.sns.SnsConstants;
import org.apache.camel.component.aws.sns.SnsEndpoint;
import org.apache.camel.component.aws.sns.SnsUri;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;

/**
 * Helper class for testing the producing and consuming messages on the topic.
 * <p/>
 * The class is configured with a URI for the subscription and another for producing
 * messages. The callers add messages that should be sent and also indicate whether
 * the message should be received by the subscription or filtered out. The route
 * to use for the test is also configurable with the default route being one
 * without any filtering.
 * <p/>
 * 
 * The tester will continually poll the SQS queue prior to sending any messages to it
 * until it sees that the queue's attributes include a policy that allows the SNS topic
 * the send messages to it. 
 * 
 */
public class SnsTester {
    private static final Log LOG = LogFactory.getLog(SnsTester.class);

    SnsUri consumerUri;
    SnsUri producerUri;
    CamelContext context;
    List<String[]> data = new LinkedList<String[]>();
    List<String> expectedMessages = new LinkedList<String>();
    RouteBuilder routeBuilder;
    AmazonSQSClient sqsClient;
    
    public SnsTester(AmazonSQSClient sqsClient, SnsUri consumerUri, SnsUri producerUri, CamelContext context) {
        this.consumerUri = consumerUri;
        this.producerUri = producerUri;
        this.context = context;
        this.sqsClient = sqsClient;
    }

    protected Iterator<String[]> getMessages() {
        return data.iterator();
    }

    protected List<String> getExpectedMessageBodies() {
        return expectedMessages;
    }

    protected SnsTester withAcceptedMessage(String subject, String body) {
        return withMessage(subject, body, true);
    }

    protected SnsTester withFilteredMessage(String subject, String body) {
        return withMessage(subject, body, false);
    }

    protected SnsTester withMessage(String subject, String body, boolean accepted) {
        data.add(new String[]{subject, body});
        if (accepted) {
            expectedMessages.add(body);
        }
        return this;
    }

    protected void send() throws Exception {

        // create our route
        context.addRoutes(getRouteBuilder());

        // setup the mock endpoint's expected values
        MockEndpoint sink = getMockEndpoint();
        sink.expectedBodiesReceivedInAnyOrder(getExpectedMessageBodies());

        context.start();

        LOG.debug("delaying sending of messages until SQS policy propagated");
        SnsEndpoint endpoint = getConsumerEndpoint();
        pollPolicy(endpoint.getConfiguration().getQueueUrl());

        sendBodies();
    }
    
    /**
     * Keeps polling the policy every second until we see the markers that indicate that permission 
     * has been granted for the SNS topic to publish. This gives us the green light to either
     * start the context or send the messages, depending on what stage we're at.
     * 
     * @param aQUrl
     * @param aTimeout
     * @param aTimeUnit
     * @throws InterruptedException
     */
    protected void pollPolicy(String aQUrl) throws Exception {
        GetQueueAttributesRequest getReq = new GetQueueAttributesRequest().withQueueUrl(aQUrl).withAttributeNames("Policy", "QueueArn");
        long startTime = System.currentTimeMillis();

        int policiesSeen = 0;
        int requiredPoliciesSeen = 5;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        long timeout = 120;
        
        // keep looping until we hit our drop-dead timeout (2 minutes) or until we've seen the 
        // policy for 5 times. This business about checking for 4 extra times is extra padding to
        // ensure that all nodes have seen the policy propragation. 
        while(System.currentTimeMillis()<(startTime + timeUnit.toMillis(timeout))) {
            GetQueueAttributesResult response = sqsClient.getQueueAttributes(getReq);
            Map<String, String> attribs = response.getAttributes();
            String policy = attribs.get("Policy");
            String qArn = attribs.get("QueueArn");
            
            LOG.debug("polled policy:" + policy);

            if (policy != null && policy.contains(qArn+"/statementId")) {
                if (policiesSeen == 0) {
                    LOG.debug("policy propagated after:" + (System.currentTimeMillis()-startTime) + " millis");
                }
                policiesSeen++;
                if (policiesSeen >= requiredPoliciesSeen)
                    return;
            }
            Thread.sleep(1000);
        }
        throw new Exception("policy wasn't set prior to timeout");
    }

    /**
     * Returns the RouteBuilder used to exercise the code. Will lazily create a simple
     * route w/o any filtering if not already configured with one.
     */
    protected RouteBuilder getRouteBuilder() {
        if (routeBuilder == null) {
            return new RouteBuilder() {

                @Override
                public void configure() throws Exception {
                    from(consumerUri.toString()).to(AbstractUseCase.MOCK_SINK);
                }
            };
        }
        return routeBuilder;
    }

    protected void setRouteBuilder(RouteBuilder routeBuilder) {
        this.routeBuilder = routeBuilder;
    }

    private void sendBodies() throws Exception {
        if (producerUri == null) {
            return;
        }
        SnsEndpoint endpoint = (SnsEndpoint) context.getEndpoint(producerUri.toString());
        Producer producer = endpoint.createProducer();
        producer.start();

        for (Iterator it = getMessages(); it.hasNext();) {
            Exchange exchange = producer.createExchange(ExchangePattern.InOnly);
            Message message = exchange.getIn();
            String[] messageData = (String[]) it.next();
            message.setHeader(SnsConstants.SNS_SUBJECT, messageData[0]);
            message.setBody(messageData[1]);
            producer.process(exchange);
        }
    }

    protected MockEndpoint getMockEndpoint() {
        return (MockEndpoint) context.getEndpoint(AbstractUseCase.MOCK_SINK);
    }

    public SnsEndpoint getConsumerEndpoint() {
        return (SnsEndpoint) context.getEndpoint(consumerUri.toString());
    }
}

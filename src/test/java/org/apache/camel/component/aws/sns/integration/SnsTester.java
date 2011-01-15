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

/**
 * Helper class for testing the producing and consuming messages on the topic.
 * <p/>
 * The class is configured with a URI for the subscription and another for producing
 * messages. The callers add messages that should be sent and also indicate whether
 * the message should be received by the subscription or filtered out. The route
 * to use for the test is also configurable with the default route being one
 * without any filtering.
 * <p/>
 * There are three points in the test process that have the possibility for introducing
 * a delay (Thread.sleep). The main purpose for this delay is to allow for changes made
 * to a SQS policy to propagate. For example, there is a 90+ second delay on setting the
 * policy on a queue and having it take effect. The points for a delay are as follows:
 * - pre start: sleep before starting the context
 * - post start: sleep after starting the context
 * - post send: sleep after sending the messages
 *
 */
public class SnsTester {
    private static final Log LOG = LogFactory.getLog(SnsTester.class);

    long preStartDelay;
    long postStartDelay;
    long postSendDelay;

    SnsUri consumerUri;
    SnsUri producerUri;
    CamelContext context;
    List<String[]> data = new LinkedList<String[]>();
    List<String> expectedMessages = new LinkedList<String>();
    RouteBuilder routeBuilder;

    public SnsTester(SnsUri consumerUri, SnsUri producerUri, CamelContext context) {
        this.consumerUri = consumerUri;
        this.producerUri = producerUri;
        this.context = context;
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

    protected SnsTester withPreStartDelay(long preStartDelay) {
        setPreStartDelay(preStartDelay);
        return this;
    }

    protected SnsTester withPostStartDelay(long postStartDelay) {
        setPostStartDelay(postStartDelay);
        return this;
    }

    protected SnsTester withPostSendDelay(long postSendDelay) {
        setPostSendDelay(postSendDelay);
        return this;
    }

    protected long getPreStartDelay() {
        return preStartDelay;
    }

    protected void setPreStartDelay(long preStartDelay) {
        this.preStartDelay = preStartDelay;
    }

    protected long getPostStartDelay() {
        return postStartDelay;
    }

    protected void setPostStartDelay(long postStartDelay) {
        this.postStartDelay = postStartDelay;
    }

    protected long getPostSendDelay() {
        return postSendDelay;
    }

    protected void setPostSendDelay(long postSendDelay) {
        this.postSendDelay = postSendDelay;
    }

    protected void send() throws Exception {

        context.addRoutes(getRouteBuilder());

        MockEndpoint sink = getMockEndpoint();
        sink.expectedBodiesReceivedInAnyOrder(getExpectedMessageBodies());

        LOG.debug("pre-start-delay:" + getPreStartDelay());
        Thread.sleep(getPreStartDelay());

        context.start();

        LOG.debug("post-start-delay:" + getPostStartDelay());
        Thread.sleep(getPostStartDelay());

        sendBodies();

        // assert that we get the message
        LOG.debug("post-send-delay:" + getPostSendDelay());
        Thread.sleep(getPostSendDelay());
    }

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

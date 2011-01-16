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

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.component.aws.sns.support.SnsSqsObject;
import org.apache.camel.component.aws.sns.support.SnsSqsTypeConverter;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Consumer subscribes to a topic (optionally creating it) and sends the
 * messages it receives as part of the subscription to the its processor.
 *
 */
public class SnsConsumer extends ScheduledPollConsumer {

    private static final Log LOG = LogFactory.getLog(SnsConsumer.class);
    private static final int PROCESSED_HISTORY_QUEUE_SIZE = 100;

    /**
     * subscription arn is kept in order to unsubscribe when the consumer is
     * stopped
     */
    private String subscriptionArn;
    /**
     * Set of previously processed messages kept when the idempotent flag is set
     */
    private LinkedHashSet<String> alreadyProcessed = new LinkedHashSet<String>();

    public SnsConsumer(SnsEndpoint endpoint, Processor processor) throws IllegalArgumentException {
        super(endpoint, processor);

        // validate parameters
        SnsConfiguration config = endpoint.getConfiguration();
        if (config.getQueueArn() == null && config.getQueueName() == null) {
            throw new IllegalArgumentException("queueARN or queueName must be specified.");
        }

        if (config.getAmazonSQSClient() == null && (config.getAccessKey() == null || config.getSecretKey() == null)) {
            throw new IllegalArgumentException("AmazonSQSClient or accessKey and secretKey must be set");
        }

    }

    public SnsEndpoint getEndpoint() {
        return (SnsEndpoint) super.getEndpoint();
    }

    public void start() throws Exception {

        LOG.debug("starting");

        SnsEndpoint endpoint = getEndpoint();

        String topicArn = endpoint.getTopicArn();
        if (LOG.isDebugEnabled()) {
            LOG.debug("topicArn:" + topicArn);
        }
        // create the queue if it doesn't exist
        createQueue();

        // subscribe
        SubscribeRequest subReq = new SubscribeRequest().withProtocol("sqs").withTopicArn(topicArn).withEndpoint(endpoint.getConfiguration().getQueueArn());
        SubscribeResult subresult = endpoint.getClient().subscribe(subReq);

        if (LOG.isDebugEnabled()) {
            LOG.debug("subscribed to topic: " + subresult.getSubscriptionArn());
        }

        setSubscriptionArn(subresult.getSubscriptionArn());

        super.start();
    }


    public void stop() throws Exception {
        if (this.isStopped()) {
            LOG.debug("consumer already stopped, why is this being called twice?");
            return;
        }
        LOG.debug("stopping consumer...");
        SnsEndpoint endpoint = getEndpoint();
        endpoint.stop();

        if (!endpoint.getConfiguration().isDeleteTopicOnStop()) {
            LOG.debug("unsubscribing from topic");
            endpoint.getClient().unsubscribe(new UnsubscribeRequest().withSubscriptionArn(subscriptionArn));
        }

        super.stop();
    }

    public static String getQueueArn(AmazonSQSClient client, String queueURL) {
        GetQueueAttributesRequest queueReq = new GetQueueAttributesRequest().withQueueUrl(queueURL).withAttributeNames("QueueArn");
        return client.getQueueAttributes(queueReq).getAttributes().get("QueueArn");
    }

    public String getPolicy(String topicArn, String queueArn, String queueURL) throws Exception {
        // FIXME can make this more efficient
        String s = IOUtils.toString(getEndpoint().getCamelContext().getClassResolver().loadResourceAsStream("default-sqs-policy-template.json"));
        s = s.replace("$SNS_ARN", topicArn);
        s = s.replace("$SQS_ARN", queueArn);
        s = s.replace("$SQS_URL", new URI(queueURL).getPath());
        s = s.replace("\n", " ");
        s = s.replace("\r", " ");
        return s;
    }


    public static boolean verifyMessage(SnsSqsObject snsSqsObject) throws Exception {
        return SnsSqsTypeConverter.verify(snsSqsObject);
    }


    @Override
    public String toString() {
        return "Consumer[" + SnsEndpoint.stripCredentials(getEndpoint().getEndpointUri()) + "]";
    }

    protected boolean isAlreadyProcessed(String messageId) {
        boolean msgAlreadyProcessed = !alreadyProcessed.add(messageId);
        if (alreadyProcessed.size() > PROCESSED_HISTORY_QUEUE_SIZE) {
            Object oldest = alreadyProcessed.iterator().next();
            if (oldest != null) {
                alreadyProcessed.remove(oldest);
            }
        }
        return msgAlreadyProcessed;
    }

    protected int poll() throws Exception {

        SnsEndpoint endpoint = getEndpoint();
        String queueURL = endpoint.getConfiguration().getQueueUrl();

        LOG.trace("polling queue...");

        int messagesProcessed = 0;

        ReceiveMessageRequest msgReq = new ReceiveMessageRequest().withQueueUrl(queueURL).withMaxNumberOfMessages(1);
        ReceiveMessageResult result = endpoint.getSqsClient().receiveMessage(msgReq);

        if (!result.getMessages().isEmpty()) {

            LOG.debug("received message");

            Message message = result.getMessages().get(0);
            String receiptHandle = message.getReceiptHandle();

            String messageBody = message.getBody();
            SnsSqsObject snsSqsObject = SnsSqsTypeConverter.toSQSObject(messageBody);

            if (endpoint.getConfiguration().isVerify() && !verifyMessage(snsSqsObject)) {
                LOG.debug("message failed verification, deleting");
            } else {
                String messageId = snsSqsObject.getMessageId();
                if (endpoint.getConfiguration().isIdempotent() & isAlreadyProcessed(messageId)) {
                    LOG.debug("message already processed and idempotent flag set, ignoring message");
                    return messagesProcessed;
                }

                Exchange exchange = endpoint.createExchange(ExchangePattern.InOnly);

                if (LOG.isTraceEnabled()) {
                    LOG.trace(snsSqsObject.toString());
                }

                org.apache.camel.Message camelMessage = exchange.getIn();
                camelMessage.setBody(snsSqsObject.getMessage());
                setHeaders(snsSqsObject, camelMessage);

                getProcessor().process(exchange);
                messagesProcessed++;
            }

            DeleteMessageRequest delReq = new DeleteMessageRequest().withQueueUrl(queueURL).withReceiptHandle(receiptHandle);
            endpoint.getSqsClient().deleteMessage(delReq);
        }
        return messagesProcessed;
    }

    protected String getSubscriptionArn() {
        return subscriptionArn;
    }

    protected void setSubscriptionArn(String subscriptionArn) {
        this.subscriptionArn = subscriptionArn;
    }

    private void setHeaders(SnsSqsObject snsSqsObject, org.apache.camel.Message camelMessage) {
        camelMessage.setHeader(SnsConstants.SQS_MESSAGE_ID, snsSqsObject.getMessageId());
        camelMessage.setHeader(SnsConstants.SQS_TIMESTAMP, snsSqsObject.getTimestamp());
        camelMessage.setHeader(SnsConstants.SQS_TOPIC_ARN, snsSqsObject.getTopicArn());
        camelMessage.setHeader(SnsConstants.SQS_TYPE, snsSqsObject.getType());
        camelMessage.setHeader(SnsConstants.SQS_UNSUBSCRIBE_URL, snsSqsObject.getUnsubscribe());
        camelMessage.setHeader(SnsConstants.SNS_SUBJECT, snsSqsObject.getSubject());
        camelMessage.setHeader(SnsConstants.SQS_SIGNATURE, snsSqsObject.getSignature());
        camelMessage.setHeader(SnsConstants.SQS_SIGNATURE_VERSION, snsSqsObject.getSignatureVersion());
    }

    private void createQueue() throws Exception {

        if (getEndpoint().getConfiguration().getQueueArn() == null) {

            LOG.debug("queueArn not set, creating queue from queue name");

            CreateQueueRequest queueReq = new CreateQueueRequest().withQueueName(getEndpoint().getConfiguration().getQueueName());
            String queueURL = getEndpoint().getSqsClient().createQueue(queueReq).getQueueUrl();

            getEndpoint().getConfiguration().setQueueUrl(queueURL);

            if (LOG.isDebugEnabled()) {
                LOG.debug("queueURL=" + queueURL);
            }

            String queueArn = getQueueArn(getEndpoint().getSqsClient(), queueURL);
            if (LOG.isDebugEnabled()) {
                LOG.debug("queueArn=" + queueArn);
            }
            getEndpoint().getConfiguration().setQueueArn(queueArn);

            // set policy
            Map<String, String> policyMap = new HashMap<String, String>();
            String policy = getPolicy(getEndpoint().getTopicArn(), queueArn, queueURL);

            policyMap.put("Policy", policy);
            if (LOG.isDebugEnabled()) {
                LOG.debug("setting policy on newly created queue:" + policy);
            }

            SetQueueAttributesRequest setReq = new SetQueueAttributesRequest().withQueueUrl(queueURL).withAttributes(policyMap);
            getEndpoint().getSqsClient().setQueueAttributes(setReq);

        } else {
            String queueArn = getEndpoint().getConfiguration().getQueueArn();
            String queueURL = SnsEndpoint.toQueueURL(queueArn);
            getEndpoint().getConfiguration().setQueueUrl(queueURL);
            if (LOG.isDebugEnabled()) {
                LOG.debug("queueArn=" + queueArn);
                LOG.debug("queueURL=" + queueURL);
            }
        }
    }
}

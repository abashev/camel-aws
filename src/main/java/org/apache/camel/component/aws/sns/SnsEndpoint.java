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
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.aws.AmazonClientFactory;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SnsEndpoint extends ScheduledPollEndpoint {

    private static final Log LOG = LogFactory.getLog(SnsEndpoint.class);
    private static final String AMAZON_QUEUE_URL = "https://queue.amazonaws.com/";

    private SnsConfiguration configuration;

    private AmazonSNSClient client;
    private AmazonSQSClient sqsClient;

    public SnsEndpoint(String uri, CamelContext context, SnsConfiguration configuration) {
        super(uri, context);
        this.configuration = configuration;
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating consumer for endpoint:" + stripCredentials(getEndpointUri()));
        }

        // check for required parameters
        if (configuration.getQueueArn() == null && configuration.getQueueName() == null) {
            throw new IllegalArgumentException("Must provide either a queueName or queueARN");
        }

        SnsConsumer consumer = new SnsConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public String toString() {
        return "Endpoint[" + stripCredentials(getEndpointUri()) + "]";
    }

    protected static String stripCredentials(String aUri) {
        return aUri.replaceAll("(accessKey=)[A-Za-z0-9]+", "$1hidden")
            .replaceAll("(secretKey=)[A-Za-z0-9]+", "$1hidden");
    }

    protected String createEndpointUri() {
        return getEndpointUri();
    }

    public Producer createProducer() throws Exception {
        return new SnsProducer(this);
    }

    public boolean isSingleton() {
        return true;
    }

    public String getTopicArn() throws Exception {
        if (configuration.getTopicArn() == null) {

            CreateTopicRequest topicReq = new CreateTopicRequest().withName(configuration.getTopicName());
            CreateTopicResult result = client.createTopic(topicReq);

            configuration.setTopicArn(result.getTopicArn());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Topic ARN retrieved from AWS: " + configuration.getTopicArn());
            }
        }
        return configuration.getTopicArn();
    }

    @Override
    public void start() {
        client = getClient();
        sqsClient = getSqsClient();
    }

    @Override
    public void stop() {
        LOG.debug("stopping endpoint");
        if (configuration.isDeleteTopicOnStop()) {
            try {
                String topicArn = getTopicArn();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("deleting topic on consumer stop:" + topicArn);
                }
                client.deleteTopic(new DeleteTopicRequest().withTopicArn(topicArn));
            } catch (Exception e) {
                LOG.error("error deleting topic during stop", e);
            }
        }

        if (configuration.isDeleteQueueOnStop()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleting queue on consumer stop:" + configuration.getQueueUrl());
            }
            try {
                sqsClient.deleteQueue(new DeleteQueueRequest().withQueueUrl(configuration.getQueueUrl()));
            } catch (Exception e) {
                LOG.error("error deleting queue during stop", e);
            }
        }
    }

    protected static String toQueueURL(String queueArn) {
        String[] values = queueArn.split(":");

        StringBuilder sb = new StringBuilder();
        String accountId = values[values.length - 2];
        String queueName = values[values.length - 1];
        sb.append(AMAZON_QUEUE_URL).append(accountId).append('/').append(queueName);
        return sb.toString();
    }

    public SnsConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(SnsConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Provide the possibility to override this method for an mock implementation
     *
     * @return AmazonSNSClient
     */
    AmazonSNSClient createClient() {
        return AmazonClientFactory.createSNSClient(configuration.getAccessKey(), configuration.getSecretKey());
    }

    public AmazonSNSClient getClient() {
        if (client == null) {
            if (configuration.getAmazonSNSClient() == null) {
                client = createClient();
            } else {
                client = configuration.getAmazonSNSClient();
            }
        }
        return client;
    }

    public void setClient(AmazonSNSClient client) {
        this.client = client;
    }

    /**
     * Provide the possibility to override this method for an mock implementation
     *
     * @return AmazonSQSClient
     */
    AmazonSQSClient createSQSClient() {
        return AmazonClientFactory.createSQSClient(configuration.getAccessKey(), configuration.getSecretKey());
    }

    public AmazonSQSClient getSqsClient() {
        if (sqsClient == null) {
            if (configuration.getAmazonSQSClient() == null) {
                sqsClient = createSQSClient();
            } else {
                sqsClient = configuration.getAmazonSQSClient();
            }
        }
        return sqsClient;
    }

    public void setSqsClient(AmazonSQSClient sqsClient) {
        this.sqsClient = sqsClient;
    }
}

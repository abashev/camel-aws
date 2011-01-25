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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.camel.component.aws.AmazonClientFactory;
import org.apache.camel.component.aws.sns.SnsConsumer;
import org.apache.camel.component.aws.sns.SnsEndpoint;
import org.apache.camel.component.aws.sns.SnsUri;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

// FIXME need test to show that the unsubscribe call is happening.

public abstract class AbstractUseCase {
    private static final Log LOG = LogFactory.getLog(AbstractUseCase.class);

    protected static final String MOCK_SINK = "mock:sink";

    DefaultCamelContext context = new DefaultCamelContext();
    AmazonSNSClient client;
    AmazonSQSClient sqsClient;
    String topicName = "topic-junit-" + UUID.randomUUID().toString();
    String queueName = "queue-junit-" + UUID.randomUUID().toString();
    AWSCredentials credentials;

    String queueURL;
    String topicArn;

    SnsTester tester;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/awscreds.properties"));
        String accessKey = props.getProperty("accessKey");
        String secretKey = props.getProperty("secretKey");
        // if the keys aren't found look in the environment
        if ("${env.accessKey}".equals(accessKey)) {
            accessKey = System.getenv("accessKey");
            secretKey = System.getenv("secretKey");
        }
        assertNotNull("accessKey must be provided as an environment variable or configured in awscreds.properties", accessKey);
        assertNotNull("secretKey must be provided as an environment variable or configured in awscreds.properties", secretKey);
        credentials = new BasicAWSCredentials(accessKey, secretKey);
        client = AmazonClientFactory.createSNSClient(credentials);
        sqsClient = AmazonClientFactory.createSQSClient(credentials);
        assertNotNull(getClass().getResource("/META-INF/services/org/apache/camel/component/aws-sns"));
        PropertyConfigurator.configure(getClass().getResource("/log4j.properties"));
    }

    @After
    public void tearDown() throws Exception {

        if (!context.isStopped()) {
            context.stop();
        }

        if (tester == null) {
            return;
        }

        // delete all topics
        LOG.debug("deleting topic created for test");
        if (topicArn != null) {
            try {
                LOG.debug("deleting topic:" + topicArn);
                client.deleteTopic(new DeleteTopicRequest().withTopicArn(topicArn));
            } catch (Exception e) {
                LOG.error("Failed to delete topic: " + e);
            }
        }

        // delete all queues
        LOG.debug("deleting queue created for test");
        if (queueURL != null) {
            try {
                LOG.debug("deleting q:" + queueURL);
                sqsClient.deleteQueue(new DeleteQueueRequest().withQueueUrl(queueURL));
            } catch (Exception e) {
                LOG.error("Failed to delete queue: " + e);
            }
        }
    }

    // FIXME need test to show that the unsubscribe call is happening.

    protected String createQueue() throws IOException {
        CreateQueueResult result = sqsClient.createQueue(new CreateQueueRequest().withQueueName(queueName));
        String queueUrl = result.getQueueUrl();
        queueURL = queueUrl;
        String queueArn = SnsConsumer.getQueueArn(sqsClient, queueUrl);

        String policyStr = IOUtils.toString(AbstractUseCase.class.getResourceAsStream("/open-sqs-policy-template.json"));
        policyStr = policyStr.replace("$SQS_ARN", queueArn);

        Map<String, String> attribs = new HashMap<String, String>();
        attribs.put("Policy", policyStr);
        sqsClient.setQueueAttributes(new SetQueueAttributesRequest().withQueueUrl(queueUrl).withAttributes(attribs));
        return queueArn;
    }

    protected String createTopic() {
        topicArn = client.createTopic(new CreateTopicRequest().withName(topicName)).getTopicArn();
        return topicArn;
    }

    protected void setTester(SnsTester aTester) {
        tester = aTester;
    }

    protected SnsUri createUri() {
        return new SnsUri(credentials);
    }

    protected void doTest(SnsTester aTester) throws Exception {
        setTester(aTester);
        tester.send();

        SnsEndpoint snsEndpoint = tester.getConsumerEndpoint();
        topicArn = snsEndpoint.getTopicArn();
        queueURL = snsEndpoint.getConfiguration().getQueueUrl();

        MockEndpoint.assertIsSatisfied(120, TimeUnit.SECONDS, tester.getMockEndpoint());
        context.stop();
    }
}

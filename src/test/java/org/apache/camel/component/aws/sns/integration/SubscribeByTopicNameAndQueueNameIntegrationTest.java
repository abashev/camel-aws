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

import org.apache.camel.component.aws.sns.SnsUri;
import org.junit.Test;

/**
 * Tests subscribing to a topic by name and registering an SQS queue for the
 * notification by name.
 * <p/>
 * Sample uri: aws-sns://topicName/testTopic?queueName=testQueue
 *
 */
public class SubscribeByTopicNameAndQueueNameIntegrationTest extends AbstractUseCase {

    @Test
    public void test() throws Exception {

        SnsUri consumer = createUri().withTopicName(topicName).withQueueName(queueName);
        SnsUri producer = createUri().withTopicName(topicName);

        SnsTester tester = new SnsTester(sqsClient, consumer, producer, context)
            .withAcceptedMessage("subject-1", "message body-1")
            .withAcceptedMessage("subject-2", "message body-2");

        doTest(tester);
    }
}

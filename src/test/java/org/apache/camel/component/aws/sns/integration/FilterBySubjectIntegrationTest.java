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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.sns.SnsConstants;
import org.apache.camel.component.aws.sns.SnsUri;
import org.junit.Test;

public class FilterBySubjectIntegrationTest extends AbstractUseCase {

    @Test
    public void test() throws Exception {

        final SnsUri consumer = createUri().withTopicName(topicName).withQueueName(queueName).withDelay(500);
        SnsUri producer = createUri().withTopicName(topicName);

        SnsTester tester = new SnsTester(sqsClient, consumer, producer, context)
            .withFilteredMessage("on the right subject", "message body-0")
            .withAcceptedMessage("ok-subject", "message body-1")
            .withAcceptedMessage("ok-subject", "message body-2")
            .withFilteredMessage("will get filtered", "message body-3")
            .withFilteredMessage("also filtered", "message body-4")
            .withAcceptedMessage("ok-subject", "message body-5")
            .withAcceptedMessage("ok-subject", "message body-6");

        tester.setRouteBuilder(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from(consumer.toString()).filter(header(SnsConstants.SNS_SUBJECT).isEqualTo("ok-subject")).to(MOCK_SINK);
            }
        });

        doTest(tester);
    }

}

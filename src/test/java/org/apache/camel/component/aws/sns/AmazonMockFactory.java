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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.mockito.Matchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Using Mockito instead of extending the Amazon classes to allow for additional tests (coming soon) that
 * are much more difficult to do without a mock framework.
 */
public final class AmazonMockFactory {

    private AmazonMockFactory() {
        // hide the constructor
    }

    public static AmazonSNSClient getAmazonSNSClient() {
        AmazonSNSClient client = mock(AmazonSNSClient.class);

        CreateTopicResult topicResult = mock(CreateTopicResult.class);
        when(topicResult.getTopicArn()).thenReturn("arn:aws:sqs:us-east-1:266383121696:final-project-queue-junit-f0842ce2-6896-4df7-905e-f22efa401878");

        SubscribeResult subscribeResult = mock(SubscribeResult.class);
        when(subscribeResult.getSubscriptionArn()).thenReturn("arn:aws:sqs:us-east-1:266383121696:final-project-queue-junit-f0842ce2-6896-4df7-905e-f22efa401878");

        when(client.createTopic(Matchers.<CreateTopicRequest>anyObject())).thenReturn(topicResult);
        when(client.subscribe(Matchers.<SubscribeRequest>anyObject())).thenReturn(subscribeResult);

        return client;
    }

    public static AmazonSQSClient getAmazonSQSClient() {
        AmazonSQSClient sqsClient = mock(AmazonSQSClient.class);

        Map<String, String> attr = new HashMap<String, String>();
        attr.put("QueueArn", "aws:foo");

        GetQueueAttributesResult queueAttributesResult = mock(GetQueueAttributesResult.class);
        when(queueAttributesResult.getAttributes()).thenReturn(attr);

        CreateQueueResult result = new CreateQueueResult();
        result.setQueueUrl("https://queue.amazonaws.com/541925086079/MyQueue");


        ReceiveMessageResult msgResult = new ReceiveMessageResult();
        Collection<Message> messages = new ArrayList<Message>();
        Message message = new Message();
        message.setBody("{\n"
            + "  \"Type\" : \"Notification\",\n"
            + "  \"MessageId\" : \"0d6d3e75-33d0-41a4-a89f-dee2367d9d32\",\n"
            + "  \"TopicArn\" : \"arn:aws:sns:us-east-1:903742277936:final-project-topic-junit-ddec9039-57b1-4b81-a615-e321fe36b973\",\n"
            + "  \"Subject\" : \"subject-2\",\n"
            + "  \"Message\" : \"message body-2\",\n"
            + "  \"Timestamp\" : \"2011-01-14T13:14:18.397Z\",\n"
            + "  \"SignatureVersion\" : \"1\",\n"
            + "  \"Signature\" : \"c/YApEPO9U4caDYk67KAiUjNQx332fv2ZvuFX+XEOdZPdEvUiQfqEH45t3Adiy1Ty9nE4TlyPNCKLbdguV5WDnf7IfGhis8XJ7iNnsRT6fP3c7Os8tdk"
            + "FNrzMTK8yXjAl7lmtIVNN25ZB7qixu3di5X/wYg/hUF84UfnmBBk7F8=\",\n"
            + "  \"UnsubscribeURL\" : \"https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:903742277936:"
            + "final-project-topic-junit-ddec9039-57b1-4b81-a615-e321fe36b973:27e86aed-ed5a-4c10-963b-0dfe4ba77532\"\n"
            + "}");
        message.setMD5OfBody("6a1559560f67c5e7a7d5d838bf0272ee");
        message.setMessageId("f6fb6f99-5eb2-4be4-9b15-144774141458");
        message.setReceiptHandle("0NNAq8PwvXsyZkR6yu4nQ07FGxNmOBWi5zC9+4QMqJZ0DJ3gVOmjI2Gh/oFnb0IeJqy5Zc8kH4JX7GVpfjcEDjaAPSeOkXQZRcaBqt"
            + "4lOtyfj0kcclVV/zS7aenhfhX5Ixfgz/rHhsJwtCPPvTAdgQFGYrqaHly+etJiawiNPVc=");
        messages.add(message);
        msgResult.setMessages(messages);

        when(sqsClient.receiveMessage(Matchers.<ReceiveMessageRequest>anyObject())).thenReturn(msgResult);

        when(sqsClient.getQueueAttributes(Matchers.<GetQueueAttributesRequest>anyObject())).thenReturn(queueAttributesResult);
        when(sqsClient.createQueue(Matchers.<CreateQueueRequest>anyObject())).thenReturn(result);

        return sqsClient;
    }
}

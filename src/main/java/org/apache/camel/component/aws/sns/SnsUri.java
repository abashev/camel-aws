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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.auth.AWSCredentials;

public class SnsUri {
    /**
     * query params
     */
    Map<String, String> queryProps = new LinkedHashMap<String, String>();
    /**
     * topic arn that we're targeting
     */
    String topicArn;

    public SnsUri(SnsUri uri) {
        queryProps.putAll(uri.queryProps);
        topicArn = uri.topicArn;
    }

    public SnsUri(AWSCredentials credentials) {
        addProperty("accessKey", credentials.getAWSAccessKeyId());
        addProperty("secretKey", credentials.getAWSSecretKey());
        addProperty("delay", "2000");
        addProperty("idempotent", "true");
    }

    public SnsUri withQueueName(String queueName) {
        addProperty("queueName", queueName);
        return this;
    }

    public SnsUri withQueueArn(String queueArn) {
        addProperty("queueArn", queueArn);
        return this;
    }

    public SnsUri withDeleteQueueOnStop(boolean deleteQueueOnStop) {
        addProperty("deleteQueueOnStop", String.valueOf(deleteQueueOnStop));
        return this;
    }

    public SnsUri withDeleteTopicOnStop(boolean deleteTopicOnStop) {
        addProperty("deleteTopicOnStop", String.valueOf(deleteTopicOnStop));
        return this;
    }

    public SnsUri withDelay(long millis) {
        addProperty("delay", String.valueOf(millis));
        return this;
    }

    public void addProperty(String name, String value) {
        queryProps.put(name, value);
    }

    public SnsUri withProperty(String name, String value) {
        addProperty(name, value);
        return this;
    }

    public SnsUri withTopicName(String topicName) {
        setTopicName(topicName);
        return this;
    }

    public void setTopicName(String topicName) {
        topicArn = "topicName/" + topicName;
    }

    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }

    public SnsUri withTopicArn(String topicArn) {
        setTopicArn(topicArn);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("aws-sns:").append(topicArn);
        if (!queryProps.isEmpty()) {
            sb.append('?');

            String delim = "";
            for (Entry<String, String> entry : queryProps.entrySet()) {
                sb.append(delim);
                sb.append(entry.getKey()).append('=').append(entry.getValue());
                delim = "&";
            }
        }
        return sb.toString();
    }
}

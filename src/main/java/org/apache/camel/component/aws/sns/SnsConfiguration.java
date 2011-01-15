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
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.apache.camel.RuntimeCamelException;

public class SnsConfiguration implements Cloneable {

    // Common properties
    private String topicArn;
    private String topicName;
    private boolean deleteTopicOnStop;
    private AmazonSNSClient amazonSNSClient;
    private AmazonSQSClient amazonSQSClient;
    private String accessKey;
    private String secretKey;

    // Consumer only properties
    private String queueName;
    private String queueArn;
    private boolean idempotent;
    private boolean deleteQueueOnStop;
    private boolean verify;

    // Producer only properties
    private String subject;

    // other properties (not settable from the URI)
    private String queueUrl;

    public SnsConfiguration copy() {
        try {
            SnsConfiguration newCopy = (SnsConfiguration) clone();
            // override any properties where a reference copy isn't what we want
            return newCopy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeCamelException(e);
        }
    }


    public boolean isDeleteTopicOnStop() {
        return deleteTopicOnStop;
    }

    public void setDeleteTopicOnStop(boolean deleteTopicOnStop) {
        this.deleteTopicOnStop = deleteTopicOnStop;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueArn() {
        return queueArn;
    }

    public void setQueueArn(String queueArn) {
        this.queueArn = queueArn;
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public void setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
    }

    public boolean isDeleteQueueOnStop() {
        return deleteQueueOnStop;
    }

    public void setDeleteQueueOnStop(boolean deleteQueueOnStop) {
        this.deleteQueueOnStop = deleteQueueOnStop;
    }

    public boolean isVerify() {
        return verify;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTopicArn() {
        return topicArn;
    }

    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }


    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public AmazonSNSClient getAmazonSNSClient() {
        return amazonSNSClient;
    }

    public void setAmazonSNSClient(AmazonSNSClient amazonSNSClient) {
        this.amazonSNSClient = amazonSNSClient;
    }

    public AmazonSQSClient getAmazonSQSClient() {
        return amazonSQSClient;
    }

    public void setAmazonSQSClient(AmazonSQSClient amazonSQSClient) {
        this.amazonSQSClient = amazonSQSClient;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}





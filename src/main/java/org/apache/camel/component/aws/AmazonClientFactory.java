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
package org.apache.camel.component.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory for creating client instances. Handles the application of the proxy values in case you're running behind a
 * proxy server. The standard environment variables http.proxyHost and http.proxyPort are consulted.
 *
 */
public final class AmazonClientFactory {

    private static final transient Log LOG = LogFactory.getLog(AmazonClientFactory.class);

    private AmazonClientFactory() {
        // hide the constructor
    }

    public static AmazonSNSClient createSNSClient(AWSCredentials credentials) {
        return createSNSClient(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey());
    }

    public static AmazonSNSClient createSNSClient(String accessKey, String secretKey) {
        return new AmazonSNSClient(new BasicAWSCredentials(accessKey, secretKey), new ClientConfiguration().withProxyHost(getProxyHost()).withProxyPort(getProxyPort()));
    }

    public static AmazonSQSClient createSQSClient(AWSCredentials credentials) {
        return createSQSClient(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey());
    }

    public static AmazonSQSClient createSQSClient(String accessKey, String secretKey) {
        return new AmazonSQSClient(new BasicAWSCredentials(accessKey, secretKey), new ClientConfiguration().withProxyHost(getProxyHost()).withProxyPort(getProxyPort()));
    }

    private static String getProxyHost() {
        String proxyHost = System.getProperty("http.proxyHost");
        if (LOG.isTraceEnabled()) {
            LOG.trace("proxy host=" + proxyHost);
        }
        return proxyHost;
    }

    private static int getProxyPort() {
        String port = System.getProperty("http.proxyPort");
        if (port == null || port.length() == 0) {
            return -1;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("proxy port=" + port);
        }
        return Integer.parseInt(port);
    }
}

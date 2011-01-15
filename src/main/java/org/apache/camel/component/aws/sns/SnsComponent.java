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

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.ObjectHelper;

/**
 * Standard component that creates SNSEndpoints given a uri and params.
 *
 */
public class SnsComponent extends DefaultComponent {

    private static final String TOPIC_NAME = "topicName/";

    private SnsConfiguration configuration;

    public SnsComponent() {
        super();
        configuration = new SnsConfiguration();
    }

    public SnsComponent(CamelContext context) {
        super(context);
        configuration = new SnsConfiguration();
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        // must use copy as each endpoint can have different options
        ObjectHelper.notNull(configuration, "config");
        SnsConfiguration config = configuration.copy();

        setProperties(config, parameters);

        if (remaining == null) {
            throw new IllegalArgumentException("Topic name or ARN must be specified.");
        }

        if (remaining.startsWith("arn:aws")) {
            config.setTopicArn(remaining);
        } else {
            if (remaining.startsWith(TOPIC_NAME)) {
                // topic name is everything after 'topicName/'
                config.setTopicName(remaining.substring(TOPIC_NAME.length()));
            } else {
                throw new IllegalArgumentException("Topic name or ARN must be specified. URI didn't begin with 'topicName/' or 'arn:aws'");
            }
        }

        if (config.getAmazonSNSClient() == null && (config.getAccessKey() == null || config.getSecretKey() == null)) {
            throw new IllegalArgumentException("AmazonSNSClient or accessKey and secretKey must be set");
        }

        SnsEndpoint endpoint = new SnsEndpoint(uri, getCamelContext(), config);
        endpoint.setConsumerProperties(parameters);
        return endpoint;
    }

}

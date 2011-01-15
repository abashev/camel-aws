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

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SnsProducer extends DefaultProducer {

    private static final Log LOG = LogFactory.getLog(SnsProducer.class);

    public SnsProducer(Endpoint endpoint) {
        super(endpoint);
    }

    public void process(Exchange exchange) throws Exception {

        SnsEndpoint endpoint = (SnsEndpoint) getEndpoint();

        String topicArn = endpoint.getTopicArn();

        String subject = (String) exchange.getIn().getHeader(SnsConstants.SNS_SUBJECT);
        if (subject == null) {
            subject = endpoint.getConfiguration().getSubject();
        }
        String message = exchange.getIn().getBody(String.class);

        if (LOG.isDebugEnabled()) {
            LOG.debug("producing aws-sns message: subject | message=" + subject + " | " + message);
        }

        PublishRequest pubReq = new PublishRequest().withTopicArn(topicArn).withMessage(message).withSubject(subject);
        PublishResult result = endpoint.getClient().publish(pubReq);

        if (LOG.isDebugEnabled()) {
            LOG.debug("publish result:" + result.getMessageId());
        }
    }

    public void stop() throws Exception {
        if (this.isStopped()) {
            return;
        }

        LOG.debug("Stopping SnsConsumer");
        getEndpoint().stop();
        super.stop();
    }

}

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
package org.apache.camel.component.aws.s3;


import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.aws.AmazonClientFactory;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class S3Endpoint extends ScheduledPollEndpoint {
    private static final Log LOG = LogFactory.getLog(S3Endpoint.class);

    private S3Configuration configuration;
    private AmazonS3Client amazonS3Client;

    public S3Endpoint(String uri, S3Component component, S3Configuration configuration) {
        super(uri, component);
        this.configuration = configuration;
    }

    public Producer createProducer() throws Exception {
        return new S3Producer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating consumer for endpoint:");// + stripCredentials(getEndpointUri()));
        }

        // check for required parameters

        S3Consumer consumer = new S3Consumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    public boolean isSingleton() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public S3Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(S3Configuration configuration) {
        this.configuration = configuration;
    }

    public void setAmazonS3Client(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    public AmazonS3Client getAmazonS3Client() {
        if (amazonS3Client == null) {
            if (configuration.getAmazonS3Client() == null) {
                amazonS3Client = createS3Client();
            } else {
                amazonS3Client = configuration.getAmazonS3Client();
            }
        }
        return amazonS3Client;
    }

    private AmazonS3Client createS3Client() {
        return AmazonClientFactory.createS3Client(configuration.getAccessKey(), configuration.getSecretKey());
    }
}

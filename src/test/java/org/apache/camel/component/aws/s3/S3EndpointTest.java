package org.apache.camel.component.aws.s3;


import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class S3EndpointTest extends CamelTestSupport {


    @Test
    public void createCheckClientInjection() throws Exception {
        AmazonS3Client mockClient = mock(AmazonS3Client.class);

        ((JndiRegistry) ((PropertyPlaceholderDelegateRegistry) context.getRegistry()).getRegistry()).bind("s3Client", mockClient);

        S3Component component = new S3Component(context);
        S3Endpoint endpoint = (S3Endpoint) component.createEndpoint("aws-s3://?amazonS3Client=#s3Client");

        assertNotNull(endpoint.getConfiguration().getAmazonS3Client());
    }

    @Test
    public void createEndpointWithMinimalConfiguration() throws Exception {
        S3Component component = new S3Component(context);
        S3Endpoint endpoint = (S3Endpoint) component.createEndpoint("aws-s3://MyQueue?accessKey=xxx&secretKey=yyy");

        assertEquals("xxx", endpoint.getConfiguration().getAccessKey());
        assertEquals("yyy", endpoint.getConfiguration().getSecretKey());
    }
}

package org.apache.camel.component.aws.ses;

import static com.amazonaws.services.simpleemail.AWSJavaMailTransport.AWS_ACCESS_KEY_PROPERTY;
import static com.amazonaws.services.simpleemail.AWSJavaMailTransport.AWS_EMAIL_SERVICE_ENDPOINT_PROPERTY;
import static com.amazonaws.services.simpleemail.AWSJavaMailTransport.AWS_SECRET_KEY_PROPERTY;

import java.util.Properties;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.amazonaws.services.simpleemail.AWSJavaMailTransport;

/**
 * Spring JavaMailSender extension for using Amazon Simple Mail Service as transport.
 * 
 * Code snippet I took from https://github.com/ryanlea/spring-amazon-services
 *   
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class AmazonSESMailSender extends JavaMailSenderImpl {
    public static final String MAIL_TRANSPORT_PROTOCOL_KEY = "mail.transport.protocol";

    /**
     * @param accessKey
     * @param secretKey
     * @param httpsEndpoint
     */
    public AmazonSESMailSender(String accessKey, String secretKey, String httpsEndpoint) {
        super();

        Properties props = getJavaMailProperties();
        
        props.setProperty(MAIL_TRANSPORT_PROTOCOL_KEY, "aws");
        props.setProperty(AWS_ACCESS_KEY_PROPERTY, accessKey);
        props.setProperty(AWS_SECRET_KEY_PROPERTY, secretKey);
        
        if (httpsEndpoint != null) {
            props.setProperty(AWS_EMAIL_SERVICE_ENDPOINT_PROPERTY, httpsEndpoint);
        }
        
        // set port to -1 to ensure that spring calls the equivalent of transport.connect().
        setPort(-1);
    }

    @Override
    protected Transport getTransport(Session session) throws NoSuchProviderException {
        return new AWSJavaMailTransport(session, null);
    }
}

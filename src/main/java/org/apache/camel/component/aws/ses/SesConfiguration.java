package org.apache.camel.component.aws.ses;

import org.apache.camel.component.mail.MailConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.amazonaws.auth.AWSCredentials;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class SesConfiguration extends MailConfiguration {
    private final Logger log = LoggerFactory.getLogger(SesConfiguration.class);

    private AWSCredentials credentials;
    private String accessKey;
    private String secretKey;
    private String httpsEndpoint;

    /**
     * @return the accessKey
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * @param accessKey the accessKey to set
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * @return the secretKey
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey the secretKey to set
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * @return the httpsEndpoint
     */
    public String getHttpsEndpoint() {
        return httpsEndpoint;
    }

    /**
     * @param httpsEndpoint the httpsEndpoint to set
     */
    public void setHttpsEndpoint(String httpsEndpoint) {
        this.httpsEndpoint = httpsEndpoint;
    }

    /**
     * @return the credentials
     */
    public AWSCredentials getCredentials() {
        return credentials;
    }

    /**
     * @param credentials the credentials to set
     */
    public void setCredentials(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    /* (non-Javadoc)
     * @see org.apache.camel.component.mail.MailConfiguration#createJavaMailSender()
     */
    @Override
    protected JavaMailSenderImpl createJavaMailSender() {
        final String access;
        final String secret;

        if (credentials != null) {
            access = credentials.getAWSAccessKeyId();
            secret = credentials.getAWSSecretKey();
        } else {
            access = accessKey;
            secret = secretKey;
        }

        log.debug(
                "Initalize new AmazonSESMailSender with [access={},secret=*****,endpoint={}]",
                access, httpsEndpoint
        );

        return (new AmazonSESMailSender(access, secret, httpsEndpoint));
    }

    /* (non-Javadoc)
     * @see org.apache.camel.component.mail.MailConfiguration#setJavaMailSender(org.springframework.mail.javamail.JavaMailSender)
     */
    @Override
    public final void setJavaMailSender(JavaMailSender javaMailSender) {
        // Block from redefine JavaMailSender
    }

    /* (non-Javadoc)
     * @see org.apache.camel.component.mail.MailConfiguration#getJavaMailSender()
     */
    @Override
    public final JavaMailSender getJavaMailSender() {
        // Block from redefine JavaMailSender

        return null;
    }
}

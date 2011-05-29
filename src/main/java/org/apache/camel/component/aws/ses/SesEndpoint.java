package org.apache.camel.component.aws.ses;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.component.mail.MailEndpoint;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class SesEndpoint extends MailEndpoint {
    public SesEndpoint() {
    }

    public SesEndpoint(String uri, SesComponent component, SesConfiguration configuration) {
        super(uri, component, configuration);
    }

    /* (non-Javadoc)
     * @see org.apache.camel.component.mail.MailEndpoint#createConsumer(org.apache.camel.Processor)
     */
    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException(
                "Amazon Simple Email Service is not support message consuming"
        );
    }

    /* (non-Javadoc)
     * @see org.apache.camel.component.mail.MailEndpoint#createConsumer(org.apache.camel.Processor, org.springframework.mail.javamail.JavaMailSenderImpl)
     */
    @Override
    public Consumer createConsumer(Processor processor, JavaMailSenderImpl sender) throws Exception {
        throw new UnsupportedOperationException(
                "Amazon Simple Email Service is not support message consuming"
        );
    }
}

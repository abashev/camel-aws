package org.apache.camel.component.aws.ses;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.mail.MailComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class SesComponent extends MailComponent {
    private final Logger log = LoggerFactory.getLogger(SesComponent.class);
    
    public SesComponent() {
        super();
    }

    public SesComponent(CamelContext context) {
        super(context);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        SesConfiguration configuration = new SesConfiguration();
        
        log.debug("Configure SesComponent with parameters {}", parameters);
        
        setProperties(configuration, parameters);

        if (configuration.getCredentials() == null && 
                (configuration.getAccessKey() == null || configuration.getSecretKey() == null)) {

            throw new IllegalArgumentException("credentials or accessKey and secretKey must be specified");
        }

        SesEndpoint endpoint = new SesEndpoint(uri, this, configuration);
        
        endpoint.setContentTypeResolver(getContentTypeResolver());

        return endpoint;
    }
}

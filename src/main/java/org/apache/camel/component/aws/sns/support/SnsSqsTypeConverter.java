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

package org.apache.camel.component.aws.sns.support;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

/**
 * SQS Type Converter is a strategy that converts objects to and from
 * {@link SnsSqsObject}s.
 */
@Converter
public final class SnsSqsTypeConverter {
    private static final transient Log LOG = LogFactory.getLog(SnsSqsTypeConverter.class);

    private static PublicKey sPublicKey;

    private SnsSqsTypeConverter() {
        // hide the constructor
    }

    @Converter
    public static String toString(SnsSqsObject value, Exchange exchange) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("converting sqsObject to String: " + value);
        }
        return value.toString();
    }

    @Converter
    public static SnsSqsObject toSQSObject(String value, Exchange exchange) throws Exception {
        return toSQSObject(value);
    }

    public static SnsSqsObject toSQSObject(String aMessage) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("raw SQSMessage: " + aMessage);
        }

        JSONObject json = new JSONObject(aMessage);

        return new SnsSqsObject(json);
    }

    public static boolean verify(SnsSqsObject aSnsSqsObject) throws Exception {
        byte[] canonicalString = toCanonicalString(aSnsSqsObject);

        String encodedSig = aSnsSqsObject.getSignature();
        byte[] signed = decode(encodedSig);

        PublicKey publicKey = getAmazonPublicKey();

        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(publicKey);

        signature.update(canonicalString);
        return signature.verify(signed);
    }

    private static byte[] toCanonicalString(SnsSqsObject aSnsSqsObject) throws UnsupportedEncodingException {
        String message = aSnsSqsObject.getMessage();
        String messageId = aSnsSqsObject.getMessageId();
        String subject = aSnsSqsObject.getSubject();
        String timestamp = aSnsSqsObject.getTimestamp();
        String topicArn = aSnsSqsObject.getTopicArn();
        String type = aSnsSqsObject.getType();

        StringBuilder sb = new StringBuilder();
        sb.append("Message\n");
        sb.append(message).append("\n");
        sb.append("MessageId\n");
        sb.append(messageId).append("\n");
        if (subject != null) {
            sb.append("Subject\n");
            sb.append(subject).append("\n");
        }
        sb.append("Timestamp\n");
        sb.append(timestamp).append("\n");
        sb.append("TopicArn\n");
        sb.append(topicArn).append("\n");
        sb.append("Type\n");
        sb.append(type).append("\n");
        return sb.toString().getBytes("UTF-8");
    }

    private static byte[] decode(String aMessage) {
        return Base64.decodeBase64(aMessage);
    }

    private static PublicKey getAmazonPublicKey() throws Exception {
        if (sPublicKey == null) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new URL(
                "http://sns.us-east-1.amazonaws.com/SimpleNotificationService.pem").openStream());
            sPublicKey = cert.getPublicKey();
        }
        return sPublicKey;
    }

}

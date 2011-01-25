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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class SnsSqsObject {

    private static final Log LOG = LogFactory.getLog(SnsSqsObject.class);

    JSONObject jsonObject;

    public SnsSqsObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public String getMessageId() {
        return getValue("MessageId");
    }

    public String getTimestamp() {
        return getValue("Timestamp");
    }

    public String getTopicArn() {
        return getValue("TopicArn");
    }

    public String getType() {
        return getValue("Type");
    }

    public String getUnsubscribe() {
        return getValue("UnsubscribeURL");
    }

    public String getMessage() {
        return getValue("Message");
    }

    public String getSubject() {
        return getValue("Subject");
    }

    public String getSignature() {
        return getValue("Signature");
    }

    public String getSignatureVersion() {
        return getValue("SignatureVersion");
    }

    public String toString() {
        try {
            return jsonObject.toString(3);
        } catch (JSONException e) {
            LOG.trace("Error converting JSON object to string: " + e);
            return "";
        }
    }

    public String getValue(String prop) {
        try {
            return jsonObject.getString(prop);
        } catch (JSONException e) {
            LOG.trace("field not found", e);
            return "";
        }
    }
}

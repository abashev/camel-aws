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

/**
 * The AWS S3 component configuration properties
 *
 */
public class S3Configuration implements Cloneable {

    private String accessKey;
    private String secretKey;
    private AmazonS3Client amazonS3Client;

    private String bucketName;
    private String region;
    private boolean deleteAfterRead = true;
    private boolean deleteAfterUpload = false;
    private String amazonS3Endpoint;

    public void setAmazonS3Endpoint(String amazonS3Endpoint) {
        this.amazonS3Endpoint = amazonS3Endpoint;
    }

    public String getAmazonS3Endpoint() {
        return amazonS3Endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public AmazonS3Client getAmazonS3Client() {
        return amazonS3Client;
    }

    public void setAmazonS3Client(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isDeleteAfterRead() {
        return deleteAfterRead;
    }

    public void setDeleteAfterRead(boolean deleteAfterRead) {
        this.deleteAfterRead = deleteAfterRead;
    }

    /**
     * @return the deleteAfterUpload
     */
    public boolean isDeleteAfterUpload() {
        return deleteAfterUpload;
    }

    /**
     * @param deleteAfterUpload the deleteAfterUpload to set
     */
    public void setDeleteAfterUpload(boolean deleteAfterUpload) {
        this.deleteAfterUpload = deleteAfterUpload;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "S3Configuration [accessKey=" + accessKey + ", secretKey=*****, amazonS3Client="
                + amazonS3Client + ", bucketName=" + bucketName + ", region=" + region + ", deleteAfterRead="
                + deleteAfterRead + ", deleteAfterUpload=" + deleteAfterUpload + ", amazonS3Endpoint="
                + amazonS3Endpoint + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public S3Configuration clone() {
        try {
            return (S3Configuration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Something terrible happend with clone");
        }
    }
}
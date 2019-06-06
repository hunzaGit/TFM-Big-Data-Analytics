/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rodrigo.tfm.kinesis;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.tfm.kinesis.utils.ConfigurationUtils;
import com.rodrigo.tfm.kinesis.utils.MiAWSCredentialsProvider;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class KinesisProducer {


    private static String streamName;
    private static String regionName;
    private static AmazonKinesis kinesisClient;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static KinesisProducer uniqueInstance;

    public static KinesisProducer getInstance() {

        if (uniqueInstance == null) {
            uniqueInstance = new KinesisProducer();
        }
        return uniqueInstance;
    }

    private KinesisProducer() {
        regionName = System.getenv("regionName");
        System.out.println("regionName = [" + regionName + "]");

        Region region = RegionUtils.getRegion(regionName);
        if (region == null) {
            System.err.println(regionName + " is not a valid AWS region.");
            System.exit(1);
        }


        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();
        clientBuilder.setRegion(regionName);
        clientBuilder.setCredentials(new MiAWSCredentialsProvider());
        clientBuilder.setClientConfiguration(ConfigurationUtils.getClientConfigWithUserAgent());


        System.out.println("Creating kinesisClient");
        kinesisClient = clientBuilder.build();


        streamName = System.getenv("streamName");
        System.out.println("streamName = [" + streamName + "]");
        // Validate that the stream exists and is active
        validateStream(streamName);


    }


    public void produceRecords(String idAnimal, Set<String> listEmails) {
        Map<String, String> map = new HashMap<>();

        map.put("id_animal", idAnimal);
        for (String email : listEmails) {
            map.put("email", email);
            System.out.println("Prepare send");
            sendJSONtoKinesis(map);
        }

        System.out.println("---------------------------------");
        System.out.println("- Send all records ");
        System.out.println("---------------------------------");

    }


    /**
     * Uses the Kinesis client to send the JSON to the given stream.
     *
     * @param map instance representing the email-animal
     */
    private static void sendJSONtoKinesis(Map map) {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(map);
            byte[] jsonBytes = json.getBytes();

            if (jsonBytes == null) {
                System.err.println("jsonbytes cant be null");
            }

            System.out.println("Creating PutRecordRequest");

            PutRecordRequest putRecord = new PutRecordRequest();
            putRecord.setStreamName(streamName);
            putRecord.setPartitionKey(("id-" + Math.floor(Math.random() * 10000000)));
            putRecord.setData(ByteBuffer.wrap(jsonBytes));

            System.out.println("Send record to Stream [" + streamName + "] -> [" + json + "]");
            PutRecordResult result = kinesisClient.putRecord(putRecord);

            System.out.println("result = [" + result + "]");

        } catch (AmazonClientException | JsonProcessingException ex) {
            System.err.println("Error sending record to Amazon Kinesis: " + ex.getMessage());
            ex.printStackTrace();
        }

    }


    /**
     * Checks if the stream exists and is active
     *
     * @param streamName Name of stream
     */
    private static void validateStream(String streamName) {
        try {
            DescribeStreamResult result = kinesisClient.describeStream(streamName);
            if (!"ACTIVE".equals(result.getStreamDescription().getStreamStatus())) {
                System.err.println("Stream " + streamName + " is not active. Please wait a few moments and try again.");
                System.exit(1);
            }
        } catch (ResourceNotFoundException e) {
            System.err.println("Stream " + streamName + " does not exist. Please create it in the console.");
            System.err.println(e);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error found while describing the stream " + streamName);
            System.err.println(e.getMessage());
            e.printStackTrace();

            System.exit(1);
        }
    }

}

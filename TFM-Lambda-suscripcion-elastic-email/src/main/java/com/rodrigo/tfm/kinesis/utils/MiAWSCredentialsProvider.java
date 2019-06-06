package com.rodrigo.tfm.kinesis.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class MiAWSCredentialsProvider extends ProfileCredentialsProvider implements AWSCredentialsProvider {


    private static AWSCredentials credentials;



    public AWSCredentials getCredentials() {
        if (credentials == null) {
            credentials = createCredentians();
        }
        return credentials;
    }


    private AWSCredentials createCredentians() {

        final String AWSAccessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
//        System.out.println("AWS_ACCESS_KEY_ID = [" + AWSAccessKeyId + "]");
        final String AWSSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
//        System.out.println("AWS_SECRET_ACCESS_KEY = [" + AWSSecretKey + "]");

        final String AWSSessionToken = System.getenv("AWS_SESSION_TOKEN");
//        System.out.println("AWS_SESSION_TOKEN = [" + AWSSessionToken + "]");

        return new AWSSessionCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return AWSAccessKeyId;
            }

            @Override
            public String getAWSSecretKey() {
                return AWSSecretKey;
            }

            @Override
            public String getSessionToken() {
                return AWSSessionToken;
            }
        };

    }
    

}

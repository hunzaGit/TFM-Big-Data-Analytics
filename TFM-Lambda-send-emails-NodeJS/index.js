console.log('Loading function');
const axios = require('axios');

exports.handler = function (event, context, callback) {
    return SendEmail(event, context);
};


function SendEmail(event, context) {

    event.Records.forEach(function (record) {
        // Kinesis data is base64 encoded so decode here
        //console.log("----------------------------------------------------------------------");

        //console.log("buff - utf8: " + new Buffer('bXlEYXRhLTA=','base64').toString('utf8'));

        console.log(JSON.stringify(record));
        console.log(record.kinesis.data);

        const payload = new Buffer(record.kinesis.data, 'base64').toString('utf8');

        const jsonPayload = JSON.parse(payload);
        console.log("payload: " + JSON.stringify(jsonPayload));

        //var payload = record.kinesis.data
        //console.log('Payload decoded:', payload);
        //console.log("----------------------------------------------------------------------");
        //console.log("**********************************************************************");


        const dataEmail = {
            "toEmail": jsonPayload.email,
            "fromEmail": process.env.fromEmail,
            "tipoEmail": process.env.tipoEmail,
            "animal": jsonPayload.id_animal,
        };

        console.log("**************************************************************");
        console.log(" *******************  Datos para el email ******************* ");
        console.log(dataEmail);
        console.log("**************************************************************");

        axios.post(process.env.endpoint_email, dataEmail)
            .then(responseSendEmail => {
                console.log("Successfully send email to -> "+jsonPayload.email);

                // console.log(responseSendEmail);
                return "Successfully send email to -> "+jsonPayload.email;
            })

            .catch(error => {
                console.log()
                console.error("*******************************************************");
                console.error("* Error en la respuesta del servidor")
                console.error("*  -> error  = '" + error + "'");
                console.error("*******************************************************");
                return error;

            });

    });
}


SendEmail(JSON.parse("{\n" +
    "  \"Records\": [\n" +
    "    {\n" +
    "      \"kinesis\": {\n" +
    "        \"kinesisSchemaVersion\": \"1.0\",\n" +
    "        \"partitionKey\": \"1\",\n" +
    "        \"sequenceNumber\": \"49590338271490256608559692538361571095921575989136588898\",\n" +
    "        \"data\": \"eyJlbWFpbCI6InJvZHJpZ28udHJhemFzQGdtYWlsLmNvbSIsImlkX2FuaW1hbCI6IjU5OTgzNDlmOGIzYzIxMWE0NDI3ZTk1ZiJ9\",\n" +
    "        \"approximateArrivalTimestamp\": 1545084650.987\n" +
    "      },\n" +
    "      \"eventSource\": \"aws:kinesis\",\n" +
    "      \"eventVersion\": \"1.0\",\n" +
    "      \"eventID\": \"shardId-000000000006:49590338271490256608559692538361571095921575989136588898\",\n" +
    "      \"eventName\": \"aws:kinesis:record\",\n" +
    "      \"invokeIdentityArn\": \"arn:aws:iam::123456789012:role/lambda-kinesis-role\",\n" +
    "      \"awsRegion\": \"us-east-2\",\n" +
    "      \"eventSourceARN\": \"arn:aws:kinesis:us-east-2:123456789012:stream/lambda-stream\"\n" +
    "    }\n" +
    "  ]\n" +
    "}"));

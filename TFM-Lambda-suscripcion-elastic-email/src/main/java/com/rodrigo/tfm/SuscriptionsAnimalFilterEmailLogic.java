package com.rodrigo.tfm;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.tfm.elastic.ElasticSearchService;
import com.rodrigo.tfm.kinesis.KinesisProducer;

import java.io.IOException;
import java.util.*;

public class SuscriptionsAnimalFilterEmailLogic {


    private static ObjectMapper objectMapper = new ObjectMapper();


    public static void execute(LinkedHashMap input, Context context) {

        System.out.println("START EXECUTION");


        List<LinkedHashMap> records = (List<LinkedHashMap>) input.get("Records");
        System.out.println("records.size() = [" + records.size() + "]");

        for (LinkedHashMap record : records) {
//             process record

            LinkedHashMap kinesisObject = (LinkedHashMap) record.get("kinesis");
            String data = (String) kinesisObject.get("data");

            Map animal = null;
            try {
                animal = objectMapper.readValue(decodeString(data), HashMap.class);
            } catch (IOException e) {
                System.out.println("Error procesing record [" + record.get("eventID") +"]: " + e.getMessage());
                e.printStackTrace();
            }

            Map<String, Object> filtro = getFilterFromAnimal(animal);

            System.out.println("INSTANCIANDO ELASTICCONNECTOR");

            System.out.println("pidiendo emails");
            Set<String> listEmails = ElasticSearchService.getIntance().getEmails(filtro);

            for (String email : listEmails) {
                System.out.println(email);
            }


            KinesisProducer.getInstance().produceRecords((String) animal.get("id"), listEmails);

        }


        System.out.println("END EXECUTION");
    }


    private static Map<String, Object> getFilterFromAnimal(Map<String, Object> animal) {

        Map f = new HashMap<String, Object>();

        if (animal.get("especie") != null) f.put("especie", animal.get("especie"));
        if (animal.get("raza") != null) f.put("raza", animal.get("raza"));
        if (animal.get("sexo") != null) f.put("sexo", animal.get("sexo"));
        if (animal.get("edad") != null) f.put("edad", animal.get("edad"));
        if (animal.get("tamano") != null) f.put("tamano", animal.get("tamano"));
        return f;
    }

    private static String decodeString(String data) {
        return new String(Base64.getDecoder().decode(data));
    }


}

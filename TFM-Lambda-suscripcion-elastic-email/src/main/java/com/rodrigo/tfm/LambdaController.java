package com.rodrigo.tfm;

import com.amazonaws.services.lambda.runtime.Context;

import java.util.*;

public class LambdaController {

    public static void main(String[] args)  {
        long t1 = System.currentTimeMillis();

        new LambdaController().handleRequest(null, null);
        long t2 = System.currentTimeMillis();

        System.out.println("timing --> [" + (t2 - t1) + " ms]");
    }

    public void handleRequest(LinkedHashMap input, Context context) {

        SuscriptionsAnimalFilterEmailLogic.execute(input,context);

    }


}
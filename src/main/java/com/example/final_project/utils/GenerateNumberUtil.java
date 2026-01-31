package com.example.final_project.utils;

import java.util.Random;

public class GenerateNumberUtil {

    public static String generateCodeNumber() {
        Random random = new Random();
        int number = random.nextInt(9999);
        return String.format("%06d", number);
    }


}

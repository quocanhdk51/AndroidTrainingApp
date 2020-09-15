package com.example.s3727634.afinal;

import java.util.Random;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by s3727634 on 1/5/19.
 */

public class Anagram {
    public static String WORD = "";
    public static String[] KEYS ;

    public static final String[] LIST_OF_WORDS = {"APPLE", "BANANA", "PINEAPPLE", "DURIAN" , "DOG", "CAT", "TIGER", "LION", "BLACKBERRY",
        "BLUEBERRY", "CHERRY", "MANGO", "ORANGE", "PAPAYA", "PEACH"};


    public static String randomWord() {
        Random randomInx= new Random();
        int wordIndex = randomInx.nextInt(LIST_OF_WORDS.length);
        WORD = LIST_OF_WORDS[wordIndex];
        return WORD;
    }

    public static String[] shuffleWord(String word) {

        List<String> letters = Arrays.asList(word.split(""));
        Collections.shuffle(letters);
        String shuffledWord = "";
        for (String letter : letters) {
            shuffledWord += letter;
        }

        KEYS  = new String[shuffledWord.length()];
        for (int i = 0; i < shuffledWord.length(); i++) {
            KEYS[i] = Character.toString(shuffledWord.charAt(i));
        }

        return KEYS;

    }

    public static String getWord(int index){
        WORD = LIST_OF_WORDS[index];
        return WORD;
    }

    public static int totalLevel(){
        return LIST_OF_WORDS.length;
    }
}
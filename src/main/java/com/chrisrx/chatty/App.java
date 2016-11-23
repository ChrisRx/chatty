package com.chrisrx.chatty;

import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import java.util.HashMap;
import java.util.Map;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.*;


public class App {

    private static final String ACOUSTIC_MODEL =
        "resource:/edu/cmu/sphinx/models/en-us/en-us";
    private static final String DICTIONARY_PATH =
        "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
    private static final String GRAMMAR_PATH =
        "resource:/config";
    //private static final String LANGUAGE_MODEL =
        //"resource:/edu/cmu/sphinx/demo/dialog/weather.lm";

    //private static LiveSpeechRecognizer jsgfRecognizer;
    //private static LiveSpeechRecognizer commandRecognizer;

    public static enum State {READY, WAITING};

    private State currentState = State.READY;

    private String controlWord;

    public App(String controlWord) throws Exception {
        this.controlWord = controlWord;
    }

    private synchronized void setState(State newState) {
        currentState = newState;
    }

    private boolean checkForTrigger(String utterance) {
        String[] parts = utterance.split(" ");
        for (String s: parts) {
            if (s.equals(controlWord)) {
                return true;
            }
        }
        return false;
    }

    //private static boolean recognizeCommand(LiveSpeechRecognizer recognizer) {
    //private static boolean recognizeCommand() {
        //System.out.format("Awaiting command ...\n");
        ////String utterance = recognizer.getResult().getHypothesis();
        //String utterance = jsgfRecognizer.getResult().getHypothesis();
        //System.out.format("COMMAND: %s\n", utterance);
        //return true;
    //}

    public static void main(String[] args) throws Exception {
        try {
            App app = new App("cynthia");
            app.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath(ACOUSTIC_MODEL);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setGrammarPath(GRAMMAR_PATH);
        configuration.setUseGrammar(true);
        configuration.setGrammarName("command");
        LiveSpeechRecognizer jsgfRecognizer = new LiveSpeechRecognizer(configuration);
        jsgfRecognizer.startRecognition(true);

        while (true) {
            String utterance = jsgfRecognizer.getResult().getHypothesis();

            if (currentState == State.WAITING) {
                System.out.format("COMMAND: %s\n", utterance);
                setState(State.READY);
                continue;
            } else {
                System.out.format("Input: %s\n", utterance);
            }

            if (checkForTrigger(utterance)) {
                setState(State.WAITING);
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Hi!");
                        setState(State.READY);
                    }
                }, 5000);
            }
            if (utterance.equals("exit")) {
                break;
            }
        }

        jsgfRecognizer.stopRecognition();
    }
}

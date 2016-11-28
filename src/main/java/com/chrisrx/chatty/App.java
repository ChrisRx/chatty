package com.chrisrx.chatty;

import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import java.util.HashMap;
import java.util.Map;

import java.nio.file.Files;

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

    public static void main(String[] args) throws Exception {
        try {
            App app = new App("cynthia");
            app.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(String[] args) throws Exception {
        SphinxServer.start("0.0.0.0", 3000, 10);
    }
}

package com.chrisrx.chatty;

import java.io.*;

import javax.sound.sampled.*;
import java.nio.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.net.URL;
import javax.sound.sampled.*;
import java.nio.*;

import java.lang.Runtime.*;
import java.io.InputStreamReader;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.result.WordResult;
import edu.cmu.sphinx.util.TimeFrame;

public class SphinxServer extends AbstractHandler {

    private static final String ACOUSTIC_MODEL =
        "resource:/edu/cmu/sphinx/models/en-us/en-us";
    private static final String DICTIONARY_PATH =
        "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
    private static final String GRAMMAR_PATH =
        "resource:/config";
    private static final String LANGUAGE_MODEL =
        "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";

    public Recognizer recognizer;
    public Context context;

    public SphinxServer() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath(ACOUSTIC_MODEL);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setLanguageModelPath(LANGUAGE_MODEL);
        configuration.setGrammarPath(GRAMMAR_PATH);
        configuration.setUseGrammar(true);
        configuration.setGrammarName("command");
        context = new Context(configuration);
        //context.setLocalProperty("decoder->searchManager", "allphoneSearchManager");
        this.recognizer = context.getInstance(Recognizer.class);
        recognizer.allocate();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String text = null;
        InputStream in = (InputStream) request.getInputStream();
        
        context.setSpeechSource(in, TimeFrame.INFINITE);
        Result result;
        while ((result = recognizer.recognize()) != null) {
            SpeechResult speechResult = new SpeechResult(result);
            text = speechResult.getHypothesis();
            System.out.format("Hypothesis: %s\n", speechResult.getHypothesis());

            System.out.println("List of recognized words and their times:");
            for (WordResult r : speechResult.getWords()) {
                System.out.println(r);
            }
        }
        // response
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        if (text != null) {
            response.getWriter().println(text);
        } else {
            response.getWriter().println("(NULL)");        
        }
    }
    
    public static void start(String addr, int port, int nthreads) throws Exception {
        Server server = new Server();
        HttpConfiguration http_config = new HttpConfiguration();

        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
        http.setPort(port);
        http.setIdleTimeout(30000);

        server.setConnectors(new Connector[] { http });
        server.setHandler(new SphinxServer()); 
 
        server.start();
        server.join();
    }
}

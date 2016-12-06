/**
 package org.nanohttpd.samples.http;

 /*
 * #%L
 * NanoHttpd-Samples
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.util.ServerRunner;

/**
 * An example of subclassing NanoHTTPD to make a custom HTTP server.
 */
public class Main extends NanoHTTPD {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        try {
            Thread t = new Thread(() ->{
                try {
                    BadugiCFRTrainer.main(null);
                }finally {

                    try {
                        printValues("inner");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            ServerRunner.run(Main.class);
        }finally {
            printValues("outer");
        }
    }

    private static void printValues(String fileName) throws FileNotFoundException {

        System.setOut(new PrintStream(new File(fileName +".txt")));
        for(String k :BadugiCFRTrainer.nodeMap.keySet()) {
            System.out.printf("%s : %s\n", k, BadugiCFRTrainer.nodeMap.get(k));
        }
    }

    public Main() {
        super(8080);
    }

    @Override
    public Response serve(IHTTPSession session) {
        //Method method = session.getMethod();
        String u = session.getUri();
        URI uri = null;
        try {
            uri = new URI(u);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String path = uri == null? "" : uri.getPath();
        String state = path.split("/")[1];
        if(state.startsWith("r: ")) return  Response.newFixedLengthResponse("2");
        int res = -1;
        BadugiCFRTrainer.Node n = BadugiCFRTrainer.nodeMap.get(state);
        if (n == null) {
            System.out.println("Failed to find state:" + state);
            return Response.newFixedLengthResponse("2");
        }
        synchronized (n){

            float[] strat =  n.getAverageStrategy();
            float f = BadugiCFRTrainer.random.nextFloat();
            float thresh = 0;
            for (int i = 0; i < strat.length; i++) {
                float s = strat[i];
                thresh+=s;
                if(f<thresh){
                    res =  i + (n.numActions ==5?0 :-1);
                }
            }
        }
        return Response.newFixedLengthResponse(res+"");
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2018 Rajeev Hegde
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.helperservice.service.rest;

import lombok.Getter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class can be used to perform parallel rest operations.
 * Dependency: HttpClient, Lombok,
 */
@Getter
public class ParallelRestClient {

    private int MAX_THREADS = 5;
    private ExecutorService executorService;
    private HttpClient httpClient;

    private Map<String, HttpUriRequest> httpRequests = new HashMap<>();

    ParallelRestClient() {
        this(HttpClients.createDefault());
    }


    ParallelRestClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }


    public String addRequest(HttpGet httpGet) {
        String requestId = UUID.randomUUID().toString();
        if (httpGet != null)
            this.httpRequests.put(requestId, httpGet);
        return requestId;
    }

    public String addRequest(HttpPost httpPost) {
        String requestId = UUID.randomUUID().toString();
        if (httpPost != null)
            this.httpRequests.put(requestId, httpPost);
        return requestId;
    }

    public Map<String, HttpUriRequest> addRequests(List<HttpUriRequest> httpRequests) {
        Map<String, HttpUriRequest> requestMap = null;
        if (httpRequests != null) {
            requestMap = httpRequests.stream().collect(Collectors.toMap(val -> UUID.randomUUID().toString(), Function.identity()));
            this.httpRequests.putAll(requestMap);
        }
        return requestMap;
    }


    public List<HttpResponse> executeAll() throws InterruptedException {
        if (!this.httpRequests.isEmpty()) {

            List<Callable<HttpResponse>> callableList = new ArrayList<>();
            this.httpRequests.forEach((requestId, httpUriRequest) -> {
                try {
                    this.httpClient.execute(httpUriRequest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return executorService.invokeAll(callableList)
                    .stream()
                    .map(httpResponseFuture -> {
                        HttpResponse response=null;
                        try {
                            response= httpResponseFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                        return response;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return null;
    }


    public static void main(String[] args) {
        System.out.println(UUID.randomUUID().toString());
    }

}

package com.hazelcast.test.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.test.IncrementingEntryProcessor;

@WebServlet(urlPatterns = "/*")
public class ClientServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter writer = resp.getWriter();
        ClientConfig clientConfig = new ClientConfig();
        ClientUserCodeDeploymentConfig clientUserCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
        clientUserCodeDeploymentConfig.addJar("module-shared-0.1.0-SNAPSHOT.jar");
        clientUserCodeDeploymentConfig.setEnabled(true);
        // clientConfig.setClassLoader(Thread.currentThread().getContextClassLoader());
        clientConfig.setClassLoader(ClientServlet.class.getClassLoader());
        clientConfig.setUserCodeDeploymentConfig(clientUserCodeDeploymentConfig);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        IncrementingEntryProcessor incrementingEntryProcessor = new IncrementingEntryProcessor();
        int keyCount = 10;
        IMap<Integer, Integer> map = client.getMap("sample map");

        if (map.isEmpty()) {
            for (int i = 0; i < keyCount; i++) {
                map.put(i, 0);
            }
        }
        map.executeOnEntries(incrementingEntryProcessor);

        for (int i = 0; i < keyCount; i++) {
            writer.println(map.get(i));
        }
        client.shutdown();
    }
}

package com.example.demo.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class SimpleStatusServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/plain");
        PrintWriter writer = resp.getWriter();
        writer.write("Multi-Location Inventory Balancer is running");
        writer.flush();
    }
}

package com.dmytrobilokha.zghadai;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/HelloWorld"})
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter pw = resp.getWriter();
        pw.println("<html>");
        pw.println("<head><title>Hello World</title></title>");
        pw.println("<body>");
        pw.println("<h1>Hello World from Servlet!</h1>");
        pw.println("</body></html>");
    }

}

/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.avro.ui.sandbox.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.kaaproject.avro.ui.sandbox.services.cache.JsonCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class FileUploadServlet extends HttpServlet {

    private static final long serialVersionUID = -1660736020844466375L;
    
    private static final String FILE_NAME = "fileName";
    private static final String JSON_KEY = "jsonKey";

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(FileUploadServlet.class);
    
    @Autowired
    private JsonCacheService jsonCacheService;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
          config.getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletFileUpload upload = new ServletFileUpload();

        try{
            FileItemIterator iterator = upload.getItemIterator(request);
            if (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                String name = item.getFieldName();

                logger.debug("Uploading file '{}' with item name '{}'", item.getName(), name);

                InputStream stream = item.openStream();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Streams.copy(stream, out, true);

                byte[] data = out.toByteArray();

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/html");
                response.setCharacterEncoding("utf-8");
                response.getWriter().print(new String(data));
                response.flushBuffer();
            }
            else {
                logger.error("No file found in post request!");
                throw new RuntimeException("No file found in post request!");
            }
        }
        catch(Exception e){
            logger.error("Unexpected error in FileUploadServlet.doPost: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jsonKey = request.getParameter(JSON_KEY);
        String fileName = request.getParameter(FILE_NAME);
        if (jsonKey != null && fileName != null) {
            try ( OutputStream os = response.getOutputStream() )  {
                  response.setContentType("application/json");
                  response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
                  byte[] content = jsonCacheService.getJson(jsonKey).getBytes();
                  os.write(content, 0, content.length);
                  logger.debug("Returning text file with name '{}'", fileName);
                  response.flushBuffer();
            } catch (IOException e) {
                logger.error("Unexpected error in FileUploadServlet.doGet: ", e);
                throw new RuntimeException(e);
            }
        } else {
            logger.error("Empty parameter with file content in get file request!");
            throw new RuntimeException("Empty parameter with file content in get file request!");
        }
    }
}

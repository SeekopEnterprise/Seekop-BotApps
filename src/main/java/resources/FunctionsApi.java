/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resources;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Fabian Seekop
 */
public class FunctionsApi {

    private int Status;

    public static String getUrlEncode(String cadena) {
        String url = null;
        if (cadena != null) {
            try {
                url = URLEncoder.encode(cadena, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException ex) {
                System.out.println("Error=" + ex.toString());
            }
        }
        return url;
    }

    
    public int getStatus() {
        return Status;
    }

    public void setStatus(int Status) {
        this.Status = Status;
    }

}

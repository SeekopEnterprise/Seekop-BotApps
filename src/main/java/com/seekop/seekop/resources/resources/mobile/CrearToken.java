package com.seekop.seekop.resources.resources.mobile;

import java.io.UnsupportedEncodingException;
import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Seekop
 */
public class CrearToken extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Crear Token";
    private String fechafin = "";

    private String ip = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String recibidoJSON = "";
    private String jsonBody = "[]";
    //////
    private String idProspecto = "";
    private String idDistribuidor = "";

    public CrearToken(String contenido, String ip) {
        recibidoJSON = contenido;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON;
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON);
                this.ip = ip;
                this.idProspecto = objetoJson.getString("IdProspecto");
                this.idDistribuidor = objetoJson.getString("IdDistribuidor");
            } catch (JSONException ex) {
                setErrorMensaje("JSON malformed: " + ex.toString());
            }
            if (!idDistribuidor.isEmpty()) {
                abrirConnectionDistribuidor(idDistribuidor);
                if (getConnectionDistribuidor() != null) {
                    conection();
                } else {
                    setErrorMensaje("No se encontro una conexion para el IdDistribuidor='" + idDistribuidor + "'");
                }
            } else {
                setErrorMensaje("el 'IdDistribuidor' es necesario para esta operacion y no debe estar vacio");
            }

        } else {
            setErrorMensaje("Not JSON fount");
        }
        json = generaJSONRespuesta();
        CloseConnection();
    }

    public void conection() {
        buscarDatosProspecto(idProspecto);
        String nuevoToken = getGenerateToken(getIdMarca(), getIdDistribuidor(), getIdEjecutivo(), idProspecto, null, 10, 0);
        jsonBody = nuevoToken;

    }

    private String getGenerateToken(String idMarca, String idDistribuidor, String idUsuario, String id, String vigencia, int length, int retry) {
        StringBuilder token = new StringBuilder(Long.toString(new GregorianCalendar().getTimeInMillis(), 36).toUpperCase());
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        if (getConnectionATI().executeQuery("Select Token From sicopbdc.Tokens Where IdMarca = '" + idMarca + "' And IdDistribuidor = '" + idDistribuidor + "' And IdUsuario = '" + idUsuario + "' And Id = '" + id + "'") && getConnectionATI().next()) {
            token = new StringBuilder(getConnectionATI().getString("Token"));
        } else {
            Random random = new Random();
            while (token.length() < length) {
                token.append(alpha.charAt(random.nextInt(alpha.length() + 1)));
            }
            boolean insertBoolean;
            insertBoolean = getConnectionATI().execute("Insert Into sicopbdc.Tokens(Token, IdMarca, IdDistribuidor, IdUsuario, Id, Vigencia, Registro) Values('" + token.toString() + "','" + idMarca + "','" + idDistribuidor + "','" + idUsuario + "','" + id + "','" + (vigencia == null ? "1900-01-01" : vigencia) + "', Now());");
            if (!insertBoolean) {
                if (retry < 5) {
                    return getGenerateToken(idMarca, idDistribuidor, idUsuario, id, vigencia, length, retry + 1);
                }else{
                    setErrorMensaje("Error al guardar el token '"+getConnectionATI().getErrorMessage()+"'");
                }
            }
        }
        return token.toString();
    }

    public static String encodeToISO88591(String input) {
        String resultado = "";
        try {
            // Convierte la cadena a bytes en UTF-8 y luego a una nueva cadena en ISO-8859-1
            resultado = new String(input.getBytes("UTF-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            resultado = input;
        }
        return resultado;
    }

    private String generaJSONRespuesta() {
        String respuesta = "";
        switch (getStatus()) {
            case 0:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"" + getMensaje() + "\"\n"
                        + "    \"token\": null\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\"\n"
                        + "    \"token\": \""+jsonBody+"\"\n"
                        + "}";
                break;
        }
        return respuesta;
    }

    private void cargarRequestsApi() {
        fechafin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String bitacora = "INSERT INTO sicopdb.requestsapi\n"
                + "(IdInterfaz, IdDistribuidor, Servicio, Operacion, Api, FechaInicio, FechaFin, Ip, Response, Request) \n"
                + "VALUES ('MX00000SKP', '" + idDistribuidor + "', '" + descripcionServicio + "', 'POST', 'REST', '" + getFechaHoy() + "', '" + fechafin + "', '" + ip + "', '" + json.replaceAll("'", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "") + "', '" + jsonMandado.replaceAll("'", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "") + "');";
        if (getConnectionATI().execute(bitacora, false)) {
        } else {
            setErrorMensaje(getConnectionATI().getErrorMessage());
        }
    }

    public String getJson() {
        return json;
    }

    public String crearProgramada(int dias) {
        String fecha = "1900-01-01 00:00:00";
        Date fechaProgramada = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(fechaProgramada);
        c.add(Calendar.DATE, dias);
        fechaProgramada = c.getTime();
        fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaProgramada);

        return fecha;
    }

}

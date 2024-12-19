package com.seekop.seekop.resources.resources.mobile;

import java.io.UnsupportedEncodingException;
import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Seekop
 */
public class aceptarValuacion extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "aceptar valuacion";
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    private String nuevaFecha = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String recibidoJSON = "";
    private String jsonBody = "[]";
    //////
    private String idValuacion = "";
    private boolean aceptada = false;

    public aceptarValuacion(String contenido, String ip) {
        recibidoJSON = contenido;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON;
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON);
                this.ip = ip;
                this.token = objetoJson.getString("Token");
                this.idValuacion = objetoJson.getString("IdVauacion");
                this.aceptada = objetoJson.getBoolean("Aceptada");

            } catch (JSONException ex) {
                setErrorMensaje("JSON malformed: " + ex.toString());
            }
            if (!token.isEmpty()) {
                getTokenInformation(token);
                if (getConnectionDistribuidor() != null) {
                    conection();
                } else {
                    setErrorMensaje("No se encontro una conexion para el TOKEN='" + token + "'");
                }
            } else {
                setErrorMensaje("el 'TOKEN' es necesario para esta operacion y no debe estar vacio");
            }

        } else {
            setErrorMensaje("Not JSON fount");
        }
        json = generaJSONRespuesta();
        CloseConnection();
    }

    public void conection() {
        String sql = "SELECT \n"
                + "    IdValuacion, IdStatus\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".valuacion\n"
                + "WHERE\n"
                + "    idvaluacion = '" + idValuacion + "'\n"
                + "        AND IdProspecto = '" + getIdProspecto() + "';";
        String idStatus = "";
        if (this.aceptada) {
            idStatus = "12";
        } else {
            idStatus = "11";
        }
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                if (getConnectionDistribuidor().getString("IdStatus").equals("0")) {
                    sql = "UPDATE " + getDbDistribuidor() + ".`valuacion` SET `IdStatus` = '" + idStatus + "' WHERE (`IdValuacion` = '" + idValuacion + "');";
                    if (getConnectionDistribuidor().executeUpdate(sql)) {

                    } else {
                        setErrorMensaje("Error al actualizar valuacion= '" + getConnectionDistribuidor().getErrorMessage() + "'");
                    }
                } else {
                    setErrorMensaje("La valuacion con el IdValuacion '" + idValuacion + "' y el token '" + token + "' tiene ya otro estatus");
                }

            } else {
                setErrorMensaje("No se encontro valuacion con el IdValuacion '" + idValuacion + "' y el token '" + token + "'");
            }
        } else {
            setErrorMensaje("Error al buscar la valuacion='" + getConnectionDistribuidor().getErrorMessage() + "'");
        }

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
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\"\n"
                        + "}";
                break;
        }
        return respuesta;
    }

    private void cargarRequestsApi() {
        fechafin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String bitacora = "INSERT INTO sicopdb.requestsapi\n"
                + "(IdInterfaz, IdDistribuidor, Servicio, Operacion, Api, FechaInicio, FechaFin, Ip, Response, Request) \n"
                + "VALUES ('MX00000SKP', '" + token + "', '" + descripcionServicio + "', 'POST', 'REST', '" + getFechaHoy() + "', '" + fechafin + "', '" + ip + "', '" + json.replaceAll("'", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "") + "', '" + jsonMandado.replaceAll("'", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "") + "');";
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

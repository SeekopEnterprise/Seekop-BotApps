package com.seekop.seekop.resources.resources.mobile;

import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Seekop
 */
public class GetHorariosDisponibles extends CommonSeekopUtilities {
    
    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de horarios disponibles";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";
    
    private String ip = "";
    private String token = "";
    private String fecha = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = " ";
    //////

    public GetHorariosDisponibles(String recibidoJSON, String ip) {
        jsonMandado = recibidoJSON;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON.toUpperCase();
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON.toUpperCase());
                this.ip = ip;
                this.token = objetoJson.getString("TOKEN");
                this.fecha = objetoJson.getString("FECHA");
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
    
    private void conection() {
        String sql = "SELECT \n"
                + "    IdUsuario, AgendaIntervalo, AgendaInicio, AgendaFin\n"
                + "FROM\n"
                + "    sicopdb.usuarios\n"
                + "WHERE\n"
                + "    IdUsuario = '" + getIdEjecutivo() + "';";
        int AgendaIntervalo = 30, AgendaInicio = 9, AgendaFin = 20;
        if (getConnectionATI().executeQuery(sql)) {
            if (getConnectionATI().next()) {
                AgendaIntervalo = getConnectionATI().getInteger("AgendaIntervalo");
                AgendaInicio = getConnectionATI().getInteger("AgendaInicio");
                AgendaFin = getConnectionATI().getInteger("AgendaFin");
            }
        }
        LocalTime horaActual = LocalTime.of(AgendaInicio, 0); // Hora de inicio (HH:00)
        LocalTime auxHoraActual = LocalTime.of(AgendaInicio, 0); // Hora de inicio (HH:00)
        LocalTime horaFin = LocalTime.of(AgendaFin, 0);       // Hora de fin (HH:00)

        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

        // Generar los horarios en intervalos
        while (!horaActual.isAfter(horaFin)) {
            auxHoraActual = horaActual;
            auxHoraActual = auxHoraActual.plusSeconds((AgendaIntervalo * 60) - 1);
//            horarios.add(horaActual.format(formatoHora));
            if (validarDisponible(horaActual.format(formatoHora), auxHoraActual.format(formatoHora))) {
             
            jsonBody += "\n{\n"
                    + "    \"inicio\": \"" + horaActual.format(formatoHora) + "\",\n"
                    + "    \"fin\": \"" + auxHoraActual.format(formatoHora) + "\"\n"
                    + "},";
            }
            horaActual = horaActual.plusMinutes(AgendaIntervalo);
        }
        jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
    }
    
    private boolean validarDisponible(String fechaInicio, String fechaFin) {
        boolean status = false;
        String sql = "SELECT \n"
                + "    IdSeguimiento, Programada, IdProspecto\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".seguimientos\n"
                + "WHERE\n"
                + "    programada >= '" + fecha + " " + fechaInicio + ":00'\n"
                + "        AND programada <= '" + fecha + " " + fechaFin + ":59'\n"
                + "        AND Cumplida = '1900-01-01 00:00:00'\n"
                + "        AND IdPropietario = '" + getIdEjecutivo() + "';";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                status = false;
            } else {
                status = true;
            }
        } else {
            setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
        }
        
        return status;
    }
    
    private String generaJSONRespuesta() {
        String respuesta = "";
        switch (getStatus()) {
            case 0:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"" + getMensaje() + "\",\n"
                        + "    \"horarios\": []\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\",\n"
                        + "    \"horarios\": [" + jsonBody + "]\n"
                        + "}";
                break;
        }
        return respuesta;
    }
    
    private void cargarRequestsApi() {
        fechafin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String bitacora = "INSERT INTO sicopdb.requestsapi\n"
                + "(IdInterfaz, IdDistribuidor, Servicio, Operacion, Api, FechaInicio, FechaFin, Ip, Response, Request) \n"
                + "VALUES ('MX00000SKP', '" + token + "', '" + descripcionServicio + "', 'POST', 'REST', '" + fechainicio + "', '" + fechafin + "', '" + ip + "', '" + json.replaceAll("'", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "") + "', '" + jsonMandado.replaceAll("'", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "") + "');";
        if (getConnectionATI().execute(bitacora, false)) {
        } else {
            setErrorMensaje(getConnectionATI().getErrorMessage());
        }
    }
    
    public String getJson() {
        return json;
    }
    
}

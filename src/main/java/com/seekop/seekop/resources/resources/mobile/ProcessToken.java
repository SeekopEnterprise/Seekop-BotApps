package com.seekop.seekop.resources.resources.mobile;

import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Seekop
 */
public class ProcessToken extends CommonSeekopUtilities {
    
    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de datos Token";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";
    
    private String ip = "";
    private String token = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = "[]";
    //////
    
    public ProcessToken(HttpServletRequest request, String ip) {
        jsonMandado = request.getParameter("TOKEN");
//        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
//            recibidoJSON = recibidoJSON.toUpperCase();
            JSONObject objetoJson;
            try {
//                objetoJson = new JSONObject(recibidoJSON);
                this.ip = ip;
                this.token = jsonMandado;
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
            
//        } else {
//            setErrorMensaje("Not JSON fount");
//        }
        json = generaJSONRespuesta();
        CloseConnection();
    }
    
    private void conection() {
        jsonBody = "{\n"
                + "    \"TOKEN\": \""+getToken()+"\",\n"
                + "    \"IdDistribuidor\": \""+getIdDistribuidor()+"\",\n"
                + "    \"IdMarca\": \""+getIdMarca()+"\",\n"
                + "    \"NombreDistribuidor\": \""+getNombreDistribuidor()+"\",\n"
                + "    \"CalleDistribuidor\": \""+getCalleDistribuidor()+"\",\n"
                + "    \"ColoniaDistribuidor\": \""+getColoniaDistribuidor()+"\",\n"
                + "    \"CPDistribuidor\": \""+getCpDistribuidor()+"\",\n"
                + "    \"DelegacionDistribuidor\": \""+getDelegacionDistribuidor()+"\",\n"
                + "    \"LadaDistribuidor\": \""+getLadaDistribuidor()+"\",\n"
                + "    \"TelefonosDistribuidor\": \""+getLadaDistribuidor()+"\",\n"
                + "    \"ZonaHoraria\": \""+getZonaHoraria(getIdDistribuidor())+"\",\n"
                + "    \"IdEjecutivo\": \""+getIdEjecutivo()+"\",\n"
                + "    \"NombreEjecutivo\": \""+getNombreCompletoEjecutivo()+"\",\n"
                + "    \"IdProspecto\": \""+getIdProspecto()+"\",\n"
                + "    \"NombreMarca\": \""+getDbMarca()+"\",\n" 
                + "    \"Registro\": \""+getRegistro()+"\",\n"
                + "    \"destino\": \""+getDestino()+"\",\n"
                + "    \"vin\": \""+getVin()+"\",\n"
                + "    \"placa\": \""+getPlaca()+"\",\n"
                + "    \"PoolName\": \""+getPoolDeConexion()+"\"\n"
                + "}";
    }
    
    private String generaJSONRespuesta() {
        String respuesta = "";
        switch (getStatus()) {
            case 0:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"" + getMensaje() + "\",\n"
                        + "    \"distribuidor\": {}\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\",\n"
                        + "    \"distribuidor\": " + jsonBody + "\n"
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

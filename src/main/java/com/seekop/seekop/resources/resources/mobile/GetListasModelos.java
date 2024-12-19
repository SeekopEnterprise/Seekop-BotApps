package com.seekop.seekop.resources.resources.mobile;

import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Seekop
 */
public class GetListasModelos extends CommonSeekopUtilities {
    
    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de lista de modelos";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";
    
    private String ip = "";
    private String token = "";
    private String idMarca = "";
    private String idAuto = "";
    private String tipo = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = " ";
    private String dbsicop = "";
    private String auxdbsicop = "sicopdb";
    //////

    public GetListasModelos(String recibidoJSON, String ip) {
        jsonMandado = recibidoJSON;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON.toUpperCase();
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON.toUpperCase());
                this.ip = ip;
                this.token = objetoJson.getString("TOKEN");
                this.tipo = objetoJson.getString("TIPO");
                this.tipo = validarvacio(this.tipo, "N");
            } catch (JSONException ex) {
                setErrorMensaje("JSON malformed: " + ex.toString());
            }
            if (!token.isEmpty()) {
                getTokenInformation(token);
                if (getConnectionDistribuidor() != null) {
                    dbsicop = getDbMarca();
                    if (tipo.equals("N")) {
                        auxdbsicop = "sicopdb";
                    } else {
                        setApuntadoSeminuevo(true);
                        dbsicop = "sicopdbseminuevos";
                        auxdbsicop = dbsicop;
                        AbrirConnectionSeminuevos();
                    }
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
        String sql = "SELECT DISTINCT\n"
                + "    p.idModelo, p.idModelo AS Modelo\n"
                + "FROM\n"
                + "    " + dbsicop + ".productos p\n"
                + "WHERE\n"
                + "    p.Activo = 1\n"
                + getClaveCatalogoSeminuevos("p")
                + "ORDER BY idModelo DESC;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            while (getConnectionDistribuidor().next()) {
                if (!validarvacio(getConnectionDistribuidor().getString("Modelo"), "").equals("")) {
                    jsonBody += "\n{\n"
                            + "    \"IdModelo\": \"" + getConnectionDistribuidor().getString("IdModelo") + "\",\n"
                            + "    \"Modelo\": \"" + validarvacio(getConnectionDistribuidor().getString("Modelo"), "") + "\"\n"
                            + "},";
                }
            }
        } else {
            setErrorMensaje("Error en la busqueda '" + getConnectionDistribuidor().getErrorMessage() + "'");
        }
        jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
        
    }
    
    private String generaJSONRespuesta() {
        String respuesta = "";
        switch (getStatus()) {
            case 0:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"" + getMensaje() + "\",\n"
                        + "    \"modelos\": []\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\",\n"
                        + "    \"modelos\": [" + jsonBody + "]\n"
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

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
public class GetListasCotizacion extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de listas cotizacion";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = "";

    public GetListasCotizacion(HttpServletRequest request, String ip) {
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
        String sql = "SELECT \n"
                + "    c.IdCotizacion,\n"
                + "    c.Referencia,\n"
                + "    c.IdProspecto,\n"
                + "    c.IdMarca,\n"
                + "    c.IdAuto,\n"
                + "    c.Modelo,\n"
                + "    c.IdProducto,\n"
                + "    c.Precio,\n"
                + "    c.Enganche,\n"
                + "    c.PlanPlazo,\n"
                + "    c.TomarAuto,\n"
                + "    c.Observaciones,\n"
                + "    c.Registro\n"
                + "FROM\n"
                + "    "+getDbDistribuidor()+".cotizaciones c\n"
                + "WHERE\n"
                + "    c.IdProspecto = '"+getIdProspecto()+"'\n"
                + "ORDER BY Registro DESC;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            while (getConnectionDistribuidor().next()) {
                jsonBody += "\n{\n"
                        + "        \"IdCotizacion\": \"" + getConnectionDistribuidor().getString("IdCotizacion") + "\",\n"
                        + "        \"Referencia\": \"" + getConnectionDistribuidor().getString("Referencia") + "\",\n"
                        + "        \"IdProspecto\": \"" + getConnectionDistribuidor().getString("IdProspecto") + "\",\n"
                        + "        \"IdMarca\": \"" + getConnectionDistribuidor().getString("IdMarca") + "\",\n"
                        + "        \"IdAuto\": \"" + getConnectionDistribuidor().getString("IdAuto") + "\",\n"
                        + "        \"Modelo\": \"" + getConnectionDistribuidor().getString("Modelo") + "\",\n"
                        + "        \"IdProducto\": \"" + getConnectionDistribuidor().getString("IdProducto") + "\",\n"
                        + "        \"Precio\": \"" + getConnectionDistribuidor().getString("Precio") + "\",\n"
                        + "        \"Enganche\": \"" + getConnectionDistribuidor().getString("Enganche") + "\",\n"
                        + "        \"PlanPlazo\": \"" + getConnectionDistribuidor().getString("PlanPlazo") + "\",\n"
                        + "        \"TomarAuto\": \"" + getConnectionDistribuidor().getString("TomarAuto") + "\",\n"
                        + "        \"Observaciones\": \"" + getConnectionDistribuidor().getString("Observaciones") + "\",\n"
                        + "        \"Registro\": \"" + getConnectionDistribuidor().getString("Registro") + "\"\n"
                        + "    },";
            }
            if (jsonBody.length() > 0) {
                jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
            }

        }
    }

    private String generaJSONRespuesta() {
        String respuesta = "";
        switch (getStatus()) {
            case 0:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"" + getMensaje() + "\",\n"
                        + "    \"cotizaciones\": []\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\",\n"
                        + "    \"cotizaciones\": [" + jsonBody + "]\n"
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

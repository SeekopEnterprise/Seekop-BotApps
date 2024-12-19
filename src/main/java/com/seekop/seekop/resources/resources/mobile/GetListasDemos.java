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
public class GetListasDemos extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de listas demostraciones";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = "";

    public GetListasDemos(HttpServletRequest request, String ip) {
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
                + "    d.NoMotor,\n"
                + "    d.Fecha,\n"
                + "    d.IdProspecto,\n"
                + "    d.IdEjecutivo,\n"
                + "    d.Observaciones,\n"
                + "    d.IdSeguimiento,\n"
                + "    d.registro,\n"
                + "    d.Fecha,\n"
                + "    id.VIN,\n"
                + "    id.Modelo,\n"
                + "    d.IdProducto,\n"
                + "    p.nombre AS nombreProducto,\n"
                + "    d.IdAuto,\n"
                + "    a.nombre AS nombreAuto\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".demostraciones d\n"
                + "        LEFT JOIN\n"
                + "    " + getDbDistribuidor() + ".inventariosautosdemo id ON d.NoMotor = id.VIN\n"
                + "        LEFT JOIN\n"
                + "    " + getDbMarca() + ".productos p ON d.IdProducto = p.IdProducto\n"
                + "        LEFT JOIN\n"
                + "    sicopdb.autos a ON d.IdAuto = a.IdAuto\n"
                + "        LEFT JOIN\n"
                + "    " + getDbDistribuidor() + ".seguimientos s ON s.IdSeguimiento = d.IdSeguimiento\n"
                + "WHERE\n"
                + "    d.IdProspecto = '" + getIdProspecto() + "'\n"
                + "    AND s.Cumplida='1900-01-01 00:00:00'\n"
                + "ORDER BY d.Registro DESC;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            while (getConnectionDistribuidor().next()) {
                jsonBody += "\n{\n"
                        + "        \"NoMotor\": \"" + getConnectionDistribuidor().getString("NoMotor") + "\",\n"
                        + "    \"NombreDistribuidor\": \"" + getNombreDistribuidor() + "\",\n"
                        + "    \"CalleDistribuidor\": \"" + getCalleDistribuidor() + "\",\n"
                        + "    \"ColoniaDistribuidor\": \"" + getColoniaDistribuidor() + "\",\n"
                        + "    \"CPDistribuidor\": \"" + getCpDistribuidor() + "\",\n"
                        + "    \"DelegacionDistribuidor\": \"" + getDelegacionDistribuidor() + "\",\n"
                        + "    \"LadaDistribuidor\": \"" + getLadaDistribuidor() + "\",\n"
                        + "    \"TelefonosDistribuidor\": \"" + getLadaDistribuidor() + "\",\n"
                        + "    \"IdEjecutivo\": \"" + getIdEjecutivo() + "\",\n"
                        + "    \"NombreEjecutivo\": \"" + getNombreCompletoEjecutivo() + "\",\n"
                        + "        \"Fecha\": \"" + getConnectionDistribuidor().getString("Fecha") + "\",\n"
                        + "        \"IdProspecto\": \"" + getConnectionDistribuidor().getString("IdProspecto") + "\",\n"
                        + "        \"IdEjecutivo\": \"" + getConnectionDistribuidor().getString("IdEjecutivo") + "\",\n"
                        + "        \"Observaciones\": \"" + getConnectionDistribuidor().getString("Observaciones") + "\",\n"
                        + "        \"registro\": \"" + getConnectionDistribuidor().getString("registro") + "\",\n"
                        + "        \"VIN\": \"" + getConnectionDistribuidor().getString("VIN") + "\",\n"
                        + "        \"Modelo\": \"" + getConnectionDistribuidor().getString("Modelo") + "\",\n"
                        + "        \"IdProducto\": \"" + getConnectionDistribuidor().getString("IdProducto") + "\",\n"
                        + "        \"nombreProducto\": \"" + getConnectionDistribuidor().getString("nombreProducto") + "\",\n"
                        + "        \"IdSeguimiento\": \"" + getConnectionDistribuidor().getString("IdSeguimiento") + "\",\n"
                        + "        \"IdAuto\": \"" + getConnectionDistribuidor().getString("IdAuto") + "\",\n"
                        + "        \"nombreAuto\": \"" + getConnectionDistribuidor().getString("nombreAuto") + "\"\n"
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
                        + "    \"demostraciones\": []\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\",\n"
                        + "    \"demostraciones\": [" + jsonBody + "]\n"
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

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
public class GetListasCitas extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de listas citas";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = "";

    public GetListasCitas(HttpServletRequest request, String ip) {
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
                + "    s.IdSeguimiento,\n"
                + "    s.Programada,\n"
                + "    s.idprospecto,\n"
                + "    s.Observaciones,\n"
                + "    s.ObservacionesCumplimiento,\n"
                + "    s.Cumplida,\n"
                + "    ad.uso,\n"
                + "    u.nombre,\n"
                + "    s.registro\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".seguimientos s\n"
                + "        LEFT JOIN\n"
                + "    " + (traerValorConfiguracion("Multiseguimiento", "Migrar").equals("1")
                ? getDbDistribuidor() : getDbMarca()) + ".tipoactividaddetalle ad ON ad.IdTipoActividadDetalle = s.IdTipoActividadDetalle\n"
                + "        LEFT JOIN\n"
                + "    sicopdb.usoactividades u ON u.IdUso = ad.uso\n"
                + "WHERE\n"
                + "    ad.Uso='102'\n"
                + "        AND s.idprospecto = '" + getIdProspecto() + "'\n"
                + "        AND s.Cumplida='1900-01-01 00:00:00'\n"
                + "ORDER BY s.registro DESC;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            while (getConnectionDistribuidor().next()) {
                jsonBody += "\n{\n"
                        + "    \"IdSeguimiento\": \"" + getConnectionDistribuidor().getString("IdSeguimiento") + "\",\n"
                        + "    \"NombreDistribuidor\": \"" + getNombreDistribuidor() + "\",\n"
                        + "    \"CalleDistribuidor\": \"" + getCalleDistribuidor() + "\",\n"
                        + "    \"ColoniaDistribuidor\": \"" + getColoniaDistribuidor() + "\",\n"
                        + "    \"CPDistribuidor\": \"" + getCpDistribuidor() + "\",\n"
                        + "    \"DelegacionDistribuidor\": \"" + getDelegacionDistribuidor() + "\",\n"
                        + "    \"LadaDistribuidor\": \"" + getLadaDistribuidor() + "\",\n"
                        + "    \"TelefonosDistribuidor\": \"" + getLadaDistribuidor() + "\",\n"
                        + "    \"IdEjecutivo\": \"" + getIdEjecutivo() + "\",\n"
                        + "    \"NombreEjecutivo\": \"" + getNombreCompletoEjecutivo() + "\",\n"
                        + "    \"Programada\": \"" + getConnectionDistribuidor().getString("Programada") + "\",\n"
                        + "    \"idprospecto\": \"" + getConnectionDistribuidor().getString("idprospecto") + "\",\n"
                        + "    \"Observaciones\": \"" + getConnectionDistribuidor().getString("Observaciones") + "\",\n"
                        + "    \"ObservacionesCumplimiento\": \"" + getConnectionDistribuidor().getString("ObservacionesCumplimiento") + "\",\n"
                        + "    \"Cumplida\": \"" + getConnectionDistribuidor().getString("Cumplida") + "\",\n"
                        + "    \"uso\": \"" + getConnectionDistribuidor().getString("uso") + "\",\n"
                        + "    \"registro\": \"" + getConnectionDistribuidor().getString("registro") + "\"\n"
                        + "},";
            }
        } else {
            setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
        }

        if (jsonBody.length() > 0) {
            jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
        }

    }

    private String generaJSONRespuesta() {
        String respuesta = "";
        switch (getStatus()) {
            case 0:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"" + getMensaje() + "\",\n"
                        + "    \"citas\": []\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\",\n"
                        + "    \"citas\": [" + jsonBody + "]\n"
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

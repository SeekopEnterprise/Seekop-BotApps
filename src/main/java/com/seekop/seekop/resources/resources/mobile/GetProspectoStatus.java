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
public class GetProspectoStatus extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de status de prospecto";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = " ";
    private String dbsicop = "";
    private String auxdbsicop = "sicopdb";
    //////

    public GetProspectoStatus(String recibidoJSON, String ip) {
        jsonMandado = recibidoJSON;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON.toUpperCase();
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON.toUpperCase());
                this.ip = ip;
                this.token = objetoJson.getString("TOKEN");
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
        boolean demos = false, valuaciones = false, citas = false, compra = false;
        String sql = "SELECT \n"
                + "    IdProspecto\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".demostraciones\n"
                + "WHERE\n"
                + "    IdProspecto = '" + getIdProspecto() + "' and Status='0'\n"
                + "LIMIT 1;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                demos = true;
            }
        }
        sql = "SELECT \n"
                + "    IdProspecto\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".valuacion\n"
                + "WHERE\n"
                + "    IdProspecto = '" + getIdProspecto() + "' and Status='0'\n"
                + "LIMIT 1;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                valuaciones = true;
            }
        }
        sql = "SELECT \n"
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
                + "ORDER BY s.registro DESC LIMIT 1;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                citas = true;
            }
        }
        sql = "SELECT \n"
                + "    s.idseguimiento, s.idtipoactividaddetalle, tad.nombre\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".seguimientos s\n"
                + "        LEFT JOIN\n"
                + "    " + (traerValorConfiguracion("Multiseguimiento", "Migrar").equals("1")
                ? getDbDistribuidor() : getDbMarca()) + ".tipoactividaddetalle tad ON s.idtipoactividaddetalle = tad.idtipoactividaddetalle\n"
                + "WHERE\n"
                + "    tad.TramiteCompra = '1' and Cumplida='1900-01-01 00:00:00'\n"
                + "LIMIT 1;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                compra = true;
            }
        }
        jsonBody = "{\n"
                + "    \"Demostraciones\": "+demos+",\n"
                + "    \"Valuaciones\": "+valuaciones+",\n"
                + "    \"Citas\": "+citas+",\n"
                + "    \"ProcesoCompra\": "+compra+"\n"
                + "}";

    }

    private String generaJSONRespuesta() {
        String respuesta = "";
        switch (getStatus()) {
            case 0:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"" + getMensaje() + "\",\n"
                        + "    \"prospecto\": {}\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\",\n"
                        + "    \"prospecto\": " + jsonBody + "\n"
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

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
public class GetListasAutos extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de lista de autos";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    private String idMarca = "";
    private String idAuto = "";
    private String idModelo = "";
    private String tipo = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = " ";
    private String dbsicop = "";
    private String auxdbsicop = "sicopdb";
    //////

    public GetListasAutos(String recibidoJSON, String ip) {
        jsonMandado = recibidoJSON;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON.toUpperCase();
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON.toUpperCase());
                this.ip = ip;
                this.token = objetoJson.getString("TOKEN");
                this.idMarca = validarvacio(objetoJson.getString("IDMARCA"), "");
                this.idAuto = validarvacio(objetoJson.getString("IDAUTO"), "");
                this.idModelo = validarvacio(objetoJson.getString("IDMODELO"), "");
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
                + "    a.idauto, a.nombre AS nombreAuto, a.idMarca, m.nombre AS nombreMarca\n"
                + "FROM\n"
                + "    " + dbsicop + ".productos pr\n"
                + "        LEFT JOIN\n"
                + "    " + auxdbsicop + ".marcas m ON pr.IdMarca = m.idmarca\n"
                + "        LEFT JOIN\n"
                + "    " + auxdbsicop + ".autos a ON pr.idauto = a.idauto\n"
                + "WHERE\n"
                + "    pr.Activo = '1'\n"
                + (idMarca.equals("") ? "" : "AND a.IdMarca='" + idMarca + "' \n")
                + (idAuto.equals("") ? "" : "AND a.idAuto='" + idAuto + "' \n")
                + (idModelo.equals("") ? "" : "AND pr.idmodelo='" + idModelo + "' \n")
                + getClaveCatalogoSeminuevos("pr")
                + "GROUP BY a.nombre\n"
                + "ORDER BY a.nombre;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            while (getConnectionDistribuidor().next()) {
                if (!validarvacio(getConnectionDistribuidor().getString("nombreAuto"), "").equals("")) {
                    jsonBody += "\n{\n"
                            + "    \"IdMarca\": \"" + getConnectionDistribuidor().getString("IdMarca") + "\",\n"
                            + "    \"NombreMarca\": \"" + validarvacio(getConnectionDistribuidor().getString("nombreMarca"), "").replace("\"", "") + "\",\n"
                            + "    \"IdAuto\": \"" + getConnectionDistribuidor().getString("IdAuto") + "\",\n"
                            + "    \"NombreAuto\": \"" + validarvacio(getConnectionDistribuidor().getString("nombreAuto"), "").replace("\"", "") + "\"\n"
                            + "},";
                }

            }
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
                        + "    \"autos\": []\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\",\n"
                        + "    \"autos\": [" + jsonBody + "]\n"
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

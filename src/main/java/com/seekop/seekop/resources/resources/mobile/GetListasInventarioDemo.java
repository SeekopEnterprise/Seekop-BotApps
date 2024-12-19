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
public class GetListasInventarioDemo extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de lista de inventario demo";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    private String idMarca = "";
    private String idAuto = "";
    private String idProducto = "";
    private String idModelo = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = " ";
    //////

    public GetListasInventarioDemo(String recibidoJSON, String ip) {
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
                this.idProducto = validarvacio(objetoJson.getString("IDPRODUCTO"), "");
                if (recibidoJSON.contains("IDMODELO")) {
                    this.idModelo = validarvacio(objetoJson.getString("IDMODELO"), "");
                }
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
                + "    Vin,\n"
                + "    IdMarca,\n"
                + "    marca,\n"
                + "    IdAuto,\n"
                + "    Auto,\n"
                + "    Modelo,\n"
                + "    IdProducto,\n"
                + "    Producto,\n"
                + "    Inicio,\n"
                + "    Fin,\n"
                + "    Intervalo\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".ListaDeInventariosAutosDemo\n"
                + "WHERE\n"
                + "    EsAutoDemo <> 0 AND Activo <> 0\n"
                + "        AND IdDistribuidor = '" + getIdDistribuidor() + "'\n"
                + (idMarca.equals("") ? "" : "AND IdMarca='" + idMarca + "' ") + "\n"
                + (idAuto.equals("") ? "" : "AND idAuto='" + idAuto + "' ") + "\n"
                + (idProducto.equals("") ? "" : "AND dProducto='" + idProducto + "' ") + "\n"
                + (idModelo.equals("") ? "" : "AND IdModelo='" + idModelo + "' ") + "\n"
                + "ORDER BY Auto , Producto;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            while (getConnectionDistribuidor().next()) {
                jsonBody += "\n{\n"
                        + "    \"IdMarca\": \"" + getConnectionDistribuidor().getString("IdMarca") + "\",\n"
                        + "    \"NombreMarca\": \"" + validarvacio(getConnectionDistribuidor().getString("marca"), "").replace("\"", "") + "\",\n"
                        + "    \"IdAuto\": \"" + getConnectionDistribuidor().getString("IdAuto") + "\",\n"
                        + "    \"NombreAuto\": \"" + validarvacio(getConnectionDistribuidor().getString("Auto"), "").replace("\"", "") + "\",\n"
                        + "    \"IdProducto\": \"" + getConnectionDistribuidor().getString("IdProducto") + "\",\n"
                        + "    \"NombreProducto\": \"" + validarvacio(getConnectionDistribuidor().getString("Producto"), "").replace("\"", "") + "\",\n"
                        + "    \"Vin\": \"" + validarvacio(getConnectionDistribuidor().getString("Vin"), "").replace("\"", "") + "\",\n"
                        + "    \"Intervalo\": \"" + validarvacio(getConnectionDistribuidor().getString("Intervalo"), "").replace("\"", "") + "\",\n"
                        + "    \"Inicio\": \"" + validarvacio(getConnectionDistribuidor().getString("Inicio"), "").replace("\"", "") + "\",\n"
                        + "    \"Fin\": \"" + validarvacio(getConnectionDistribuidor().getString("Fin"), "").replace("\"", "") + "\",\n"
                        + "    \"Modelo\": \"" + validarvacio(getConnectionDistribuidor().getString("Modelo"), "").replace("\"", "") + "\"\n"
                        + "},";
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

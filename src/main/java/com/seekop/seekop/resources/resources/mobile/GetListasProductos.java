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
public class GetListasProductos extends CommonSeekopUtilities {
    
    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de lista de productos";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";
    
    private String ip = "";
    private String token = "";
    private String idMarca = "";
    private String idAuto = "";
    private String idProducto = "";
    private String idModelo = "";
    private String tipo = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = " ";
    private String dbsicop = "";
    private String auxdbsicop = "sicopdb";
    //////

    public GetListasProductos(String recibidoJSON, String ip) {
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
        String sql = "SELECT \n"
                + "    pr.idmarca,\n"
                + "    m.nombre AS nombreMarca,\n"
                + "    pr.IdAuto,\n"
                + "    a.nombre AS NombreAuto,\n"
                + "    pr.IdProducto,\n"
                + "    pr.Nombre AS nombreProducto,\n"
                + "    pr.IdModelo\n"
                + "FROM\n"
                + "    " + dbsicop + ".productos pr\n"
                + "        LEFT JOIN\n"
                + "    " + auxdbsicop + ".autos a ON pr.IdAuto = a.IdAuto\n"
                + "        LEFT JOIN\n"
                + "    " + auxdbsicop + ".marcas m ON pr.idmarca = m.idmarca\n"
                + "WHERE\n"
                + "    pr.Activo = '1' "
                + (idMarca.equals("") ? "" : "AND pr.IdMarca='" + idMarca + "' ") + "\n"
                + (idAuto.equals("") ? "" : "AND pr.idAuto='" + idAuto + "' ") + "\n"
                + (idProducto.equals("") ? "" : "AND pr.idProducto='" + idProducto + "' ") + "\n"
                + (idModelo.equals("") ? "" : "AND pr.IdModelo='" + idModelo + "' ") + "\n"
                + getClaveCatalogoSeminuevos("pr")
                + "ORDER BY pr.nombre;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            while (getConnectionDistribuidor().next()) {
                if (!validarvacio(getConnectionDistribuidor().getString("nombreProducto"), "").equals("")) {
                jsonBody += "\n{\n"
                        + "    \"IdMarca\": \"" + getConnectionDistribuidor().getString("IdMarca") + "\",\n"
                        + "    \"NombreMarca\": \"" + validarvacio(getConnectionDistribuidor().getString("nombreMarca"), "").replace("\"", "") + "\",\n"
                        + "    \"IdAuto\": \"" + getConnectionDistribuidor().getString("IdAuto") + "\",\n"
                        + "    \"NombreAuto\": \"" + validarvacio(getConnectionDistribuidor().getString("nombreAuto"), "").replace("\"", "") + "\",\n"
                        + "    \"IdProducto\": \"" + getConnectionDistribuidor().getString("IdProducto") + "\",\n"
                        + "    \"NombreProducto\": \"" + validarvacio(getConnectionDistribuidor().getString("nombreProducto"), "").replace("\"", "") + "\",\n"
                        + "    \"Modelo\": \"" + validarvacio(getConnectionDistribuidor().getString("IdModelo"), "").replace("\"", "") + "\"\n"
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

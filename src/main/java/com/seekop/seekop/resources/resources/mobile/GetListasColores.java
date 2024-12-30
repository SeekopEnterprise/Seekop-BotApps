package com.seekop.seekop.resources.resources.mobile;

import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.json.JSONException;
import org.json.JSONObject;
import resources.ConnectionManager;

/**
 *
 * @author Seekop
 */
public class GetListasColores extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de lista de colores";
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

    public GetListasColores(String recibidoJSON, String ip) {
        jsonMandado = recibidoJSON;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON.toUpperCase();
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON.toUpperCase());
                this.ip = ip;
                this.token = objetoJson.getString("TOKEN");
                this.idProducto = validarvacio(objetoJson.getString("IDPRODUCTO"), "");
                this.tipo = objetoJson.getString("TIPO");
                this.tipo = validarvacio(this.tipo, "N");
                this.idMarca = objetoJson.getString("IDMARCA");
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
                + "    pr.IdProducto,\n"
                + "    pr.Nombre AS nombreProducto,\n"
                + "    pc.IdColor,\n"
                + "    c.nombre as nombreColor\n"
                + "FROM\n"
                + "    " + dbsicop + ".productos pr\n"
                + "        LEFT JOIN\n"
                + "    " + dbsicop + ".productoscolores pc ON pr.IdProducto = pc.IdProducto\n"
                + "        LEFT JOIN\n"
                + "    sicopdb.colores c ON pc.IdColor = c.IdColor\n"
                + "WHERE\n"
                + "    pr.Activo = '1'\n"
                + (idProducto.equals("") ? "" : "AND pr.idProducto='" + idProducto + "' ") + "\n"
                + getClaveCatalogoSeminuevos("pr")
                + "ORDER BY c.nombre;";
        if (isApuntadoSeminuevo()) {
            sql = "SELECT \n"
                    + "    IdColor, Nombre as nombreColor\n"
                    + "FROM\n"
                    + "    sicopdbseminuevos.Colores\n"
                    + "WHERE\n"
                    + "    (IdMarca = '"+ idMarca +"'\n"
                    + "        OR IdColor = '0000000000')\n"
                    + "        AND Activo = 1\n"
                    + "        AND Interior = 0\n"
                    + "ORDER BY Nombre;";

            ConnectionManager connectionSeminuevos = abrirConnection(getIdDistribuidor());
            if (connectionSeminuevos.executeQuery(sql)) {
                while (connectionSeminuevos.next()) {
                    if (!validarvacio(connectionSeminuevos.getString("nombreColor"), "").equals("")) {
                        jsonBody += "\n{\n"
                                + "    \"IdColor\": \"" + connectionSeminuevos.getString("IdColor") + "\",\n"
                                + "    \"nombreColor\": \"" + validarvacio(connectionSeminuevos.getString("nombreColor"), "").replace("\"", "") + "\"\n"
                                + "},";
                    }
                }
            }
            
        }
        else
        {
            if (getConnectionDistribuidor().executeQuery(sql)) {
                while (getConnectionDistribuidor().next()) {
                    if (!validarvacio(getConnectionDistribuidor().getString("nombreColor"), "").equals("")) {
                        jsonBody += "\n{\n"
                                + "    \"IdColor\": \"" + getConnectionDistribuidor().getString("IdColor") + "\",\n"
                                + "    \"nombreColor\": \"" + validarvacio(getConnectionDistribuidor().getString("nombreColor"), "").replace("\"", "") + "\"\n"
                                + "},";
                    }
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

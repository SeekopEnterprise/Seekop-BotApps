package com.seekop.seekop.resources.resources.mobile;

import java.io.UnsupportedEncodingException;
import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import resources.ConnectionManager;

/**
 *
 * @author Seekop
 */
public class aceptarValuacion extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "aceptar valuacion";
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    private String nuevaFecha = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String recibidoJSON = "";
    private String jsonBody = "[]";
    //////
    private String idValuacion = "";
    private boolean aceptada = false;
    private String idMotivo = "";
    private String observaciones = "";
    private String precio = "";

    public aceptarValuacion(String contenido, String ip) {
        recibidoJSON = contenido;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON;
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON);
                this.ip = ip;
                this.token = objetoJson.getString("Token");
                this.idValuacion = objetoJson.getString("IdVauacion");
                this.aceptada = objetoJson.getBoolean("Aceptada");
                this.idMotivo = objetoJson.getString("IdMotivo");
                this.observaciones = objetoJson.getString("Observaciones");
                this.precio = objetoJson.getString("Precio");

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

    public void conection() {

     String activityId = "";
        
        String sql = "SELECT \n"
                + " *\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".valuacion\n"
                + "WHERE\n"
                + "    idvaluacion = '" + idValuacion + "'\n"
                + "        AND IdProspecto = '" + getIdProspecto() + "';";
        String idStatus = "";
        if (this.aceptada) {
            idStatus = "12";
            activityId = "97";
        } else {
            idStatus = "11";
            activityId = "96";
        }
        if (getConnectionDistribuidor().executeQuery(sql)) {
            
            if (getConnectionDistribuidor().next()) {
                
                String idValuador = getConnectionDistribuidor().getString("IdEjecutivoValuacion");
                String idProspecto = getIdProspecto();
                
                String idMarcaValuacion = getConnectionDistribuidor().getString("IdMarcaAnterior");
                String modeloValuacion = getConnectionDistribuidor().getString("ModeloActual");
                String idAuto = getConnectionDistribuidor().getString("IdAutoActual");
                String idProducto = getConnectionDistribuidor().getString("IdProductoActual");
                String idColor = getConnectionDistribuidor().getString("IdColor");
                String kilometraje = getConnectionDistribuidor().getString("Kilometraje");
                String serie = getConnectionDistribuidor().getString("NoSerie");
                String precioCompra = "";
                String precioVenta = "";         
                String propuesta = getPropuestaValuacion(idValuacion);
                
                sql = "UPDATE " + getDbDistribuidor() + ".`valuacion` SET `IdStatus` = '" + idStatus + "' WHERE (`IdValuacion` = '" + idValuacion + "');";
                  
                if (getConnectionDistribuidor().executeUpdate(sql)) 
                {    
                    
                    sendDispositionValuation(idValuacion,activityId,true,null);
                    ConnectionManager connectionSeminuevos = abrirConnection(getIdDistribuidor());
                    String baseSeminuevos = getNombreSeminuevos(getIdDistribuidor());
                    AbrirConnectionSeminuevos();
                    
                    sql = "UPDATE " + baseSeminuevos + ".`valuacion` SET `IdStatus` = '" + idStatus + "' WHERE (`IdValuacion` = '" + idValuacion + "');";
                    
                    getConnectionDistribuidor().executeUpdate(sql);
                    if(!this.aceptada)
                    {
                        guardaMotivosRechazo(baseSeminuevos,idMotivo,"","0");
                        if(!observaciones.isEmpty())
                        {
                            guardaMotivosRechazo(baseSeminuevos,"1414148999",observaciones,"0");
                        }
                        if(!precio.isEmpty())
                        {
                            guardaMotivosRechazo(baseSeminuevos,"1414148990","",precio);
                        }
                       
                        String titulo = "Propuesta rechazada";
                        String mensajeNotificacion = "El prospecto " + buscarNombreProspecto(idProspecto) + " rechazó la propuesta de valuación ";
                        
                        JSONObject dataObject = new JSONObject();
                        dataObject.put("r", idProspecto);
                        dataObject.put("r2", idValuacion);
                        dataObject.put("r3", "");
                        dataObject.put("r4", "11");
                        
                        sendNotification("27",idValuador,idProspecto,titulo,mensajeNotificacion,dataObject);                                      
                    }
                    else
                    {
                        String titulo = "Propuesta aceptada";
                        String mensajeNotificacion = "El prospecto " + buscarNombreProspecto(idProspecto) + " aceptó la propuesta de valuación ";
                        
                        JSONObject dataObject = new JSONObject();
                        dataObject.put("r", idProspecto);
                        dataObject.put("r2", idValuacion);
                        dataObject.put("r3", "");
                        dataObject.put("r4", "12");
                        
                        
                        String isDalton = traerValorConfiguracion("Habilitar", "EstatusInventario");
                        
                        String idNotificacion = "";
                        
                        if (isDalton.equals("1")) {                        
                            idNotificacion = "32";
                        }
                        else
                        {
                            dataObject.put("titulo", idProspecto);
                            dataObject.put("mensaje", idValuacion);
                            dataObject.put("r5", idMarcaValuacion);
                            dataObject.put("r6", modeloValuacion);
                            dataObject.put("r7", idAuto);
                            dataObject.put("r8", idProducto);
                            dataObject.put("r9", idColor);
                            dataObject.put("r10", kilometraje);
                            dataObject.put("r11", precioCompra);
                            dataObject.put("r12", precioVenta);
                            dataObject.put("r13", serie);
                            dataObject.put("r14", propuesta);
                            
                            idNotificacion = "34";
                        }
                        
                        sendNotification(idNotificacion,idValuador,idProspecto,titulo,mensajeNotificacion,dataObject);  
                    }

                } else {
                    setErrorMensaje("Error al actualizar valuacion= '" + getConnectionDistribuidor().getErrorMessage() + "'");
                }
            } else {
                setErrorMensaje("No se encontro valuacion con el IdValuacion '" + idValuacion + "' y el token '" + token + "'");
            }
        } else {
            setErrorMensaje("Error al buscar la valuacion='" + getConnectionDistribuidor().getErrorMessage() + "'");
        }
    }
    
    public static String encodeToISO88591(String input) {
        String resultado = "";
        try {
            // Convierte la cadena a bytes en UTF-8 y luego a una nueva cadena en ISO-8859-1
            resultado = new String(input.getBytes("UTF-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            resultado = input;
        }
        return resultado;
    }

    private String generaJSONRespuesta() {
        String respuesta = "";
        switch (getStatus()) {
            case 0:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"" + getMensaje() + "\"\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\"\n"
                        + "}";
                break;
        }
        return respuesta;
    }

    private void cargarRequestsApi() {
        fechafin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String bitacora = "INSERT INTO sicopdb.requestsapi\n"
                + "(IdInterfaz, IdDistribuidor, Servicio, Operacion, Api, FechaInicio, FechaFin, Ip, Response, Request) \n"
                + "VALUES ('MX00000SKP', '" + token + "', '" + descripcionServicio + "', 'POST', 'REST', '" + getFechaHoy() + "', '" + fechafin + "', '" + ip + "', '" + json.replaceAll("'", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "") + "', '" + jsonMandado.replaceAll("'", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "") + "');";
        if (getConnectionATI().execute(bitacora, false)) {
        } else {
            setErrorMensaje(getConnectionATI().getErrorMessage());
        }
    }

    public String getJson() {
        return json;
    }

    public String crearProgramada(int dias) {
        String fecha = "1900-01-01 00:00:00";
        Date fechaProgramada = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(fechaProgramada);
        c.add(Calendar.DATE, dias);
        fechaProgramada = c.getTime();
        fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaProgramada);

        return fecha;
    }
    
    private void guardaMotivosRechazo(String baseSeminuevos,String idMotivoLocal,String observacionesLocal,String precioLocal) {
        
        String idDeclinacion = "1";
        idDeclinacion = getIdDeclinacion(baseSeminuevos);
        
        int number = Integer.parseInt(idDeclinacion);
        number += 1;
        // Convertir de nuevo a String si es necesario
        String updatedIdDeclinacion = String.valueOf(number);
        
        ConnectionManager connectionSeminuevos = abrirConnection(getIdDistribuidor());
        
        String query = "INSERT INTO " + baseSeminuevos + ".ValuacionDeclinacion\n"
                + "(IdValuacion, IdMotivo, IdDeclinacion, Observaciones, Precio, FechaRegistro) \n"
                + "VALUES ('" + idValuacion + "', '" + idMotivoLocal + "',  '" + updatedIdDeclinacion + "', '" + observacionesLocal + "', '" + precioLocal + "', '" + getFechaHoy() + "');";
        
        
        if (connectionSeminuevos.execute(query,false)) 
        {
            connectionSeminuevos.close();
        } else {
            setErrorMensaje(connectionSeminuevos.getErrorMessage());
        }
        connectionSeminuevos.close();
    }
    
    
     private String getIdDeclinacion(String baseSeminuevos) {
        String idDeclinacion = "1";
        
        String sql = "SELECT MAX(IdDeclinacion) as IdDeclinacion FROM " + baseSeminuevos + ".Valuaciondeclinacion";

            ConnectionManager connectionSeminuevos = abrirConnection(getIdDistribuidor());
            if (connectionSeminuevos.executeQuery(sql)) {
                while (connectionSeminuevos.next()) {
                    idDeclinacion = connectionSeminuevos.getString("IdDeclinacion");
                }
            } else {
            setErrorMensaje("Error al buscar la valuacion='" + connectionSeminuevos.getErrorMessage() + "'");
        }
         connectionSeminuevos.close();   

        return idDeclinacion;
    }
}

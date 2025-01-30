package com.seekop.seekop.resources.resources.mobile;

import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import resources.ConnectionManager;

/**
 *
 * @author Seekop
 */
public class GetListasValuaciones extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Obtencion de listas valuaciones";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = "";

    public GetListasValuaciones(HttpServletRequest request, String ip) {
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
                + "    IdValuacion,\n"
                + "    IdProspecto,\n"
                + "    Observaciones,\n"
                + "    Solicitud,\n"
                + "    Respuesta,\n"
                + "    IdStatus,\n"
                + "    IdSeguimiento\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".valuacion\n"
                + "WHERE\n"
                + "    IdProspecto = '" + getIdProspecto() + "'\n"
                + "ORDER BY Solicitud DESC;";
        String auxIdSeguimiento = "";
        String auxJSON = "";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            while (getConnectionDistribuidor().next()) {
                auxIdSeguimiento = getIdSeguimientoSeminuevos(getConnectionDistribuidor().getString("IdValuacion"));
                if (!auxIdSeguimiento.equals("NI")) {
                    jsonBody += "\n{\n"
                            + "    \"IdValuacion\": \"" + getConnectionDistribuidor().getString("IdValuacion") + "\",\n"
                            + "    \"NombreDistribuidor\": \"" + getNombreDistribuidor() + "\",\n"
                            + "    \"CalleDistribuidor\": \"" + getCalleDistribuidor() + "\",\n"
                            + "    \"ColoniaDistribuidor\": \"" + getColoniaDistribuidor() + "\",\n"
                            + "    \"CPDistribuidor\": \"" + getCpDistribuidor() + "\",\n"
                            + "    \"DelegacionDistribuidor\": \"" + getDelegacionDistribuidor() + "\",\n"
                            + "    \"LadaDistribuidor\": \"" + getLadaDistribuidor() + "\",\n"
                            + "    \"TelefonosDistribuidor\": \"" + getLadaDistribuidor() + "\",\n"
                            + "    \"IdEjecutivo\": \"" + getIdEjecutivo() + "\",\n"
                            + "    \"NombreEjecutivo\": \"" + getNombreCompletoEjecutivo() + "\",\n"
                            + "    \"IdProspecto\": \"" + getConnectionDistribuidor().getString("IdProspecto") + "\",\n"
                            + "    \"Observaciones\": \"" + getConnectionDistribuidor().getString("Observaciones") + "\",\n"
                            + "    \"Solicitud\": \"" + getConnectionDistribuidor().getString("Solicitud") + "\",\n"
                            + "    \"Respuesta\": \"" + getConnectionDistribuidor().getString("Respuesta") + "\",\n"
                            + "    \"IdStatus\": \"" + getConnectionDistribuidor().getString("IdStatus") + "\",\n"
                            + getInfoValuacionSeminuevos(getConnectionDistribuidor().getString("IdValuacion"), getConnectionDistribuidor().getString("IdProspecto"),getConnectionDistribuidor().getString("IdStatus"))
                            + "    \"IdSeguimiento\": \"" + validarvacio(auxIdSeguimiento, "") + "\"\n"
                            + "},";
                }

            }
        } else {
            setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
        }

        if (jsonBody.length() > 0) {
            jsonBody = jsonBody.substring(0, jsonBody.length() - 1);
        }

    }

    private String getInfoValuacionSeminuevos(String idValuacion, String idProspecto,String idStatus) {
        String campos = "";
        String valuacionLegal = " ";
        String valuacionMecanica = " ";
        String idSeminuevos = "";
        ConnectionManager connectionAux = abrirConnection(getIdDistribuidor());
        String idSeguimiento = "NI";
        String sql = "SELECT \n"
                + "IdDistribuidorSemiNuevos, Nombre\n"
                + "FROM\n"
                + getDbGrupoCorporativo() + ".distribuidores,\n"
                + getDbGrupoCorporativo() + ".distribuidoresnuevosseminuevos\n"
                + "WHERE\n"
                + "IdDistribuidorNuevo = '" + getIdDistribuidor() + "'\n"
                + " AND IdDistribuidor = IdDistribuidorSemiNuevos\n"
                + " ORDER BY Nombre;";
        if (connectionAux.executeQuery(sql)) {
            while (connectionAux.next()) {
                idSeminuevos = connectionAux.getString("IdDistribuidorSemiNuevos");
                setConnectionAux(abrirConnection(idSeminuevos));
                sql = "SELECT \n"
                        + "    v.IdValuacion,\n"
                        + "    v.IdProspecto,\n"
                        + "    v.Observaciones,\n"
                        + "    v.Solicitud,\n"
                        + "    v.Respuesta,\n"
                        + "    v.IdStatus,\n"
                        + "    v.IdSeguimiento,\n"
                        + "    v.IdEjecutivoValuacion,\n"
                        + "    v.IdMarca,\n"
                        + "    v.IdAutoActual,\n"
                        + "    v.ModeloActual,\n"
                        + "    v.IdProductoActual,\n"
                        + "    v.Kilometraje,\n"
                        + "    a.nombre AS NombreAuto,\n"
                        + "    p.nombre AS NombreProducto,\n"
                        + "    m.nombre AS NombreMarca\n"
                        + "FROM\n"
                        + "    " + getAuxdbDistribuidor() + ".valuacion v\n"
                        + "        LEFT JOIN\n"
                        + "    sicopdbseminuevos.autos a ON v.IdAutoActual = a.idauto\n"
                        + "        LEFT JOIN\n"
                        + "    sicopdbseminuevos.productos p ON v.IdProductoActual = p.idproducto\n"
                        + "        LEFT JOIN\n"
                        + "    sicopdbseminuevos.marcas m ON v.IdMarca = m.IdMarca\n"
                        + "WHERE\n"
                        + "    IdValuacion = '" + idValuacion + "';";
                if (getConnectionAux().executeQuery(sql)) {
                    if (getConnectionAux().next()) {
//                        buscarEjecutivo(getConnectionAux().getString("IdEjecutivoValuacion"));
                        campos = "    \"Modelo\": \"" + getConnectionAux().getString("ModeloActual") + "\",\n"
                                + "    \"Marca\": \"" + getConnectionAux().getString("NombreMarca") + "\",\n"
                                + "    \"Auto\": \"" + getConnectionAux().getString("NombreAuto") + "\",\n"
                                + "    \"Versión\": \"" + getConnectionAux().getString("NombreProducto") + "\",\n"
                                + "    \"Kilometraje\": \"" + getConnectionAux().getString("Kilometraje") + "\",\n"
                                + "    \"Valuador\": \"" + buscarValuador(getConnectionAux().getString("v.IdEjecutivoValuacion")) + "\",\n" 
                                + "    \"PropuestaInicial\": \"" + getPropuestaInicial(idValuacion,getConnectionAux().getString("IdStatus")) + "\",\n"
                                + "    \"PropuestaActual\": \"" + getPropuestaActual(idValuacion,getConnectionAux().getString("IdStatus")) + "\",\n";
                        sql = "SELECT \n"
                                + "    vd.IdValuacion,\n"
                                + "    vd.IdSubcategoria,\n"
                                + "    vd.Valor,\n"
                                + "    vd.ValorPenalizacion,\n"
                                + "    vd.Observaciones,\n"
                                + "    vd.ValorBonificacion,\n"
                                + "    svc.Titulo AS NombreSubCategoria,\n"
                                + "    svc.IdCategoria,\n"
                                + "    vc.Titulo AS NombreCategoria,\n"
                                + "    svc2.Titulo AS NombreSubCategoria2,\n"
                                + "    svc2.IdCategoria,\n"
                                + "    vc2.Titulo AS NombreCategoria2\n"
                                + "FROM\n"
                                + "    " + getAuxdbDistribuidor() + ".Valuaciondetalle vd\n"
                                + "        LEFT JOIN\n"
                                + "    " + getAuxdbMarca() + ".subvaluacioncategoria svc ON vd.IdSubcategoria = svc.IdSubcategoria\n"
                                + "        LEFT JOIN\n"
                                + "    " + getAuxdbMarca() + ".valuacioncategoria vc ON svc.IdCategoria = vc.IdCategoria\n"
                                + "        LEFT JOIN\n"
                                + "    " + getAuxdbDistribuidor() + ".subvaluacioncategoria svc2 ON vd.IdSubcategoria = svc2.IdSubcategoria\n"
                                + "        LEFT JOIN\n"
                                + "    " + getAuxdbDistribuidor() + ".valuacioncategoria vc2 ON svc2.IdCategoria = vc2.IdCategoria\n"
                                + "WHERE\n"
                                + "    idvaluacion = '" + idValuacion + "'\n"
                                + "ORDER BY vd.valor;";
                        if (getConnectionAux().executeQuery(sql)) {
                            while (getConnectionAux().next()) {
                                if (!validarvacio(getConnectionAux().getString("ValorPenalizacion"), "0.00").equals("0.00")
                                        || !validarvacio(getConnectionAux().getString("ValorBonificacion"), "0.00").equals("0.00")) {
                                    if (!validarvacio(getConnectionAux().getString("NombreCategoria"), getConnectionAux().getString("NombreCategoria2")).equals("")
                                            && !validarvacio(getConnectionAux().getString("NombreSubCategoria"), getConnectionAux().getString("NombreSubCategoria2")).equals("")) {
                                        valuacionMecanica += "{\"Categoria\": \"" + validarvacio(getConnectionAux().getString("NombreCategoria"), getConnectionAux().getString("NombreCategoria2")) + "\","
                                                + "\"Subcategoria\": \"" + validarvacio(getConnectionAux().getString("NombreSubCategoria"), getConnectionAux().getString("NombreSubCategoria2")) + "\","
                                                + "\"Observaciones\": \"" + validarvacio(getConnectionAux().getString("Observaciones"), "") + "\","
                                                + "\"Penalizacion\": \"" + validarvacio(getConnectionAux().getString("ValorPenalizacion"), "0.00") + "\","
                                                + "\"Bonificacion\": \"" + validarvacio(getConnectionAux().getString("ValorBonificacion"), "0.00") + "\"},";
                                    }
                                }
                            }
                           if(!valuacionMecanica.isEmpty())
                           {
                                valuacionMecanica = valuacionMecanica.substring(0, valuacionMecanica.length() - 1); 
                           }
                            valuacionMecanica = "\"Mecanico\": [" + valuacionMecanica + "],";
     
                        }
                        sql = "SELECT \n"
                                + "    cll.IdChecklist,\n"
                                + "    cll.IdProspecto,\n"
                                + "    cll.IdValuacion,\n"
                                + "    clld.IdCategoria,\n"
                                + "    clld.Observaciones,\n"
                                + "    clld.Status,\n"
                                + "    clld.Penalizacion,\n"
                                + "    cllc.Titulo\n"
                                + "FROM\n"
                                + "    " + getAuxdbDistribuidor() + ".checklistlegal cll\n"
                                + "        LEFT JOIN\n"
                                + "    " + getAuxdbDistribuidor() + ".checklistlegaldetalle clld ON cll.IdChecklist = clld.IdChecklist\n"
                                + "        LEFT JOIN\n"
                                + "    " + getAuxdbDistribuidor() + ".checklistlegalcategorias cllc ON clld.IdCategoria = cllc.IdCategoria\n"
                                + "WHERE\n"
                                + "    cll.IdValuacion = '" + idValuacion + "'\n"
                                + "        AND cll.IdProspecto = '" + idProspecto + "';";
                        if (getConnectionAux().executeQuery(sql)) {
                            while (getConnectionAux().next()) {
                                if (!validarvacio(getConnectionAux().getString("Penalizacion"), "0.000").equals("0.000")) {
                                    valuacionLegal += "{\"Categoria\": \"" + validarvacio(getConnectionAux().getString("Titulo"), "") + "\","
                                            + "\"Observaciones\": \"" + validarvacio(getConnectionAux().getString("Observaciones"), "") + "\","
                                            + "\"Penalizacion\": \"" + validarvacio(getConnectionAux().getString("Penalizacion"), "") + "\"},";
                                }
                            }

                            if(!valuacionLegal.isEmpty())
                            {
                                 valuacionLegal = valuacionLegal.substring(0, valuacionLegal.length() - 1); 
                            }
                            valuacionLegal = "\"Legal\": [" + valuacionLegal + "],";
                        }
                        campos = campos + valuacionMecanica + valuacionLegal;
                    }
                }
            }
        }
        return campos;
    }
    
   private String getPropuestaInicial(String idValuacion,String idStatus) {
        String propuestaActual = "0";

        if(!idStatus.equals("0"))
        {

            String sql = "SELECT \n"
                    + "Propuesta1\n"
                    + "FROM\n"
                    + getAuxdbDistribuidor() + ".valuacionprecios\n"
                    + "WHERE\n"
                    + "IdValuacion = '" + idValuacion + "'";

            if (getConnectionAux().executeQuery(sql)) {
                if (getConnectionAux().next()) {
                    propuestaActual = getConnectionAux().getString("Propuesta1"); 
                } 
            }
        }
        
        int numeroSinDecimales = (int) Double.parseDouble(propuestaActual);
        String resultado = Integer.toString(numeroSinDecimales);
        
        return resultado;
    }
    
   private String getPropuestaActual(String idValuacion,String idStatus) {
    String propuestaActual = "0";
    
    if(!idStatus.equals("0"))
    {
    
        String sql = "SELECT \n"
                + "Propuesta1, Propuesta2, Propuesta3, Propuesta4, Propuesta5\n"
                + "FROM\n"
                + getAuxdbDistribuidor() + ".valuacionprecios\n"
                + "WHERE\n"
                + "IdValuacion = '" + idValuacion + "'";

        if (getConnectionAux().executeQuery(sql)) {
            if (getConnectionAux().next()) {
                if (!"0".equals(getConnectionAux().getString("Propuesta5"))) {
                    propuestaActual = getConnectionAux().getString("Propuesta5");
                } else if (!"0".equals(getConnectionAux().getString("Propuesta4"))) {
                    propuestaActual = getConnectionAux().getString("Propuesta4");
                } else if (!"0".equals(getConnectionAux().getString("Propuesta3"))) {
                    propuestaActual = getConnectionAux().getString("Propuesta3");
                } else if (!"0".equals(getConnectionAux().getString("Propuesta2"))) {
                    propuestaActual = getConnectionAux().getString("Propuesta2");
                } else if (!"0".equals(getConnectionAux().getString("Propuesta1"))) {
                    propuestaActual = getConnectionAux().getString("Propuesta1");
                }
            } 

        }
    }
    
    int numeroSinDecimales = (int) Double.parseDouble(propuestaActual);
    String resultado = Integer.toString(numeroSinDecimales);
 
    return resultado;
}
   

    private String getIdSeguimientoSeminuevos(String idValuacion) {
        String idSeminuevos = "";
        ConnectionManager connectionAux = abrirConnection(getIdDistribuidor());
        String idSeguimiento = "NI";
        String sql = "SELECT \n"
                + "IdDistribuidorSemiNuevos, Nombre\n"
                + "FROM\n"
                + getDbGrupoCorporativo() + ".distribuidores,\n"
                + getDbGrupoCorporativo() + ".distribuidoresnuevosseminuevos\n"
                + "WHERE\n"
                + "IdDistribuidorNuevo = '" + getIdDistribuidor() + "'\n"
                + " AND IdDistribuidor = IdDistribuidorSemiNuevos\n"
                + " ORDER BY Nombre;";
        if (connectionAux.executeQuery(sql)) {
            while (connectionAux.next()) {
                idSeminuevos = connectionAux.getString("IdDistribuidorSemiNuevos");
                setConnectionAux(abrirConnection(idSeminuevos));
                sql = "SELECT \n"
                        + "    v.IdValuacion, v.IdSeguimiento, s.cumplida\n"
                        + "FROM\n"
                        + "    " + getAuxdbDistribuidor() + ".valuacion v\n"
                        + "        LEFT JOIN\n"
                        + "    " + getAuxdbDistribuidor() + ".seguimientos s ON v.IdSeguimiento = s.IdSeguimiento\n"
                        + "WHERE\n"
                        + "    v.IdValuacion = '" + idValuacion + "'\n";
                if (getConnectionAux().executeQuery(sql)) {
                    if (getConnectionAux().next()) {
                        if (idSeguimiento.equals("NI")) {
                            idSeguimiento = validarvacio(getConnectionAux().getString("IdSeguimiento"), "NI");
                            break;
                        }
                    }
                }

            }
        }
        if (connectionAux != null) {
            connectionAux.close();
        }
        return idSeguimiento;
    }

    private String generaJSONRespuesta() {
        String respuesta = "";
        switch (getStatus()) {
            case 0:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"" + getMensaje() + "\",\n"
                        + "    \"valuaciones\": []\n"
                        + "}";
                break;
            case 1:
                respuesta = "{\n"
                        + "    \"codigo\": \"" + getStatus() + "\",\n"
                        + "    \"mensaje\": \"OK\",\n"
                        + "    \"valuaciones\": [" + jsonBody + "]\n"
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

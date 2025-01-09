package com.seekop.seekop.resources.resources.mobile;

import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Seekop
 */
public class crearSeguimiento extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Crear Seguimiento";
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
    private String actividad = ""; // 1 citas, 2 demos, 3 cotizaciones, 4 valuaciones
    private String fecha = "";
    private String idAuto = "";
    private String idProducto = "";
    private String modelo = "";
    private String observaciones = "";
    private String vin = "";

    public crearSeguimiento(String contenido, String ip) {
        recibidoJSON = contenido;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON;
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON);
                this.ip = ip;
                this.token = objetoJson.getString("Token");
                this.actividad = objetoJson.getString("Actividad"); // 1 citas, 2 demos
                this.fecha = objetoJson.getString("Fecha");
                this.idAuto = validarvacio(objetoJson.getString("IdAuto"), "");
                this.modelo = validarvacio(objetoJson.getString("modelo"), "");
                this.idProducto = validarvacio(objetoJson.getString("IdProducto"), "");
                this.observaciones = objetoJson.getString("Observaciones");
                this.vin = validarvacio(objetoJson.getString("Vin"), "");
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
        String idSeguimiento = "";
        String sql = "SELECT MAX(IdSeguimiento) AS idS FROM " + getDbDistribuidor() + ".seguimientos WHERE IdSeguimiento LIKE '" + getIdProspecto() + "A____';";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                String temporal = validarvacio(getConnectionDistribuidor().getString("idS"), "0");
                idSeguimiento = generaIdVenta(getIdProspecto(), "A", temporal);
            } else {
                idSeguimiento = generaIdVenta(getIdProspecto(), "A", "0");
            }
        } else {
            setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
        }
        String idTipoDeCompra = "";
        String idTipodeventa = "";
        String idTipodecierre = "";
        String idFuente = "";
        String idSubcampana = "";
        String idStatus = "";
        String idColor = "";
        String IdAutoActual = "";
        String ModeloActual = "";
        String IdProductoActual = "";
        String auxIdPropietario = "";
        sql = "SELECT \n"
                + "    IdProspecto,\n"
                + "    IdAuto,\n"
                + "    IdTipoDeCompra,\n"
                + "    modelo,\n"
                + "    idProducto,\n"
                + "    idTipodeventa,\n"
                + "    idTipodecierre,\n"
                + "    idFuente,\n"
                + "    idSubcampana,\n"
                + "    idStatus,\n"
                + "    IdAutoActual,\n"
                + "    ModeloActual,\n"
                + "    IdProductoActual,\n"
                + "    idcolor,\n"
                + "    IdPropietario\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".prospectos\n"
                + "WHERE\n"
                + "    idprospecto = '" + getIdProspecto() + "';";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                idAuto = validarvacio(idAuto, validarvacio(getConnectionDistribuidor().getString("IdAuto"), ""));
                idProducto = validarvacio(idProducto, validarvacio(getConnectionDistribuidor().getString("idProducto"), ""));
                modelo = validarvacio(modelo, validarvacio(getConnectionDistribuidor().getString("modelo"), ""));
                idTipoDeCompra = validarvacio(getConnectionDistribuidor().getString("IdTipoDeCompra"), "");
                idTipodeventa = validarvacio(getConnectionDistribuidor().getString("idTipodeventa"), "");
                idTipodecierre = validarvacio(getConnectionDistribuidor().getString("idTipodecierre"), "");
                idFuente = validarvacio(getConnectionDistribuidor().getString("idFuente"), "");
                idSubcampana = validarvacio(getConnectionDistribuidor().getString("idSubcampana"), "");
                idStatus = validarvacio(getConnectionDistribuidor().getString("idStatus"), "");
                idColor = validarvacio(getConnectionDistribuidor().getString("idcolor"), "");
                IdAutoActual = validarvacio(getConnectionDistribuidor().getString("IdAutoActual"), "");
                ModeloActual = validarvacio(getConnectionDistribuidor().getString("ModeloActual"), "");
                IdProductoActual = validarvacio(getConnectionDistribuidor().getString("IdProductoActual"), "");
                auxIdPropietario = validarvacio(validarvacio(getConnectionDistribuidor().getString("IdPropietario"), ""), getIdEjecutivo());
            }
        } else {
            setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
        }
        String auxActividad = "";
        switch (actividad) {
            case "1":
                auxActividad = "Cita";
                break;
            case "2":
                auxActividad = "Demostracion";
                break;
            default:
                throw new AssertionError();
        }
        String idTipoActividad = "";
        String idtipoactividaddetalle = "";
        String dias = "";
        String idStatusNuevo = "";
        String idSubStatus = "";
        sql = "SELECT \n"
                + "    nombre,\n"
                + "    IdTipoActividadDetalle,\n"
                + "    IdTipoActividad,\n"
                + "    dias,\n"
                + "    Tipo,\n"
                + "    IdStatus,\n"
                + "    IdSubstatus,\n"
                + "    cita\n"
                + "FROM\n"
                + "    " + (traerValorConfiguracion("Multiseguimiento", "Migrar").equals("1")
                ? getDbDistribuidor() : getDbMarca()) + ".tipoactividaddetalle\n"
                + "WHERE\n"
                + "    " + (auxActividad.equals("Cita") ? "Uso='102'" : "Nombre = '" + auxActividad + "'") + " AND Activo = '1';";

        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                idTipoActividad = validarvacio(getConnectionDistribuidor().getString("IdTipoActividad"), "");
                idtipoactividaddetalle = validarvacio(getConnectionDistribuidor().getString("IdTipoActividadDetalle"), "");
                dias = validarvacio(getConnectionDistribuidor().getString("dias"), "");
                idStatusNuevo = validarvacio(getConnectionDistribuidor().getString("IdStatus"), "");
                idSubStatus = validarvacio(getConnectionDistribuidor().getString("IdSubstatus"), "");

                String idTipoNegocio = "";
                String idProceso = "";
                sql = "SELECT IdTipoNegocio, IdProceso \n"
                        + "FROM " + getDbDistribuidor() + ".prospectostiponegocio \n"
                        + "WHERE IdProspecto='" + getIdProspecto() + "' order by Registro ;";
                if (getConnectionDistribuidor()
                        .executeQuery(sql)) {
                    if (getConnectionDistribuidor().next()) {
                        idTipoNegocio = validarvacio(getConnectionDistribuidor().getString("IdTipoNegocio"), "");
                        idProceso = validarvacio(getConnectionDistribuidor().getString("IdProceso"), "");
                    }
                } else {
                    setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                }
                sql = "INSERT INTO " + getDbDistribuidor() + ".seguimientos \n"
                        + "(`IdSeguimiento`, `Referencia`, `Tipo`, `IdProspecto`, `IdAuto`, `Modelo`, `IdProducto`, `IdTipoDeVenta`, \n"
                        + "`IdTipoDeCompra`, `IdTipoDeCierre`, `IdFuente`, `IdSubcampana`, `IdStatus`, `IdColor`, `IdTipoActividad`, `IdTipoActividadDetalle`, \n"
                        + "`IdContacto`, `Contacto`, `Programada`, `Reprogramaciones`, `Aviso`, `MomentoAviso`, `MinutosAviso`, `Observaciones`, \n"
                        + "`ObservacionesCumplimiento`, `Cumplida`, `Exitosa`, `IdCallCenter`, `IdPropietario`, `Modificado`, `IdModificado`, `Registro`, \n"
                        + "`IdCreado`, `Sincronizado`, `Lead`, `IdCompartir`, `IdProceso`, `IdTipoNegocio`, `IdDetalle`, `Origen`, \n"
                        + "`IdCumplido`, `IdSubstatus`, `Evaluacion`, `IdConfirmada`, `StatusCheckIn`, `Confirmada`, `IdOrigen`, `TipoEvaluacion`) \n"
                        + "VALUES \n"
                        + "('" + idSeguimiento + "', '" + idSeguimiento + "', '', '" + getIdProspecto() + "', '" + idAuto + "', '" + modelo + "', '" + idProducto + "', '" + idTipodeventa + "', \n"
                        + "'" + idTipoDeCompra + "', '" + idTipodecierre + "', '" + idFuente + "', '" + idSubcampana + "', '" + idStatusNuevo + "', '" + idColor + "', '" + idTipoActividad + "', '" + idtipoactividaddetalle + "', \n"
                        + "'0000000000', '', '" + fecha + "', '0', 0, '1900-01-01 00:00:00', 0, '" + observaciones + "', \n"
                        + "'', '1900-01-01 00:00:00', 0, '', '" + getIdEjecutivo() + "', '1900-01-01 00:00:00', '0000000000', '" + getFechaHoy() + "', \n"
                        + "'" + auxIdPropietario + "', 0, 0, '0000000000', '" + idProceso + "', '" + idTipoNegocio + "', '', '0', \n"
                        + "'0000000000', '" + idSubStatus + "', '0', '0000000000', '0', 0, '00000000000000000000', '1');";

                if (getConnectionDistribuidor()
                        .execute(sql)) {
                    Map<String, Object> parameters = new HashMap<>();
                    switch (actividad) {
                        case "1":
                            parameters.put("fecha", dateFormatter(fecha + ":00", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss"));
                            parameters.put("idseguimiento", idSeguimiento);
                            sendDispositionRealTime("16", getIdDistribuidor(), getIdProspecto(), parameters);
                            break;
                        case "2":
                            sql = "INSERT INTO `" + getDbDistribuidor() + "`.`demostraciones` \n"
                                    + "(`NoMotor`, `Fecha`, `IdProspecto`, `IdEjecutivo`, \n"
                                    + "`IdTipoDeVenta`, `IdTipoDeCompra`, `IdTipoDeCierre`, `IdFuente`, \n"
                                    + "`IdSubcampana`, `IdStatus`, `IdSeguimiento`, `IdRuta`, \n"
                                    + "`Placas`, `KmInicio`, `KmFin`, `Licencia`, \n"
                                    + "`Observaciones`, `Status`, `Sincronizado`, `Registro`, \n"
                                    + "`IdCreado`, `Modificado`, `IdModificado`, `Tipo`, \n"
                                    + "`HrInicio`, `HrFin`, `Folio`, `IdAuto`, \n"
                                    + "`IdProducto`, `Url_Licencia`) \n"
                                    + "VALUES \n"
                                    + "('" + vin + "', '" + fecha + "', '" + getIdProspecto() + "', '" + auxIdPropietario + "', \n"
                                    + "'" + idTipodeventa + "', '" + idTipoDeCompra + "', '" + idTipodecierre + "', '" + idFuente + "', \n"
                                    + "'" + idSubcampana + "', '" + idStatus + "', '" + idSeguimiento + "', '', \n"
                                    + "'', '0.00000000', '0.00000000', '', \n"
                                    + "'" + observaciones + "', 0, 0, '" + getFechaHoy() + "', \n"
                                    + "'" + auxIdPropietario + "', '1900-01-01 00:00:00', '0000000000', 0, \n"
                                    + "'00:00:00', '00:00:00', '00000000000000000000', '" + idAuto + "', \n"
                                    + "'" + idProducto + "', '');";
                            if (!getConnectionDistribuidor().execute(sql)) {
                                setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                            }
                            parameters.put("fecha", dateFormatter(fecha + ":00", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss"));
                            parameters.put("idseguimiento", idSeguimiento);
                            parameters.put("vin", vin);
                            sendDispositionRealTime("57", getIdDistribuidor(), getIdProspecto(), parameters);
                            break;

                        default:
                            break;
                    }
                } else {
                    setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                }
            } else {
                setErrorMensaje("No se encontro una actividad detalle para este seguimiento");
            }
        } else {
            setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
        }
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

    public String generaIdVenta(String idProspecto, String textoId, String numeroId) {
        String idGenerado = null;
        String baseId = "0000";
        String idBase = "";
        if (numeroId == null) {
            numeroId = "";
        }
        if (numeroId.equals("")) {
            numeroId = "0";
        }
        if (idProspecto != null && !idProspecto.equals("")) {
            if (textoId != null && !textoId.equals("")) {
                if (numeroId != null && !numeroId.equals("")) {
                    idBase = idProspecto + textoId;
                    if (numeroId.length() >= idBase.length()) {
                        if (idBase.equals(numeroId.substring(0, idBase.length()))) {
                            numeroId = numeroId.substring(idBase.length(), numeroId.length());
                        } else {
                            numeroId = numeroId.replace(idProspecto, "");
                            numeroId = numeroId.replace(textoId, "");
                        }
                    }
                    numeroId = numeroId.replaceAll("\\D+", "");
                    int auxid = Integer.parseInt(numeroId) + 1;
                    numeroId = String.valueOf(auxid);
                    if (baseId.length() >= numeroId.length()) {

                        idGenerado = idProspecto + textoId + baseId.substring(0, (baseId.length() - numeroId.length())) + numeroId;
                    }
                }
            }
        }
        return idGenerado;
    }

    public String generaIdValuacion(String idProspecto, String textoId, String numeroId) {
        String idGenerado = null;
        String baseId = "00000";
        String idBase = "";
        if (numeroId == null) {
            numeroId = "";
        }
        if (numeroId.equals("")) {
            numeroId = "0";
        }
        if (idProspecto != null && !idProspecto.equals("")) {
            if (textoId != null && !textoId.equals("")) {
                if (numeroId != null && !numeroId.equals("")) {
                    idBase = idProspecto + textoId;
                    if (numeroId.length() >= idBase.length()) {
                        if (idBase.equals(numeroId.substring(0, idBase.length()))) {
                            numeroId = numeroId.substring(idBase.length(), numeroId.length());
                        } else {
                            numeroId = numeroId.replace(idProspecto, "");
                            numeroId = numeroId.replace(textoId, "");
                        }
                    }
                    numeroId = numeroId.replaceAll("\\D+", "");
                    int auxid = Integer.parseInt(numeroId) + 1;
                    numeroId = String.valueOf(auxid);
                    if (baseId.length() >= numeroId.length()) {

                        idGenerado = idProspecto + textoId + baseId.substring(0, (baseId.length() - numeroId.length())) + numeroId;
                    }
                }
            }
        }
        return idGenerado;
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

    private void validarDatosProspecto() {

    }
}

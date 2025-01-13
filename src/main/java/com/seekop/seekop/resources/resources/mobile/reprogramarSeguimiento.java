package com.seekop.seekop.resources.resources.mobile;

import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Seekop
 */
public class reprogramarSeguimiento extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "reprogramar seguimiento";
    private String fechainicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    private String fechafin = "";

    private String ip = "";
    private String token = "";
    private String idSeguimiento = "";
    private String nuevaFecha = "";
    //////
    private String json = "";
    private String jsonMandado = "";
    private String jsonBody = "[]";
    //////
    private String idValuacion = "";
    private String typeName = "";

    public reprogramarSeguimiento(String recibidoJSON, String ip) {
        jsonMandado = recibidoJSON;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON.toUpperCase();
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON.toUpperCase());
                this.ip = ip;
                this.token = objetoJson.getString("TOKEN");
                this.idSeguimiento = objetoJson.getString("IDSEGUIMIENTO");
                this.nuevaFecha = objetoJson.getString("FECHA");
                this.idValuacion = objetoJson.getString("IDVALUACION");
                this.typeName = objetoJson.getString("TYPENAME");
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
        String distribuidor = getDbDistribuidor();
        Map<String, Object> parameters = new HashMap<>();
        String activityId = "";
        if (!idValuacion.isEmpty()) {
            distribuidor = getNombreSeminuevos(getIdDistribuidor());
            AbrirConnectionSeminuevos();
        }

        int contadorReprogramaciones = 0;
        String sql = "SELECT \n"
                + "    s.IdSeguimiento,\n"
                + "    s.Referencia,\n"
                + "    s.Programada,\n"
                + "    s.Reprogramaciones,\n"
                + "    s.Cumplida,\n"
                + "    s.Modificado,\n"
                + "    s.IdModificado,\n"
                + "    s.IdTipoActividadDetalle,\n"
                + "    ta.IdTipoActividadDetalle,\n"
                + "    ta.Uso,\n"
                + "    ta.Nombre AS nombreTipoAD,\n"
                + "    ta.cita\n"
                + "FROM\n"
                + "    " + distribuidor + ".seguimientos s\n"
                + "        LEFT JOIN\n"
                + "    " + distribuidor + ".tipoactividaddetalle ta ON ta.IdTipoActividadDetalle = s.IdTipoActividadDetalle\n"
                + "WHERE\n"
                + "    IdSeguimiento = '" + idSeguimiento + "';";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                if (getConnectionDistribuidor().getString("Cumplida").equals("1900-01-01 00:00:00.0")) {
                    contadorReprogramaciones = Integer.parseInt(validarvacio(getConnectionDistribuidor().getString("Reprogramaciones"), "0"));
                    contadorReprogramaciones++;
                    String idUso = validarvacio(getConnectionDistribuidor().getString("Uso"), "");
                    String nombreUso = getIdUso(idUso);
                    sql = "UPDATE `" + distribuidor + "`.`seguimientos` \n"
                            + "SET \n"
                            + "    `Programada` = '" + nuevaFecha + "',\n"
                            + "    `Reprogramaciones` = '" + contadorReprogramaciones + "',\n"
                            + "    `Modificado` = '" + getFechaHoy() + "',\n"
                            + "    `IdModificado` = '" + getIdEjecutivo() + "'\n"
                            + "WHERE\n"
                            + "    (`IdSeguimiento` = '" + idSeguimiento + "');";
                    if (getConnectionDistribuidor().execute(sql)) {
                        switch (typeName) {
                            case "APPOINTMENT":
                                activityId = "170";
                                break;
                            case "TESTDRIVE":
                                activityId = "165";
                                break;
                            case "unknown":
                            default:
                                break;
                        }
                        
                        if (!idValuacion.isEmpty()) {
                            sendDispositionValuation(idValuacion,"166",false);
                            ////ACTUALIZA SEMINUEVOS
                            sql = "UPDATE `" + distribuidor + "`.`valuacion` \n"
                                    + "SET \n"
                                    + "    `Solicitud` = '" + nuevaFecha + "'\n"
                                    + "WHERE\n"
                                    + "    (`IdSeguimiento` = '" + idSeguimiento + "'\n"
                                    + "        AND `IdProspecto` = '" + getIdProspecto() + "');";
                            if (!getConnectionDistribuidor().execute(sql)) {
                                setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                            }

                            getTokenInformation(token);
                            if (getConnectionDistribuidor() != null) {
                                ////ACTUALIZA NUEVOS
                                sql = "UPDATE `" + getDbDistribuidor() + "`.`valuacion` \n"
                                        + "SET \n"
                                        + "    `Solicitud` = '" + nuevaFecha + "'\n"
                                        + "WHERE\n"
                                        + "    (`IdValuacion` = '" + idValuacion + "'\n"
                                        + "        AND `IdProspecto` = '" + getIdProspecto() + "');";
                                if (!getConnectionDistribuidor().execute(sql)) {
                                    setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                                }
                            }
                        } else {
                            parameters.put("idseguimiento", idSeguimiento);
                            sendDispositionRealTime(activityId, getIdDistribuidor(), getIdProspecto(), parameters);
                            switch (nombreUso) {
                                case "Demostración":
                                    sql = "UPDATE `" + distribuidor + "`.`demostraciones` \n"
                                            + "SET \n"
                                            + "    `Fecha` = '" + nuevaFecha + "',\n"
                                            + "    `Modificado` = '" + getFechaHoy() + "',\n"
                                            + "    `IdModificado` = '" + getIdEjecutivo() + "'\n"
                                            + "WHERE\n"
                                            + "    (`IdSeguimiento` = '" + idSeguimiento + "')\n"
                                            + "        AND (`IdProspecto` = '" + getIdProspecto() + "');";
                                    if (!getConnectionDistribuidor().execute(sql)) {
                                        setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                                    }
                                    break;
                                case "Valuación":

                                    break;
                                default:

                                    break;
                            }
                        }
                    } else {
                        setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                    }
                } else {
                    setErrorMensaje("El seguimiento no se puede reprogramar por que ya fue cumplida");
                }
            } else {
                setErrorMensaje("No se encontro seguimiento para el IdSeguimiento='" + idSeguimiento + "'");
            }
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

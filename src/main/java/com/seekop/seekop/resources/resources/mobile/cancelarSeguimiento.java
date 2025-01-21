package com.seekop.seekop.resources.resources.mobile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.json.JSONException;
import org.json.JSONObject;
import resources.ConnectionManager;

/**
 *
 * @author Seekop
 */
public class cancelarSeguimiento extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "CANCELAR seguimiento";
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

    public cancelarSeguimiento(String recibidoJSON, String ip) {
        jsonMandado = recibidoJSON;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON.toUpperCase();
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON);
                this.ip = ip;
                this.token = objetoJson.getString("TOKEN");
                this.idSeguimiento = objetoJson.getString("IDSEGUIMIENTO");
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
                if (!getConnectionDistribuidor().getString("Cumplida").equals("1900-01-01 00:00:00")) {
                    contadorReprogramaciones = Integer.parseInt(validarvacio(getConnectionDistribuidor().getString("Reprogramaciones"), "0"));
                    contadorReprogramaciones++;
                    String idUso = validarvacio(getConnectionDistribuidor().getString("Uso"), "");
                    String nombreUso = getIdUso(idUso);
                    sql = "UPDATE `" + distribuidor + "`.`seguimientos` \n"
                            + "SET \n"
                            + "    `Cumplida` = '" + getFechaHoy() + "',\n"
                            + "    `Exitosa` = 0,\n"
                            + "    `Modificado` = '" + getFechaHoy() + "',\n"
                            + "    `IdModificado` = '" + getIdEjecutivo() + "'\n"
                            + "WHERE\n"
                            + "    (`IdSeguimiento` = '" + idSeguimiento + "');";
                    if (getConnectionDistribuidor().executeUpdate(sql)) {

                        switch (typeName) {
                            case "APPOINTMENT":
                                activityId = "58";
                                break;
                            case "TESTDRIVE":
                                parameters.put("vin", getVinDemo());
                                activityId = "57";
                                break;
                            case "unknown":
                            default:
                                break;
                        }
                        if (!idValuacion.isEmpty()) {
                            
                            sendDispositionValuation(idValuacion,"99",false);
                            
                            ////ACTUALIZA SEMINUEVOS
                            sql = "UPDATE `" + distribuidor + "`.`valuacion` \n"
                                    + "SET \n"
                                    + "    `IdStatus` = '6'\n"
                                    + "WHERE\n"
                                    + "    (`IdSeguimiento` = '" + idSeguimiento + "'\n"
                                    + "        AND `IdProspecto` = '" + getIdProspecto() + "');";
                            if (!getConnectionDistribuidor().executeUpdate(sql)) {
                                setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                            }
                            else
                            {
                                String idProspecto = getIdProspecto();
                                String sqlValuacion = "SELECT \n"
                                + "    IdValuacion,IdEjecutivoValuacion, IdStatus\n"
                                + "FROM\n"
                                + "    " + getDbDistribuidor() + ".valuacion\n"
                                + "WHERE\n"
                                + "    idvaluacion = '" + idValuacion + "'\n"
                                + "        AND IdProspecto = '" + idProspecto + "';";
 
                                if (getConnectionDistribuidor().executeQuery(sqlValuacion)) {
                                    if (getConnectionDistribuidor().next()) {
                                        
                                        String idValuador = getConnectionDistribuidor().getString("IdEjecutivoValuacion");
                                        
                                        
                                        JSONObject dataObject = new JSONObject();
                                        dataObject.put("r", idProspecto);
                                        dataObject.put("r2", idValuacion);
                                        dataObject.put("r3", "");
                                        dataObject.put("r4", "6");

                                        String titulo = "Valuación cancelada";
                                        String mensajeNotificacion = "El prospecto " + buscarNombreProspecto(idProspecto) + " canceló la valuación ";

                                        sendNotification("27",idValuador,idProspecto,titulo,mensajeNotificacion,dataObject);  
                                    }
                                } 
                            }

                            getTokenInformation(token);
                            if (getConnectionDistribuidor() != null) {
                                ////ACTUALIZA NUEVOS
                                sql = "UPDATE `" + getDbDistribuidor() + "`.`valuacion` \n"
                                        + "SET \n"
                                        + "    `IdStatus` = '6'\n"
                                        + "WHERE\n"
                                        + "    (`IdValuacion` = '" + idValuacion + "'\n"
                                        + "        AND `IdProspecto` = '" + getIdProspecto() + "');";
                                if (!getConnectionDistribuidor().execute(sql)) {
                                    setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                                }
                            }
                        } else {
                            parameters.put("valor", -1);
                            parameters.put("idseguimiento", idSeguimiento);
                            sendDispositionRealTime(activityId, getIdDistribuidor(), getIdProspecto(), parameters);
                            switch (nombreUso) {
                                case "Demostración":
                                    sql = "UPDATE `" + distribuidor + "`.`demostraciones` \n"
                                            + "SET \n"
                                            + "    `Status` = 3\n"
                                            + "WHERE\n"
                                            + "    (`IdSeguimiento` = '" + idSeguimiento + "')\n"
                                            + "        AND (`IdProspecto` = '" + getIdProspecto() + "');";
                                    if (!getConnectionDistribuidor().executeUpdate(sql)) {
                                        setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                                    }
                                    break;
                                case "Valuación":
                                    sql = "UPDATE `" + getDbDistribuidor() + "`.`valuacion` \n"
                                            + "SET \n"
                                            + "    `IdStatus` = '6'\n"
                                            + "WHERE\n"
                                            + "    (`IdSeguimiento` = '" + idSeguimiento + "'\n"
                                            + "        AND `IdProspecto` = '" + getIdProspecto() + "');";
                                    if (!getConnectionDistribuidor().executeUpdate(sql)) {
                                        setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                                    }
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

    private Object getVinDemo() {
        StringBuilder query = new StringBuilder();
        String noMotor = "";
        query.append("SELECT ")
                .append("demo.NoMotor ").append("FROM ")
                .append(getDbDistribuidor())
                .append(".demostraciones AS demo ")
                .append("WHERE demo.IdSeguimiento = ?");

        DataSource datasource = ConnectionManager.getDatasource(getPoolDeConexion());

        assert datasource != null;

        try (Connection con = datasource.getConnection(); PreparedStatement preparedStatement = con.prepareStatement(query.toString())) {
            preparedStatement.setString(1, idSeguimiento);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    noMotor = resultSet.getString("NoMotor");
                }
            }
        } catch (SQLException ex) {
            setErrorMensaje("JSON malformed: " + ex.toString());
        }
        return noMotor;
    }
}

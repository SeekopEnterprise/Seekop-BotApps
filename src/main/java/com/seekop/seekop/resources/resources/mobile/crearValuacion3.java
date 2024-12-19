package com.seekop.seekop.resources.resources.mobile;

import com.sicop.web.common.xml.services.XMLServiceTable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import resources.CommonSeekopUtilities;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Seekop
 */
public class crearValuacion3 extends CommonSeekopUtilities {

    private boolean produccion = true;
    private String descripcionServicio = "Crear valuacion";
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
    private String idMarcaActual = "";
    private String idAutoActual = "";
    private String modeloActual = "";
    private String idProductoActual = "";
    private String idColorActual = "";
    private String placas = "";
    private String vin = ""; // A partir de aquí solo para citas
    private String FechaValuacion = "";
    private String observaciones = "";

    public crearValuacion3(String contenido, String ip) {
        recibidoJSON = contenido;
        if (recibidoJSON != null && !"".equals(recibidoJSON)) {
            recibidoJSON = recibidoJSON;
            JSONObject objetoJson;
            try {
                objetoJson = new JSONObject(recibidoJSON);
                this.ip = ip;
                this.token = objetoJson.getString("Token");
                this.idMarcaActual = objetoJson.getString("IdMarcaActual");
                this.idAutoActual = objetoJson.getString("IdAutoActual");
                this.modeloActual = objetoJson.getString("ModeloActual");
                this.idProductoActual = objetoJson.getString("IdProductoActual");
                this.idColorActual = objetoJson.getString("IdColorActual");
                this.placas = objetoJson.getString("Placas");
                this.vin = objetoJson.getString("VIN");
                this.FechaValuacion = objetoJson.getString("FechaValuacion");
                this.observaciones = objetoJson.getString("Observaciones");
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
        String asignarVencidoSubasta = generateXMLresponseSolicitud(getIdProspecto());
        boolean respuesta = getProperties(asignarVencidoSubasta, "state");
        if (respuesta) {

        } else {
            setErrorMensaje("Ocurrio un error durante la solicitud favor de volver a intentar");
        }
    }

    protected String generateXMLresponseSolicitud(String idProspecto) {
        String URL_SERVICIOS = traerValorConfiguracion("SERVICIOSXML", "URL");
        String[][] filtro = new String[0][0];
        String[][] columns = new String[0][0];
        String XMLrequest = getServiceSolicitud("solicitaValuacion", filtro, columns, idProspecto).getXML();
        String action = "write";
        HttpURLConnection urlConnection;
        String responseXML = "";
        try {
            URL url = new URL(URL_SERVICIOS + action + ".xml");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "text/xml");
            OutputStream output = new BufferedOutputStream(urlConnection.getOutputStream());
            output.write(XMLrequest.getBytes());
            output.flush();
            try (InputStream responseStream = new BufferedInputStream(urlConnection.getInputStream()); BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"))) {
                StringBuilder stringBuilder = new StringBuilder();
                while ((XMLrequest = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(XMLrequest).append("");
                }
                responseXML = stringBuilder.toString();
            }
        } catch (IOException e) {
            System.err.println(e.toString());
        }

        return responseXML;
    }

    public final String getMD5Code(String registryId, String idMarca, String eMail, String licencia) {
        String passwordInterface = "";
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"));
            String str = "sicop.MX00000001.";
            str += idMarca + ".";
            str += eMail + ".";
            if (licencia != null) {
                str += licencia + ".";
            }
            str += registryId + ".";
            str += c.get(GregorianCalendar.YEAR) + ".";
            str += (c.get(GregorianCalendar.MONTH) + 1) + ".";
            str += c.get(GregorianCalendar.DAY_OF_MONTH);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            byte[] bytes = digest.digest(str.getBytes());
            StringBuilder MD5Temp = new StringBuilder(2 * bytes.length);
            for (byte aByte : bytes) {
                int bajo = aByte & 0x0f;
                int alto = (aByte & 0xf0) >> 4;
                MD5Temp.append(hex[alto]);
                MD5Temp.append(hex[bajo]);
            }
            passwordInterface = MD5Temp.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("getMD5HashCode: " + e.toString());
        }
        return passwordInterface;
    }

    protected XMLService getServiceSolicitud(String nameServicio, String[][] filters, String[][] columns, String idProspecto) {
        XMLService service = new XMLService();
        XMLServiceTable table = new XMLServiceTable();
        table = service.addTable(nameServicio);
        service.setIdDistribuidor(getIdDistribuidor());
        service.setIdInterface("MX00000001");
        service.setIdMarca(getIdMarca());
        service.setPassword(getMD5Code(getIdEjecutivo(), getIdMarca(), "", null));
        service.setPoolname(getPoolDeConexion());
        service.setRegistryId(getIdEjecutivo());
        service.seteMail(getEmalEjecutivo());

        for (int i = 0; i < filters.length; i++) { //10 filas
            if (filters[i][2] == null) {
                table.addFilter(filters[i][0], filters[i][1]);
            } else {
                table.addFilter(filters[i][0], filters[i][1], filters[i][2]);
            }
        }

        for (int i = 0; i < columns.length; i++) { //10 filas
            table.addRow();
            for (int j = 0; j < columns[0].length; j++) { // 3 columnas
                table.setValue(columns[i][0], columns[i][1]);
            }
        }
        table.addRow();
        table.setValue("IdUsuario", getIdEjecutivo());
        table.setValue("IdProspecto", idProspecto);

        table.setValue("IdMarcaActual", idMarcaActual);
        table.setValue("IdAutoActual", idAutoActual);
        table.setValue("ModeloActual", modeloActual);
        table.setValue("IdProductoActual", idProductoActual);
        table.setValue("IdColorActual", idColorActual);
        table.setValue("Placas", placas);
        table.setValue("Vin", vin);
        table.setValue("IdClase", "");
        table.setValue("Observaciones", encodeToISO88591(observaciones));
        table.setValue("IdDistribuidorSeminuevos", getIdSeminuevos(getIdDistribuidor()));
        table.setValue("IdValuador", "");
        table.setValue("HabilitarSeminuevos", getHabilitarSeminuevos(getIdDistribuidor()));
        table.setValue("PrecioPropuestoCliente", "0");
        table.setValue("TipoValuacion", "0");
        table.setValue("FechaValuacion", FechaValuacion);
//        if (!metodoReloj.equals("")) {
//            table.setValue("MetodoReloj", metodoReloj);
//        }

        table.getXML();
        return service;
    }

    public static String encodeToISO88591(String input) {
        String resultado = "";
        try {
            // Convierte la cadena a bytes en UTF-8 y luego a una nueva cadena en ISO-8859-1
            resultado = new String(input.getBytes("UTF-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            resultado=input;
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

    private boolean getProperties(String XML, String etiqueta) {
        boolean correcto = true;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document documents = builder.parse(new InputSource(new StringReader(
                    XML)));
            documents.getDocumentElement().normalize();
            NodeList propLists = documents.getElementsByTagName(etiqueta);
            for (int i = 0; i < propLists.getLength(); i++) {
                Node propList = propLists.item(i);
                if (propList.getTextContent().equals("1") && i == 1) {
                    correcto = false;
                }
            }
        } catch (IOException | ParserConfigurationException | DOMException | SAXException e) {
            e.printStackTrace();
        }
        return correcto;
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

    private String getIdSeminuevos(String idDistribuidor) {
        String idSeminuevos = "";
        String sql = "SELECT \n"
                + "IdDistribuidorSemiNuevos, Nombre\n"
                + "FROM\n"
                + getDbGrupoCorporativo() + ".distribuidores,\n"
                + getDbGrupoCorporativo() + ".distribuidoresnuevosseminuevos\n"
                + "WHERE\n"
                + "IdDistribuidorNuevo = '" + idDistribuidor + "'\n"
                + " AND IdDistribuidor = IdDistribuidorSemiNuevos\n"
                + " ORDER BY Nombre;";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                idSeminuevos = getConnectionDistribuidor().getString("IdDistribuidorSemiNuevos");
            }
        }
        return idSeminuevos;
    }
}

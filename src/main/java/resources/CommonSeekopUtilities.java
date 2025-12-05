package resources;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import org.json.JSONObject;

public class CommonSeekopUtilities {

    //////
    private String puerto = "";
    private String user = "";
    private String password = "";
    private String servidor = "";
    private static String dbDistribuidor = "";
    private static String dbMarca = "";
    private String dbGrupoCorporativo = "";
    private String idDistribuidor = "";
    private String idMarca = "";
    private String idGrupoCorporativo = "";
    private String nombreDistribuidor = "";
    private static String PoolDeConexion = "";
    private String emalEjecutivo = "";
    private String idPais = "";
    ConnectionManager connectionATI = null;
    ConnectionManager connectionDistribuidor = null;
    ConnectionManager connectionGrupoCorporativo = null;
    ConnectionManager connectionAux = null;
    //////
    private String calleDistribuidor = "";
    private String coloniaDistribuidor = "";
    private String cpDistribuidor = "";
    private String delegacionDistribuidor = "";
    private String ladaDistribuidor = "";
    private String telefonosDistribuidor = "";

    private final String json = "";
    private String mensaje = "";
    private int status = 1;
    private final String fechaHoy = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    String puertoAti = "3306";
    String userAti = "root";
    String passwordAti = "MAX.42C.MM6";
    String servidorAti = "192.168.0.47";
    boolean seminuevos = false;
    boolean apuntadoSeminuevo = false;

    public void setProduccion(boolean produccion) {
        if (produccion == true) {
            servidorAti = "192.168.0.47";
        } else {
            servidorAti = "192.168.2.14";
        }
    }

    private String token = "";
    private String idEjecutivo = "";
    private String nombreEjecutivo = "";
    private String paternoEjecutivo = "";
    private String maternoEjecutivo = "";
    private String generoEjecutivo = "";
    private String idProspecto = "";
    private String registro = "";
    private String placa = "";
    private String vin = "";
    private String destino = "";

    private static String idDevice = "";

    private String origen = "4";
    private String licencia = "S/D";
    private String email;
    private String poolNameBPM;
    private String idMarcaBPM;
    private String idDistribuidorBPM;
    private String baseNameBPM;

    public enum SearchType {
        BRAND,
        DEALER,
        BDC,
        ORIGINAL
    }
    
    
    public void getTokenInformation(String token) {
        String sql = "SELECT TOKEN, IDDISTRIBUIDOR, IDUSUARIO, ID, REGISTRO, VIN, PLACA, destino FROM sicopbdc.tokens WHERE TOKEN='" + token + "' or ID='" + token + "';";
        abrirConnectionAti();
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                this.token =  connectionATI.getString("TOKEN");
                this.idEjecutivo = connectionATI.getString("IDUSUARIO");
                this.idProspecto = connectionATI.getString("ID");
                this.registro = connectionATI.getString("REGISTRO");
                this.placa = validarvacio(connectionATI.getString("PLACA"), "");
                this.vin = validarvacio(connectionATI.getString("VIN"), "");
                this.destino = validarvacio(connectionATI.getString("destino"), "");
                abrirConnectionDistribuidor(connectionATI.getString("IDDISTRIBUIDOR"));
                buscarDatosProspecto(this.idProspecto);
            }
        } else {
            setErrorMensaje(connectionATI.getErrorMessage());
        }

    }

    public void getTokenInformationBPM(String token) {
        //String sql = "SELECT IDMARCA, IDDISTRIBUIDOR FROM sicopbdc.tokens WHERE TOKEN='" + token + "';";

        String sql = "SELECT "
                + "t.IDMARCA as IDMARCA, "
                + "t.IDDISTRIBUIDOR as IDDISTRIBUIDOR, "
                + "b.NOMBRE AS NOMBRE, "
                + "s.PoolDeConexion as POOlDECONEXION "
                + // Si existe este campo
                "FROM sicopbdc.tokens t "
                + "JOIN sicopbdc.BasesDeDatos b ON b.idBasededatos = t.IDDISTRIBUIDOR "
                + "JOIN sicopbdc.servidores s ON s.idservidor = b.idservidor "
                + "WHERE t.TOKEN ='" + token + "'";

        abrirConnectionAti();
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                this.idMarcaBPM = connectionATI.getString("IDMARCA");
                this.idDistribuidorBPM = connectionATI.getString("IDDISTRIBUIDOR");
                this.poolNameBPM = connectionATI.getString("POOlDECONEXION");
                this.baseNameBPM = connectionATI.getString("NOMBRE");
            }
        } else {
            setErrorMensaje(connectionATI.getErrorMessage());
        }

    }

    public boolean isMultiMarca(String idDsitribuidor) {
        boolean is = false;
        String sql = "SELECT \n"
                + "    IdDistribuidor, HabilitarMultimarca\n"
                + "FROM\n"
                + "    sicopdb.distribuidores\n"
                + "WHERE\n"
                + "    IdDistribuidor = '" + idDsitribuidor + "';";
        if (connectionDistribuidor.executeQuery(sql)) {
            if (connectionDistribuidor.next()) {
                if (connectionDistribuidor.getString("HabilitarMultimarca").equals("1")) {
                    is = true;
                }
            }
        } else {
            setErrorMensaje(connectionDistribuidor.getErrorMessage());
        }
        return is;
    }

    public void AbrirConnectionSeminuevos() {
        String idSeminuevos = "";
        ConnectionManager connectionAux = abrirConnection(getIdDistribuidor());
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
            if (connectionAux.next()) {
                idSeminuevos = connectionAux.getString("IdDistribuidorSemiNuevos");
                abrirConnectionDistribuidor(idSeminuevos);
            } else {
                if (connectionAux != null) {
                    connectionAux.close();
                }
                connectionAux = new ConnectionManager("com.mysql.jdbc.Driver", "jdbc:mysql://" + servidorAti + ":" + puertoAti + "/sicopbdc", userAti, passwordAti);

                sql = "SELECT \n"
                        + "    d2.iddistribuidor\n"
                        + "FROM\n"
                        + "    sicopdb.distribuidores d1\n"
                        + "        LEFT JOIN\n"
                        + "    sicopbdc.distribuidores d2 ON d1.iddistribuidor = d2.iddistribuidor\n"
                        + "WHERE\n"
                        + "    d1.EsSeminuevo = '1'\n"
                        + "        AND d1.HabilitarSeminuevos = '1'\n"
                        + "        AND d1.Activo = '1'\n"
                        + "        AND d2.iddistribuidor IS NOT NULL\n"
                        + "ORDER BY d1.Registro DESC;";
                if (connectionAux.executeQuery(sql)) {
                    while (connectionAux.next()) {
                        abrirConnectionDistribuidor(connectionAux.getString("iddistribuidor"));
                        if (getConnectionDistribuidor() != null) {
                            break;
                        }
                    }
                }
            }
        }
        if (connectionAux != null) {
            connectionAux.close();
        }
    }

    public void abrirConnectionAti() {
        if (connectionATI == null) {
            connectionATI = new ConnectionManager("com.mysql.jdbc.Driver", "jdbc:mysql://" + servidorAti + ":" + puertoAti + "/sicopbdc", userAti, passwordAti);
        }
    }

    public ConnectionManager abrirConnectionDistribuidor(String idDistribuidor) {
        connectionDistribuidor = null;
        cleanErrorMensaje();
        boolean statusAti = false;
        String sql = "SELECT a.Nombre As Servidor, a.PoolDeConexion, a.Puerto, a.User, a.Password, b.Nombre As BaseDeDatos, b.IdBaseDeDatos AS IdBaseDeDatos, "
                + "c.IdDistribuidor, d.nombre AS NombreMarca, d.IdMarca, c.nombre AS NombreDistribuidor, c.IdPais\n"
                + "FROM sicopbdc.Servidores a, sicopbdc.BasesDeDatos b, sicopbdc.Distribuidores c\n"
                + "LEFT JOIN sicopbdc.marcas d ON d.IdMarca=c.IdMarca\n"
                + "WHERE a.IdServidor = b.IdServidor AND b.IdBaseDeDatos = c.IdDistribuidor AND c.IdDistribuidor = '" + idDistribuidor + "' AND a.Activo <> 0 And c.Activo <> 0\n"
                + "ORDER BY c.IdDistribuidor;";
        abrirConnectionAti();
        grupoCorporativo(idDistribuidor);
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                this.puerto = connectionATI.getString("Puerto");
                this.user = connectionATI.getString("user");
                this.password = connectionATI.getString("password");
                this.servidor = connectionATI.getString("Servidor");
                this.dbDistribuidor = connectionATI.getString("BaseDeDatos");
                this.dbMarca = connectionATI.getString("NombreMarca");
                this.nombreDistribuidor = connectionATI.getString("NombreDistribuidor");
                this.idDistribuidor = connectionATI.getString("IdDistribuidor");
                this.idMarca = connectionATI.getString("IdMarca");
                this.idPais = connectionATI.getString("IdPais");
                connectionDistribuidor = new ConnectionManager("com.mysql.jdbc.Driver", "jdbc:mysql://" + this.servidor + ":" + this.puerto + "/sicopdb", this.user, this.password);
                statusAti = true;
                this.PoolDeConexion = connectionATI.getString("PoolDeConexion");
                if (PoolDeConexion.equals("HERACLESSERVER3306")) {
                    seminuevos = true;
                }
                sql = "SELECT \n"
                        + "    Calle, Colonia, CodigoPostal, Delegacion, Lada, Telefonos\n"
                        + "FROM\n"
                        + "    sicopdb.Distribuidores\n"
                        + "WHERE\n"
                        + "    IdDistribuidor = '" + this.idDistribuidor + "';";
                if (connectionDistribuidor.executeQuery(sql)) {
                    if (connectionDistribuidor.next()) {
                        this.calleDistribuidor = validarvacio(connectionDistribuidor.getString("Calle"), "");
                        this.coloniaDistribuidor = validarvacio(connectionDistribuidor.getString("Colonia"), "");
                        this.cpDistribuidor = validarvacio(connectionDistribuidor.getString("CodigoPostal"), "");
                        this.delegacionDistribuidor = validarvacio(connectionDistribuidor.getString("Delegacion"), "");
                        this.ladaDistribuidor = validarvacio(connectionDistribuidor.getString("Lada"), "");
                        this.telefonosDistribuidor = validarvacio(connectionDistribuidor.getString("Telefonos"), "");

                    }
                }
                sql = "SELECT \n"
                        + "    Calle, Colonia, CP, Delegacion, Lada, Telefonos\n"
                        + "FROM\n"
                        + "    sicopbdc.Distribuidores\n"
                        + "WHERE\n"
                        + "    IdDistribuidor = '" + this.idDistribuidor + "';";
                if (connectionATI.executeQuery(sql)) {
                    if (connectionATI.next()) {
                        this.calleDistribuidor = validarvacio(calleDistribuidor, connectionATI.getString("Calle"));
                        this.coloniaDistribuidor = validarvacio(coloniaDistribuidor, connectionATI.getString("Colonia"));
                        this.cpDistribuidor = validarvacio(cpDistribuidor, connectionATI.getString("CP"));
                        this.delegacionDistribuidor = validarvacio(delegacionDistribuidor, connectionATI.getString("Delegacion"));
                        this.ladaDistribuidor = validarvacio(ladaDistribuidor, connectionATI.getString("Lada"));
                        this.telefonosDistribuidor = validarvacio(telefonosDistribuidor, connectionATI.getString("Telefonos"));

                    }
                }

            } else {
                setErrorMensaje("No se encontro el distribuidor: " + idDistribuidor);

            }
        } else {
            setErrorMensaje(connectionATI.getErrorMessage());
        }
//        connectionATI.close();
        return connectionDistribuidor;
    }

    public void buscarEjecutivo(String idEjecutivo) {
        String sql = "SELECT \n"
                + "    IdEjecutivo, Nombre, Paterno, Materno, Genero\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".ejecutivos\n"
                + "WHERE\n"
                + "    IdEjecutivo = '" + idEjecutivo + "';";
        if (connectionDistribuidor.executeQuery(sql)) {
            if (connectionDistribuidor.next()) {
                this.idEjecutivo = idEjecutivo;
                this.nombreEjecutivo = validarvacio(connectionDistribuidor.getString("Nombre"), "");
                this.paternoEjecutivo = validarvacio(connectionDistribuidor.getString("Paterno"), "");
                this.maternoEjecutivo = validarvacio(connectionDistribuidor.getString("Materno"), "");
                this.generoEjecutivo = validarvacio(connectionDistribuidor.getString("Genero"), "");
            }
        }
    }

    String idauto = "";
    String nombreAuto = "";

    public void buscarDatosProspecto(String idProspecto) {
        String sql = "SELECT \n"
                + "    IdProspecto, Nombre, Paterno, Materno, idpropietario, activo, IdAuto\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".prospectos\n"
                + "WHERE\n"
                + "    idprospecto = '" + idProspecto + "';";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                this.idProspecto = validarvacio(getConnectionDistribuidor().getString("IdProspecto"), "");
                this.idauto = validarvacio(getConnectionDistribuidor().getString("IdAuto"), "");
                this.idEjecutivo = validarvacio(getConnectionDistribuidor().getString("idpropietario"), "");
                buscarAutoProspecto(idauto);
                buscarEjecutivo(idEjecutivo);
            } else {
                setErrorMensaje("No se encontraron datos para el prospecto '" + idProspecto + "'");
            }
        }
    }

    public void buscarAutoProspecto(String idAuto) {
        String sql = "SELECT \n"
                + "    Nombre\n"
                + "FROM\n"
                + "    sicopdb.autos\n"
                + "where idauto='" + idAuto + "';";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                this.nombreAuto = validarvacio(getConnectionDistribuidor().getString("Nombre"), "");
            } else {
                 sql = "SELECT \n"
                + "    Nombre\n"
                + "FROM\n"
                + "    sicopbdc.autos\n"
                + "where idauto='" + idAuto + "';";
                if (getConnectionDistribuidor().executeQuery(sql)) {
                    if (getConnectionDistribuidor().next()) {
                        this.nombreAuto = validarvacio(getConnectionDistribuidor().getString("Nombre"), "");
                    } else {
                        setErrorMensaje("No se encontraron datos para el prospecto '" + idProspecto + "'");
                    }
                }
            }
        }
    }

    public String buscarNombreProspecto(String idProspecto) {
        String nombre = "";
        String sql = "SELECT \n"
                + "    IdProspecto, Nombre, Paterno, Materno, idpropietario, activo\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".prospectos\n"
                + "WHERE\n"
                + "    idprospecto = '" + idProspecto + "';";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                return nombre = validarvacio(getConnectionDistribuidor().getString("Nombre"), "") + " " + validarvacio(getConnectionDistribuidor().getString("Paterno"), "") + " " + validarvacio(getConnectionDistribuidor().getString("Materno"), "");
            } else {
                setErrorMensaje("No se encontraron datos para el prospecto '" + idProspecto + "'");
                return "";
            }
        }
        return "";
    }

    public ConnectionManager abrirConnectionGrupoCorporativo(String idDistribuidor) {
        boolean statusAti = false;
        String sql = "SELECT a.Nombre As Servidor, a.PoolDeConexion, a.Puerto, a.User, a.Password, b.Nombre As BaseDeDatos, b.IdBaseDeDatos AS IdBaseDeDatos, "
                + "c.IdDistribuidor, d.nombre AS NombreMarca, d.IdMarca, c.nombre AS NombreDistribuidor, c.IdPais\n"
                + "FROM sicopbdc.Servidores a, sicopbdc.BasesDeDatos b, sicopbdc.Distribuidores c\n"
                + "LEFT JOIN sicopbdc.marcas d ON d.IdMarca=c.IdMarca\n"
                + "WHERE a.IdServidor = b.IdServidor AND b.IdBaseDeDatos = c.IdDistribuidor AND c.IdDistribuidor = '" + idDistribuidor + "' AND a.Activo <> 0 And c.Activo <> 0\n"
                + "ORDER BY c.IdDistribuidor;";
        abrirConnectionAti();
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                this.idGrupoCorporativo = connectionATI.getString("IdDistribuidor");
                this.dbGrupoCorporativo = connectionATI.getString("BaseDeDatos");
                connectionGrupoCorporativo = new ConnectionManager("com.mysql.jdbc.Driver", "jdbc:mysql://" + this.servidor + ":" + this.puerto + "/sicopdb", this.user, this.password);
            } else {
                setErrorMensaje("No se encontro el distribuidor: " + idDistribuidor);

            }
        } else {
            setErrorMensaje(connectionATI.getErrorMessage());
        }
//        connectionATI.close();
        return connectionGrupoCorporativo;
    }

    private String auxdbDistribuidor = "";
    private String auxdbMarca = "";
    private String auxidDistribuidor = "";
    private String auxidMarca = "";

    public ConnectionManager abrirConnection(String idDistribuidor) {
        boolean statusAti = false;
        ConnectionManager connectionAux = null;
        String sql = "SELECT a.Nombre As Servidor, a.PoolDeConexion, a.Puerto, a.User, a.Password, b.Nombre As BaseDeDatos, b.IdBaseDeDatos AS IdBaseDeDatos, "
                + "c.IdDistribuidor, d.nombre AS NombreMarca, d.IdMarca, c.nombre AS NombreDistribuidor, c.IdPais\n"
                + "FROM sicopbdc.Servidores a, sicopbdc.BasesDeDatos b, sicopbdc.Distribuidores c\n"
                + "LEFT JOIN sicopbdc.marcas d ON d.IdMarca=c.IdMarca\n"
                + "WHERE a.IdServidor = b.IdServidor AND b.IdBaseDeDatos = c.IdDistribuidor AND c.IdDistribuidor = '" + idDistribuidor + "' AND a.Activo <> 0 And c.Activo <> 0\n"
                + "ORDER BY c.IdDistribuidor;";
        abrirConnectionAti();
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                connectionAux = new ConnectionManager("com.mysql.jdbc.Driver", "jdbc:mysql://" + connectionATI.getString("Servidor")
                        + ":" + connectionATI.getString("Puerto")
                        + "/sicopdb", connectionATI.getString("User"), connectionATI.getString("Password"));
                this.auxdbDistribuidor = connectionATI.getString("BaseDeDatos");
                this.auxdbMarca = connectionATI.getString("NombreMarca");
                this.auxidDistribuidor = connectionATI.getString("IdDistribuidor");
                this.auxidMarca = connectionATI.getString("IdMarca");
            } else {
//                setErrorMensaje("No se encontro el distribuidor: " + idDistribuidor);

            }
        } else {
            setErrorMensaje(connectionATI.getErrorMessage());
        }
//        connectionATI.close();
        return connectionAux;
    }

    public void setConnectionAux(ConnectionManager connectionAux) {
        if (this.connectionAux != null) {
            this.connectionAux.close();
        }
        this.connectionAux = connectionAux;
    }

    public String traerValorConfiguracion(String proceso, String variable) {
        String valor = "";
        String sqlDistribuidor = "SELECT Valor FROM " + dbDistribuidor + ".Configuracion where Proceso = '" + proceso + "' AND Variable = '" + variable + "';";
        String sqlMarca = "SELECT Valor FROM " + dbMarca + ".Configuracion where Proceso = '" + proceso + "' AND Variable = '" + variable + "';";
        if (connectionDistribuidor.executeQuery(sqlDistribuidor)) {
            if (connectionDistribuidor.next()) {
                valor = validarvacio(connectionDistribuidor.getString("Valor"), "");
            } else if (connectionDistribuidor.executeQuery(sqlMarca)) {
                if (connectionDistribuidor.next()) {
                    valor = validarvacio(connectionDistribuidor.getString("Valor"), "");
                }
            } else {
                setErrorMensaje(connectionDistribuidor.getErrorMessage());
            }

        } else {
            setErrorMensaje(connectionDistribuidor.getErrorMessage());
        }
        return valor;
    }

    public String getIdUso(String id) {
        String nombre = "";
        String sql = "SELECT \n"
                + "    IdUso, Nombre\n"
                + "FROM\n"
                + "    sicopbdc.usoactividades\n"
                + "WHERE\n"
                + "    IdUso = '" + id + "' AND Activo = '1';";
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                nombre = validarvacio(connectionATI.getString("Nombre"), "");
            }
        }
        return nombre;
    }

    public String getZonaHoraria(String idDistribuidor) {
        String resultado = "";
        String sql = "SELECT \n"
                + "    ZonaHoraria\n"
                + "FROM\n"
                + "    sicopdb.distribuidores\n"
                + "WHERE\n"
                + "    IdDistribuidor = '" + idDistribuidor + "';";
        if (connectionDistribuidor.executeQuery(sql)) {
            if (connectionDistribuidor.next()) {
                resultado = validarvacio(connectionDistribuidor.getString("ZonaHoraria"), "");
            }
        }
        return resultado;
    }

    public String validarvacio(String primerCampo, String segundoCampo) {
        String texto = "";
        if (primerCampo == null) {
            primerCampo = "";
        }
        if (segundoCampo == null) {
            segundoCampo = "";
        }
        if (primerCampo.equals("") || primerCampo.toLowerCase().equals("null") || primerCampo.toLowerCase().equals("na")) {
            if (segundoCampo.equals("") || segundoCampo.toLowerCase().equals("null") || segundoCampo.toLowerCase().equals("na")) {
                texto = "";
            } else {
                texto = segundoCampo;
            }
        } else {
            texto = primerCampo;
        }
        return texto;
    }

    public void setErrorMensaje(String mensaje) {
        if (this.status == 1) {
            this.mensaje = mensaje;
            status = 0;
        }
    }

    public void cleanErrorMensaje() {
        this.mensaje = "";
        status = 1;
    }

    public String fechaFormato(String fecha, String formatoInicio, String formatoFin) {
        String fechaResultado = "";
        try {
            DateFormat formateadororigen = new SimpleDateFormat(formatoInicio);
            DateFormat formateadordestino = new SimpleDateFormat(formatoFin);
            Date fechaDate = formateadororigen.parse(fecha);
            fechaResultado = formateadordestino.format(fechaDate);
        } catch (ParseException ex) {
            setErrorMensaje("Error: " + ex.toString());
        }
        return fechaResultado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getStatus() {
        return status;
    }

    public String getDbDistribuidor() {
        return dbDistribuidor;
    }

    public String getDbMarca() {
        return dbMarca;
    }

    public ConnectionManager getConnectionDistribuidor() {
        return connectionDistribuidor;
    }

    public String getFechaHoy() {
        return fechaHoy;
    }

    public String getIdDistribuidor() {
        return idDistribuidor;
    }

    public String getIdMarca() {
        return idMarca;
    }

    public String getNombreDistribuidor() {
        return nombreDistribuidor;
    }

    public String getToken() {
        return token;
    }

    public String getIdEjecutivo() {
        return idEjecutivo;
    }

    public String getIdProspecto() {
        return idProspecto;
    }

    public String getRegistro() {
        return registro;
    }

    public ConnectionManager getConnectionATI() {
        return connectionATI;
    }

    public String getPoolDeConexion() {
        return PoolDeConexion;
    }

    public String getDestino() {
        return destino;
    }

    public String getIdauto() {
        return idauto;
    }

    public String getNombreAuto() {
        return nombreAuto;
    }

    public String getEmalEjecutivo() {
        if (emalEjecutivo.equals("") || emalEjecutivo == null || emalEjecutivo.toLowerCase().equals("null")) {
            emalEjecutivo = getEmail(idEjecutivo);
        }
        return emalEjecutivo;
    }

    public String getEmail(String idEjecutivo) {
        String mail = "";
        String sql = "SELECT \n"
                + "    IdUsuario, eMail\n"
                + "FROM\n"
                + "    sicopdb.usuarios\n"
                + "WHERE\n"
                + "    IdUsuario = '" + idEjecutivo + "';";
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                mail = connectionATI.getString("eMail");
            } else {
                if (connectionDistribuidor.executeQuery(sql)) {
                    if (connectionDistribuidor.next()) {
                        mail = connectionDistribuidor.getString("eMail");
                    }
                }
            }
        }

        return mail;
    }

    public String getEmailBDC() {
        String mail = "";
        String sql = "SELECT \n"
                + "    IdUsuario, eMail\n"
                + "FROM\n"
                + "    sicopbdc.usuarios\n"
                + "WHERE\n"
                + "    IdUsuario = '" + idEjecutivo + "';";
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                mail = connectionATI.getString("eMail");
            }
        }

        return mail;
    }

    public String getHabilitarSeminuevos(String idDistribuidor) {
        String dato = "";
        String sql = "SELECT \n"
                + "    HabilitarSeminuevos\n"
                + "FROM\n"
                + "    sicopdb.distribuidores\n"
                + "WHERE\n"
                + "    IdDistribuidor = '" + idDistribuidor + "';";
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                dato = connectionATI.getString("HabilitarSeminuevos");
            } else {
                if (connectionDistribuidor.executeQuery(sql)) {
                    if (connectionDistribuidor.next()) {
                        dato = connectionDistribuidor.getString("HabilitarSeminuevos");
                    }
                }
            }
        }

        return dato;
    }

    public void CloseConnection() {
        if (connectionDistribuidor != null) {
            connectionDistribuidor.close();
        }
        if (connectionATI != null) {
            connectionATI.close();
        }
        if (connectionGrupoCorporativo != null) {
            connectionGrupoCorporativo.close();
        }
        if (connectionAux != null) {
            connectionAux.close();
        }
    }

    public String getIdPais() {
        return idPais;
    }

    public boolean isSeminuevos() {
        return seminuevos;
    }

    public Connection getConnection() {
        try {
            // Implementa este método para devolver una conexión válida a tu base de datos
            // Por ejemplo:
            String url = "jdbc:mysql://" + this.servidor + ":" + this.puerto + "/sicopdb";
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            System.out.println("error=" + ex.toString());
            return null;
        }
    }

    public String getClaveCatalogoSeminuevos(String apostrofe) {
        String clave = "";
        if (isApuntadoSeminuevo()) {
            String tipoClave = traerValorConfiguracion("Valuacion", "HabilitarPreciosLibroAzul");
            String isDalton = traerValorConfiguracion("Habilitar", "EstatusInventario");

            if (isDalton.equals("1")) {
                clave = "And " + apostrofe + ".Clave IN ('DLTN') \n";
            } else {
                switch (tipoClave) {
                    case "1":
                        clave = "And " + apostrofe + ".Clave IN ('sbb') \n";
                        break;
                    case "2":
                        clave = "And " + apostrofe + ".Clave IN ('atm') \n";
                        break;
                    default:
                        clave = "And " + apostrofe + ".Clave NOT IN ('sbb', 'atm') \n";
                        break;
                }
            }

            if (getIdPais().equals("EC")) {
                clave = "And " + apostrofe + ".Clave IN ('LAE') \n";
            }
        }
        return clave;
    }

    public boolean isApuntadoSeminuevo() {
        return apuntadoSeminuevo;
    }

    public void setApuntadoSeminuevo(boolean apuntadoSeminuevo) {
        this.apuntadoSeminuevo = apuntadoSeminuevo;
    }

    private void grupoCorporativo(String idDistribuidor) {
        String sql = "SELECT \n"
                + "    dg.IdDistribuidor, dg.IdGrupoCorporativo, g.NombreBase\n"
                + "FROM\n"
                + "    sicopbdc.DistribuidoresGruposCorporativos dg\n"
                + "        LEFT JOIN\n"
                + "    sicopbdc.gruposcorporativos g ON dg.IdGrupoCorporativo = g.IdGrupoCorporativo\n"
                + "WHERE\n"
                + "    dg.IdDistribuidor = '" + idDistribuidor + "'\n"
                + "        AND g.Activo = '1';";
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                idGrupoCorporativo = validarvacio(connectionATI.getString("IdGrupoCorporativo"), "");
                dbGrupoCorporativo = validarvacio(connectionATI.getString("NombreBase"), "sicopdb");
            }
        }
    }

    public String getDbGrupoCorporativo() {
        return dbGrupoCorporativo;
    }

    public void setDbGrupoCorporativo(String dbGrupoCorporativo) {
        this.dbGrupoCorporativo = dbGrupoCorporativo;
    }

    public String getIdGrupoCorporativo() {
        return idGrupoCorporativo;
    }

    public void setIdGrupoCorporativo(String idGrupoCorporativo) {
        this.idGrupoCorporativo = idGrupoCorporativo;
    }

    public ConnectionManager getConnectionGrupoCorporativo() {
        return connectionGrupoCorporativo;
    }

    public void setConnectionGrupoCorporativo(ConnectionManager connectionGrupoCorporativo) {
        this.connectionGrupoCorporativo = connectionGrupoCorporativo;
    }

    public String getAuxdbDistribuidor() {
        return auxdbDistribuidor;
    }

    public void setAuxdbDistribuidor(String auxdbDistribuidor) {
        this.auxdbDistribuidor = auxdbDistribuidor;
    }

    public String getAuxdbMarca() {
        return auxdbMarca;
    }

    public void setAuxdbMarca(String auxdbMarca) {
        this.auxdbMarca = auxdbMarca;
    }

    public String getAuxidDistribuidor() {
        return auxidDistribuidor;
    }

    public void setAuxidDistribuidor(String auxidDistribuidor) {
        this.auxidDistribuidor = auxidDistribuidor;
    }

    public String getAuxidMarca() {
        return auxidMarca;
    }

    public void setAuxidMarca(String auxidMarca) {
        this.auxidMarca = auxidMarca;
    }

    public ConnectionManager getConnectionAux() {
        return connectionAux;
    }

    public String getCalleDistribuidor() {
        return calleDistribuidor;
    }

    public String getColoniaDistribuidor() {
        return coloniaDistribuidor;
    }

    public String getCpDistribuidor() {
        return cpDistribuidor;
    }

    public String getDelegacionDistribuidor() {
        return delegacionDistribuidor;
    }

    public String getLadaDistribuidor() {
        return ladaDistribuidor;
    }

    public String getTelefonosDistribuidor() {
        return telefonosDistribuidor;
    }

    public String getNombreEjecutivo() {
        return nombreEjecutivo;
    }

    public String getPaternoEjecutivo() {
        return paternoEjecutivo;
    }

    public String getMaternoEjecutivo() {
        return maternoEjecutivo;
    }

    public String getGeneroEjecutivo() {
        return generoEjecutivo;
    }

    public String getPlaca() {
        return placa;
    }

    public String getVin() {
        return vin;
    }

    public String getNombreCompletoEjecutivo() {
        String nombreCompleto = "";
        if (!getNombreEjecutivo().equals("")) {
            nombreCompleto += getNombreEjecutivo();
        }
        if (!getPaternoEjecutivo().equals("")) {
            if (!nombreCompleto.equals("")) {
                nombreCompleto += " ";
            }
            nombreCompleto += getPaternoEjecutivo();
        }
        if (!getMaternoEjecutivo().equals("")) {
            if (!nombreCompleto.equals("")) {
                nombreCompleto += " ";
            }
            nombreCompleto += getMaternoEjecutivo();
        }
        return nombreCompleto;
    }

    public String getIdSeminuevos(String idDistribuidor) {
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

    public String getNombreSeminuevos(String idDistribuidor) {
        String idSeminuevos = getIdSeminuevos(idDistribuidor);
        String nombre = "";
        String sql = "SELECT \n"
                + "Nombre\n"
                + "FROM\n"
                + getDbGrupoCorporativo() + ".basesdedatos\n"
                + "WHERE\n"
                + "IdBaseDeDatos = '" + idSeminuevos + "'";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                nombre = getConnectionDistribuidor().getString("Nombre");
            }
        }
        return nombre;
    }

    public String buscarValuador(String idEjecutivo) {
        String valuador = "";
        String sql = "SELECT \n"
                + "    IdEjecutivo, Nombre, Paterno, Materno, Genero\n"
                + "FROM\n"
                + "    " + getAuxdbDistribuidor() + ".ejecutivos\n"
                + "WHERE\n"
                + "    IdEjecutivo = '" + idEjecutivo + "';";
        if (getConnectionAux().executeQuery(sql)) {
            if (getConnectionAux().next()) {
                valuador = validarvacio(getConnectionAux().getString("Nombre"), "") + " " + validarvacio(getConnectionAux().getString("Paterno"), "") + " " + validarvacio(getConnectionAux().getString("Materno"), "");
            }
        }
        return valuador;
    }

    public static String getParametroDeSistema(String process, String variable, SearchType searchType, boolean searchByProcess) {
        String result = "";

        if (searchType == SearchType.ORIGINAL) {
            result = getParametroDeSistema(process, variable, SearchType.DEALER, searchByProcess);

            if (result == null || result.isEmpty()) {
                result = getParametroDeSistema(process, variable, SearchType.BRAND, searchByProcess);
            }

            if (result == null || result.isEmpty()) {
                result = getParametroDeSistema(process, variable, SearchType.BDC, searchByProcess);
            }

            return result;
        }

        DataSource datasource = null;
        StringBuilder builder = new StringBuilder();
        builder.append("Select Valor From ");

        if (null != searchType) {
            switch (searchType) {
                case BRAND:
                    builder.append(dbMarca).append(" ");
                    datasource = ConnectionManager.getDatasource(PoolDeConexion);
                    break;
                case DEALER:
                    builder.append(dbDistribuidor).append(" ");
                    datasource = ConnectionManager.getDatasource(PoolDeConexion);
                    break;
                case BDC:
                    builder.append(ConnectionManager.getBDCDatabaseName()).append(" ");
                    datasource = ConnectionManager.getDatasource(ConnectionManager.getBDCPoolName());
                    break;
                default:
                    break;
            }
        }

        builder.append(".Configuracion Where ");

        if (searchByProcess && variable != null) {
            builder.append("Proceso = ? AND Variable = ? ");
        } else if (searchByProcess) {
            builder.append("Proceso = ? ");
        } else {
            builder.append("Variable = ? ");
        }

        assert datasource != null;

        try (Connection connection = datasource.getConnection(); PreparedStatement statement = connection.prepareStatement(builder.toString())) {

            if (searchByProcess && variable != null) {
                statement.setString(1, process);
                statement.setString(2, variable);
            } else if (searchByProcess) {
                statement.setString(1, process);
            } else {
                statement.setString(1, variable);
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    result = rs.getString("Valor");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    protected void sendDispositionRealTime(String idActivity, String idDealer, String idProspect, Map<String, Object> parameters) {
        if ("1".equals(getParametroDeSistema("Habilitar", "DispositionsRealTime", SearchType.ORIGINAL, true))) {
            try {

                String urlDispositions = getParametroDeSistema("Dispositions", "urlDispositions", SearchType.ORIGINAL, true);

                if (urlDispositions.isEmpty()) {
                    urlDispositions = "https://qa.sicopweb.com/Api/Bot/sendDisposition";
                }

                URL url = new URL(urlDispositions);

                LocalDateTime date = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String dateFormatter = date.format(formatter);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("disposition", idActivity);
                bodyMap.put("idDistribuidor", idDealer);
                bodyMap.put("idProspecto", idProspect);
                bodyMap.put("origen", "BOT-CLIENTE");
                bodyMap.put("atencion_inicial", "BOT-IA");
                bodyMap.put("generado_por", "CLIENTE");

                if (parameters == null || !parameters.containsKey("fecha")) {
                    bodyMap.put("fecha", dateFormatter); // formato: 2024-08-28 12:00:00
                }

                if (parameters != null) {
                    bodyMap.putAll(parameters);
                }

                Gson gson = new Gson();
                String json = gson.toJson(bodyMap);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                    os.close();
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        //NOTE: DEJAR POR SI NECESITO DEBUGEAR
                        /*try {
                        Map<String, Object> responseMap = gson.fromJson(response.toString(), Map.class);
                        System.out.println("Respuesta: " + responseMap);
                        } catch (Exception e) {
                        e.printStackTrace();
                        }*/
                    }
                }
                //NOTE:DEJAR POR SI NECESITO DEBUGEAR
                /*else {
                    // Leer la respuesta de error del servidor
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line.trim());
                    }
                    System.out.println("Respuesta de error: " + errorResponse.toString());
                }*/

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String dateFormatter(String fecha, String formato, String from) {
        return dateFormatter(fecha, formato, from, true, Locale.ENGLISH);
    }

    public String dateFormatter(String fecha, String formato, String from, boolean largeFormat, Locale localeFrom) {
        String fechaF = fecha;
        boolean today = false;

        try {
            if (from.equals("")) {
                from = "yyyy-MM-dd HH:mm:ss.S";
            } else {
                today = true;
            }

            Calendar date = Calendar.getInstance();

            try {
                date.setTime(new SimpleDateFormat(from, localeFrom).parse(fecha));
            } catch (Exception e) {
                date.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", localeFrom).parse(fecha));
            }

            if (formato.equals("")) {
                Calendar calendar = Calendar.getInstance();

                long startTime = date.getTimeInMillis();
                long endTime = calendar.getTimeInMillis();
                double diffTime = endTime - startTime;
                double diffDays = diffTime / (1000.0 * 60.0 * 60.0 * 24.0);

                if (Math.ceil(Math.abs(diffDays)) >= 6) {
                    formato = largeFormat ? "dd/MM/yyyy 'a' 'las' HH:mm" : "dd/MM/yyyy";
                } else {
                    int diaSemana = date.get(Calendar.DAY_OF_WEEK);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int timeDiff = dayOfWeek - diaSemana;

                    if (date.compareTo(calendar) > 0) {
                        if (diaSemana > dayOfWeek) {
                            timeDiff = dayOfWeek - diaSemana;
                        } else {
                            timeDiff = diaSemana - dayOfWeek;
                        }
                    } else {
                        if (diaSemana < dayOfWeek) {
                            timeDiff = dayOfWeek - diaSemana;
                        } else {
                            timeDiff = diaSemana - dayOfWeek;
                        }
                    }
                    switch (timeDiff) {
                        case -2:
                        case -3:
                        case -4:
                        case -5:
                        case -6:
                            formato = largeFormat ? "'Pr\u00F3ximo' EEEE 'a' 'las' HH:mm" : "'Pr\u00F3ximo' EEEE";
                            break;
                        case -1:
                            formato = largeFormat ? "'Ma\u00F1ana' 'a' 'las' HH:mm" : "'Ma\u00F1ana'";
                            break;
                        case 0:
                            formato = largeFormat ? "'Hoy' 'a' 'las' HH:mm" : today ? "'Hoy'" : "HH:mm";
                            break;
                        case 1:
                            formato = largeFormat ? "'Ayer' 'a' 'las' HH:mm" : "'Ayer'";
                            break;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            formato = largeFormat ? "EEEE dd 'de' MMMM 'a' 'las' HH:mm" : "EEEE";
                            break;
                        default:
                            formato = largeFormat ? "dd/MM/yyyy 'a' 'las' HH:mm" : "dd/MM/yyyy";
                            break;
                    }
                }
            }

            final SimpleDateFormat newFormat = new SimpleDateFormat(formato, new Locale("ES", "MX"));
            fechaF = newFormat.format(date.getTime());
        } catch (Exception e) {
            fechaF = fecha;
        }

        return fechaF;
    }

    public Boolean sendDispositionValuation(String idValuacion, String activityId, Boolean tienePropuesta, Map<String, Object> parametersAux) {

        Map<String, Object> parameters = new HashMap<>();
        String propuesta = "0";

        if (tienePropuesta) {
            propuesta = getPropuestaValuacion(idValuacion);
        }

        parameters.put("idvaluacion", idValuacion);
        parameters.put("precio_valuacion", tienePropuesta ? propuesta : null);
        parameters.put("fecha", getFechaHoy());

        if (parametersAux != null && !parametersAux.isEmpty()) {
            parameters.putAll(parametersAux);
        }

        sendDispositionRealTime(activityId, getIdDistribuidor(), getIdProspecto(), parameters);

        return true;
    }

    public String getPropuestaValuacion(String idValuacion) {
        String propuestaActual = "0";

        String baseSeminuevos = getNombreSeminuevos(getIdDistribuidor());
        AbrirConnectionSeminuevos();

        String sql = "SELECT \n"
                + "Propuesta1, Propuesta2, Propuesta3, Propuesta4, Propuesta5\n"
                + "FROM\n"
                + baseSeminuevos + ".valuacionprecios\n"
                + "WHERE\n"
                + "IdValuacion = '" + idValuacion + "'";

        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                if (!"0".equals(getConnectionDistribuidor().getString("Propuesta5"))) {
                    propuestaActual = getConnectionDistribuidor().getString("Propuesta5");
                } else if (!"0".equals(getConnectionDistribuidor().getString("Propuesta4"))) {
                    propuestaActual = getConnectionDistribuidor().getString("Propuesta4");
                } else if (!"0".equals(getConnectionDistribuidor().getString("Propuesta3"))) {
                    propuestaActual = getConnectionDistribuidor().getString("Propuesta3");
                } else if (!"0".equals(getConnectionDistribuidor().getString("Propuesta2"))) {
                    propuestaActual = getConnectionDistribuidor().getString("Propuesta2");
                } else if (!"0".equals(getConnectionDistribuidor().getString("Propuesta1"))) {
                    propuestaActual = getConnectionDistribuidor().getString("Propuesta1");
                }
            }

        }
        getTokenInformation(token);

        int numeroSinDecimales = (int) Double.parseDouble(propuestaActual);
        String resultado = Integer.toString(numeroSinDecimales);

        return resultado;
    }

    protected void sendNotification(String id, String idEjecutivo, String titulo, String mensaje, JSONObject dataObject) {
        try {

            String urlNotifications = traerValorConfiguracion("Notificaciones", "UrlNotificaciones");

            URL url = new URL(urlNotifications.isEmpty() ? "https://bdc.sicopweb.com/xml/servicios/30/resources/fcm/send" : urlNotifications);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");

            String token = generateMD5Code(false);
            conn.setRequestProperty("Token", token);

            conn.setDoOutput(true);

            // Crear el objeto JSON principal
            JSONObject jsonObject = new JSONObject();

            // Agregar "data" al objeto principal
            if (dataObject.length() != 0) {
                jsonObject.put("data", dataObject);
            }
            jsonObject.put("id", id);
            jsonObject.put("title", titulo);
            jsonObject.put("body", mensaje);
            jsonObject.put("userId", idEjecutivo);
            jsonObject.put("referenceId", "");

            // Convertir el objeto JSON a String
            String jsonString = jsonObject.toString();

            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> jsonMap = gson.fromJson(jsonString, mapType);

            String json = gson.toJson(jsonMap);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes("UTF-8");
                os.write(input, 0, input.length);
                os.close();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    //NOTE: DEJAR POR SI NECESITO DEBUGEAR
                    try {
                        Map<String, Object> responseMap = gson.fromJson(response.toString(), Map.class);
                        System.out.println("Respuesta: " + responseMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } //NOTE:DEJAR POR SI NECESITO DEBUGEAR
            else {
                // Leer la respuesta de error del servidor
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line.trim());
                }
                System.out.println("Respuesta de error: " + errorResponse.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String generateMD5Code(boolean strict) {

        try {
            String s = "sicop.";
            if (strict) {
                s = s + idMarca + ".";
                s = s + idDistribuidor + ".";
                s = s + idDevice + ".";
            }

            s = s + GregorianCalendar.getInstance().get(1) + ".";
            s = s + (GregorianCalendar.getInstance().get(2) + 1) + ".";
            s = s + GregorianCalendar.getInstance().get(5);

            MessageDigest digest = MessageDigest.getInstance("MD5");
            char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            byte[] bytes = digest.digest(s.getBytes());
            StringBuilder MD5Temp = new StringBuilder(2 * bytes.length);

            for (int i = 0; i < bytes.length; i++) {
                int bajo = bytes[i] & 0xF;
                int alto = (bytes[i] & 0xF0) >> 4;
                MD5Temp.append(hex[alto]);
                MD5Temp.append(hex[bajo]);
            }

            return MD5Temp.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("getMD5HashCode: " + e.toString());
        }

        return null;
    }

    public static String formatTimestamp(String originalTimestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = inputFormat.parse(originalTimestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getIdCheckList(String idValuacion, String idProspecto, String distribuidor, boolean openConection) {

        String idCheckList = "";
        if (openConection) {
            AbrirConnectionSeminuevos();
        }

        String sql = "SELECT \n"
                + "IdChecklist\n"
                + "FROM\n"
                + distribuidor + ".checklistlegal\n"
                + "WHERE\n"
                + "IdValuacion = '" + idValuacion + "'\n"
                + " AND IdProspecto = '" + idProspecto + "'";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                idCheckList = getConnectionDistribuidor().getString("IdChecklist");
            }
        }
        if (openConection) {
            getTokenInformation(token);
        }
        return idCheckList;
    }

    public static String capitalize(String str) {
        String[] words = removeMultipleSpaces(str).split(" ");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                capitalized.append(Character.toUpperCase(word.charAt(0)));
                capitalized.append(word.substring(1).toLowerCase());
                capitalized.append(" ");
            }
        }

        return capitalized.toString().trim();
    }

    public static String removeMultipleSpaces(String str) {
        return str.replaceAll("\\s+", " ").trim();
    }

    public void bpmReview(String idSeguimiento, String revision) {
        bpmReview(idSeguimiento, revision, 1);
    }

    public void bpmReview(String idReferencia, String revision, int intento) {
        String parametros = "&IdSeguimiento=" + idReferencia + "&Revisar=" + revision;

        email = getEmailBDC();
        idEjecutivo = getIdEjecutivo();
        getTokenInformationBPM(token);

        if ("5".equals(revision)) {
            parametros = "&IdProspecto=" + idReferencia + "&Revisar=" + revision;
        }

        parametros += "&Origen=" + origen;

        String parametroSistema = getParametroDeSistema("URL", "BPM", SearchType.ORIGINAL, true);

        String url = parametroSistema + "?IdDistribuidor=" + idDistribuidorBPM + "&PoolName=" + poolNameBPM + "&IdMarca=" + idMarcaBPM + "&eMail=" + email + "&RegistryId=" + idEjecutivo + "&Token=" + getMD5Code() + parametros;
        insertBitacoraDetonacionMS(url, idReferencia, revision);

        String msj = requestURL(url), msjFormat;
        boolean finish = false;

        if (msj.contains("actividades") || msj.contains("Seguimiento procesado con")) {
            msjFormat = msj.replace("valid: false, status: {{actividades: [", "").replace("]}}", "").replace("\"", "").replace("valid: false, status: {{actividades: [", "");
            getTokenJson(msjFormat);
            finish = true;
        } else if (intento < 5) {
            bpmReview(idReferencia, revision, intento + 1);
        } else {
            finish = true;
        }

        if (finish) {
            try {
                abrirConnectionDistribuidor(idDistribuidorBPM);
                String query = "UPDATE " + baseNameBPM + ".bitacoradetonacionms SET Respuesta = '" + msj + "'  WHERE Revisar = '" + revision + "' AND Respuesta = '' AND URL = '" + url + "' AND IdSeguimiento = '" + idReferencia + "';";
                if (!getConnectionDistribuidor().execute(query, false)) {
                    setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
                }
            } catch (Exception e) {

            } finally {

            }
        }
    }

    protected void insertBitacoraDetonacionMS(String url, String idSeguimiento, String revision) {
        try {
            abrirConnectionDistribuidor(idDistribuidorBPM);
            String query = "INSERT INTO " + baseNameBPM + ".bitacoradetonacionms (Fecha, URL, Origen, IdSeguimiento, Respuesta, Revisar) VALUES ('" + getCurrentSQLDateTime() + "', '" + url + "', '" + origen + "', '" + idSeguimiento + "', '', '" + revision + "');";
            if (!getConnectionDistribuidor().execute(query, false)) {
                setErrorMensaje("Error= " + getConnectionDistribuidor().getErrorMessage());
            }
        } catch (Exception e) {

        } finally {

        }
    }

    private String getTokenJson(String var) {
        String token = "";
        StringTokenizer st;
        st = new StringTokenizer(var, ",");
        boolean ban = true;
        while (st.hasMoreTokens()) {
            if (ban) {
                token = st.nextToken();
            }
        }
        return token;
    }

    protected String requestURL(String URL) {
        return requestURL(URL, null, null);
    }

    protected String requestURL(String URL, String content, String soapAction) {
        String responseText;
        try {
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setDefaultUseCaches(false);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml; charset=ISO-8859-1");
            connection.setRequestProperty("Charset", "ISO-8859-1");

            if (soapAction != null) {

                connection.setRequestProperty("SOAPAction", soapAction);
            }

            connection.setUseCaches(false);

            if (content != null) {

                BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "ISO-8859-1"));

                wr.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + content);
                wr.flush();
                wr.close();
            }

            try {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());

                if (connection.getResponseCode() == 200) {
                    BufferedReader buffered = new BufferedReader(reader);
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = buffered.readLine()) != null) {
                        builder.append(line);
                    }
                    responseText = builder.toString();
                } else {
                    responseText = "Error con status " + connection.getResponseCode();
                }

            } catch (IOException e) {
                responseText = "Error con status " + connection.getResponseCode();
            }

            connection.disconnect();
        } catch (IOException e) {
            responseText = "Error en el procesamiento";
        }

        return responseText;
    }

    public String getCurrentSQLDateTime() {
        FormatManager fm = new FormatManager();
        return fm.getCurrentSQLDateTime();
    }

    public final String getMD5Code() {
        return getMD5Code(idEjecutivo);
    }

    public final String getMD5Code(String registryId) {
        String passwordInterface = "";
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"));
            String str = "sicop.MX00000001.";
            str += idMarcaBPM + ".";
            str += email + ".";
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

}

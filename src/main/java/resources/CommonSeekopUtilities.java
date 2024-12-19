package resources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import resources.ConnectionManager;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonSeekopUtilities {

    //////
    private String puerto = "";
    private String user = "";
    private String password = "";
    private String servidor = "";
    private String dbDistribuidor = "";
    private String dbMarca = "";
    private String dbGrupoCorporativo = "";
    private String idDistribuidor = "";
    private String idMarca = "";
    private String idGrupoCorporativo = "";
    private String nombreDistribuidor = "";
    private String PoolDeConexion = "";
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

    private String json = "";
    private String mensaje = "";
    private int status = 1;
    private String fechaHoy = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
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

    public void getTokenInformation(String token) {
        String sql = "SELECT TOKEN, IDDISTRIBUIDOR, IDUSUARIO, ID, REGISTRO FROM sicopbdc.tokens WHERE TOKEN='" + token + "';";
        abrirConnectionAti();
        if (connectionATI.executeQuery(sql)) {
            if (connectionATI.next()) {
                this.token = token;
                this.idEjecutivo = connectionATI.getString("IDUSUARIO");
                this.idProspecto = connectionATI.getString("ID");
                this.registro = connectionATI.getString("REGISTRO");
                abrirConnectionDistribuidor(connectionATI.getString("IDDISTRIBUIDOR"));
                buscarDatosProspecto(this.idProspecto);
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
                if (connectionAux!=null) {
                    connectionAux.close();
                }
                connectionAux=new ConnectionManager("com.mysql.jdbc.Driver", "jdbc:mysql://" + servidorAti + ":" + puertoAti + "/sicopbdc", userAti, passwordAti);
       
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
                        if (getConnectionDistribuidor()!=null) {
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
        connectionDistribuidor=null;
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
                PoolDeConexion = connectionATI.getString("PoolDeConexion");
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

    public void buscarDatosProspecto(String idProspecto) {
        String sql = "SELECT \n"
                + "    IdProspecto, Nombre, Paterno, Materno, idpropietario, activo\n"
                + "FROM\n"
                + "    " + getDbDistribuidor() + ".prospectos\n"
                + "WHERE\n"
                + "    idprospecto = '" + idProspecto + "';";
        if (getConnectionDistribuidor().executeQuery(sql)) {
            if (getConnectionDistribuidor().next()) {
                this.idProspecto = validarvacio(getConnectionDistribuidor().getString("IdProspecto"), "");
                buscarEjecutivo(validarvacio(getConnectionDistribuidor().getString("idpropietario"), ""));
            } else {
                setErrorMensaje("No se encontraron datos para el prospecto '" + idProspecto + "'");
            }
        }
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
        if ( this.connectionAux!=null) {
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

}

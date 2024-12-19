package com.seekop.seekop.resources.resources.mobile;

import com.sicop.web.common.xml.XMLParser;
import com.sicop.web.common.xml.XMLStatus;
import com.sicop.web.common.xml.XMLTag;
import com.sicop.web.common.xml.constants.DataTypeConstants;
import com.sicop.web.common.xml.constants.StateConstants;
import com.sicop.web.common.xml.constants.StateConstants.StateType;
import com.sicop.web.common.xml.services.XMLServiceTable;
import com.sicop.web.common.xml.services.XMLServiceTables;
import org.w3c.dom.NodeList;

public class XMLService extends XMLParser implements DataTypeConstants, StateConstants {
  
  private String idInterface;
  private String idDistribuidor;
  
  private String idMarca;
  private String eMail;
  private String registryId;
  private String password;
  private String poolname;
  
  private XMLStatus status;
  
  public XMLServiceTables tables;
  
  private boolean notAdded = true;

  public XMLService() {
    super("service");
    status = new XMLStatus("Error al procesar la peticion", StateType.Incorrect, true);
    tables = new XMLServiceTables();
  }

  public XMLService(String idInterface, String registryId, String idDistribuidor, String password) {
    this();
    this.idInterface = idInterface;
    this.registryId = registryId;
    this.idDistribuidor = idDistribuidor;
    this.password = password;
  }
  
  public XMLServiceTable addTable(String name) {
    return tables.addTable(name);
  }
  
  public XMLServiceTable getTable(int index) {
    return (XMLServiceTable)tables.get(index);
  }
  
  public XMLServiceTable getTable(String name) {
    XMLServiceTable table;
    for(int t = 0; t < tables.size(); t++) {
      table = (XMLServiceTable)tables.get(t);
      if(table.getName().equalsIgnoreCase(name)) {
        return table;
      }
    }
    return null;
  }
  
  public int getTableCount() {
    return tables.size();
  }

  @Override
  public void parse(String XML) {
    super.parse(XML);
    if(main==null) {
      status.setMessage("XML mal formado");
    } else {
      idMarca = getNodeContent(main, "IdMarca");
      idDistribuidor = getNodeContent(main, "IdDistribuidor");
      idInterface = getNodeContent(main, "IdInterface");
      eMail = getNodeContent(main, "eMail");
      registryId = getNodeContent(main, "RegistryId");
      password = getNodeContent(main, "Password");
      poolname = getNodeContent(main, "PoolName");
      NodeList list = getNodeCollection(main, "table");
      for(int i = 0; i < list.getLength(); i++) {
        tables.addTable(list.item(i));
      }
      status.setState(StateType.Correct);
      status.setMessage("XML procesado correctamente");
    }
  }

  @Override
  public String getXML() {
    if(notAdded) {
      notAdded = false;
      if(idMarca!=null && idMarca.length()>0) {
        super.add(new XMLTag("IdMarca", null, null, idMarca));
      }
      if(idDistribuidor!=null && idDistribuidor.length()>0) {
        super.add(new XMLTag("IdDistribuidor", null, null, idDistribuidor));
      }
      if(idInterface!=null && idInterface.length()>0) {
        super.add(new XMLTag("IdInterface", null, null, idInterface));
      }
      if(eMail!=null && eMail.length()>0) {
        super.add(new XMLTag("eMail", null, null, eMail));
      }
      if(registryId!=null && registryId.length()>0) {
        super.add(new XMLTag("RegistryId", null, null, registryId));
      }
      if(password!=null && password.length()>0) {
        super.add(new XMLTag("Password", null, null, password));
      }
      if(poolname!=null && poolname.length()>0) {
        super.add(new XMLTag("PoolName", null, null, poolname));
      }
      if(status!=null) {
        super.add(status);
      }
      super.add(tables);
    }
    return super.getXML(); 
  }

  public XMLStatus getStatus() {
    return status;
  }

  public String getIdMarca() {
    return idMarca;
  }

  public void setIdMarca(String idMarca) {
    this.idMarca = idMarca;
  }

  public void setIdDistribuidor(String idDistribuidor) {
    this.idDistribuidor = idDistribuidor;
  }

  public String getIdDistribuidor() {
    return idDistribuidor==null ? "" : idDistribuidor;
  }

  public String geteMail() {
    return eMail;
  }

  public void seteMail(String eMail) {
    this.eMail = eMail;
  }

  public void setIdInterface(String idInterface) {
    this.idInterface = idInterface;
  }

  public String getIdInterface() {
    return idInterface==null ? "" : idInterface;
  }

  public void setRegistryId(String registryId) {
    this.registryId = registryId;
  }

  public String getRegistryId() {
    return registryId==null ? "" : registryId;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword() {
    return password==null ? "" : password;
  }

  public void setPoolname(String poolname) {
    this.poolname = poolname;
  }

  public String getPoolname() {
    return poolname;
  }
  
}

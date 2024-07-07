package org.sola.admin.services.ejb.system.repository.entities;

import jakarta.persistence.Column;
import org.sola.common.ConfigConstants;
import org.sola.services.common.repository.entities.AbstractEntity;

public class DbInfo extends AbstractEntity {
    
    public static final String QUERY = 
            "select system.get_setting('" + ConfigConstants.PRODUCT_NAME + "') as product_name, "
            + "system.get_setting('" + ConfigConstants.PRODUCT_CODE + "') as product_code, "
            + "current_database()::text as db_name, host(inet_client_addr()) as ip, inet_server_port()::text as port";
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "product_code")
    private String productCode;
    
    @Column(name = "db_name")
    private String dbName;
    
    @Column(name = "ip")
    private String ip;
    
    @Column(name = "port")
    private String port;
    
    public DbInfo(){
        super();
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}

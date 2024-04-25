/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.clients.resultSet2XML;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;

/**
 *
 * @author User
 */
public class ModelClientAddress {
    public static void main (String [] args){
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);

        GRider instance = new GRider("gRider");

        if (!instance.logUser("gRider", "M001000001")){
            System.err.println(instance.getErrMsg());
            System.exit(1);
        }

        System.out.println("Connected");
        
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/Model_Client_Address.xml");
        
        
        String lsSQL = "SELECT " +
                        "  a.sAddrssID, " +
                        "  a.sClientID, " +
                        "  a.sHouseNox, " +
                        "  a.sAddressx, " +
                        "  a.sBrgyIDxx, " +
                        "  a.sTownIDxx, " +
                        "  a.nLatitude, " +
                        "  a.nLongitud, " +
                        "  a.cPrimaryx, " +
                        "  a.cRecdStat, " +
                        "  a.dModified, " +
                        "  b.sTownName    xTownName, " +
                        "  d.sBrgyName    xBrgyName, " +
                        "  c.sProvName    xProvName " +
                        "FROM Client_Address a " +
                        "  LEFT JOIN TownCity b " +
                        "    ON a.sTownIDxx = b.sTownIDxx " +
                        "  LEFT JOIN Province c " +
                        "    ON b.sProvIDxx = c.sProvIDxx " +
                        "  LEFT JOIN Barangay d " +
                        "    ON a.sBrgyIDxx = d.sBrgyIDxx " +
                        "WHERE 0=1";
        
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        try {
            if (MiscUtil.resultSet2XML(instance, loRS, System.getProperty("sys.default.path.metadata"), "Client_Address", "")){
                System.out.println("ResultSet exported.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}

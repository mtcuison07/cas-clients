/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.cas.clients.resultSet2XML;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;

/**
 *
 * @author User
 */
public class ModelAPClientMaster {
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
        
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/Model_AP_Client_Master.xml");
        
        
        String lsSQL = "SELECT" +
                        "  a.sClientID" +
                        ", a.sAddrssID" +
                        ", a.sContctID" +
                        ", a.sCategrCd" +
                        ", a.dCltSince" +
                        ", a.dBegDatex" +
                        ", a.nBegBalxx" +
                        ", a.sTermIDxx" +
                        ", a.nDiscount" +
                        ", a.nCredLimt" +
                        ", a.nABalance" +
                        ", a.nOBalance" +
                        ", a.nLedgerNo" +
                        ", a.cVatablex" +
                        ", a.cRecdStat" +
                        ", a.sModified" +
                        ", a.dModified" +
                        ", b.sCompnyNm xClientNm" +
                        ", TRIM(CONCAT(c.sHouseNox, ' ', c.sAddressx, ', ', g.sBrgyName, ' ', h.sTownName, ', ', i.sProvName)) xAddressx" +
                        ", d.sCPerson1 xCPerson1" +
                        ", d.sCPPosit1 xCPPosit1" +
                        ", e.sDescript xCategrNm" +
                        ", f.sDescript xTermName" +
                        ", b.sTaxIDNox xTaxIDNox" +
                        ", d.sMobileNo xMobileNo" +
                    " FROM AP_Client_Master a" +
                        " LEFT JOIN Client_Master b ON a.sClientID = b.sClientID" +
                        " LEFT JOIN Client_Address c" + 
                            " LEFT JOIN Barangay  g ON c.sBrgyIDxx = g.sBrgyIDxx" +
                            " LEFT JOIN TownCity h ON c.sTownIDxx = h.sTownIDxx" +
                            " LEFT JOIN Province i ON h.sProvIDxx = i.sProvIDxx" +
                        " ON a.sAddrssID = c.sAddrssID" +
                        " LEFT JOIN Client_Institution_Contact_Person d ON a.sContctID = d.sContctID" +
                        " LEFT JOIN Category e ON a.sCategrCd = e.sCategrCd" +
                        " LEFT JOIN Term f ON a.sTermIDxx = f.sTermCode";
        
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        try {
            if (MiscUtil.resultSet2XML(instance, loRS, System.getProperty("sys.default.path.metadata"), "AP_Client_Master", "")){
                System.out.println("ResultSet exported.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}

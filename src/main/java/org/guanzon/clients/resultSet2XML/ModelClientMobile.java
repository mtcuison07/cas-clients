package org.guanzon.clients.resultSet2XML;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;

public class ModelClientMobile {
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
        
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/Model_Client_Mobile.xml");
        
        
        String lsSQL = "SELECT" +
                    "  sMobileID" +
                    ", sClientID" +
                    ", sMobileNo" +
                    ", cMobileTp" +
                    ", cOwnerxxx" +
                    ", cPrimaryx" +
                    ", cIncdMktg" +
                    ", nUnreachx" +
                    ", dLastVeri" +
                    ", dInactive" +
                    ", nNoRetryx" +
                    ", cInvalidx" +
                    ", cConfirmd" +
                    ", dConfirmd" +
                    ", cSubscrbr" +
                    ", dHoldMktg" +
                    ", dMktgMsg1" +
                    ", dMktgMsg2" +
                    ", dMktgMsg3" +
                    ", cNewMobil" +
                    ", cRecdStat" +
                    ", dModified" +
                        " FROM Client_Mobile" +
                        " WHERE 0=1";
        
        
        ResultSet loRS = instance.executeQuery(lsSQL);
        try {
            if (MiscUtil.resultSet2XML(instance, loRS, System.getProperty("sys.default.path.metadata"), "Client_Mobile", "")){
                System.out.println("ResultSet exported.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

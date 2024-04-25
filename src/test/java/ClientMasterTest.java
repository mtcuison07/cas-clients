
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.clients.Client_Master;
import org.json.simple.JSONObject;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
public class ClientMasterTest {
    public static void main(String [] args){
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
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/");
        
        
        JSONObject json;
        
        System.out.println("sBranch code = " + instance.getBranchCode());
        Client_Master model = new Client_Master(instance, false, instance.getBranchCode());
        json = model.newRecord();
        
        if (!"success".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
        System.err.println("result = " + (String) json.get("result"));
//        
        json = model.setMaster("cClientTp","0");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
        
        json = model.setMaster("sLastName","Sabiniano");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
    
        
        json = model.setMaster("sFrstName","Jonathan");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
    
        
        json = model.setMaster("sMiddName","Tamayo");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
    
        
        json = model.setMaster("sSuffixNm","");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
        
        json = model.setMaster("sMaidenNm","");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
        String sCompnyNm = model.getMaster("sLastName") + ", " + model.getMaster("sFrstName") + " " + model.getMaster("sSuffixNm") +  " " + model.getMaster("sMiddName");
        json = model.setMaster("sCompnyNm", sCompnyNm);
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
        
        json = model.setMaster("cGenderCd", 0);
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
        
        json = model.setMaster("cCvilStat", 0);
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
    
        json = model.setMaster("sCitizenx", "01");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
        
        json = model.setMaster("dBirthDte", "1990-06-03");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
        
        json = model.setMaster("sBirthPlc", "0335");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }        
        json = model.setMaster("sAddlInfo", "");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        } 
        json = model.setMaster("sSpouseID", "");
        if ("error".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        }
        System.out.println("mobile size = " + model.getMobileList().size());
        for(int lnctr = 0; lnctr < model.getMobileList().size(); lnctr++){
            model.setMobile(lnctr, "sMobileNo", ("0912345678" + String.valueOf(lnctr)));
            model.setMobile(lnctr, "nUnreachx", 0);
            model.setMobile(lnctr, "nNoRetryx", 0);
            model.setMobile(lnctr, "dLastVeri", "2024-03-29 11:28:00");
            model.setMobile(lnctr, "dInactive", "2024-03-29 11:28:00");
            model.setMobile(lnctr, "dConfirmd", "2024-03-29 11:28:00");
            model.setMobile(lnctr, "dHoldMktg", "2024-03-29 11:28:00");
            model.setMobile(lnctr, "dMktgMsg1", "2024-03-29 11:28:00");
            model.setMobile(lnctr, "dMktgMsg2", "2024-03-29 11:28:00");
            model.setMobile(lnctr, "dMktgMsg3", "2024-03-29 11:28:00");
           
        }
        
        for(int lnctr = 0; lnctr < model.getAddressList().size(); lnctr++){
            model.setAddress(lnctr, "sHouseNox", "123");
            model.setAddress(lnctr, "sAddressx", "sample");
            model.setAddress(lnctr, "sBrgyIDxx", "1200145");
            model.setAddress(lnctr, "sTownIDxx", "0335");
            model.setAddress(lnctr, "nLatitude", 0.0);
            model.setAddress(lnctr, "nLongitud", 0.0);
           
        }
        for(int lnctr = 0; lnctr < model.getEmailList().size(); lnctr++){
            model.setEmail(lnctr, "sEMailAdd", "samplemail@gmail.com");
            model.setEmail(lnctr, "cOwnerxxx", "0");
            model.setEmail(lnctr, "cPrimaryx", "0");
            model.setEmail(lnctr, "cRecdStat", "0");
           
        }
        for(int lnctr = 0; lnctr < model.getInsContactList().size(); lnctr++){
            model.setInsContact(lnctr, "sEMailAdd", "samplemail@gmail.com");
            model.setInsContact(lnctr, "sCPerson1", "Teejei De Celis");
            model.setInsContact(lnctr, "sCPPosit1", "0");
            model.setInsContact(lnctr, "sMobileNo", "09123456987");
            model.setInsContact(lnctr, "sTelNoxxx", "");
            model.setInsContact(lnctr, "sFaxNoxxx", "");
            model.setInsContact(lnctr, "sAccount1", "iamtjd@gmail.com");
            model.setInsContact(lnctr, "sAccount2", "");
            model.setInsContact(lnctr, "sAccount3", "");
            model.setInsContact(lnctr, "sRemarksx", "");
           
        }
        for(int lnctr = 0; lnctr < model.getSocialMediaList().size(); lnctr++){
            model.setSocialMed(lnctr, "sAccountx", "Teejei De Celis");
            model.setSocialMed(lnctr, "cSocialTp", "0");
            model.setSocialMed(lnctr, "sRemarksx", "");
           
        }
        json = model.saveRecord();
//        1200145 - barangay id
//        0335 - town id
        if (!"success".equals((String) json.get("result"))){
            System.err.println((String) json.get("message"));
            System.exit(1);
        } else {
            System.out.println((String) json.get("message"));
            System.exit(0);
        }
    }
}

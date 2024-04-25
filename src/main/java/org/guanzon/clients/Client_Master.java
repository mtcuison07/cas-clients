package org.guanzon.clients;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.iface.GRecord;
import org.guanzon.validators.client.Validator_Client_Address;
import org.guanzon.validators.client.Validator_Client_Institution_Contact;
import org.guanzon.validators.client.Validator_Client_Mail;
import org.guanzon.validators.client.Validator_Client_Master;
import org.guanzon.validators.client.Validator_Client_Mobile;
import org.guanzon.validators.client.Validator_Client_Social_Media;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Michael Cuison
 */
public class Client_Master implements GRecord{
    
    final String XML = "Model_Client_Master.xml";
    final String MOBILE_XML = "Model_Client_Mobile.xml";
    final String ADDRESS_XML = "Model_Client_Mobile.xml";
    final String SOCMED_XML = "Model_Client_Mobile.xml";
    final String EMAIL_XML = "Model_Client_Mobile.xml";
    final String INSCONTACT_XML = "Model_Client_Mobile.xml";
    GRider poGRider;
    String psBranchCd;
    boolean pbWtParent;
    
    int pnEditMode;
    String psMessagex;
    
    Model_Client_Master poClient;
    
    ArrayList<Model_Client_Mail> paMail;
    ArrayList<Model_Client_Mobile> paMobile;
    ArrayList<Model_Client_Address> paAddress;
    ArrayList<Model_Client_Social_Media> paSocMed;
    ArrayList<Model_Client_Institution_Contact> paInsContc;
    
    
    public JSONObject poJSON;
    
    public Client_Master(GRider foAppDrver, boolean fbWtParent, String fsBranchCd){
        poGRider = foAppDrver;
        pbWtParent = fbWtParent;
        psBranchCd = fsBranchCd.isEmpty() ? foAppDrver.getBranchCode() : fsBranchCd;
    }

    @Override
    public int getEditMode() {
        return pnEditMode;        
    }

    @Override
    public JSONObject newRecord() {
        
            poJSON = new JSONObject();
        try{
            
            pnEditMode = EditMode.ADDNEW;
            org.json.simple.JSONObject obj;

            poClient = new Model_Client_Master(setConnection(), poGRider);
            Connection loConn = null;
            loConn = setConnection();

            poClient.setClientID(MiscUtil.getNextCode(poClient.getTable(), "sClientID", true, loConn, psBranchCd));
            poClient.newRecord();

            //init detail
            //init detail
            paMobile = new ArrayList<>();
            paMail = new ArrayList<>();
            paAddress = new ArrayList<>();
            paSocMed = new ArrayList<>();
            paInsContc = new ArrayList<>();
            
            if (poClient == null){
                
                poJSON.put("result", "error");
                poJSON.put("message", "initialized new record failed.");
                return poJSON;
            }else{
                addAddress();
                addContact();
                addMail();
                addInsContact();
                addSocialMedia();
                
                
                poJSON.put("result", "success");
                poJSON.put("message", "initialized new record.");
                pnEditMode = EditMode.ADDNEW;
            }
               
        }catch(NullPointerException e){
            
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        
        return poJSON;
    }
        
    

    @Override
    public JSONObject openRecord(String fsValue) {
        pnEditMode = EditMode.READY;
        poJSON = new JSONObject();
        poJSON = poClient.openRecord(fsValue);
//        int lnCtr;
        poJSON = SearchClientAddress(fsValue);
//        poJSON = SearchClientMobile(fsValue);
        
        return poJSON;
    }

    @Override
    public JSONObject updateRecord() {
        pnEditMode = EditMode.UPDATE;
        JSONObject obj = new JSONObject();
        obj.put("pnEditMode", pnEditMode);
        return obj;
    }

    @Override
    public JSONObject saveRecord() {
        
        poJSON = new JSONObject();
        
        if (!pbWtParent){
            poGRider.beginTrans();
            System.out.println("beginTrans");
        }
        
        poClient.setFullName(poClient.getLastName() + ", " + poClient.getFirstName() + " " + poClient.getSuffixName() + " " + poClient.getMiddleName());
        Validator_Client_Master validator = new Validator_Client_Master(poClient);
        if (!validator.isEntryOkay()){
            poJSON.put("result", "success");
            poJSON.put("message", validator.getMessage());
            return poJSON;
        }
        poJSON =  poClient.saveRecord();
        if("error".equalsIgnoreCase((String)poJSON.get("result"))){
            poGRider.rollbackTrans();
            return poJSON;
        }

        poJSON =  saveAddress();
        if("error".equalsIgnoreCase((String)poJSON.get("result"))){
            
            poGRider.rollbackTrans();
            return poJSON;
        }
        poJSON =  saveMobile();
        if("error".equalsIgnoreCase((String)poJSON.get("result"))){
            
            poGRider.rollbackTrans();
            return poJSON;
        }
        poJSON =  saveEmail();
        
        if("error".equalsIgnoreCase((String)poJSON.get("result"))){
            
            poGRider.rollbackTrans();
            return poJSON;
        }
        poJSON =  saveSocialAccount();
        
        if("error".equalsIgnoreCase((String)poJSON.get("result"))){
            
            poGRider.rollbackTrans();
            return poJSON;
        }
        poJSON =  saveInstitution();
        
        if("error".equalsIgnoreCase((String)poJSON.get("result"))){
            
            poGRider.rollbackTrans();
            return poJSON;
        }
        
        if (!pbWtParent) {
            poGRider.commitTrans();
            System.out.println("commitTrans");
        }
        return poJSON;
    }

    @Override
    public JSONObject deactivateRecord(String fsValue) {
        pnEditMode = EditMode.READY;
        JSONObject obj = new JSONObject();
        obj.put("pnEditMode", pnEditMode);
        return obj;
    }

    @Override
    public JSONObject activateRecord(String fsValue) {
        pnEditMode = EditMode.READY;
        JSONObject obj = new JSONObject();
        obj.put("pnEditMode", pnEditMode);
        return obj;
    }
    
    public JSONObject addContact(){
        poJSON = new JSONObject();
        if (paMobile.size()<=0){
            paMobile.add(new Model_Client_Mobile(poGRider.getConnection(), poGRider));
            paMobile.get(0).newRecord();
            paMobile.get(0).setValue("sClientID", poClient.getClientID());
            poJSON.put("result", "success");
            poJSON.put("message", "Mobile No. add record.");
        } else {
            
            Validator_Client_Mobile  validator = new Validator_Client_Mobile(paMobile.get(paMobile.size()-1));
            if(!validator.isEntryOkay()){
                poJSON.put("result", "error");
                poJSON.put("message", validator.getMessage());
                return poJSON;
            }
            paMobile.add(new Model_Client_Mobile(poGRider.getConnection(), poGRider));
            paMobile.get(paMobile.size()-1).newRecord();

            paMobile.get(paMobile.size()-1).setClientID(poClient.getClientID());
        }
        
        return poJSON;
    }
    
    public Model_Client_Mobile getContact(int fnIndex){
        if (fnIndex > paMobile.size() - 1 || fnIndex < 0) return null;
        
        return paMobile.get(fnIndex);
    }
    
    public JSONObject addMail(){
        poJSON = new JSONObject();
        if (paMail.isEmpty()){
            paMail.add(new Model_Client_Mail(poGRider.getConnection(), poGRider));
            paMail.get(0).newRecord();
            poJSON.put("result", "success");
            poJSON.put("message", "Email address add record.");
        } else {
            Validator_Client_Mail  validator = new Validator_Client_Mail(paMail.get(paMail.size()-1));
            if(!validator.isEntryOkay()){
                poJSON.put("result", "error");
                poJSON.put("message", validator.getMessage());
                return poJSON;
            }
            paMail.add(new Model_Client_Mail(poGRider.getConnection(), poGRider));
            paMail.get(paMail.size()-1).newRecord();

            paMail.get(paMail.size()-1).setClientID(poClient.getClientID());
            
            poJSON.put("result", "success");
            poJSON.put("message", "Email address add record.");
//            if (paMail.get(paMail.size()-1).getEmail().isEmpty()){
//                paMail.add(new Model_Client_Mail(poGRider.getConnection(), poGRider));
//                poJSON.put("result", "success");
//                poJSON.put("message", "Email address add record.");
//            } else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Last contact information has no email address.");
//                return poJSON;
//            }
        }
        return poJSON;
    }
    
    public Model_Client_Mail getEMail(int fnIndex){
        if (fnIndex > paMail.size() - 1 || fnIndex < 0) return null;
        
        return paMail.get(fnIndex);
    }
   
//    public JSONObject addAddress(){
//        poJSON = new JSONObject();
//        if (paAddress.isEmpty()){
//            paAddress.add(new Model_Client_Address(poGRider.getConnection(), poGRider));
//            paAddress.get(0).newRecord();
//            poJSON.put("result", "success");
//            poJSON.put("message", "Address add record.");
//            
//        } else {
//            if (paAddress.get(paAddress.size()-1).getAddress().isEmpty()){
//                paAddress.add(new Model_Client_Address(poGRider.getConnection(), poGRider));
//                poJSON.put("result", "success");
//                poJSON.put("message", "Address add record.");
//            } else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Last contact information has no address.");
//                return poJSON;
//            }
//        }
//        return poJSON;
//    }
    
    public JSONObject addAddress(){
        poJSON = new JSONObject();
        if (paAddress.isEmpty()){
            paAddress.add(new Model_Client_Address(poGRider.getConnection(), poGRider));
            paAddress.get(0).newRecord();
            paAddress.get(0).setClientID(poClient.getClientID());
            poJSON.put("result", "success");
            poJSON.put("message", "Address add record.");

        } else {
            Validator_Client_Address  validator = new Validator_Client_Address(paAddress.get(paAddress.size()-1));
            if(!validator.isEntryOkay()){
                poJSON.put("result", "error");
                poJSON.put("message", validator.getMessage());
                return poJSON;
            }
            paAddress.add(new Model_Client_Address(poGRider.getConnection(), poGRider));
            paAddress.get(paAddress.size()-1).newRecord();
            paAddress.get(paAddress.size()-1).setClientID(poClient.getClientID());
//            if (!paAddress.get(paAddress.size()-1).getAddress().isEmpty()){
//                paAddress.add(new Model_Client_Address(poGRider.getConnection(), poGRider));
//                poJSON.put("result", "success");
//                poJSON.put("message", "Address add record.");
//            } else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Last contact information has no address.");
//                return poJSON;
//            }
        }
        return poJSON;
    }
    
    public Model_Client_Address getAddress(int fnIndex){
        if (fnIndex > paAddress.size() - 1 || fnIndex < 0) return null;
        
        return paAddress.get(fnIndex);
    }
    
    public JSONObject addInsContact(){
        poJSON = new JSONObject();
        if (paInsContc.isEmpty()){
            paInsContc.add(new Model_Client_Institution_Contact(poGRider.getConnection(), poGRider));
            paInsContc.get(0).newRecord();
            paInsContc.get(0).setClientID(poClient.getClientID());
            poJSON.put("result", "success");
            poJSON.put("message", "Contact person add record.");
        } else {
            
            Validator_Client_Institution_Contact  validator = new Validator_Client_Institution_Contact(paInsContc.get(paInsContc.size()-1));
            if(!validator.isEntryOkay()){
                poJSON.put("result", "error");
                poJSON.put("message", validator.getMessage());
                return poJSON;
            }
            paInsContc.add(new Model_Client_Institution_Contact(poGRider.getConnection(), poGRider));
            paInsContc.get(paInsContc.size()-1).newRecord();
            paInsContc.get(paInsContc.size()-1).setClientID(poClient.getClientID());
//            if (paInsContc.get(paInsContc.size()-1).getContactID().isEmpty()){
//                paInsContc.add(new Model_Client_Institution_Contact(poGRider.getConnection(), poGRider));
//                poJSON.put("result", "success");
//                poJSON.put("message", "Contact person add record.");
//            } else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Last contact information has no contact person.");
//                return poJSON;
//            }
        }
        return poJSON;
    }
    
    public Model_Client_Institution_Contact getContactID(int fnIndex){
        if (fnIndex > paInsContc.size() - 1 || fnIndex < 0) return null;
        
        return paInsContc.get(fnIndex);
    }
   
    
    
    public JSONObject addSocialMedia(){
        poJSON = new JSONObject();
        if (paSocMed.isEmpty()){
            paSocMed.add(new Model_Client_Social_Media(poGRider.getConnection(), poGRider));
            paSocMed.get(0).newRecord();
            paSocMed.get(0).setClientID(poClient.getClientID());
            poJSON.put("result", "success");
            poJSON.put("message", "Social media add record.");
        } else {
            Validator_Client_Social_Media validator = new Validator_Client_Social_Media(paSocMed.get(paSocMed.size()-1));
            
            if (!validator.isEntryOkay()){
                poJSON.put("result", "error");
                poJSON.put("message", validator.getMessage());
                return poJSON;
//                System.err.println(validator.getMessage());
//                System.exit(1);
            }
            paSocMed.add(new Model_Client_Social_Media(poGRider.getConnection(), poGRider));
            paSocMed.get(paSocMed.size()-1).newRecord();
            paSocMed.get(paSocMed.size()-1).setClientID(poClient.getClientID());
            poJSON.put("result", "success");
            poJSON.put("message", "Social media add record.");
//            if (paSocMed.get(paSocMed.size()-1).getSocialID().isEmpty()){
//                paSocMed.add(new Model_Client_Social_Media(poGRider.getConnection(), poGRider));
//                poJSON.put("result", "success");
//            poJSON.put("message", "Social media add record.");
//            } else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Last social media information has no social account.");
//                return poJSON;
//            }
        }
        return poJSON;
    }
    
    public Model_Client_Social_Media getSocialID(int fnIndex){
        if (fnIndex > paSocMed.size() - 1 || fnIndex < 0) return null;
        
        return paSocMed.get(fnIndex);
    }
   
    
    //unused implemented methods
    @Override
    public JSONObject deleteRecord(String fsValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    

    
    @Override
    public JSONObject setMaster(int fnCol, Object foData) {
        
        JSONObject obj = new JSONObject();
        obj.put("pnEditMode", pnEditMode);
        if (pnEditMode != EditMode.UNKNOWN){
            // Don't allow specific fields to assign values
            if(!(fnCol == poClient.getColumn("sClientID") ||
                fnCol == poClient.getColumn("cRecdStat") ||
                fnCol == poClient.getColumn("sModified") ||
                fnCol == poClient.getColumn("dModified"))){
                poClient.setValue(fnCol, foData);
                obj.put(fnCol, pnEditMode);
            }
        }
        return obj;
    }

    @Override
    public JSONObject setMaster(String fsCol, Object foData) {
        return setMaster(poClient.getColumn(fsCol), foData);
    }

    @Override
    public Object getMaster(int fnCol) {
        if(pnEditMode == EditMode.UNKNOWN)
            return null;
        else 
            return poClient.getValue(fnCol);
    }

    @Override
    public Object getMaster(String fsCol) {
        return getMaster(poClient.getColumn(fsCol));
    }

    @Override
    public JSONObject searchRecord(String fsValue, boolean fbByCode) {
        return SearchClient(fsValue, fbByCode);
    }
    
    
    public ArrayList<Model_Client_Mobile> getMobileList(){return paMobile;}
    public void setMobileList(ArrayList<Model_Client_Mobile> foObj){this.paMobile = foObj;}
    
    public ArrayList<Model_Client_Address> getAddressList(){return paAddress;}
    public void setAddressList(ArrayList<Model_Client_Address> foObj){this.paAddress = foObj;}
    
    public ArrayList<Model_Client_Mail> getEmailList(){return paMail;}
    public void setEmailList(ArrayList<Model_Client_Mail> foObj){this.paMail = foObj;}
    
    
    public ArrayList<Model_Client_Institution_Contact> getInsContactList(){return paInsContc;}
    public void setInsContactList(ArrayList<Model_Client_Institution_Contact> foObj){this.paInsContc = foObj;}
    
    public ArrayList<Model_Client_Social_Media> getSocialMediaList(){return paSocMed;}
    public void setSocialMediaList(ArrayList<Model_Client_Social_Media> foObj){this.paSocMed = foObj;}
    
    
    
    public void setMobile(int fnRow, int fnIndex, Object foValue){ paMobile.get(fnRow).setValue(fnIndex, foValue);}
    public void setMobile(int fnRow, String fsIndex, Object foValue){ paMobile.get(fnRow).setValue(fsIndex, foValue);}
    public Object getMobile(int fnRow, int fnIndex){return paMobile.get(fnRow).getValue(fnIndex);}
    public Object getMobile(int fnRow, String fsIndex){return paMobile.get(fnRow).getValue(fsIndex);}
    
    public void setEmail(int fnRow, int fnIndex, Object foValue){ paMail.get(fnRow).setValue(fnIndex, foValue);}
    public void setEmail(int fnRow, String fsIndex, Object foValue){ paMail.get(fnRow).setValue(fsIndex, foValue);}
    
    public Object getEmail(int fnRow, int fnIndex){return paMail.get(fnRow).getValue(fnIndex);}
    public Object getEmail(int fnRow, String fsIndex){return paMail.get(fnRow).getValue(fsIndex);}
    
    public void setAddress(int fnRow, int fnIndex, Object foValue){ paAddress.get(fnRow).setValue(fnIndex, foValue);}
    public void setAddress(int fnRow, String fsIndex, Object foValue){ paAddress.get(fnRow).setValue(fsIndex, foValue);}
    public Object getAddress(int fnRow, int fnIndex){return paAddress.get(fnRow).getValue(fnIndex);}
    public Object getAddress(int fnRow, String fsIndex){return paAddress.get(fnRow).getValue(fsIndex);}
    
    public void setInsContact(int fnRow, int fnIndex, Object foValue){ paInsContc.get(fnRow).setValue(fnIndex, foValue);}
    public void setInsContact(int fnRow, String fsIndex, Object foValue){ paInsContc.get(fnRow).setValue(fsIndex, foValue);}
    public Object getInsContact(int fnRow, int fnIndex){return paInsContc.get(fnRow).getValue(fnIndex);}
    public Object getInsContact(int fnRow, String fsIndex){return paInsContc.get(fnRow).getValue(fsIndex);}
    
    
    public void setSocialMed(int fnRow, int fnIndex, Object foValue){ paSocMed.get(fnRow).setValue(fnIndex, foValue);}
    public void setSocialMed(int fnRow, String fsIndex, Object foValue){ paSocMed.get(fnRow).setValue(fsIndex, foValue);}
    public Object getSocialMed(int fnRow, int fnIndex){return paSocMed.get(fnRow).getValue(fnIndex);}
    public Object getSocialMed(int fnRow, String fsIndex){return paSocMed.get(fnRow).getValue(fsIndex);}
    
    
//    public JSONObject SearchClient(String fsValue, boolean fbByCode){
//        String lsHeader = "ID»Name»Address»Last Name»First Name»Midd Name»Suffix";
//        String lsColName = "sClientID»sClientNm»xAddressx»sLastName»sFrstName»sMiddName»sSuffixNm";
//        String lsColCrit = "a.sClientID»a.sClientNm»CONCAT(b.sHouseNox, ' ', b.sAddressx, ', ', c.sTownName, ' ', d.sProvName)»a.sLastName»a.sFrstName»a.sMiddName»a.sSuffixNm";
//        String lsSQL = "SELECT " +
//                            "  a.sClientID" +
//                            ", a.sClientNm" +
//                            ", CONCAT(b.sHouseNox, ' ', b.sAddressx, ', ', c.sTownName, ' ', d.sProvName) xAddressx" +
//                            ", a.sLastName" + 
//                            ", a.sFrstName" + 
//                            ", a.sMiddName" + 
//                            ", a.sSuffixNm" + 
//                        " FROM Client_Master a" + 
//                            " LEFT JOIN Client_Address b" + 
//                                " ON a.sClientID = b.sClientID" + 
//                                    " AND b.nPriority = 1" +
//                            " LEFT JOIN TownCity c" + 
//                                " ON b.sTownIDxx = c.sTownIDxx" + 
//                            " LEFT JOIN Client_Mobile d" +
//                                " ON c.sProvIDxx = d.sProvIDxx";
//        
//        poJSON = ShowDialogFX.Search(poGRider, 
//                                        lsSQL, 
//                                        fsValue, 
//                                        lsHeader, 
//                                        lsColName, 
//                                        lsColCrit, 
//                                        fbByCode ? 0 :1);
//        return openRecord((String) poJSON.get("sClientID"));
//    }
//    
    private JSONObject saveMobile(){
        
        JSONObject obj = new JSONObject();
        if (paMobile.size()<= 0){
            obj.put("result", "error");
            obj.put("message", "No mobile number detected. Please encode client mobile number.");
            return obj;
        }
        
        int lnCtr;
        String lsSQL;
        
        for (lnCtr = 0; lnCtr <= paMobile.size() -1; lnCtr++){
            paMobile.get(lnCtr).setClientID(poClient.getClientID());
            Validator_Client_Mobile validator = new Validator_Client_Mobile(paMobile.get(lnCtr));
            
            paMobile.get(lnCtr).setMobileNetwork(CommonUtils.classifyNetwork(paMobile.get(lnCtr).getContactNo()));
            paMobile.get(lnCtr).setModifiedDate(poGRider.getServerDate());
            
            if (!validator.isEntryOkay()){
                obj.put("result", "error");
                obj.put("message", validator.getMessage());
                return obj;
            }
            obj = paMobile.get(lnCtr).saveRecord();

        }    
        
        return obj;
    }
    
    
    private JSONObject saveAddress(){
        
        JSONObject obj = new JSONObject();
        if (paAddress.size()<= 0){
            obj.put("result", "error");
            obj.put("message", "No client address detected. Please encode client address.");
            return obj;
        }
        
        int lnCtr;
        String lsSQL;
        
        for (lnCtr = 0; lnCtr <= paAddress.size() -1; lnCtr++){
            paAddress.get(lnCtr).setClientID(poClient.getClientID());
            Validator_Client_Address validator = new Validator_Client_Address(paAddress.get(lnCtr));
            
            paAddress.get(lnCtr).setModifiedDate(poGRider.getServerDate());
            
            if (!validator.isEntryOkay()){
                obj.put("result", "error");
                obj.put("message", validator.getMessage());
                return obj;
            
            }
            obj = paAddress.get(lnCtr).saveRecord();

        }    
        
        return obj;
    }
    
    
    private JSONObject saveEmail(){
        
        JSONObject obj = new JSONObject();
        if (paMail.size()<= 0){
            obj.put("result", "error");
            obj.put("message", "No client email address detected. Please encode client email address.");
            return obj;
        }
        
        int lnCtr;
        String lsSQL;
        
        for (lnCtr = 0; lnCtr <= paMail.size() -1; lnCtr++){
            paMail.get(lnCtr).setClientID(poClient.getClientID());
            Validator_Client_Mail validator = new Validator_Client_Mail(paMail.get(lnCtr));
            
            paMail.get(lnCtr).setModifiedDate(poGRider.getServerDate());
            
            if (!validator.isEntryOkay()){
                obj.put("result", "error");
                obj.put("message", validator.getMessage());
                return obj;
            
            }
            obj = paMail.get(lnCtr).saveRecord();

        }    
        
        return obj;
    }
    private JSONObject saveInstitution (){
        
        JSONObject obj = new JSONObject();
        if (paInsContc.size()<= 0){
            obj.put("result", "error");
            obj.put("message", "No contact person detected. Please encode contact person .");
            return obj;
        }
        
        int lnCtr;
        String lsSQL;
        
        for (lnCtr = 0; lnCtr <= paInsContc.size() -1; lnCtr++){
            paInsContc.get(lnCtr).setClientID(poClient.getClientID());
            Validator_Client_Institution_Contact validator = new Validator_Client_Institution_Contact(paInsContc.get(lnCtr));
            
            paInsContc.get(lnCtr).setModifiedDate(poGRider.getServerDate());
            
            if (!validator.isEntryOkay()){
                obj.put("result", "error");
                obj.put("message", validator.getMessage());
                return obj;
            
            }
            obj = paInsContc.get(lnCtr).saveRecord();

        }    
        
        return obj;
    }
    
    private JSONObject saveSocialAccount (){
        
        JSONObject obj = new JSONObject();
        if (paSocMed.size()<= 0){
            obj.put("result", "error");
            obj.put("message", "No social media account detected. Please encode social media account.");
            return obj;
        }
        
        int lnCtr;
        String lsSQL;
        
        for (lnCtr = 0; lnCtr <= paSocMed.size() -1; lnCtr++){
            paSocMed.get(lnCtr).setClientID(poClient.getClientID());
            Validator_Client_Social_Media validator = new Validator_Client_Social_Media(paSocMed.get(lnCtr));
            
            paSocMed.get(lnCtr).setModifiedDate(poGRider.getServerDate());
            
            if (!validator.isEntryOkay()){
                obj.put("result", "error");
                obj.put("message", validator.getMessage());
                return obj;
            
            }
            obj = paSocMed.get(lnCtr).saveRecord();

        }    
        
        return obj;
    }
    
    private Connection setConnection(){
        Connection foConn;
        
        if (pbWtParent){
            foConn = (Connection) poGRider.getConnection();
            if (foConn == null) foConn = (Connection) poGRider.doConnect();
        }else foConn = (Connection) poGRider.doConnect();
        
        return foConn;
    }

    @Override
    public void setRecordStatus(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object getModel() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public JSONObject searchCitizenship(String fsValue, boolean fbByCode) {
      
        
        String lsSQL = "SELECT" + 
                    "  sCntryCde" +
                    ", sNational" +
                " FROM Country " +
                " WHERE cRecdStat = '1' " + 
                " AND (sNational IS NOT NULL AND sNational != '') " + 
                " ORDER BY sCntryCde ";
               
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sCntryCde = " + SQLUtil.toSQL(fsValue));
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "sNational LIKE " + SQLUtil.toSQL(fsValue + "%"));
        
        JSONObject loJSON;
        System.out.println(lsSQL);
        loJSON = ShowDialogFX.Search(
                        poGRider, 
                        lsSQL, 
                        fsValue, 
                        "Code»Nationality", 
                        "sCntryCde»sNational", 
                        "sCntryCde»sNational", 
                        fbByCode ? 0 : 1);
            
            if (loJSON != null) {
                setMaster(11,(String) loJSON.get("sCntryCde"));
                loJSON.put("result", "success");
                loJSON.put("message", "Search citizenship success.");
                return loJSON;
            }else {
                loJSON.put("result", "success");
                loJSON.put("message", "No record selected.");
                return loJSON;
            }
    }
    
    
    public JSONObject searchBirthPlce(String fsValue, boolean fbByCode) {
      
        
        String lsSQL = "SELECT" + 
                    "  a.sTownIDxx" +
                    ", CONCAT(a.sTownName, ', ', b.sProvName) AS xBrthPlce" +
                " FROM TownCity a " +
                " INNER JOIN Province b "+
                "   ON a.sProvIDxx = b.sProvIDxx " +
                " WHERE a.cRecdStat = 1 " + 
                " ORDER BY a.sTownName ASC";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sTownIDxx = " + SQLUtil.toSQL(fsValue));
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sTownName LIKE " + SQLUtil.toSQL(fsValue + "%"));
        
      
        JSONObject loJSON;
        
        loJSON = ShowDialogFX.Search(
                        poGRider, 
                        lsSQL, 
                        fsValue, 
                        "Code»Birth Place", 
                        "a.sTownIDxx»xBrthPlce", 
                        "a.sTownIDxx»CONCAT(a.sTownName, ', ', b.sProvName)", 
                        fbByCode ? 0 : 1);
            
            if (loJSON != null) {
                setMaster(13,(String) loJSON.get("sTownIDxx"));
                loJSON.put("result", "success");
                loJSON.put("message", "Search birth place success.");
                return loJSON;
            }else {
                loJSON.put("result", "success");
                loJSON.put("message", "No record selected.");
                return loJSON;
            }
    }
    
    
    public JSONObject SearchBarangayAddress(int lnRow,String fsValue, boolean fbByCode){
        if(paAddress.get(lnRow).getTownID().isEmpty()){
            JSONObject loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "Kindly choose the town or city first.");
            return loJSON;
        }
        return paAddress.get(lnRow).SearchBarangay(fsValue, fbByCode);
    }
    public JSONObject SearchTownAddress(int lnRow,String fsValue, boolean fbByCode){
        return paAddress.get(lnRow).SearchTown(fsValue, fbByCode);
    }
    public JSONObject SearchClient(String fsValue, boolean fbByCode){
        String lsHeader = "ID»Name»Address»Last Name»First Name»Midd Name»Suffix";
        String lsColName = "sClientID»sCompnyNm»xAddressx»sLastName»sFrstName»sMiddName»sSuffixNm";
        String lsColCrit = "a.sClientID»a.sCompnyNm»CONCAT(b.sHouseNox, ' ', b.sAddressx, ', ', c.sTownName, ' ', d.sProvName)»a.sLastName»a.sFrstName»a.sMiddName»a.sSuffixNm";
        String lsSQL = "SELECT " +
                            "  a.sClientID" +
                            ", a.sCompnyNm" +
                            ", CONCAT(b.sHouseNox, ' ', b.sAddressx, ', ', c.sTownName, ' ', d.sProvName) xAddressx" +
                            ", a.sLastName" + 
                            ", a.sFrstName" + 
                            ", a.sMiddName" + 
                            ", a.sSuffixNm" + 
                        " FROM Client_Master a" + 
                            " LEFT JOIN Client_Address b" + 
                                " ON a.sClientID = b.sClientID" + 
                            " LEFT JOIN TownCity c" + 
                                " ON b.sTownIDxx = c.sTownIDxx" + 
                            " LEFT JOIN Province d" +
                                " ON c.sProvIDxx = d.sProvIDxx";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sClientID = " + SQLUtil.toSQL(fsValue));
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%"));
       
            
      
        JSONObject loJSON;
        String lsValue;
            
            
       
        System.out.println("lsSQL = " + lsSQL);
        loJSON = ShowDialogFX.Search(poGRider, 
                                        lsSQL, 
                                        fsValue, 
                                        lsHeader, 
                                        lsColName, 
                                        lsColCrit, 
                                        fbByCode ? 0 :1);
            
            if (loJSON != null) {
                
                System.out.println("sClientID = " + (String) loJSON.get("sClientID"));
                lsValue = (String) loJSON.get("sClientID");
            }else {
                loJSON.put("result", "error");
                loJSON.put("message", "No record selected.");
                return loJSON;
            }
        return openRecord(lsValue);
    }
    
    public JSONObject SearchClientAddress(String fsValue){
        String lsHeader = "ID»Address";
        String lsColName = "sAddrssID»CONCAT(b.sHouseNox, ' ', b.sAddressx, ', ', c.sTownName, ' ', d.sProvName)";
        String lsColCrit = "a.sAddrssID»CONCAT(b.sHouseNox, ' ', b.sAddressx, ', ', c.sTownName, ' ', d.sProvName)";
        String lsSQL = "SELECT " +
                        " a.sAddrssID" +
                        ", a.sClientID" +
                        ", a.sHouseNox" +
                        ", a.sAddressx" +
                        ", a.sBrgyIDxx" +
                        ", a.sTownIDxx" +
                        ", a.nLatitude" +
                        ", a.nLongitud" +
                        ", a.cPrimaryx" +
                        ", a.cRecdStat" +
                        ", a.dModified" +
                        ", b.sTownName xTownName" +
                        ", d.sBrgyName xBrgyName" +
                        ", c.sProvName xProvName" +
                " FROM Client_Address a" + 
                 " LEFT JOIN TownCity b ON a.sTownIDxx = b.sTownIDxx" +
                            " LEFT JOIN Province c ON a.cProvince = c.sProvName" +
                            " LEFT JOIN Barangay d ON a.sBrgyIDxx = d.sBrgyIDxx";
        lsSQL = MiscUtil.addCondition(lsSQL, "a.sClientID = " + SQLUtil.toSQL(fsValue));
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            while (loRS.next()){
                paAddress.add(new Model_Client_Address(poGRider.getConnection(), poGRider));
                paAddress.get(paAddress.size()-1).openRecord(loRS.getString("sAddrssID"));
                

                pnEditMode = EditMode.UPDATE;

                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } 
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return ShowDialogFX.Browse(poGRider, loRS, lsHeader, lsColName);
    }
    
    public JSONObject SearchClientMobile(String fsValue){
        String lsHeader = "ID»Address";
        String lsColName = "sMobileID»sMobileNo";
        String lsColCrit = "sMobileID»sMobileNo";
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
                        " FROM Client_Mobile" ;
        lsSQL = MiscUtil.addCondition(lsSQL, "sClientID = " + SQLUtil.toSQL(fsValue) + " AND a.nPriority = 1");
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        JSONObject loJSON = new JSONObject();
        try {
            int lnctr = 0;
            while (loRS.next()){
                paMobile.add(new Model_Client_Mobile(poGRider.getConnection(), poGRider));
                
                paMobile.get(paMobile.size() - 1).openRecord(loRS.getString("sMobileID"));
                lnctr++;
                pnEditMode = EditMode.UPDATE;

                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } 
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return poJSON;
    }
    
}
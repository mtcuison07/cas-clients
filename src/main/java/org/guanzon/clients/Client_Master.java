package org.guanzon.clients;

import com.google.gson.Gson;
import java.sql.Connection;
import java.util.ArrayList;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.iface.GRecord;
import org.json.simple.JSONObject;

/**
 *
 * @author Michael Cuison
 */
public class Client_Master implements GRecord{
    GRider poAppDrver;
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
    
    public Client_Master(GRider foAppDrver, boolean fbWtParent, String fsBranchCd){
        poAppDrver = foAppDrver;
        pbWtParent = fbWtParent;
        psBranchCd = fsBranchCd.isEmpty() ? foAppDrver.getBranchCode() : fsBranchCd;
    }

    @Override
    public int getEditMode() {
        return pnEditMode;
    }

    @Override
    public JSONObject newRecord() {
        
            JSONObject json = new JSONObject();
        try{
            
            pnEditMode = EditMode.ADDNEW;
            org.json.simple.JSONObject obj;

            poClient = new Model_Client_Master(setConnection());
            Connection loConn = null;
            loConn = setConnection();

            poClient.setClientID(MiscUtil.getNextCode(poClient.getTable(), "sClientID", true, loConn, psBranchCd));

            //init detail
            //init detail
            paMobile = new ArrayList<>();
            paMail = new ArrayList<>();
            paAddress = new ArrayList<>();
            paSocMed = new ArrayList<>();
            paInsContc = new ArrayList<>();
            json.put("result", "success");
            json.put("message", "initialized new record.");
        }catch(NullPointerException e){
            
            json.put("result", "error");
            json.put("message", e.getMessage());
        }
        
        return json;
    }
        
    

    @Override
    public JSONObject openRecord(String fsValue) {
        pnEditMode = EditMode.READY;
        JSONObject obj = new JSONObject();
        obj.put("pnEditMode", pnEditMode);
        return obj;
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
        pnEditMode = EditMode.READY;
        JSONObject obj = new JSONObject();
        obj.put("pnEditMode", pnEditMode);
        return obj;
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
    
    public boolean addContact(){
        if (paMobile.isEmpty()){
            paMobile.add(new Model_Client_Mobile(poAppDrver.getConnection()));
        } else {
            if (paMobile.get(paMobile.size()-1).getContactNo().isEmpty()){
                paMobile.add(new Model_Client_Mobile(poAppDrver.getConnection()));
            } else {
                psMessagex = "Last contact information has no contact number.";
                return false;
            }
        }
        
        return true;
    }
    
    public Model_Client_Mobile getContact(int fnIndex){
        if (fnIndex > paMobile.size() - 1 || fnIndex < 0) return null;
        
        return paMobile.get(fnIndex);
    }
    
    public boolean addMail(){
        if (paMail.isEmpty()){
            paMail.add(new Model_Client_Mail(poAppDrver.getConnection()));
        } else {
            if (paMail.get(paMail.size()-1).getEmail().isEmpty()){
                paMail.add(new Model_Client_Mail(poAppDrver.getConnection()));
            } else {
                psMessagex = "Last contact information has no contact number.";
                return false;
            }
        }
        return true;
    }
    
    public Model_Client_Mail getEMail(int fnIndex){
        if (fnIndex > paMail.size() - 1 || fnIndex < 0) return null;
        
        return paMail.get(fnIndex);
    }
   
    public boolean addAddress(){
        if (paAddress.isEmpty()){
            paAddress.add(new Model_Client_Address(poAppDrver.getConnection()));
        } else {
            if (paAddress.get(paAddress.size()-1).getAddress().isEmpty()){
                paAddress.add(new Model_Client_Address(poAppDrver.getConnection()));
            } else {
                psMessagex = "Last contact information has no contact number.";
                return false;
            }
        }
        return true;
    }
    
    public Model_Client_Address getAddress(int fnIndex){
        if (fnIndex > paAddress.size() - 1 || fnIndex < 0) return null;
        
        return paAddress.get(fnIndex);
    }
    
    public boolean addInsContact(){
        if (paInsContc.isEmpty()){
            paInsContc.add(new Model_Client_Institution_Contact(poAppDrver.getConnection()));
        } else {
            if (paInsContc.get(paInsContc.size()-1).getContactID().isEmpty()){
                paInsContc.add(new Model_Client_Institution_Contact(poAppDrver.getConnection()));
            } else {
                psMessagex = "Last contact information has no contact number.";
                return false;
            }
        }
        return true;
    }
    
    public Model_Client_Institution_Contact getContactID(int fnIndex){
        if (fnIndex > paInsContc.size() - 1 || fnIndex < 0) return null;
        
        return paInsContc.get(fnIndex);
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
        JSONObject obj = new JSONObject();
        setMaster(poClient.getColumn(fsCol), foData);
        obj.put(poClient.getColumn(fsCol), foData);
        return obj;
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
    public JSONObject searchRecord(String string, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
    
    private Connection setConnection(){
        Connection foConn;
        
        if (pbWtParent){
            foConn = (Connection) poAppDrver.getConnection();
            if (foConn == null) foConn = (Connection) poAppDrver.doConnect();
        }else foConn = (Connection) poAppDrver.doConnect();
        
        return foConn;
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.cas.clients.account;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GRecord;
import org.guanzon.appdriver.iface.GTransaction;
import org.guanzon.cas.model.clients.Model_Client_Address;
import org.guanzon.cas.model.clients.Model_Client_Master;
import org.guanzon.cas.models.Model_AP_Client_Ledger;
import org.guanzon.cas.model.clients.ap.Model_AP_Client_Mater;
import org.guanzon.cas.models.Model_Account_Accreditation;
import org.guanzon.cas.validators.ValidatorFactory;
import org.guanzon.cas.validators.ValidatorInterface;
import org.json.simple.JSONObject;

/**
 *
 * @author User
 */
public class AP_Client_Master implements GRecord {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;
    
    Model_AP_Client_Mater poModel;
    ArrayList<Model_AP_Client_Ledger> poLedger;
    JSONObject poJSON;

    public AP_Client_Master(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;

//        poModel = new Model_AP_Client_Mater(foGRider);
        pnEditMode = EditMode.UNKNOWN;
    }



    @Override
    public JSONObject setMaster(int fnCol, Object foData) {
        
        JSONObject obj = new JSONObject();
        obj.put("pnEditMode", pnEditMode);
        if (pnEditMode != EditMode.UNKNOWN){
            // Don't allow specific fields to assign values
            if(!(fnCol == poModel.getColumn("cRecdStat") ||
                fnCol == poModel.getColumn("sModified") ||
                fnCol == poModel.getColumn("dModified"))){
                return poModel.setValue(fnCol, foData);
            }
        }
        return obj;
//        return poModel.setValue(fnCol, foData);
    }

    @Override
    public JSONObject setMaster(String fsCol, Object foData) {
        return poModel.setValue(fsCol, foData);
    }

    @Override
    public int getEditMode() {
        return pnEditMode;
    }

    private Connection setConnection(){
        Connection foConn;
        
        if (pbWthParent){
            foConn = (Connection) poGRider.getConnection();
            if (foConn == null) foConn = (Connection) poGRider.doConnect();
        }else foConn = (Connection) poGRider.doConnect();
        
        return foConn;
    }
    
    public ArrayList<Model_AP_Client_Ledger> getLedger(){return poLedger;}
    public void setLedger(ArrayList<Model_AP_Client_Ledger> foObj){this.poLedger = foObj;}
    
    
    public void setLedger(int fnRow, int fnIndex, Object foValue){ poLedger.get(fnRow).setValue(fnIndex, foValue);}
    public void setLedger(int fnRow, String fsIndex, Object foValue){ poLedger.get(fnRow).setValue(fsIndex, foValue);}
    public Object getLedger(int fnRow, int fnIndex){return poLedger.get(fnRow).getValue(fnIndex);}
    public Object getLedger(int fnRow, String fsIndex){return poLedger.get(fnRow).getValue(fsIndex);}
    
    public JSONObject addLedger(){
        poJSON = new JSONObject();
        if (poLedger.isEmpty()){
            poLedger.add(new Model_AP_Client_Ledger(poGRider));
            poLedger.get(0).newRecord();
            poJSON.put("result", "success");
            poJSON.put("message", "Address add record.");
            

        } else {
            
            ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.AP_Client_Ledger, poLedger.get(poLedger.size()-1));
//            Validator_Client_Address  validator = new Validator_Client_Address(paAddress.get(paAddress.size()-1));
            if(!validator.isEntryOkay()){
                poJSON.put("result", "error");
                poJSON.put("message", validator.getMessage());
                return poJSON;
            }
            poLedger.add(new Model_AP_Client_Ledger(poGRider));
            poLedger.get(poLedger.size()-1).newRecord();
            
            poJSON.put("result", "success");
            poJSON.put("message", "Address add record.");
        }
        return poJSON;
    }
    public JSONObject SearchClient(String fsValue, boolean fbByCode){
        String lsHeader = "ID»Name»Contact Person";
        String lsColName = "sClientID»sCompnyNm»sCPerson1";
        String lsColCrit = "a.sClientID»a.sCompnyNm»c.sCPerson1";
        String lsSQL = "SELECT " +
                        "  a.sClientID" +
                        ", a.sCompnyNm" +
                        ", b.sCPerson1 " +
                        ", b.sContctID " +
                        ", c.sAddrssID " + 
                        ", TRIM(CONCAT(c.sHouseNox, ' ', c.sAddressx, ', ', d.sBrgyName, ' ', e.sTownName, ', ', f.sProvName)) xAddressx" +
                        ", b.sMobileNo" +
                        ", a.sTaxIDNox" +
                        " FROM Client_Master a" +
                        " LEFT JOIN Client_Institution_Contact_Person b" +
                        "     ON a.sClientID = b.sClientID AND b.cPrimaryx = '1'" +
                        " LEFT JOIN Client_Address c ON a.sClientID = c.sClientID AND c.cPrimaryx = '1'" +
                        " LEFT JOIN Barangay  d ON c.sBrgyIDxx = d.sBrgyIDxx" +
                        " LEFT JOIN TownCity e ON d.sTownIDxx = e.sTownIDxx" +
                        " LEFT JOIN Province f ON e.sProvIDxx = f.sProvIDxx";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cClientTp = '0'  AND a.sClientID = " + SQLUtil.toSQL(fsValue)) + " GROUP BY a.sClientID";
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cClientTp = '0'  AND a.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%")) + " GROUP BY a.sClientID";
        
       
        
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
            
        System.out.println("loJSON = " + loJSON.toJSONString());
            
            if (loJSON != null && !"error".equals((String) loJSON.get("result"))) {
                System.out.println("sClientID = " + (String) loJSON.get("sClientID"));
                System.out.println("sClientID = " + (String) loJSON.get("sClientID"));
                System.out.println("sClientID = " + (String) loJSON.get("sClientID"));
                lsValue = (String) loJSON.get("sClientID");
//                getMasterModel().setClientID((String) loJSON.get("sClientID"));
                setMaster(1, (String) loJSON.get("sClientID"));
                setMaster(2, (String) loJSON.get("sContctID"));
                setMaster(20, (String) loJSON.get("sCPerson1"));
                setMaster(18, (String) loJSON.get("sCompnyNm"));
                setMaster(3, (String) loJSON.get("sAddrssID"));
                setMaster(19, (String) loJSON.get("xAddressx"));
                OpenClientLedger((String) loJSON.get("sClientID"));
                loJSON.put("result", "success");
            }else {
                loJSON.put("result", "error");
                loJSON.put("message", "No client information found for: " + fsValue + ", Please check client type and client name details.");
                return loJSON;
            }
        return loJSON;
    }
    
    public JSONObject OpenClientLedger(String fsValue){
        String lsSQL = "SELECT" +
                        "  sClientID" +
                        ", nLedgerNo" +
                        ", dTransact" +
                        ", sSourceCd" +
                        ", sSourceNo" +
                        ", nAmountIn" +
                        ", nAmountOt" +
                        ", dPostedxx" +
                        ", nABalance" +
                        ", dModified" +
                        " FROM AP_Client_Ledger";
        lsSQL = MiscUtil.addCondition(lsSQL, "sClientID = " + SQLUtil.toSQL(fsValue));
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            int lnctr = 0;
            if (MiscUtil.RecordCount(loRS) > 0) {
                poLedger = new ArrayList<>();
                while(loRS.next()){
                        poLedger.add(new Model_AP_Client_Ledger(poGRider));
                        poLedger.get(poLedger.size() - 1).openRecord(loRS.getString("sClientID"));
                        
                        pnEditMode = EditMode.UPDATE;
                        lnctr++;
                        poJSON.put("result", "success");
                        poJSON.put("message", "Record loaded successfully.");
                    } 
                
                System.out.println("lnctr = " + lnctr);
                
            }else{
                poLedger = new ArrayList<>();
                addLedger();
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record selected.");
            }
            
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return poJSON;
    }
    private JSONObject checkData(JSONObject joValue){
        if(pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE){
            if(joValue.containsKey("continue")){
                if(true == (boolean)joValue.get("continue")){
                    joValue.put("result", "success");
                    joValue.put("message", "Record saved successfully.");
                }
            }
        }
        return joValue;
    }

    @Override
    public void setRecordStatus(String fsValue) {
        psTranStatus = fsValue;
    }

    @Override
    public Object getMaster(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object getMaster(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public JSONObject newRecord() {
        
            poJSON = new JSONObject();
        try{
            
            pnEditMode = EditMode.ADDNEW;
            poModel = new Model_AP_Client_Mater(poGRider);
//            org.json.simple.JSONObject obj;
//
            Connection loConn = null;
            loConn = setConnection();

            poModel.setClientID(MiscUtil.getNextCode(poModel.getTable(), "sClientID", true, loConn, poGRider.getBranchCode()));
//            poClient.newRecord();
//            
            poModel.newRecord();

            //init detail
            //init detail
            poLedger = new ArrayList<>();
            
            if (poModel == null){
                
                poJSON.put("result", "error");
                poJSON.put("message", "initialized new record failed.");
                return poJSON;
            }else{
                addLedger();
                
                
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
        
        poModel = new Model_AP_Client_Mater(poGRider);
        poJSON = poModel.openRecord("sTransNox = " + SQLUtil.toSQL(fsValue));
        poJSON = checkData(OpenClientLedger(fsValue));
        return poJSON;
    }

    @Override
    public JSONObject updateRecord() {
        
        JSONObject loJSON = new JSONObject();

        if (poModel.getEditMode() == EditMode.UPDATE) {
            loJSON.put("result", "success");
            loJSON.put("message", "Edit mode has changed to update.");
        } else {
            loJSON.put("result", "error");
            loJSON.put("message", "No record loaded to update.");
        }

        return loJSON;
    }

    @Override
    public JSONObject saveRecord() {
        if (!pbWthParent) {
            poGRider.beginTrans();
        }

        poJSON = poModel.saveRecord();

        if ("success".equals((String) poJSON.get("result"))) {
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }
        }

        return poJSON;
    }

    @Override
    public JSONObject deleteRecord(String string) {
         poJSON = new JSONObject();

        if (poModel.getEditMode() == EditMode.READY || poModel.getEditMode() == EditMode.UPDATE) {
            poJSON = poModel.setRecdStat(TransactionStatus.STATE_CLOSED);
            poJSON = poModel.deleteRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject deactivateRecord(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public JSONObject activateRecord(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public JSONObject searchRecord(String fsValue, boolean fbByCode) {
        String lsCondition = "";
        String lsFilter = "";

        if (psTranStatus.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStatus.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStatus.charAt(lnCtr)));
            }

            lsCondition =  "cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "cRecdStat = " + SQLUtil.toSQL(psTranStatus);
        }

        if (!fbByCode) {
            lsFilter = "sCompnyNm LIKE " + SQLUtil.toSQL(fsValue);
        } else {
            lsFilter = "sClientID = " + SQLUtil.toSQL(fsValue);
        }

        String lsSQL = MiscUtil.addCondition(poModel.makeSQL(), lsCondition + " AND " + lsFilter);

        poJSON = new JSONObject();

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                fsValue,
                "ClientID No»Name»Address",
                "sClientID»sCompnyNm»xAddressx",
                "sClientID»sCompnyNm»xAddressx",
                fbByCode ? 0 : 1);

        if (poJSON != null) {
            return openRecord(fsValue);
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
            return poJSON;
        }
    }

    @Override
    public Model_AP_Client_Mater getModel() {
        return poModel;
    }

}

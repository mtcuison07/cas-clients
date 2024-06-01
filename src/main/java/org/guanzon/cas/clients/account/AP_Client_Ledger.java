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
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GRecord;
import org.guanzon.appdriver.iface.GTransaction;
import org.guanzon.cas.model.clients.ap.Model_AP_Client_Ledger;
import org.guanzon.cas.models.Model_Account_Accreditation;
import org.guanzon.cas.validators.ValidatorFactory;
import org.guanzon.cas.validators.ValidatorInterface;
import org.json.simple.JSONObject;

/**
 *
 * @author User
 */
public class AP_Client_Ledger implements GRecord {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;
    
    ArrayList<Model_AP_Client_Ledger> poModel;
    JSONObject poJSON;

    public AP_Client_Ledger(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;
        pnEditMode = EditMode.UNKNOWN;
    }
    @Override
    public JSONObject setMaster(int fnCol, Object foData) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public JSONObject setMaster(String fsCol, Object foData) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
    }

    @Override
    public int getEditMode() {
        return pnEditMode;
    }
    
    @Override
    public void setRecordStatus(String fsValue) {
        
        psTranStatus = fsValue;
    }

    @Override
    public Object getMaster(int fnCol) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object getMaster(String fsCol) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    @Override
    public JSONObject newRecord() {
         poJSON = new JSONObject();
        try{
            

            //init detail
            //init detail
            poModel = new ArrayList<>();
            
            poJSON = addLedger();
                
                
            poJSON.put("result", "success");
            poJSON.put("message", "initialized new record.");
            pnEditMode = EditMode.ADDNEW;
               
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
        
        poJSON = OpenClientLedger(fsValue);
        return poJSON;
    }

    @Override
    public JSONObject updateRecord() {
        JSONObject loJSON = new JSONObject();
        
        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE){
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

        poJSON = saveLedger();

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
    public JSONObject deleteRecord(String fsValue) {
        poJSON = new JSONObject();

        poJSON = new JSONObject();
        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE) {
            if (poGRider.getUserLevel() < UserRight.SUPERVISOR){
                poJSON.put("result", "error");
                poJSON.put("message", "User is not allowed delete transaction.");
                return poJSON;
            }
            
            Model_AP_Client_Ledger model = new Model_AP_Client_Ledger(poGRider);
            String lsSQL = "DELETE FROM " + model.getTable()+
                                " WHERE sClientID = " + SQLUtil.toSQL(fsValue);

            if (!lsSQL.equals("")){
                if (poGRider.executeQuery(lsSQL, model.getTable(), poGRider.getBranchCode(), "") > 0) {
                    poJSON.put("result", "success");
                    poJSON.put("message", "Record deleted successfully.");
                } else {
                    poJSON.put("result", "error");
                    poJSON.put("message", poGRider.getErrMsg());
                }
            }
        }else {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid update mode. Unable to save record.");
            return poJSON;
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
        Model_AP_Client_Ledger model = new Model_AP_Client_Ledger(poGRider);
        String lsSQL = MiscUtil.addCondition(model.makeSQL(), "sClientID = " + SQLUtil.toSQL(fsValue));

        poJSON = new JSONObject();

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                fsValue,
                "Client ID»Ledger No»Source No»Date",
                "sClientID»nLedgerNo»sSourceCd»dTransact",
                "sClientID»nLedgerNo»sSourceCd»dTransact",
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
    public Model_AP_Client_Ledger getModel() {
        throw new UnsupportedOperationException("Not supported yet.");  // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    public ArrayList<Model_AP_Client_Ledger> getMaster(){return poModel;}
    public void setMaster(ArrayList<Model_AP_Client_Ledger> foObj){poModel = foObj;}
    
    
    public void setMaster(int fnRow, int fnIndex, Object foValue){ poModel.get(fnRow).setValue(fnIndex, foValue);}
    public void setMaster(int fnRow, String fsIndex, Object foValue){ poModel.get(fnRow).setValue(fsIndex, foValue);}
    public Object getMaster(int fnRow, int fnIndex){return poModel.get(fnRow).getValue(fnIndex);}
    public Object getMaster(int fnRow, String fsIndex){return poModel.get(fnRow).getValue(fsIndex);}
    
    
    public JSONObject addLedger(){
        poJSON = new JSONObject();
        if (poModel.isEmpty()){
            poModel.add(new Model_AP_Client_Ledger(poGRider));
            poModel.get(0).newRecord();
            poJSON.put("result", "success");
            poJSON.put("message", "Address add record.");
            

        } else {
            
            ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.AP_Client_Ledger, poModel.get(poModel.size()-1));
//            Validator_Client_Address  validator = new Validator_Client_Address(paAddress.get(paAddress.size()-1));
            if(!validator.isEntryOkay()){
                poJSON.put("result", "error");
                poJSON.put("message", validator.getMessage());
                return poJSON;
            }
            poModel.add(new Model_AP_Client_Ledger(poGRider));
            poModel.get(poModel.size()-1).newRecord();
            
            poJSON.put("result", "success");
            poJSON.put("message", "Address add record.");
        }
        return poJSON;
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
                poModel = new ArrayList<>();
                while(loRS.next()){
                        poModel.add(new Model_AP_Client_Ledger(poGRider));
                        poModel.get(poModel.size() - 1).openRecord(loRS.getString("sClientID"));
                        
                        pnEditMode = EditMode.UPDATE;
                        lnctr++;
                        poJSON.put("result", "success");
                        poJSON.put("message", "Record loaded successfully.");
                    } 
                
                System.out.println("lnctr = " + lnctr);
                
            }else{
                poModel = new ArrayList<>();
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
    private JSONObject saveLedger(){
        
        JSONObject obj = new JSONObject();
        if (poModel.size()<= 0){
            obj.put("result", "error");
            obj.put("message", "No client address detected. Please encode client address.");
            return obj;
        }
        
        int lnCtr;
        String lsSQL;
        
        for (lnCtr = 0; lnCtr <= poModel.size() -1; lnCtr++){
            poModel.get(lnCtr).setClientID(poModel.get(lnCtr).getClientID());
//            Validator_Client_Address validator = new Validator_Client_Address(paAddress.get(lnCtr));
            
            ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.AP_Client_Ledger, poModel.get(lnCtr));
            poModel.get(lnCtr).setModifiedDate(poGRider.getServerDate());
            
            if (!validator.isEntryOkay()){
                obj.put("result", "error");
                obj.put("message", validator.getMessage());
                return obj;
            
            }
            obj = poModel.get(lnCtr).saveRecord();

        }    
        
        return obj;
    }
    

}

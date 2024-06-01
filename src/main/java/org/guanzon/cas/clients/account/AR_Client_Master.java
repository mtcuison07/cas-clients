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
import org.guanzon.cas.clients.Client_Master;
import org.guanzon.cas.model.clients.Model_Client_Address;
import org.guanzon.cas.model.clients.Model_Client_Master;
import org.guanzon.cas.model.clients.ar.Model_AR_Client_Master;
import org.guanzon.cas.validators.ValidatorFactory;
import org.guanzon.cas.validators.ValidatorInterface;
import org.json.simple.JSONObject;

/**
 *
 * @author User
 */
public class AR_Client_Master implements GRecord {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;
    
    Model_AR_Client_Master poModel;
//    ArrayList<Model_AR_Client_Ledger> poLedger;
    JSONObject poJSON;
    AR_Client_Ledger poLedger1;
    Client_Master poClient;
    
    
    public AR_Client_Master(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;

        poModel = new Model_AR_Client_Master(foGRider);
        poLedger1 = new AR_Client_Ledger(foGRider, fbWthParent);
        poClient = new Client_Master(foGRider, fbWthParent, poGRider.getBranchCode());
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
               obj =  poModel.setValue(fnCol, foData);
               
//                obj.put(fnCol, pnEditMode);
            }
        }
        return obj;
    }

    @Override
    public JSONObject setMaster(String fsCol, Object foData) {
        return setMaster(poModel.getColumn(fsCol), foData);
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
    
    public AR_Client_Ledger getLedger(){return poLedger1;}
    public void setLedger(AR_Client_Ledger foObj){this.poLedger1 = foObj;}
    
    public void setLedger(int fnRow, int fnIndex, Object foValue){ poLedger1.setMaster(fnRow, fnIndex, foValue);}
    public void setLedger(int fnRow, String fsIndex, Object foValue){ poLedger1.setMaster(fnRow, fsIndex, foValue);}
    public Object getLedger(int fnRow, int fnIndex){return poLedger1.getMaster(fnRow, fnIndex);}
    public Object getLedger(int fnRow, String fsIndex){return poLedger1.getMaster(fnRow, fsIndex);}
    
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
            lsSQL = MiscUtil.addCondition(lsSQL, "a.cClientTp = '0'  AND a.sClientID LIKE " + SQLUtil.toSQL("%" + fsValue + "%")) + " GROUP BY a.sClientID";
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
//                poModel.setClientID((String) loJSON.get("sClientID"));
                setMaster(1, (String) loJSON.get("sClientID"));
                setMaster(3, (String) loJSON.get("sContctID"));
                setMaster(20, (String) loJSON.get("sCPerson1"));
                setMaster(18, (String) loJSON.get("sCompnyNm"));
                setMaster(2, (String) loJSON.get("sAddrssID"));
                setMaster(19, (String) loJSON.get("xAddressx"));
                setMaster(24, (String) loJSON.get("sTaxIDNox"));
//                OpenClientLedger((String) loJSON.get("sClientID"));
                checkData(poLedger1.openRecord((String) loJSON.get("sClientID")));
                System.out.println("poLedger1 = " + poLedger1.openRecord((String) loJSON.get("sClientID")));
                loJSON.put("result", "success");
            }else {
                loJSON.put("result", "error");
                loJSON.put("message", "No client information found for: " + fsValue + ", Please check client type and client name details.");
                return loJSON;
            }
        return loJSON;
    }
    
    public JSONObject SearchTerm(String fsValue, boolean fbByCode){
        String lsHeader = "Code»Description";
        String lsColName = "sTermCode»sDescript";
        String lsColCrit = "sTermCode»sDescript";
        String lsSQL = "SELECT " +
                        "  sTermCode" +
                        ", sDescript" +
                        " FROM Term" +
                        " WHERE cRecdStat = '1'";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sTermCode = " + SQLUtil.toSQL(fsValue));
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "sDescript LIKE " + SQLUtil.toSQL("%" + fsValue + "%"));
        
       
        
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
                System.out.println("sTermCode = " + (String) loJSON.get("sTermCode"));
                System.out.println("sDescript = " + (String) loJSON.get("sDescript"));
//                getMasterModel().setClientID((String) loJSON.get("sClientID"));
//                poModel.setClientID((String) loJSON.get("sClientID"));
                setMaster(8, (String) loJSON.get("sTermCode"));
                setMaster(23, (String) loJSON.get("sDescript"));
                loJSON.put("result", "success");
            }else {
                loJSON.put("result", "error");
                loJSON.put("message", "No client information found for: " + fsValue + ", Please check client type and client name details.");
                return loJSON;
            }
        return loJSON;
    }
    
//    public JSONObject OpenClientLedger(String fsValue){
//        String lsSQL = "SELECT" +
//                        "  sClientID" +
//                        ", nLedgerNo" +
//                        ", dTransact" +
//                        ", sSourceCd" +
//                        ", sSourceNo" +
//                        ", nAmountIn" +
//                        ", nAmountOt" +
//                        ", dPostedxx" +
//                        ", nABalance" +
//                        ", dModified" +
//                        " FROM AR_Client_Ledger";
//        lsSQL = MiscUtil.addCondition(lsSQL, "sClientID = " + SQLUtil.toSQL(fsValue));
//        System.out.println(lsSQL);
//        ResultSet loRS = poGRider.executeQuery(lsSQL);
//
//        try {
//            int lnctr = 0;
//            if (MiscUtil.RecordCount(loRS) > 0) {
//                poLedger = new ArrayList<>();
//                while(loRS.next()){
//                        poLedger.add(new Model_AR_Client_Ledger(poGRider));
//                        poLedger.get(poLedger.size() - 1).openRecord(loRS.getString("sClientID"));
//                        
//                        pnEditMode = EditMode.UPDATE;
//                        lnctr++;
//                        poJSON.put("result", "success");
//                        poJSON.put("message", "Record loaded successfully.");
//                    } 
//                
//                System.out.println("lnctr = " + lnctr);
//                
//            }else{
//                poLedger = new ArrayList<>();
//                addLedger();
//                poJSON.put("result", "error");
//                poJSON.put("continue", true);
//                poJSON.put("message", "No record selected.");
//            }
//            
//            MiscUtil.close(loRS);
//        } catch (SQLException e) {
//            poJSON.put("result", "error");
//            poJSON.put("message", e.getMessage());
//        }
//        return poJSON;
//    }
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
    public Object getMaster(int fnCol) {
        if(pnEditMode == EditMode.UNKNOWN)
            return null;
        else 
            return poModel.getValue(fnCol);
    }

    @Override
    public Object getMaster(String fsCol) {
        return getMaster(poModel.getColumn(fsCol));
    }
    
    @Override
    public JSONObject newRecord() {
        
            poJSON = new JSONObject();
        try{
            
//            pnEditMode = EditMode.ADDNEW;
            poModel = new Model_AR_Client_Master(poGRider);
//            org.json.simple.JSONObject obj;
//
            Connection loConn = null;
            loConn = setConnection();

//            poModel.setClientID(MiscUtil.getNextCode(poModel.getTable(), "sClientID", true, loConn, poGRider.getBranchCode()));
//            poModel.newRecord();
//            
            poModel.newRecord();

            //init detail
            //init detail
//            poLedger = new ArrayList<>();
            
            if (poModel == null){
                
                poJSON.put("result", "error");
                poJSON.put("message", "initialized new record failed.");
                return poJSON;
            }else{
//                addLedger();
                poLedger1.newRecord();
                
                
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
        
        poModel = new Model_AR_Client_Master(poGRider);
        poJSON = poModel.openRecord(fsValue);
        
        poJSON = checkData(poLedger1.openRecord(fsValue));
//        poJSON = checkData(OpenClientLedger(fsValue));
        return poJSON;
    }

    @Override
    public JSONObject updateRecord() {
        
        
        poJSON = new JSONObject();
        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode.");
            return poJSON;
        }
        pnEditMode = EditMode.UPDATE;
        poJSON.put("result", "success");
        poJSON.put("message", "Update mode success.");
        return poJSON;
    }

    @Override
    public JSONObject saveRecord() {
        poJSON = new JSONObject();
        if (!pbWthParent) {
            poGRider.beginTrans();
        }
        ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.AR_Client_Master, poModel);
//        poModel.setModifiedDate(poGRider.getServerDate());

        if (!validator.isEntryOkay()){
            poJSON.put("result", "error");
            poJSON.put("message", validator.getMessage());
            return poJSON;

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
    public JSONObject deleteRecord(String fsValue) {
         poJSON = new JSONObject();

        poJSON = new JSONObject();
        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE) {
            if (poGRider.getUserLevel() < UserRight.SUPERVISOR){
                poJSON.put("result", "error");
                poJSON.put("message", "User is not allowed delete transaction.");
                return poJSON;
            }
            String lsSQL = "DELETE FROM " + poModel.getTable()+
                                " WHERE sClientID = " + SQLUtil.toSQL(fsValue);

            if (!lsSQL.equals("")){
                if (poGRider.executeQuery(lsSQL, poModel.getTable(), poGRider.getBranchCode(), "") > 0) {
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
        poJSON = new JSONObject();

        if (poModel.getEditMode() == EditMode.READY || poModel.getEditMode() == EditMode.UPDATE) {
            poJSON = poModel.setRecdStat(TransactionStatus.STATE_CLOSED);

            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject activateRecord(String string) {
        
        poJSON = new JSONObject();

        if (poModel.getEditMode() == EditMode.READY || poModel.getEditMode() == EditMode.UPDATE) {
            poJSON = poModel.setRecdStat(TransactionStatus.STATE_CLOSED);

            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject searchRecord(String fsValue, boolean fbByCode) {
        String lsCondition = "";
        String lsFilter = "";

        String lsHeader = "ID»Name»Contact Person";
        String lsColName = "sClientID»xClientNm»xCPerson1";
        String lsColCrit = "a.sClientID»b.sCompnyNm»d.sCPerson1";
        if (psTranStatus.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStatus.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStatus.charAt(lnCtr)));
            }

            lsCondition =  "a.cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(psTranStatus);
        }

        if (!fbByCode) {
            lsFilter = "sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%");
        } else {
            lsFilter = "sClientID LIKE " + SQLUtil.toSQL("%" + fsValue + "%");
        }
        String lsSQL  = "SELECT" +
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
                    " FROM AR_Client_Master a" +
                        " LEFT JOIN Client_Master b ON a.sClientID = b.sClientID" +
                        " LEFT JOIN Client_Address c" + 
                            " LEFT JOIN Barangay  g ON c.sBrgyIDxx = g.sBrgyIDxx" +
                            " LEFT JOIN TownCity h ON c.sTownIDxx = h.sTownIDxx" +
                            " LEFT JOIN Province i ON h.sProvIDxx = i.sProvIDxx" +
                        " ON a.sAddrssID = c.sAddrssID" +
                        " LEFT JOIN Client_Institution_Contact_Person d ON a.sContctID = d.sContctID" +
                        " LEFT JOIN Category e ON a.sCategrCd = e.sCategrCd" +
                        " LEFT JOIN Term f ON a.sTermIDxx = f.sTermCode";
        
        lsSQL = MiscUtil.addCondition(lsSQL, lsCondition + " AND " + lsFilter);
        System.out.println("lsSQL = " + lsSQL);
        poJSON = new JSONObject();

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                fsValue,
                lsHeader,
                lsColName,
                lsColCrit,
                fbByCode ? 0 : 1);
        
        System.out.println("poJSON = " + poJSON);
        if (poJSON != null && !"error".equals((String) poJSON.get("result"))) {
            return openRecord((String)poJSON.get("sClientID"));
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
            return poJSON;
        }
    }

    @Override
    public Model_AR_Client_Master getModel() {
        return poModel;
    }
    public JSONObject SearchCategory(String fsValue, boolean fbByCode){
        String lsHeader = "ID»Category";
        String lsColName = "sCategrCd»sDescript»";
        String lsColCrit = "sCategrCd»sDescript";
        String lsTable;
        lsTable = "Category";
        String lsSQL = " SELECT " +
                            " sCategrCd, " +
                            " sDescript, " +
                            " sInvTypCd, " +
                            " cRecdStat " +
                      " FROM " + lsTable;
        
       
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sCategrCd = " + SQLUtil.toSQL(fsValue)) + " GROUP BY sCategrCd";
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "sDescript LIKE " + SQLUtil.toSQL("%" + fsValue + "%")) + " GROUP BY sCategrCd";
        
       
      
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
                System.out.println("json sCategrCd = " + (String) loJSON.get("sCategrCd"));
                lsValue = (String) loJSON.get("sCategrCd");
                setMaster("sCategrCd", (String) loJSON.get("sCategrCd"));
                setMaster("xCategrNm", (String) loJSON.get("sDescript"));
//                setAccount(fnRow, "sDescript", (String) loJSON.get("sDescript"));
                
//                System.out.println("get sClientID = " + getAccount(fnRow, 4));
                loJSON.put("result", "success");
            }else {
                loJSON.put("result", "error");
                loJSON.put("message", "No Category information found for: " + fsValue + ", Please check Catergory Code and Description details.");
                return loJSON;
            }
        return loJSON;
    }

}

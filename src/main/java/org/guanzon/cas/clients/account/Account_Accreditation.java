package org.guanzon.cas.clients.account;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.appdriver.iface.GTransaction;
import org.guanzon.cas.model.clients.Model_Client_Address;
import org.guanzon.cas.model.clients.Model_Client_Master;
import org.guanzon.cas.models.Model_Account_Accreditation;
import org.guanzon.cas.models.Model_Supplier_Accreditation;
import org.guanzon.cas.validators.ValidatorFactory;
import org.guanzon.cas.validators.ValidatorInterface;
import org.json.simple.JSONObject;

/**
 *
 * @author Maynard
 */
public class Account_Accreditation implements GTransaction {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;

    ArrayList<Model_Account_Accreditation> paModel;
    Model_Account_Accreditation poModel;
    JSONObject poJSON;
    int pnRow;
    public void setRow(int fnRow){
        pnRow = fnRow;
    }
    public Account_Accreditation(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;
        
//        paModel = new Model_Account_Accreditation(foGRider);
        pnEditMode = EditMode.UNKNOWN;
    }

    @Override
    public JSONObject newTransaction() {
         poJSON = new JSONObject();
        try{
            

            //init detail
            //init detail
            paModel = new ArrayList<>();
            
            addDetail();
                
                
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
    public JSONObject openTransaction(String fsValue) {
        return poModel.openRecord("sTransNox = " + SQLUtil.toSQL(fsValue));
    }

    @Override
    public JSONObject updateTransaction() {
        JSONObject loJSON = new JSONObject();

        if (getEditMode() == EditMode.UPDATE) {
            loJSON.put("result", "success");
            loJSON.put("message", "Edit mode has changed to update.");
        } else {
            loJSON.put("result", "error");
            loJSON.put("message", "No record loaded to update.");
        }

        return loJSON;
    }

    @Override
    public JSONObject saveTransaction() {
        if (!pbWthParent) {
            poGRider.beginTrans();
        }

        poJSON = saveRecord("");

        if ("success".equals((String)checkData(poJSON).get("result"))) {
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
    public JSONObject deleteTransaction(String fsValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject closeTransaction(String fsValue) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY || getEditMode() == EditMode.UPDATE) {
            
            poJSON = saveRecord("Close");
//            poJSON = paModel.setTranStatus(TransactionStatus.STATE_CLOSED);
//
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            poJSON = paModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject postTransaction(String fsValue) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY
                || getEditMode() == EditMode.UPDATE) {
            poJSON = saveRecord("Post");
//            poJSON = paModel.get(0).setTranStatus(TransactionStatus.STATE_POSTED);
//
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            poJSON = paModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject voidTransaction(String string) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY
                || getEditMode() == EditMode.UPDATE) {
            
            poJSON = saveRecord("Void");
//            poJSON = paModel.setTranStatus(TransactionStatus.STATE_VOID);
//
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            poJSON = paModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject cancelTransaction(String fsTransNox) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY
                || getEditMode() == EditMode.UPDATE) {
            
            poJSON = saveRecord("Cancel");
//            poJSON = paModel.setTranStatus(TransactionStatus.STATE_CANCELLED);
//
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            poJSON = paModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject searchWithCondition(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject searchTransaction(String fsColumn, String fsValue, boolean fbByCode) {
        String lsCondition = "";
        String lsFilter = "";

        if (psTranStatus.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStatus.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStatus.charAt(lnCtr)));
            }

            lsCondition = fsColumn + " IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = fsColumn + " = " + SQLUtil.toSQL(psTranStatus);
        }

        if (!fbByCode) {
            lsFilter = fsColumn + " LIKE " + SQLUtil.toSQL(fsValue);
        } else {
            lsFilter = fsColumn + " = " + SQLUtil.toSQL(fsValue);
        }

        String lsSQL = MiscUtil.addCondition(poModel.makeSQL(), lsCondition + " AND " + lsFilter + " GROUP BY sTransNox");
        
        poJSON = new JSONObject();

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                fsValue,
                "Transaction No»Date»Name",
                "sTransNox»dTransact»sCompnyNm",
                "sTransNox»dTransact»sCompnyNm",
                fbByCode ? 0 : 1);

        if (poJSON != null) {
//            return poModel.openRecord((String) poJSON.get("sTransNox"));
            return OpenAccount((String) poJSON.get("sTransNox"));
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
            return poJSON;
        }
    }
    
    
    @Override
    public JSONObject searchMaster(String fsColumn, String fsValue, boolean fbByCode) {
        return searchMaster(paModel.get(pnRow).getColumn(fsColumn), fsValue, fbByCode);

    }

    @Override
    public JSONObject searchMaster(int fnCol, String fsValue, boolean fbByCode) {
        poJSON = new JSONObject();
        switch(fnCol){
            case 14: //sClientNm
                poJSON = SearchClient(pnRow, fsValue, fbByCode);
                break;
        }
        return poJSON;
    }

    @Override
    public Model_Account_Accreditation getMasterModel() {
        return paModel.get(pnRow);
    }

    @Override
    public JSONObject setMaster(int fnCol, Object foData) {
        return paModel.get(pnRow).setValue(fnCol, foData);
    }

    @Override
    public JSONObject setMaster(String fsCol, Object foData) {
        return paModel.get(pnRow).setValue(fsCol, foData);
    }
//    
//    public JSONObject setMaster(int fnRow, int fnCol, Object foData) {
//        return paModel.get(fnRow).setValue(fnCol, foData);
//    }
//
//    public JSONObject setMaster(int fnRow, String fsCol, Object foData) {
//        return paModel.get(fnRow).setValue(fsCol, foData);
//    }

    @Override
    public int getEditMode() {
        return pnEditMode;
    }

    @Override
    public void setTransactionStatus(String fsValue) {
        psTranStatus = fsValue;
    }
    public JSONObject OpenAccount(String fsValue){
        
        String lsSQL = MiscUtil.addCondition(poModel.makeSQL(), "sTransNox = " + SQLUtil.toSQL(fsValue));
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            int lnctr = 0;
            if (MiscUtil.RecordCount(loRS) > 0) {
                paModel = new ArrayList<>();
                while(loRS.next()){
                        paModel.add(new Model_Account_Accreditation(poGRider));
                        
                        paModel.get(paModel.size() - 1).openRecord("sTransNox = " + SQLUtil.toSQL(loRS.getString("sTransNox")));
                        
                        pnEditMode = EditMode.UPDATE;
                        lnctr++;
                        poJSON.put("result", "success");
                        poJSON.put("message", "Record loaded successfully.");
                    } 
                
                System.out.println("lnctr = " + lnctr);
                
            }else{
                paModel = new ArrayList<>();
                addDetail();
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
    
    private JSONObject saveRecord(String type){
        
        JSONObject obj = new JSONObject();
        if (paModel.size()<= 0){
            obj.put("result", "error");
            obj.put("message", "No client address detected. Please encode client address.");
            return obj;
        }
        
        int lnCtr;
        String lsSQL;
        Model_Account_Accreditation loModel = new Model_Account_Accreditation(poGRider);
        String lsTransNox = MiscUtil.getNextCode(loModel.getTable(), "sTransNox", true, poGRider.getConnection(), poGRider.getBranchCode());        
        for (lnCtr = 0; lnCtr <= paModel.size() -1; lnCtr++){
//            Validator_Client_Address validator = new Validator_Client_Address(paAddress.get(lnCtr));
            if("Cancel".equals(type)){
                obj = paModel.get(lnCtr).setTranStatus(TransactionStatus.STATE_CANCELLED);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
                
            }else if("Void".equals(type)){
                obj = paModel.get(lnCtr).setTranStatus(TransactionStatus.STATE_VOID);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
            }else if("Post".equals(type)){
                obj = paModel.get(lnCtr).setTranStatus(TransactionStatus.STATE_POSTED);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
                
            }else if("Close".equals(type)){
                obj = paModel.get(lnCtr).setTranStatus(TransactionStatus.STATE_CLOSED);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
                
            }
            ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.Account_Accreditation, paModel.get(lnCtr));
            paModel.get(lnCtr).setModifiedDate(poGRider.getServerDate());
            paModel.get(lnCtr).setTransactionNo(lsTransNox);
            
            if (!validator.isEntryOkay()){
                obj.put("result", "error");
                obj.put("message", validator.getMessage());
                return obj;
            
            }
            obj = paModel.get(lnCtr).saveRecord();

        }    
        
        return obj;
    }
    public JSONObject addDetail(){
        poJSON = new JSONObject();
        if (paModel.isEmpty()){
            poModel = new Model_Account_Accreditation(poGRider);
            paModel.add(poModel);
            paModel.get(0).newRecord();
            pnRow = 0;
            poJSON.put("result", "success");
            poJSON.put("message", "Address add record.");
            

        } else {
            
            ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.Account_Accreditation, paModel.get(paModel.size()-1));
//            Validator_Client_Address  validator = new Validator_Client_Address(paAddress.get(paAddress.size()-1));
            if(!validator.isEntryOkay()){
                poJSON.put("result", "error");
                poJSON.put("message", validator.getMessage());
                return poJSON;
            }
            poModel = new Model_Account_Accreditation(poGRider);
            paModel.add(poModel);
            paModel.get(paModel.size()-1).newRecord();
            pnRow = paModel.size()-1;
            
            poJSON.put("result", "success");
            poJSON.put("message", "Address add record.");
        }
        return poJSON;
    }
    public JSONObject SearchClient(int fnRow, String fsValue, boolean fbByCode){
        String lsHeader = "ID»Name»Contact Person";
        String lsColName = "sClientID»sCompnyNm»sCPerson1";
        String lsColCrit = "a.sClientID»b.sCompnyNm»c.sCPerson1";
        String lsTable;
        if(paModel.get(fnRow).getAcctType().equalsIgnoreCase("0")){
            lsTable = "AP_Client_Master";
        }else{
            lsTable = "AR_Client_Master";
        }
        String lsSQL = "SELECT " +
                            "  a.sClientID" +
                            ", b.sCompnyNm" +
                            ", c.sCPerson1" + 
                            ", c.sContctID" + 
                            " FROM " + lsTable + " a " +
                                " LEFT JOIN Client_Master b ON a.sClientID = b.sClientID"+
                                " LEFT JOIN Client_Institution_Contact_Person c" +
                                    " ON b.sClientID = c.sClientID AND c.cPrimaryx = '1'";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sClientID = " + SQLUtil.toSQL(fsValue)) + " GROUP BY a.sClientID";
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%")) + " GROUP BY a.sClientID";
        
       
      
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
                lsValue = (String) loJSON.get("sClientID");
                setMaster("sClientID", (String) loJSON.get("sClientID"));
                setMaster("sContctID", (String) loJSON.get("sContctID"));
                setMaster("sCPerson1", (String) loJSON.get("sCPerson1"));
                setMaster("sCompnyNm", (String) loJSON.get("sCompnyNm"));
            }else {
                loJSON.put("result", "error");
                loJSON.put("message", "No client information found for: " + fsValue + ", Please check client type and client name details.");
                return loJSON;
            }
        return loJSON;
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
}
